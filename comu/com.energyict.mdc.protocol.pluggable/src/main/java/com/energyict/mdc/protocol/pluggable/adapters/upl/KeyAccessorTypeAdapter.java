package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.energyict.mdc.upl.security.KeyAccessorType;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/05/2017 - 10:00
 */
public class KeyAccessorTypeAdapter implements KeyAccessorType {

    private final com.elster.jupiter.pki.KeyAccessorType keyAccessorType;

    KeyAccessorTypeAdapter(com.elster.jupiter.pki.KeyAccessorType keyAccessorType) {
        this.keyAccessorType = keyAccessorType;
    }

    public com.elster.jupiter.pki.KeyAccessorType getKeyAccessorType() {
        return keyAccessorType;
    }
}