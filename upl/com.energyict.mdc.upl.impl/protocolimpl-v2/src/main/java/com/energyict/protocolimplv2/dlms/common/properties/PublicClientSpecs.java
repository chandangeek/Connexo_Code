package com.energyict.protocolimplv2.dlms.common.properties;

import java.math.BigDecimal;

/**
 * This class shall be used to pass all what is needed in order to create/init a dlms session with public client
 */
public class PublicClientSpecs {

    private final int publicClientMacAddress;

    public PublicClientSpecs(int publicClientMacAddress) {
        this.publicClientMacAddress = publicClientMacAddress;
    }

    public BigDecimal getPublicClientMacAddress() {
        return BigDecimal.valueOf(publicClientMacAddress);
    }
}
