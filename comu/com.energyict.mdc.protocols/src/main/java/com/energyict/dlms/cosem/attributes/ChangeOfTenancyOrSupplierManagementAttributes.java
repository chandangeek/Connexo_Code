/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum ChangeOfTenancyOrSupplierManagementAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    START_TIME(2, 0x08),
    SCRIPT_EXECUTED(3, 0x10),
    TENANT_REFERENCE(4, 0x18),
    TENANT_ID(5, 0x20),
    SUPPLIER_REFERENCE(6, 0x28),
    SUPPLIER_ID(7, 0x30),
    PASSIVE_START_TIME(8, 0x38),
    PASSIVE_SCRIPT_EXECUTED(9, 0x40),
    PASSIVE_TENANT_REFERENCE(10, 0x48),
    PASSIVE_TENANT_ID(11, 0x50),
    PASSIVE_SUPPLIER_REFERENCE(12, 0x58),
    PASSIVE_SUPPLIER_ID(13, 0x60),
    ACTIVATION_TIME(14, 0x68);

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute
     */
    private final int shortName;

    ChangeOfTenancyOrSupplierManagementAttributes(final int attributeNumber, final int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.CHANGE_OF_TENANT_SUPPLIER_MANAGEMENT;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}