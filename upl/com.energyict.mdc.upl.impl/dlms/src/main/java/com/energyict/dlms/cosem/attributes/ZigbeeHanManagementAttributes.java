package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Contains functionality to access attributes of the Zigbee HAN management object (independent whether or not shortName or logicalNames are used)
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 22-jul-2011<br/>
 * Time: 11:36:44<br/>
 */
public enum ZigbeeHanManagementAttributes implements DLMSClassAttributes{

    LOGICAL_NAMTE(1, 0x00),
    BLACK_LIST(2, 0x08),
    ACTIVE_DEVICES(3, 0x10),
    BACKUP_DATA(4, 0x18);

        /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortName;

    ZigbeeHanManagementAttributes(final int attributeNumber, final int shortName) {
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
        return DLMSClassId.ZIGBEE_HAN_MANAGEMENT;
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
