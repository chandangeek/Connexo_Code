package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.upl.security.KeyAccessorType;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/05/2017 - 10:00
 */
public class KeyAccessorTypeAdapter implements KeyAccessorType {

    private final SecurityAccessorType securityAccessorType;

    KeyAccessorTypeAdapter(SecurityAccessorType securityAccessorType) {
        this.securityAccessorType = securityAccessorType;
    }

    public SecurityAccessorType getSecurityAccessorType() {
        return securityAccessorType;
    }
}