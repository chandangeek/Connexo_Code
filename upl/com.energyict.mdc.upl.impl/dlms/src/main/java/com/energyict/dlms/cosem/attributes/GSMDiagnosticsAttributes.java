package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Created by H165680 on 17/04/2017.
 */
public enum GSMDiagnosticsAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    OPERATOR(2, 0x08),
    STATUS(3, 0x10),
    CS_ATTACHMENT(4, 0x18),
    PS_STATUS(5, 0x20),
    CELL_INFO(6, 0x28),
    ADJACENT_CELLS(7, 0x30),
    CAPTURE_TIME(8, 0x38),
    PP3_NETWORk_STATUS(9, 0x040),
    MODEM_TYPE(-1, 0xFF),
    MODEM_VERSION(-2, 0xFF),
    IMEI(-3, 0xFF),
    IMSI(-4, 0xFF),
    SIM_CARD_ID(-5, 0xFF),
    MS_ISDN_NUMBER(-6, 0xFF),
    TOTAL_TX_BYTES(-7, 0xFF),
    TOTAL_RX_BYTES(-8, 0xFF);

    private final int attributeNumber;
    private final int shortName;

    private GSMDiagnosticsAttributes(int attrNr, int sn) {
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
        return DLMSClassId.GSM_DIAGNOSTICS;
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