package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by H245796 on 18.12.2017.
 */
public enum GPRSStandardStatusAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    SUBSCRIBER_ID(2, 0x08),
    MODEM_MODEL(3, 0x10),
    MODEM_REVISION(4, 0x18),
    MODEM_FIRMWARE(5, 0x20),
    MODEM_SERIAL_NR(6, 0x28),
    NETWORK_PROVIDER(7, 0x30),
    SIGNAL_STRENGTH(8, 0x38);

    private final int attributeNumber;
    private final int shortName;

    private GPRSStandardStatusAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
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
        return DLMSClassId.GSM_STANDARD_STATUS;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}