/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * @author khe
 */
public enum WebPortalMethods implements DLMSClassMethods {

    CHANGE_USER_NAME(1, 10),
    CHANGE_USER_PASSWORD(2, 18),
    ENABLE_GZIP(3, 20),
    ENABLE_SSL(4, 28),
    IMPORT_CLIENT_CERTIFICATE(5, 30),
    REMOVE_CLIENT_CERTIFICATE(6, 38);
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
    private WebPortalMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.WEB_PORTAL_CONFIGURATION;
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