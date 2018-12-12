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
public enum ScheduleManagerAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    SCHEDULES(2, 0x08),
    SCHEDULER_STATE(3, 0x10);

    private final int attributeNumber;
    private final int shortName;

    private ScheduleManagerAttributes(int attrNr, int sn) {
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
        return DLMSClassId.SCHEDULE_MANAGER;
    }
}