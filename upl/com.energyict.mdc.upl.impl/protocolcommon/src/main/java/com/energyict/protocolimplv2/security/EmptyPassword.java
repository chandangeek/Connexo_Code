package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.Password;

/**
 * Provides an implementation for the {@link Password} interface
 * that always returns an empty String as value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-23 (10:56)
 */
class EmptyPassword implements Password {
    @Override
    public String getValue() {
        return "";
    }
}