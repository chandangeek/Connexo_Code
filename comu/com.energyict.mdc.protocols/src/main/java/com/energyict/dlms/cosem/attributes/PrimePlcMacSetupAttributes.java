package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 6/5/12
 * Time: 4:09 PM
 */
public enum PrimePlcMacSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    MIN_SWITCH_SEARCH_TIME(2, 0x08),
    MAX_PROMOTION_PDU(3, 0x10),
    PROMOTION_PDU_TX_PERIOD(4, 0x18),
    BEACONS_PER_FRAME(5, 0x20),
    SCP_MAX_TX_ATTEMPTS(6, 0x28),
    CTL_RE_TX_TIMER(7, 0x30),
    MAX_CTL_RE_TX(8, 0x38);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param shortName       the shortName of the attribute
     */
    private PrimePlcMacSetupAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PRIME_PLC_MAC_SETUP;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
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

    /**
     * @param attributeNumber
     * @return
     */
    public static PrimePlcMacSetupAttributes findByAttributeNumber(int attributeNumber) {
        for (PrimePlcMacSetupAttributes attribute : PrimePlcMacSetupAttributes.values()) {
            if (attribute.getAttributeNumber() == attributeNumber) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attributeNumber);
    }

}
