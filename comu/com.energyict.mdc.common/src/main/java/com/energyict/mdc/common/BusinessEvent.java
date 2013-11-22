package com.energyict.mdc.common;

/**
 * BusinessEvent defines the minimum contract for an event that indicates that a
 * business object was created, changed or deleted.
 */
public interface BusinessEvent<T extends BusinessObject> {

    /**
     * @return the originator of the event
     */
    public T getSource();
}
