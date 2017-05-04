/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum WebPortalAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    USER_NAME(2, 0x08),
    USER_PASSWORD(3, 0x10),
    HTTP_PORT(4, 0x18),
    HTTPS_PORT(5, 0x20),
    IS_GZIP_ENABLED(6, 0x28),
    IS_SSL_ENABLED(7, 0x30),
    AUTHENTICATION_MECHANISM(8, 0x38),
    MAX_LOGIN_ATTEMPTS(9, 0x40),
    LOCKOUT_DURATION(10, 0x48),
    ENABLED_INTERFACES(11, 0x50),
    CLIENT_CERTIFICATES(12, 0x58);

    private int attributeNumber;
    private int shortName;

    private WebPortalAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.WEB_PORTAL_CONFIGURATION;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static WebPortalAttributes findByAttributeNumber(int attribute) {
        for (WebPortalAttributes limiterAttribute : WebPortalAttributes.values()) {
            if (limiterAttribute.getAttributeNumber() == attribute) {
                return limiterAttribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attribute);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}