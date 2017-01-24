package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Firewall setup methods.
 * 
 * @author alex
 */
public enum FirewallSetupMethods implements DLMSClassMethods {

	ACTIVATE(1, 80),
	DEACTIVATE(2, 88);
	
	/** The method number. */
	private final int methodNumber;
	
	/** The short address. */
	private final int shortAddress;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	methodNumber		The method number.
	 * @param 	shortAddress		The short address.
	 */
	private FirewallSetupMethods(final int methodNumber, final int shortAddress) {
		this.methodNumber = methodNumber;
		this.shortAddress = shortAddress;
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
		return this.shortAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getMethodNumber() {
		return this.methodNumber;
	}
}
