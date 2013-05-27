/**
 *
 */
package com.energyict.dlms.cosem.attributes;


import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * /**
 * Copyrights EnergyICT
 * Date: 19/03/12
 * Time: 16:59
 *
 * @author jme
 */
public enum PLCOFDMType2MACSetupAttribute implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x0000),
    MAC_SHORT_ADDRESS(2, 0x0008),
    MAC_ASSOCIATED_PAN_COORD(3, 0x0010),
    MAC_COORD_SHORT_ADDRESS(4, 0x0018),
    MAC_PAN_ID(5, 0x0020),
    MAC_NUMBER_OF_HOPS(6, 0x0028),
    MAC_MAX_ORPHAN_TIMER(7, 0x0030),
    MAC_NEIGHBOR_TABLE(8, 0x0038),
    MAC_SECURITY_ACTIVATION(9, 0x0040);

    private final int attributeNumber;
    private final int shortName;

    private PLCOFDMType2MACSetupAttribute(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public int getShortName() {
        return shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PLC_OFDM_TYPE2_MAC_SETUP;
    }


    /**
     * @param attributeNumber
     * @return
     */
    public static PLCOFDMType2MACSetupAttribute findByAttributeNumber(int attributeNumber) {
        for (PLCOFDMType2MACSetupAttribute attribute : PLCOFDMType2MACSetupAttribute.values()) {
            if (attribute.getAttributeNumber() == attributeNumber) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attributeNumber);
    }

    /**
     * @param shortName
     * @return
     */
    public static PLCOFDMType2MACSetupAttribute findByShortName(int shortName) {
        for (PLCOFDMType2MACSetupAttribute attribute : PLCOFDMType2MACSetupAttribute.values()) {
            if (attribute.getShortName() == shortName) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No shortName found for id = " + shortName);
    }

}
