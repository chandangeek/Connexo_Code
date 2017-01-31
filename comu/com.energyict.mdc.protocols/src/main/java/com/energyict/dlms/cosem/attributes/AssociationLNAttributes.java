/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum AssociationLNAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    OBJECT_LIST(2, 0x08),
    ASSOCIATED_PARTNERS_ID(3, 0x10),
    APPLICATION_CONTEXT_NAME(4, 0x18),
    XDLMS_CONTEXT_INFO(5, 0x20),
    AUTHENTICATION_MECHANISM_NAME(6, 0x28),
    LLS_SECRET(7, 0x30),
    ASSOCIATION_STATUS(8, 0x38),
    SECURITY_SETUP_REFERENCE(9, 0x40);

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortName;

    /**
     * Private constructor
     *
     * @param attributeNumber the chronological number of the attribute
     * @param shortName       the shortname of the attribute
     */
    private AssociationLNAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
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
        return DLMSClassId.ACTIVITY_CALENDAR;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

}
