package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 13:53
 */
public enum DeviceTypeManagerAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    DEVICE_TYPES(2, 0x08);

    private final int attributeNumber;
    private final int shortName;

    private DeviceTypeManagerAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    @Override
    public int getAttributeNumber() {
        return attributeNumber;
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
        return DLMSClassId.DEVICE_TYPE_MANAGER;
    }
}