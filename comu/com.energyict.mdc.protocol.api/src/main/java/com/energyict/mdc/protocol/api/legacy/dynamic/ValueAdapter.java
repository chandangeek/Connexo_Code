package com.energyict.mdc.protocol.api.legacy.dynamic;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (11:00)
 */
public interface ValueAdapter {

    public Object doGetValue();

    public void doSetValue(Object value);

}