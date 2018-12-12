package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * VPN setup IC, methods
 * class id = 20029, version = 0, logical name = 0-160:96.128.0.255 (00A0608000FF)
 * The VPN setup IC is a manufacturer-specific COSEM IC which can be used for configuring and quering the VPN link status. Changes made to the attributes of this IC take effect after
 * closing the DLMS association, or after invoking the refresh_vpn_config method.
 */
public enum VPNSetupMethods implements DLMSClassMethods {
    /**
     * Triggers an explicit refresh of the VPN configuration using the attributes set previously.
     * request_data ::= <ignored>
     * response_data ::= integer(0)
     */
    REFRESH_VPN_CONFIG(1, 0x00);


    /** The method number. */
    private final int methodNumber;

    /** The short address. */
    private final int shortAddress;

    /**
     * Create a new instance.
     *
     * @param 	methodNumber		The method number.
     * @param 	shortAddress		The short address.
     */
    private VPNSetupMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.VPN_SETUP;
    }

    /**
     * {@inheritDoc}
     */
    public final int getShortName() {
        return this.shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final int getMethodNumber() {
        return this.methodNumber;
    }
}
