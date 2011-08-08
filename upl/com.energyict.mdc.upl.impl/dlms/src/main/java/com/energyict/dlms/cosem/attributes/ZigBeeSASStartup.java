package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Contains functionality to access attributes of the ZigBee SAS Startup object (independent whether or not shortName or logicalNames are used)
 * <br/>
 * <p><b>
 * TODO the SN attribute numbering is purely based on logical sequence numbering. This object is not defined in the BlueBook yet, <br>
 * TODO so additional attributes may be added in the future
 * </b></p>
 * Copyrights EnergyICT
 * Date: 8-aug-2011
 * Time: 13:26:17
 */
public enum ZigBeeSASStartup {

    LOGICAL_NAMTE(1, 0x00),
    SHORT_ADDRESS(2, 0x08),
    EXTENDED_PAN_ID(3, 0x10),
    PAN_ID(4, 0x18),
    CHANNEL_MASK(5, 0x20),
    PROTOCOL_VERSION(6, 0x28),
    STACK_PROFILE(7, 0x30),
    START_UP_ONTROL(8, 0x38),
    TRUST_CENTER_ADDRESS(9, 0x40),
    LINK_KEY(10, 0x48),
    NETWORK_KEY(11, 0x50),
    USE_INSECURE_JOIN(12, 0x58),;

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortName;

    ZigBeeSASStartup(final int attributeNumber, final int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
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
        return DLMSClassId.ZIGBEE_SETC_CONTROL;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

}
