package hs.jfx.eventstream;

/**
 * Represents a subscription to an event stream, which can be used to
 * cancel the subscription.
 */
public interface Subscription {
    static final Subscription EMPTY = () -> {};

    /**
     * Cancels this subscription.
     */
    void unsubscribe();
}