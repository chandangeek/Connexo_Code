/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum MbusClientAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00, 0x00),
    MBUS_PORT_REFERENCE(2, 0x10, 0x08),
    CAPTURE_DEFINITION(3, 0x18, 0x10),
    CAPTURE_PERIOD(4, 0x20, 0x18),
    PRIMARY_ADDRESS(5, 0x28, 0x20),
    IDENTIFICATION_NUMBER(6, 0x30, 0x28),
    MANUFACTURER_ID(7, 0x38, 0x30),
    VERSION(8, 0x40, 0x38),
    DEVICE_TYPE(9, 0x48, 0x40),
    ACCESS_NUMBER(10, 0x50, 0x48),
    STATUS(11, 0x58, 0x50),
    ALARM(12, 0x60, 0x58);

    /**
     * Indicating BlueBook 9th or below is supported
     */
    public static final int VERSION9 = 9;
    /**
     * Indicating BlueBook 10th is supported
     */
    public static final int VERSION10 = 10;

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortNameV9;
    /**
     * The shortName of this attribute according to BlueBook V10
     */
    private final int shortNameV10;

    /**
     * The used version (currently only 9(BlueBook 9th or below) or 10(BlueBook 10th or higher)
     */
    private int version;

    /**
     * Private constructor
     *
     * @param attrNr the attribute number
     * @param sn9    the value for the ShortName for version 9 or below
     * @param sn10   the value for the ShortName for version 10 or higher
     */
    private MbusClientAttributes(int attrNr, int sn9, int sn10) {
        this.attributeNumber = attrNr;
        this.shortNameV9 = sn9;
        this.shortNameV10 = sn10;
        this.version = VERSION9;
    }

    /**
     * Getter for the current object with a specific version
     *
     * @param version the used version
     * @return this object with the version variable set to #version
     */
    public MbusClientAttributes forVersion(int version) {
        this.version = version;
        return this;
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
        if (version == VERSION9) {
            return shortNameV9;
        } else {
            return shortNameV10;
        }
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }


}

