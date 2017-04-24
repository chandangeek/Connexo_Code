/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * @author khe
 */
public enum UplinkPingConfigurationMethods implements DLMSClassMethods {

    ENABLE(1, 80),
    DISABLE(2, 88);

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
    private UplinkPingConfigurationMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.UPLINK_PING_SETUP;
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
