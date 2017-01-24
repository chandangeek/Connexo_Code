package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Copyrights EnergyICT
 * Date: 19/03/12
 * Time: 16:59
 *
 * @author jme
 */
public enum PLCOFDMType2PHYAndMACCountersMethods implements DLMSClassMethods {

    RESET(1, 0x10);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    /**
     * Private constructor
     *
     * @param methodNr  the number of the method
     * @param shortName the shortName of the method
     */
    private PLCOFDMType2PHYAndMACCountersMethods(int methodNr, int shortName) {
        this.methodNumber = methodNr;
        this.shortName = shortName;
    }

    /**
     * Getter for the method number
     *
     * @return the method number as int
     */
    public int getMethodNumber() {
        return this.methodNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PLC_OFDM_TYPE2_PHY_AND_MAC_COUNTERS;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }
}
