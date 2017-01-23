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
public enum DisconnectControlAttribute implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0x00),
	OUTPUT_STATE(2, 0x08),
	CONTROL_STATE(3, 0x10),
	CONTROL_MODE(4, 0x18);

	private final int attributeNumber;
	private final int shortName;

	private DisconnectControlAttribute(int attrNr, int sn) {
		this.attributeNumber = attrNr;
		this.shortName = sn;
	}

	public int getAttributeNumber() {
		return attributeNumber;
	}

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.DISCONNECT_CONTROL;
    }

    public int getShortName() {
		return shortName;
	}

	/**
	 * @param attributeNumber
	 * @return
	 */
	public static DisconnectControlAttribute findByAttributeNumber(int attributeNumber) {
		for (DisconnectControlAttribute attribute : DisconnectControlAttribute.values()) {
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
	public static DisconnectControlAttribute findByShortName(int shortName) {
		for (DisconnectControlAttribute attribute : DisconnectControlAttribute.values()) {
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
