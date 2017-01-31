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
public enum SFSKMacCountersAttribute implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0x00),
	SYNCHRONIZATION_REGISTER(2, 0x08),
	DESYNCHRONIZATION_LISTING(3, 0x10),
	BROADCAST_FRAMES_COUNTER(4, 0x18),
	REPETITIONS_COUNTER(5, 0x20),
	TRANSMISSIONS_COUNTER(6, 0x28),
	CRC_OK_FRAMES_COUNTER(7, 0x30),
	CRC_NOK_FRAMES_COUNTER(8, 0x38);

	private final int attributeNumber;
	private final int shortName;

	private SFSKMacCountersAttribute(int attrNr, int sn) {
		this.attributeNumber = attrNr;
		this.shortName = sn;
	}

	public int getAttributeNumber() {
		return attributeNumber;
	}

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.S_FSK_MAC_COUNTERS;
    }

    public int getShortName() {
		return shortName;
	}

	/**
	 * @param attributeNumber
	 * @return
	 */
	public static SFSKMacCountersAttribute findByAttributeNumber(int attributeNumber) {
		for (SFSKMacCountersAttribute attribute : SFSKMacCountersAttribute.values()) {
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
	public static SFSKMacCountersAttribute findByShortName(int shortName) {
		for (SFSKMacCountersAttribute attribute : SFSKMacCountersAttribute.values()) {
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
