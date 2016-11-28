package com.energyict.mdc.upl.security;

import com.energyict.mdc.upl.properties.Password;

/**
 * Provides services that relate to security.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-25 (09:34)
 */
public interface SecurityService {
    Password passwordFromEncryptedString(String encrypted);
}