# Streams for JavaFX

Based on the work by Tomas Mikula's excellent ReactFX project (https://github.com/TomasMikula/ReactFX)
and used with permission.

## Overview

This library allows to stream values generated by properties or changes triggered by JavaFX events with
an expressive fluent API which also enables easier management of listeners to prevent memory leaks.

### Basics

Streams can be used to listen to property changes and take an action each time the property changes.
A simple example below shows how to print the value of a button's text property to the console each
time it changes:

    Changes.of(button.textProperty())
        .subscribe(System.out::println);

A more sophisticated example may want to exclude certain values and convert the text to upper case:

    Changes.of(button.textProperty())
        .filter(text -> !text.contains("evil"))
        .map(String::toUpperCase)
        .subscribe(System.out::println);

The examples can also be written with `Values.of`:

    Values.of(button.textProperty())
        .subscribe(System.out::println);

The differences between these two ways of creating a stream will be discussed in detail further in
this document, but for now it is enough to know that a value stream will send the current value to
a new subscriber immediately upon subscription, while a change stream will only do so when an actual
change occured.

### Null Handling

In the earlier examples `null` was not explicitely handled in the `filter` or `map` functions. This is
because these functions are null-safe. The functions are not called when the value is `null`, instead
it is passed on to the next function in the chain.

Streams offer functions like Java's `Optional` to deal with cases where a value is `null`:

    Changes.of(button.textProperty())
        .map(String::toUpperCase)
        .orElse("(unavailable)")
        .subscribe(System.out::println);

This makes dealing with `null` a lot simpler as not every `map`, `flatMap` or `filter` step specifically
needs to deal with it. For example, when creating a stream with several chained properties, the `null`
checks can be omitted:

    Values.of(button.sceneProperty())
        .flatMap(scene -> Values.of(scene.windowProperty()))     // scene can't be null :)
        .flatMap(window -> Values.of(window.showingProperty()))
        .orElse(false)  // deals with the case when either scene or window is null
        .subscribe(showing -> System.out.println("button is " + (showing ? "showing" : "hidden")));

### Stream Types

Two types of streams are distinguished: value and change streams. 

Value streams are called stateful as they have a notion of a current value. A new subscriber will
immediately receive the current value upon subscription and any changes for as long as it is subscribed.
Value streams can be converted into JavaFX `Binding`s.

In contrast, change streams only emit when an actual change occurs, which may be sometime after
subscribing or never.  Change streams are unsuitable to be used as a binding because a user
expects the bound value to be always synchronized with its source. As change streams are not
guaranteed to emit anything a property bound in such a way would not always reflect its source.

The two types of streams can be converted into one or the other by calling a method resulting in a
new stream.  For example, a change stream can be turned into a value stream by calling `withDefault`.
This method assigns a default value to the stream which can be emitted immediately upon subscription.
Filtering a value stream with the `filter` method turns it into a change stream as it no longer
(always) has the notion of a current value.

Change stream also has a specialized stream subtype for JavaFX invalidations. Invalidations donot
carry a value and so these streams always emit a `null` of type `Void`. It is distinguished
because it does not allow many of the functions available to change and value streams. It can
be used as is (to signal invalidations) but it can also be converted to a change stream by
calling the `replace` function.  This function replaces the `Void` value with a value of choice,
for example:

    Invalidations.of(button.textProperty())
        .replace(() -> System.currentTimeMillis());

Here is another example which shows how the type of stream changes by calling functions:

    InvalidationStream is = Invalidations.of(button.textProperty());
    ChangeStream<Long> cs = is.replace(() -> System.currentTimeMillis());
    ValueStream<Long> vs = cs.withDefault(Long.MIN_VALUE);

The above is equivalent to this shortened version:

    ValueStream<Long> vs = Invalidations.of(button.textProperty())
        .replace(() -> System.currentTimeMillis())
        .withDefault(Long.MIN_VALUE);

The table below shows which of the most commonly used functions are available for each stream
type and whether or not the type of stream changes as a result:

| Function                    | Invalidation | Change | Value |
| --------------------------- |:------------:|:------:|:-----:|
| map, flatMap                |     -        |    X   |   X   |
| filter                      |     -        |    X   |  X(C) |
| peek                        |     -        |    X   |   X   |
| withDefault, withDefaultGet |    X(V)      |   X(V) |   -   |
| or, orElse, orElseGet       |     -        |    X   |   X   |
| conditionOn                 |     -        |    X   |   X   |
| replace                     |    X(C)      |    -   |   -   |
| flatMapToChange             |     -        |    -   |  X(C) |

