package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.Password;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-23 (11:26)
 */
public class SimplePassword implements Password {
    private final String value;

    public SimplePassword(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}