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
public enum SFSKReportingSystemListAttribute implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0x00),
	REPORTING_SYSTEM_LIST(2, 0x08);

	private final int attributeNumber;
	private final int shortName;

	private SFSKReportingSystemListAttribute(int attrNr, int sn) {
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
        return DLMSClassId.S_FSK_REPORTING_SYSTEM_LIST;
    }

    public int getShortName() {
		return shortName;
	}

	/**
	 * @param attributeNumber
	 * @return
	 */
	public static SFSKReportingSystemListAttribute findByAttributeNumber(int attributeNumber) {
		for (SFSKReportingSystemListAttribute attribute : SFSKReportingSystemListAttribute.values()) {
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
	public static SFSKReportingSystemListAttribute findByShortName(int shortName) {
		for (SFSKReportingSystemListAttribute attribute : SFSKReportingSystemListAttribute.values()) {
			if (attribute.getShortName() == shortName) {
				return attribute;
			}
		}
		throw new IllegalArgumentException("No shortName found for id = " + shortName);
	}

}
