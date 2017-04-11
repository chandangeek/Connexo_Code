package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by cisac on 11/1/2016.
 */
public enum ConcentratorSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    IS_ACTIVE(2, 0x08),
    MAX_CONCURENT_SESSIONS(3, 0x10),
    METER_INFO(4, 0x18),
    PROTOCOL_LOG_LEVEL(5, 0x20);

    private final int attributeNumber;
    private final int shortName;

    private ConcentratorSetupAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    @Override
    public int getAttributeNumber() {
        return attributeNumber;
    }

    @Override
    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    @Override
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.CONCENTRATOR_SETUP;
    }

    @Override
    public int getShortName() {
        return shortName;
    }
}
