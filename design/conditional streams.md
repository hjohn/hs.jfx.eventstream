Thoughts about conditional streams
==================================

Problem
-------

ConditionOn will temporarily block streams from emitting values

This is "incompatible" with Value streams used as binding, as the binding will not be
kept up to date during the block, but more importantly, it won't be updated initially
either (upon subscription) when blocked as the stream won't emit anything.

Question
--------

What should a ValueStream, suitable for binding, return when condition is false, if anything?

Thoughts
--------

- if it doesn't emit anything, then the bound property might be in an undefined state
- if it does emit something, then null will need to be dealt with for bound properties, unless orElse is used
- it should be consistent, currently the behavior is different whether condition itself is false or null
  -> false: nothing emitted
  -> null: emits null
- the null emission comes from the nullSupplier in flatMap, which emits a constant null
  -> this seems to be desired and expected for many other cases
- adding a filter step will not work as filter itself is null safe
  -> filter would also change the stream to a ChangeStream which does not emit anything upon (re)subscription
- for a value stream, the condition becoming true results in an immediate emission, this is a must have

Problem in part is that conditionOn breaks the guarantee made by a Binding...
... but it will immediately emit when it becomes true
... unlike a ChangeStream which will only emit when something happens after it becomes true

Options to consider
-------------------

1) ValueStream which is silent when conditionOn is false/null, and emits immediately when condition becomes true
2) ValueStream which emits null when conditionOn is false/null, and emits immediately when condition becomes true
3) ChangeStream which is silent when conditionOn is false/null, and does not emit anything when condition becomes true

The common case for `conditionOn` is to have controls only be updated when they are visible -- the control not being
updated while it is invisible (which breaks the rules for bindings) matters little as the control is not visible
anyway, and an immediate update is triggered when it does become visible. This makes option 1 desirable.

Option 3 can be eliminated as this does not emit anything immediately when the condition becomes true, which is
an important requirement.

For option 2, a `skipNull` method could be introduced (filter cannot be used to filter out the nulls, as it will
become a ChangeStream and filter is null safe, so it won't even trigger on `null`). With the common case however
requiring the `skipNull` method (to avoid downstream consumers having to deal with `null` when the stream is
blocked) this puts an extra burden upon the common case.

If going for option 1, the option 2 behavior can still be achieved by manually doing the flat mapping that
`conditionOn` does. When `stream` is the original stream, this would look something like this:

    Values.of(condition).flatMap(c -> c ? stream : Values.constant(null));

Decision
--------

2021/02/14: Support option 1, and fix the inconsistent behavior when the condition is `null` to match `false`.
