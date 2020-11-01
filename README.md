# EventStreams for JavaFX

Based on the work by Tomas Mikula's excellent ReactFX project (https://github.com/TomasMikula/ReactFX)
and used with permission.

## Motivation

This project was created in the hope to add additional functionality directly to JavaFX to address
a few of its rough edges. Specifically:

- Bindings#select, which allows to create bindings to nested properties.  The current implementation
is not type safe and does not offer much flexibility to customize the binding.

- Creating bindings to long lived properties (like those part of a model) can easily lead to unintended
memory leaks.

The project purposely contains only a small well defined subset of code adapted from ReactFX with the goal
of making the project easier to evaluate for potential inclusion into JavaFX.  Direct inclusion would
offer major advantages by adding default methods to the `ObservableValue` and `Binding` interfaces making
classes that implement these interfaces act more like `Optional`s would:

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

#### Alternative solution using EventStreams

With EventStreams we can create the same binding in a type-safe manner:

        Binding<Boolean> treeShowing = EventStreams.valuesOf(node.sceneProperty())
            .filter(Objects::nonNull)
            .flatMap(s -> EventStreams.valuesOf(s.windowProperty()))
            .filter(Objects::nonNull)
            .flatMap(w -> EventStreams.valuesOf(w.showingProperty()))
            .toBinding(false);

This is still pretty cumbersome, but a lot less so than the type-safe solution used in the `Node` example.

#### Integration into JavaFX

EventStreams can be used as-is, but they could also be fully integrated into JavaFX itself.  This
would involve adding a new `stream` method to the `ObservableValue` interface in JavaFX.  Any 
`ObservableValue` would then easily gain access to the familair methods from `Stream` and `Optional`:
`map`, `flatMap` and `filter`.

The above example could then look like this:

        Binding<Boolean> treeShowing = node.sceneProperty().stream()
            .filter(Objects::nonNull)
            .flatMap(s -> s.windowProperty().stream()))
            .filter(Objects::nonNull)
            .flatMap(w -> w.showingProperty().stream()))
            .toBinding(false);

A further enhancement would be to add the `map`, `flatMap` and `filter` methods directly on
`Binding`.  These would be implemented simply as:

        default <U> Binding<U> map(Function<? super T, ? extends U> f) {
            return EventStreams.valuesOf(this).map(f).toBinding(null);
        }

You could then write the above example simply as:

        Binding<Boolean> treeShowing = node.sceneProperty()
            .filter(Objects::nonNull)
            .flatMap(Scene::windowProperty)
            .filter(Objects::nonNull)
            .flatMap(Window::showingProperty)
            .orElse(false);

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

With EventStreams one could safely bind a UI property to a model only when the UI is visible:

    model.selectedItemProperty()
        .conditionOn(isShowing)
        .subscribe(selectedItem -> listView.getSelectionModel().select(selectedItem));

Where the `isShowing` variable can be created like this:

    Binding<Boolean> isShowing = listView.sceneProperty()
        .filter(Objects::nonNull)
        .flatMap(Scene::windowProperty)
        .filter(Objects::nonNull)
        .flatMap(Window::showingProperty)
        .orElse(false);

Or with a small helper class, which only needs the `Node` involved as a parameter:

    model.selectedItemProperty()
        .conditionOn(Helper.isShowing(listView))
        .subscribe(selectedItem -> listView.getSelectionModel().select(selectedItem));

The above binding to `model.selectedItemProperty()` will only be present while `listView` is
showing.  If the list view is hidden, the listener is unregistered, and if it is shown again
the listener is re-added.  If the UI is hidden and no longer referenced, it will instantly stop
reacting to any changes in the model and will eventually be garbage collected.