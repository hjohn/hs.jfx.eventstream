package hs.jfx.eventstream.api;

/**
 * Represents a subscription to a stream, which can be used to
 * later cancel the subscription.
 */
public interface Subscription {
    static final Subscription EMPTY = () -> {};

    /**
     * Cancels this subscription.
     */
    void unsubscribe();
}