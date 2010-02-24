/**
 *
 */
package com.energyict.dlms.cosem.attributes;

import java.io.IOException;

/**
 * @author jme
 *
 */
public enum SFSKActiveInitiatorAttribute implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0x00),
	ACTIVE_INITIATOR(2, 0x08);

	private final int attributeNumber;
	private final int shortName;

	private SFSKActiveInitiatorAttribute(int attrNr, int sn) {
		this.attributeNumber = attrNr;
		this.shortName = sn;
	}

	public int getAttributeNumber() {
		return attributeNumber;
	}

	public int getShortName() {
		return shortName;
	}

	/**
	 * @param attributeNumber
	 * @return
	 * @throws IOException
	 */
	public static SFSKActiveInitiatorAttribute findByAttributeNumber(int attributeNumber) {
		for (SFSKActiveInitiatorAttribute attribute : SFSKActiveInitiatorAttribute.values()) {
			if (attribute.getAttributeNumber() == attributeNumber) {
				return attribute;
			}
		}
		throw new IllegalArgumentException("No attributeNumber found for id = " + attributeNumber);
	}

	/**
	 * @param shortName
	 * @return
	 * @throws IOException
	 */
	public static SFSKActiveInitiatorAttribute findByShortName(int shortName) {
		for (SFSKActiveInitiatorAttribute attribute : SFSKActiveInitiatorAttribute.values()) {
			if (attribute.getShortName() == shortName) {
				return attribute;
			}
		}
		throw new IllegalArgumentException("No shortName found for id = " + shortName);
	}

}
