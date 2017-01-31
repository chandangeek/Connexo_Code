/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * @author khe
 */
public enum NTPServerAddressMethods implements DLMSClassMethods {

    NTP_SYNC(1, 80);

    /**
     * The method number.
     */
    private final int methodNumber;

    /**
     * The short address.
     */
    private final int shortAddress;

    /**
     * Create a new instance.
     *
     * @param methodNumber The method number.
     * @param shortAddress The short address.
     */
    private NTPServerAddressMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.NTP_SERVER_ADDRESS;
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