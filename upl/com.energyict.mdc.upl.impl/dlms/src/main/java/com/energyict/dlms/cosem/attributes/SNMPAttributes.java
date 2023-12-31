package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by cisac on 11/21/2016.
 */
public enum SNMPAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    ENABLED_INTERFACES(2, 0x08),
    USERS(3, 0x10),
    SYSTEM_CONTACT(4, 0x18),
    SYSTEM_LOCATION(5, 0x20),
    LOCAL_ENGINE_ID(6, 0x28),
    NOTIFICATION_TYPE(7, 0x30),
    NOTIFICATION_USER(8, 0x38),
    NOTIFICATION_HOST(9, 0x40),
    NOTIFICATION_PORT(10, 0x48),
    MAX_LOGIN_ATTEMPTS(11, 0x50),
    LOCKOUT_DURATION(12, 0x58);

    private int attributeNumber;
    private int shortName;

    private SNMPAttributes(int attrNr, int sn) {
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
        return DLMSClassId.SNMP_SETUP;
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