The following table shows which terminal operations are available for each stream type:

| Function                    | Invalidation | Change | Value |
| --------------------------- |:------------:|:------:|:-----:|
| subscribe                   |     X        |    X   |   X   |
| toBinding                   |     -        |    -   |   X   |

### Lazy Subscriptions

Streams only observe their source when a consumer is currently subscribed.

    ValueStream<String> vs = Values.of(button.textProperty())
        .map(String::toUpperCase);

In the above example, the button's text property is not observed until an actual subscriber
is added to the stream:

    vs.subscribe(System.out::println);

Streams of this type are called lazy streams. All streams provided by this package are lazy
and will only observe their source when needed. 

As lazy streams will stop observing their source when they have no subscribers, the source
stream will not prevent garbage collection when there are no more active subscribers. There
is therefore no need to use weak listeners for observing the source stream.

An advantage of this approach is that an example like below will function as one would
expect, and will keep printing changes in `button.textProperty()`:

    Values.of(button.textProperty())
        .map(t -> t + "World")
        .subscribe(System.out::println);

Contrast this with JavaFX's standard binding mechanism which may garbage collect the binding
at any time because of its use of weak listeners:

    button.textProperty()
        .concat("World")  // weak binding used here
        .addListener((obs, old, current) -> System.out.println(current));

This can be very surprising, especially when the `concat` function was added at a later stage,
because that simple change will result in unexpected runtime behavior.

## Motivation

This project was created in the hope to add additional functionality directly to JavaFX to address
a few of its rough edges. Mainly:

- Type-safe `Bindings#select` functionality, which allows to create bindings to nested properties.  The current implementation
is not type safe and does not offer much flexibility to customize the binding.

The project purposely contains only a small well defined subset of code adapted from ReactFX with the goal
of making the project easier to evaluate for potential inclusion into JavaFX.  Direct inclusion would
offer major advantages by adding default methods to the `ObservableValue` and `Binding` interfaces making
classes that implement these interfaces act more like `Optional`'s would:

    Binding<String> quotedTitleText = model.titleProperty()
        .map(text -> "'" + text "'");  // new `map` method on `Binding`

### Type-safe binding to nested properties

In standard JavaFX, creating a binding to a nested property is a cumbersome affair.  One has to keep
track of the listeners to unregister them when a parent property changes, and reregister the listener
on the new value.  With multiple levels of nesting this can quickly become complicated and error prone.

An example from JavaFX itself is the implementation of the `treeShowing` property.  It tracks whether
or not a `Node` is currently showing on the screen.  In order to do this, it must check if the `Node`
has a `Scene`, whether the `Scene` has a `Window`, and whether that `Window` is currently shown:

        ChangeListener<Boolean> windowShowingChangedListener = (win, oldVal, newVal) -> updateTreeShowing();

        ChangeListener<Window> sceneWindowChangedListener = (scene, oldWindow, newWindow) -> {
            if (oldWindow != null) {
                oldWindow.showingProperty().removeListener(windowShowingChangedListener);
            }
            if (newWindow != null) {
                newWindow.showingProperty().addListener(windowShowingChangedListener);
            }
            updateTreeShowing();
        };

        ChangeListener<Scene> sceneChangedListener = (node, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.windowProperty().removeListener(sceneWindowChangedListener);

                Window window = oldScene.windowProperty().get();
                if (window != null) {
                    window.showingProperty().removeListener(windowShowingChangedListener);
                }
            }
            if (newScene != null) {
                newScene.windowProperty().addListener(sceneWindowChangedListener);

                Window window = newScene.windowProperty().get();
                if (window != null) {
                    window.showingProperty().addListener(windowShowingChangedListener);
                }
            }

            updateTreeShowing();
        };

This can already be expressed much more succintly by using the `Bindings#select` function:

        BooleanProperty treeShowing = Bindings.selectBoolean(node.sceneProperty(), "window", "showing");

The method however is not type safe.  A mistake in one of the string parameters or the choice of select
method will lead to errors at runtime.  It will also complain about `null` values and map them to some
standard value.

#### Alternative solution using Streams

