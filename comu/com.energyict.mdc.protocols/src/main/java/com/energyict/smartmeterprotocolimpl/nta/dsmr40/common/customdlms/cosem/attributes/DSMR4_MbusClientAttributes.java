/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;

public enum DSMR4_MbusClientAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    MBUS_PORT_REFERENCE(2, 0x08),
    CAPTURE_DEFINITION(3, 0x10),
    CAPTURE_PERIOD(4, 0x18),
    PRIMARY_ADDRESS(5, 0x20),
    IDENTIFICATION_NUMBER(6, 0x28),
    MANUFACTURER_ID(7, 0x30),
    VERSION(8, 0x38),
    DEVICE_TYPE(9, 0x40),
    ACCESS_NUMBER(10, 0x48),
    STATUS(11, 0x50),
    ALARM(12, 0x58),
    ENCRYPTION_STATUS(13, 0x60),
    KEY_STATUS(14, 0x70);

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
     * @param attrNr the attribute number
     * @param sn     the value for the ShortName
     */
    private DSMR4_MbusClientAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return attributeNumber;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.MBUS_CLIENT;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

}
