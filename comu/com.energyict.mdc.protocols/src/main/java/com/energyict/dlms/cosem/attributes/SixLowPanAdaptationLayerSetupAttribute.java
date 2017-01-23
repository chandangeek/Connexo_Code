/**
 *
 */
package com.energyict.dlms.cosem.attributes;


import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 19/03/12
 * Time: 16:59
 *
 * @author jme
 */
public enum SixLowPanAdaptationLayerSetupAttribute implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x0000),
    ADP_MAX_HOPS(2, 0x0008),
    ADP_WEAK_LQI_VALUE(3, 0x0010),
    ADP_SECURITY_LEVEL(4, 0x0018),
    ADP_PREFIX_TABLE(5, 0x0020),
    ADP_ROUTING_CONFIGURATION(6, 0x0028),
    ADP_BROADCAST_LOG_TABLE_ENTRY_TTL(7, 0x0030),
    ADP_ROUTING_TABLE(8, 0x0038),
    ADP_CONTEXT_INFORMATION_TABLE(9, 0x0040),
    ADP_BLACKLIST_TABLE(10, 0x0048),
    ADP_BROADCAST_LOG_TABLE(11, 0x0050),
    ADP_GROUP_TABLE(12, 0x0058),
    ADP_MAX_JOIN_WAIT_TIME(13, 0x0060),
    ADP_PATH_DISCOVERY_TIME(14, 0x0068),
    ADP_ACTIVE_KEY_INDEX(15, 0x0070),
    ADP_METRIC_TYPE(16, 0x0078),
    ADP_COORD_SHORT_ADDRESS(17, 0x0080),
    ADP_DISABLE_DEFAULT_ROUTING(18, 0x0088),
    ADP_DEVICE_TYPE(19, 0x0090);

    private final int attributeNumber;
    private final int shortName;

    private SixLowPanAdaptationLayerSetupAttribute(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.SIX_LOW_PAN_ADAPTATION_LAYER_SETUP;
    }

    public int getShortName() {
        return shortName;
    }

    /**
     * @param attributeNumber
     * @return
     */
    public static SixLowPanAdaptationLayerSetupAttribute findByAttributeNumber(int attributeNumber) {
        for (SixLowPanAdaptationLayerSetupAttribute attribute : SixLowPanAdaptationLayerSetupAttribute.values()) {
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
    public static SixLowPanAdaptationLayerSetupAttribute findByShortName(int shortName) {
        for (SixLowPanAdaptationLayerSetupAttribute attribute : SixLowPanAdaptationLayerSetupAttribute.values()) {
            if (attribute.getShortName() == shortName) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No shortName found for id = " + shortName);
    }

}
