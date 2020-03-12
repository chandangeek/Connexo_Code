package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 25-nov-2010
 * Time: 15:46:04
 */
public enum MBusClientAttributes implements DLMSClassAttributes {

                                                                      // VERSION0_D_S_M_R_23_SPEC
                                                                           // VERSION0_BLUE_BOOK_9TH_EDITION
                                                                                  // VERSION0_BLUE_BOOK_10TH_EDITION(
                                                                                        // VERSION1
    LOGICAL_NAME(1,                               0x00, 0x00, 0x00, 0x00),
    MBUS_PORT_REFERENCE(2,                        0x10, 0x10, 0x08, 0x08),
    CAPTURE_DEFINITION(3,                         0x18, 0x18, 0x10, 0x10),
    CAPTURE_PERIOD(4,                             0x20, 0x20, 0x18, 0x18),
    PRIMARY_ADDRESS(5,                            0x28, 0x28, 0x20, 0x20),
    IDENTIFICATION_NUMBER(6,                      0x30, 0x30, 0x28, 0x28),
    MANUFACTURER_ID(7,                            0x38, 0x38, 0x30, 0x30),
    VERSION(8,                                    0x40, 0x40, 0x38, 0x38),
    DEVICE_TYPE(9,                                0x48, 0x48, 0x40, 0x40),
    ACCESS_NUMBER(10,                             0x50, 0x50, 0x48, 0x48),
    STATUS(11,                                    0x58, 0x58, 0x50, 0x50),
    ALARM(12,                                     0x60, 0x60, 0x58, 0x58),
    CONFIGURATION(13,                             0xFF, 0xFF, 0xFF, 0x60),
    ENCRYPTION_KEY_STATUS(14,                     0xFF, 0xFF, 0xFF, 0x68),
    FUAK_STATUS(255,                              0xFF, 0xFF, 0xFF, 0x80); //E.S.M.R. 5.0 SPECIFIC

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int[] shortNames;

    /**
     * The used version (currently only 9(BlueBook 9th or below) or 10(BlueBook 10th or higher)
     */
    private MBusClient.VERSION version;

    MBusClientAttributes(int attrNr, int... shortNames) {
        this.attributeNumber = attrNr;
        this.shortNames = shortNames;
        this.version = MBusClient.VERSION.VERSION1;
    }

    /**
     * Getter for the current object with a specific version
     *
     * @param version the used version
     * @return this object with the version variable set to #version
     */
    public MBusClientAttributes forVersion(MBusClient.VERSION version) {
        this.version = version;
        return this;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return attributeNumber;
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
        return shortNames[version.getIndex()];
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }


}

