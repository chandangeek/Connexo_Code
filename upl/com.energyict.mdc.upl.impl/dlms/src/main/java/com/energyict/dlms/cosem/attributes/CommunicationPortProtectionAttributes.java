package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

public enum CommunicationPortProtectionAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    PROTECTION_MODE(2, 0x08),
    ALLOWED_FAILED_ATTEMPTS(3, 0x10),
    INITIAL_LOCKOUT_TIME(4, 0x18),
    STEEPNESS_FACTOR(5, 0x20),
    MAX_LOCKOUT_TIME(6, 0x28),
    PORT_REFERENCE(7, 0x30),
    PROTECTION_STATUS(8, 0x38),
    FAILED_ATTEMPTS(9, 0x40),
    CUMULATIVE_FAILED_ATTEMPTS(10, 0x48);

    private final int attributeNumber;
    private final int shortName;

    CommunicationPortProtectionAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    @Override
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    @Override
    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    @Override
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.COMMUNICATION_PORT_PROTECTION;
    }

    @Override
    public int getShortName() {
        return this.shortName;
    }

}
