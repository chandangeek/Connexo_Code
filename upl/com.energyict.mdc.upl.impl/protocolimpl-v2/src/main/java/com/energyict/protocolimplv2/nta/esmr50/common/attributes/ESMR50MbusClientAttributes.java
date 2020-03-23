package com.energyict.protocolimplv2.nta.esmr50.common.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;
import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 31-aug-2011
 * Time: 14:45:52
 */
public enum ESMR50MbusClientAttributes implements DLMSClassAttributes {

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
    KEY_STATUS(14, 0x70),
    FUAK_STATUS(255, 0x80);

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

    private int version;
    /**
     * Indicating BlueBook 9th or below is supported
     */
    public static final int VERSION9 = 9;
    /**
     * Indicating BlueBook 10th is supported
     */
    public static final int VERSION10 = 10;

    public ESMR50MbusClientAttributes forVersion(int version) {
        this.version = version;
        return this;
    }

    /**
     * Private constructor
     *
     * @param attrNr the attribute number
     * @param sn10     the value for the ShortName DLMS version 10
     */
    private ESMR50MbusClientAttributes(int attrNr, int sn10) {
        this.attributeNumber = attrNr;
        this.shortNameV10 = sn10;
        this.shortNameV9 = sn10;
    }

    /**
     * Private constructor
     *
     * @param attrNr the attribute number
     * @param sn9     the value for the ShortName DLMS version 9
     * @param sn10     the value for the ShortName DLMS version 10
     */
    private ESMR50MbusClientAttributes(int attrNr, int sn9, int sn10) {
        this.attributeNumber = attrNr;
        this.shortNameV9 = sn9;
        this.shortNameV10 = sn10;
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
