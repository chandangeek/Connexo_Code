/**
 *
 */
package com.energyict.dlms.cosem.attributes;


import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public enum SFSKSyncTimeoutsAttribute implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0x00),
	SEARCH_INITIATOR_TIMEOUT(2, 0x08),
	SYNCHRONIZATION_CONFIRMATION_TIMEOUT(3, 0x10),
	TIME_OUT_NOT_ADDRESSED(4, 0x18),
	TIME_OUT_FRAME_NOT_OK(5, 0x20);

	private final int attributeNumber;
	private final int shortName;

	private SFSKSyncTimeoutsAttribute(int attrNr, int sn) {
		this.attributeNumber = attrNr;
		this.shortName = sn;
	}

	public int getAttributeNumber() {
		return attributeNumber;
	}

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.S_FSK_MAC_SYNC_TIMEOUTS;
    }

    public int getShortName() {
		return shortName;
	}

	/**
	 * @param attributeNumber
	 * @return
	 */
	public static SFSKSyncTimeoutsAttribute findByAttributeNumber(int attributeNumber) {
		for (SFSKSyncTimeoutsAttribute attribute : SFSKSyncTimeoutsAttribute.values()) {
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
	public static SFSKSyncTimeoutsAttribute findByShortName(int shortName) {
		for (SFSKSyncTimeoutsAttribute attribute : SFSKSyncTimeoutsAttribute.values()) {
			if (attribute.getShortName() == shortName) {
				return attribute;
			}
		}
		throw new IllegalArgumentException("No shortName found for id = " + shortName);
	}

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }


}
