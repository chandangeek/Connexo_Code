package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Contains functionality to access attributes of the Zigbee SETC Control object (independent whether or not shortName or logicalNames are used)
 * <br/>
 * <p><b>
 * TODO the SN attribute numbering is purely based on logical sequence numbering. This object is not defined in the BlueBook yet, <br>
 * TODO so additional attributes may be added in the future
 *</b></p>
 * Copyrights EnergyICT<br/>
 * Date: 22-jul-2011<br/>
 * Time: 15:14:55<br/>
 */
public enum ZigbeeSETCControlAttributes implements DLMSClassAttributes {

    LOGICAL_NAMTE(1, 0x00),
    ENABLE_DISABLE_JOINING(2, 0x08),
    JOIN_TIMEOUT(3, 0x10);

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortName;

    ZigbeeSETCControlAttributes(final int attributeNumber, final int shortName) {
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

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

}
