/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.dlms.cosem.attributes;


import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

/**
 * @author jme
 *
 */
public enum SFSKIec61334LLCSetupAttribute implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0x00),
	MAX_FRAME_LENGTH(2, 0x08),
	REPLY_STATUS_LIST(3, 0x10);

	private final int attributeNumber;
	private final int shortName;

	private SFSKIec61334LLCSetupAttribute(int attrNr, int sn) {
		this.attributeNumber = attrNr;
		this.shortName = sn;
	}

	public int getAttributeNumber() {
		return attributeNumber;
	}

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.S_FSK_IEC_61334_4_32_LLC_SETUP;
    }

    public int getShortName() {
		return shortName;
	}

	/**
	 * @param attributeNumber
	 * @return
	 */
	public static SFSKIec61334LLCSetupAttribute findByAttributeNumber(int attributeNumber) {
		for (SFSKIec61334LLCSetupAttribute attribute : SFSKIec61334LLCSetupAttribute.values()) {
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
	public static SFSKIec61334LLCSetupAttribute findByShortName(int shortName) {
		for (SFSKIec61334LLCSetupAttribute attribute : SFSKIec61334LLCSetupAttribute.values()) {
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
