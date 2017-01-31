/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import java.io.Serializable;

/**
 * Represents a shadow object for a BusinessObject.
 * Shadow objects are used to take a temporary copy of the BusinessObject's state
 *
 * @author Karel
 */
public class ObjectShadow implements Serializable, Cloneable {

    private boolean dirty = false;

    /**
     * Creates a new instance of ShadowObject
     */
    public ObjectShadow() {
    }

    /**
     * marks the receiver as modified.
     */
    public void markDirty() {
        dirty = true;
    }

    /**
     * test if the receiver has been modified.
     *
     * @return true if the receiver has been modified.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * tests if the receiver as up to date.
     *
     * @return return true if the receiver is up to date.
     * @deprecated true if the receiver has not been modified.
     */
    public boolean isClean() {
        return !isDirty();
    }

    /**
     * marks the receiver as up to date.
     */
    public void markClean() {
        dirty = false;
    }

    /**
     * Returns a clone of the receiver
     *
     * @return a clone
     * @throws CloneNotSupportedException should not be thrown
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * prepares the receiver for cloning
     */
    public void prepareCloning() {
    }

}