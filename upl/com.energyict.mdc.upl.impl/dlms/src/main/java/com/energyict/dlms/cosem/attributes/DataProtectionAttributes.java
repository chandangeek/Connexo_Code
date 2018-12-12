package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by cisac on 12/14/2016.
 */
public enum DataProtectionAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    PROTECTION_BUFFER(2, 0x08),
    PROTECTION_OBJECT_LIST(3, 0x10),
    PROTECTION_PARAMETERS_GET(4, 0x18),
    PROTECTION_PARAMETERS_SET(5, 0x20),
    REQUIRED_PROTECTION(6, 0x28);

    /** Attribute ID. */
    private final int attributeId;

    /** The short name of the attribute (offset from base address). */
    private final int shortName;

    private DataProtectionAttributes(int attributeId, int shortName) {
        this.attributeId = attributeId;
        this.shortName = shortName;
    }

    @Override
    public int getAttributeNumber() {
        return attributeId;
    }

    @Override
    public int getShortName() {
        return shortName;
    }

    @Override
    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    @Override
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.DATA_PROTECTION;
    }
}
