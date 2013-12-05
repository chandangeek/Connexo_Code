package com.energyict.mdc.common;

/**
 * Represent a business object that has a unique id.
 * Most often, but not always the id is generated automatically by the system.
 */
public interface IdBusinessObject extends BusinessObject {

    /**
     * Returns the object's unique id
     *
     * @return the id
     */
    public int getId();

}