package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Contains functionality to access attributes of the Zigbee HAN management object (independent whether or not shortName or logicalNames are used)
 * <br/>
 * <p><b>
 * TODO the SN attribute numbering is purely based on logical sequence numbering. This object is not defined in the BlueBook yet, <br>
 * TODO so additional attributes may be added in the future
 * </b></p>
 * Copyrights EnergyICT<br/>
 * Date: 22-jul-2011<br/>
 * Time: 11:36:44<br/>
 */
public enum ZigbeeHanManagementAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
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

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.ZIGBEE_HAN_MANAGEMENT;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
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
