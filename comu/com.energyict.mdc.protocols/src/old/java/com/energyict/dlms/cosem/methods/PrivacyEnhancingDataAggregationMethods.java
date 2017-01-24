package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

public enum PrivacyEnhancingDataAggregationMethods implements DLMSClassMethods {

    GENERATE_NEW_KEYPAIR(1, 0x00);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    PrivacyEnhancingDataAggregationMethods(final int methodNumber, final int shortName) {
        this.methodNumber = methodNumber;
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
        return DLMSClassId.MANUFACTURER_SPECIFIC_8194;
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
