package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;


public enum GSMDiagnosticAttributes implements DLMSClassAttributes{

    LOGICAL_NAME(1),
    OPERATOR(2),
    STATUS(3),
    CS_ATTACHMENT(4),
    PS_STATUS(5),
    CELL_INFO_BASE(6),
    /**
     * Attribute id: CELL_INFO_CELL_ID [61] - attribute 6 field 1.
     */
    CELL_INFO_CELL_ID(61),

    /**
     * Attribute id: CELL_INFO_LOCATION_ID [62] - attribute 6 field 2.
     */
    CELL_INFO_LOCATION_ID(62),

    /**
     * Attribute id: CELL_INFO_LOCATION_ID [63] - attribute 6 field 3.
     */
    CELL_INFO_SIGNAL_QUALITY(63),

    /**
     * Attribute id: CELL_INFO_BER [64] - attribute 6 field 4.
     */
    CELL_INFO_BER(64),

    /**
     * Attribute id: CELL_INFO_CELL_INFO_mcc [65] - attribute 6 field 5.
     */
    CELL_INFO_MCC(65),

    /**
     * Attribute id: CELL_INFO_CELL_INFO_mnc [66] - attribute 6 field 6.
     */
    CELL_INFO_MNC(66),

    /**
     * Attribute id: CELL_INFO_CELL_INFO_channel_number [67] - attribute 6 field 7.
     */
    CELL_INFO_CHANNEL_NUMBER(67),
    ADJACENT_CELLS_BASE(7),
    /**
     * Attribute id: ADJACENT_CELLS_CELL_ID [71] - attribute 7 field 1.
     */
    ADJACENT_CELLS_CELL_ID(71),

    /**
     * Attribute id: ADJACENT_CELLS_SIGNAL_QUALITY [72] - attribute 7 field 2.
     */
    ADJACENT_CELLS_SIGNAL_QUALITY(72),
    CAPTURE_TIME(8);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Constructor for the attribute types
     * @param lnAttribute the LONG_NAME attribute number
     */
    private GSMDiagnosticAttributes(int lnAttribute){
        this.attributeNumber = lnAttribute;
        this.shortName = (this.attributeNumber - 1) *8;
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
        return DLMSClassId.MAC_ADDRESS_SETUP;
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
