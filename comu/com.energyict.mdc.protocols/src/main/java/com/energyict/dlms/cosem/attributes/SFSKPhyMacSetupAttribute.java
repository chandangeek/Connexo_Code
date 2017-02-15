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
public enum SFSKPhyMacSetupAttribute implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0x00),
	INITIATOR_ELECTRICAL_PHASE(2, 0x08),
	DELTA_ELECTRICAL_PHASE(3, 0x10),
	MAX_RECEIVING_GAIN(4, 0x18),
	MAX_TRANSMITTING_GAIN(5, 0x20),
	SEARCH_INITIATOR_GAIN(6, 0x28),
	FREQUENCIES(7, 0x30),
	MAC_ADDRESS(8, 0x38),
	MAC_GROUP_ADDRESSES(9, 0x40),
	REPEATER(10, 0x48),
	REPEATER_STATUS(11, 0x50),
	MIN_DELTA_CREDIT(12, 0x58),
	INITIATOR_MAC_ADDRESS(13, 0x60),
	SYNCHRONIZATION_LOCKED(14, 0x68),
    TRANSMISSION_SPEED(15, 0x70),

    // Attention, this a a Manufacturer specific value (Eandis)
	ACTIVE_CHANNEL(16, 0x78);

	private final int attributeNumber;
	private final int shortName;

	private SFSKPhyMacSetupAttribute(int attrNr, int sn) {
		this.attributeNumber = attrNr;
		this.shortName = sn;
	}

	public int getAttributeNumber() {
		return attributeNumber;
	}

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.S_FSK_PHY_MAC_SETUP;
    }

    public int getShortName() {
		return shortName;
	}

	/**
	 * @param attributeNumber
	 * @return
	 */
	public static SFSKPhyMacSetupAttribute findByAttributeNumber(int attributeNumber) {
		for (SFSKPhyMacSetupAttribute attribute : SFSKPhyMacSetupAttribute.values()) {
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
	public static SFSKPhyMacSetupAttribute findByShortName(int shortName) {
		for (SFSKPhyMacSetupAttribute attribute : SFSKPhyMacSetupAttribute.values()) {
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