With streams we can create the same binding in a type-safe manner:

        Binding<Boolean> treeShowing = Values.of(node.sceneProperty())
            .flatMap(s -> Values.of(s.windowProperty()))
            .flatMap(w -> Values.of(w.showingProperty()))
            .orElse(false)
            .toBinding();

This is far less cumbersome and still 100% type safe.

### Preventing memory leaks

When you bind a property in JavaFX you have to carefully consider the lifecycle of the two properties
involved. Calling `bind` on a property will keep a target property synced with a source property.

    target.bind(source);  // keep target in sync with source

This is equivalent to adding a (weak) listener (weak listener code omitted here) and keeping
track of the property target was bound to:

    source.addListener((obs, old, current) -> target.set(current));
    target.getProperties().put("boundTo", source);

In both these cases:

- source refers to target through the listener added because it needs to update the target when it changes
- target refers to source as the property it is "bound to" in order for `unbind` to do its magic

In JavaFX, `bind` will use a weak listener, which means that the target can be garbage collected independently
from the source property.  However, the reference from target to source with its "bound to" property is a hard
reference (if it were weak then a binding could stop working without notice because the source could be 
garbage collected and stop sending its updates).  This means that the lifecycle of the source property is
now closely tied to the target property.

If the target property is a long-lived object (like a data model) and the source property is a shorter lived
object like a UI element, you could have inadvertently created a big memory leak; all UI components
have a parent and a scene property, so effectively keeping a reference to any UI element can keep an entire
scene or window from being garbage collected.

Something as simple as keeping the selection of a `ListView` in sync with a model can lead to this:

    model.selectedItemProperty()
        .addListener((obs, old, current) -> listView.getSelectionModel().select(current));

Assuming `model` here is a long-lived object that perhaps is re-used next time the UI is shown to remember
the last selected item, the listener as shown above will prevent the `ListView` and all other UI components
that it refers to from being garbage collected.

To prevent this one must remember to wrap the above listener in a `WeakChangeListener` and be careful not
to keep a reference around to the unwrapped change listener.  Using a weak listener is not a perfect
solution however.  The listener only stops working when a garbage collection cycle runs and in the mean
time the UI code may interfere with normal operations if the selected item is changed in the model, as it
could trigger code in the (soon to be garbage collected) UI, which may still trigger other changes.

It might be better to disable the listener as soon as the UI is hidden.  Doing this manually means keeping
track of the listeners involved that may need unregistering (and potentially reregistering if the UI becomes
visible again).  Instead we could listen to a property that tracks the showing status of our UI.
Unfortunately, this is somewhat involved as there is no easy property one can listen to; you have to listen
for `Scene` changes, check which `Window` it is associated with and then bind to  `Window::showingProperty`
-- and update these listeners if the scene or window changes.

With Streams one could safely bind a UI property to a model only when the UI is visible:

    model.selectedItemProperty()
        .conditionOn(isShowing)
        .subscribe(selectedItem -> listView.getSelectionModel().select(selectedItem));

Where the `isShowing` variable can be created like this:

    Binding<Boolean> isShowing = Values.of(listView.sceneProperty())
        .flatMap(s -> Values.of(s.windowProperty()))
        .flatMap(w -> Values.of(w.showingProperty()))
        .orElse(false)
        .toBinding();

Or with a small helper class, which only needs the `Node` involved as a parameter:

    model.selectedItemProperty()
        .conditionOn(Helper.isShowing(listView))
        .subscribe(selectedItem -> listView.getSelectionModel().select(selectedItem));

The above binding to `model.selectedItemProperty()` will only be present while `listView` is
showing.  If the list view is hidden, the listener is unregistered, and if it is shown again
the listener is re-added.  If the UI is hidden, it will instantly stop reacting to any 
changes in the model and (if also no longer referenced) will eventually be garbage collected.

## Overview

WIP...

Streams distinguish between
- Stateful streams (ones based on values or with a default value)
- Stateless streams (ephemeral)

Difference is that a stateful stream will emit a value on subscribe or when a condition 
operator becomes true again.  This is only possible if a value can be emitted from the
stream on demand, like values from a property (the property holds the current value can
be emitted to a new subscriber or when a condition becomes true again).

A stateless stream (like invalidations or changes) does not emit anything upon
subscription as there is no source that holds a current value.  A stateless stream can
be made stateful with `withDefaultEvent`.

ReactFX will do this slightly differently; it won't give the default (or current value)
to a second subscriber of a stream, only to the first.  This can be somewhat confusing.