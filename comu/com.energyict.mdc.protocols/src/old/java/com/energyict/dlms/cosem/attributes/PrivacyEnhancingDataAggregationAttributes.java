package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

public enum PrivacyEnhancingDataAggregationAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    OWN_PUBLIC_KEY(2, 0x08),
    MAX_GROUP_SIZE(3, 0x10),
    PUBLIC_KEYS_OF_AGGREGATION_GROUP(4, 0x18);

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
    private PrivacyEnhancingDataAggregationAttributes(int attributeNumber, int shortName) {
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

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}