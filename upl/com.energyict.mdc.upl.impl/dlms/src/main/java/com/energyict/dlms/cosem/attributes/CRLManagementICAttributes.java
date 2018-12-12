package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by cisac on 5/9/2017.
 */
public enum CRLManagementICAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    CRL_LIST(2, 0x08);

    /** Attribute ID. */
    private final int attributeId;

    /** The short name of the attribute (offset from base address). */
    private final int shortName;

    private CRLManagementICAttributes(int attributeId, int shortName) {
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
        return DLMSClassId.CRL_MANAGEMENT_IC;
    }
}
