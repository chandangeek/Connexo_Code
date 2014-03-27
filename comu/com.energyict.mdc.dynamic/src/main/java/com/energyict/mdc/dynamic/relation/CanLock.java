package com.energyict.mdc.dynamic.relation;

/**
 * Models the behavior of an object that can be locked on the database level
 * to avoid that 2 business processes are concurrently modifying it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-24 (08:49)
 */
public interface CanLock {

    public void lock ();

}