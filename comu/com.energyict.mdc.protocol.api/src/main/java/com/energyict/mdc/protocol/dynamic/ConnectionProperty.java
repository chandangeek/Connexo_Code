package com.energyict.mdc.protocol.dynamic;

/**
 * Models a property value that is used to establish a connection with a physical device.
 * Note that establishing a connection usually involves multiple properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (13:41)
 */
public interface ConnectionProperty {

    /**
     * Gets the name of the property for which a value is held.
     *
     * @return The name of the property
     */
    public String getName ();

    /**
     * Gets the value of the property.
     *
     * @return The value
     */
    public Object getValue ();

}