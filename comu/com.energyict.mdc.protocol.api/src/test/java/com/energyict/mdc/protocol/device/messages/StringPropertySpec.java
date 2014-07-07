package com.energyict.mdc.protocol.device.messages;

/**
 * Provides an implementation for the  {@link PropertySpec} interface
 * for String values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-05 (14:58)
 */
public class StringPropertySpec extends SimplePropertySpec<String> {

    public StringPropertySpec (String name) {
        super(name);
    }

    public StringPropertySpec (String name, boolean required) {
        super(name, required);
    }

}