package com.energyict.protocolimpl.dlms.as220.objects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;
import com.energyict.obis.ObisCode;


/**
 * Copyrights EnergyICT
 * Date: 31-mei-2010
 * Time: 14:56:35
 */
public enum PLCObjectAttribute implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0x00),
	REPEATER(-1, 0xB8);

	private final int attributeNumber;
	private final int shortName;

	private PLCObjectAttribute(int attrNr, int sn) {
		this.attributeNumber = attrNr;
		this.shortName = sn;
	}

	public int getAttributeNumber() {
		return attributeNumber;
	}

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.DATA;
    }

    public int getShortName() {
		return shortName;
	}

	/**
	 * @param attributeNumber
	 * @return
	 */
	public static PLCObjectAttribute findByAttributeNumber(int attributeNumber) {
		for (PLCObjectAttribute attribute : PLCObjectAttribute.values()) {
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
	public static PLCObjectAttribute findByShortName(int shortName) {
		for (PLCObjectAttribute attribute : PLCObjectAttribute.values()) {
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
