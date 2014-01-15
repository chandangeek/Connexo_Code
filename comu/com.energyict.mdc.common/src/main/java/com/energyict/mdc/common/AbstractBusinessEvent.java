package com.energyict.mdc.common;

/**
 * This class provides an implementation of the <code>BusinessEvent</code> interface.
 *
 * @see BusinessEvent
 */
public class AbstractBusinessEvent<T extends BusinessObject> implements BusinessEvent<T> {

    private T source;

    protected AbstractBusinessEvent(T source) {
        this.source = source;
    }

    /**
     * @return the originator of the event
     */
    public T getSource() {
        return source;
    }

    /**
     * @return a string representation of the object.
     */
    public String toString() {
        return "Event from " + source + " (" + source.getClass().getName() + ")";
    }

}