package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Firewall setup attributes.
 *
 * @author alex
 */
public enum FirewallSetupAttributes implements DLMSClassAttributes {

	LOGICAL_NAME(1, 0),
	ENABLED_BY_DEFAULT(2, 8),
	IS_ACTIVE(3, 16),
	WAN_SETUP(4, 24),
	LAN_SETUP(5, 32),
	GPRS_SETUP(6, 40);

	/** Attribute ID. */
	private final int attributeId;

	/** The short name of the attribute (offset from base address). */
	private final int shortName;

	private FirewallSetupAttributes(final int attributeId, final int shortName) {
		this.attributeId = attributeId;
		this.shortName = shortName;
	}

	/**
	 * {@inheritDoc}
	 */
	public final DLMSClassId getDlmsClassId() {
		return DLMSClassId.FIREWALL_SETUP;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getShortName() {
		return this.shortName;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getAttributeNumber() {
		return this.attributeId;
	}

	/**
	 * {@inheritDoc}
	 */
	public final DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
		return new DLMSAttribute(obisCode, this);
	}
}
