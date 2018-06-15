package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

public enum FirmwareConfigurationAttributes implements DLMSClassAttributes {

    /**
     * Identifies the data IC instance (the OBIS code).
     */
    LOGICAL_NAME(1, 0x01),

    /**
     * Holds the maximum interval during which no uplink communication is allowed in seconds.
     * This value is consulted by the firmware after a having performed a firmware upgrade. If no
     * uplink communication has taken place for longer than the configured value, the firmware rolls
     * back to its previous state. If the value written is 0x0 or 0xffffffff, the check after upgrade is disabled.
     */
    MAX_INACTIVE_UPLINK(2, 0x02);

    private final int attributeNumber;
    private final int shortName;

    FirmwareConfigurationAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
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
        return DLMSClassId.FIRMWARE_CONFIGURATION_IC;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static FirmwareConfigurationAttributes findByAttributeNumber(int attribute) {
        for (FirmwareConfigurationAttributes limiterAttribute : FirmwareConfigurationAttributes.values()) {
            if (limiterAttribute.getAttributeNumber() == attribute) {
                return limiterAttribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attribute);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}