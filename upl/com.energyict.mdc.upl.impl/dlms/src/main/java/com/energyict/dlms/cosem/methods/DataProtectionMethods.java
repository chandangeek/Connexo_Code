package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Firewall setup methods.
 * 
 * @author alex
 */
public enum DataProtectionMethods implements DLMSClassMethods {

	GET_PROTECTED_ATTRIBUTES(1, 0x30),
    SET_PROTECTED_ATTRIBUTES(2, 0x38),
    INVOKE_PROTECTED_METHOD(3, 0x40);

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
	private DataProtectionMethods(final int methodNumber, final int shortAddress) {
		this.methodNumber = methodNumber;
		this.shortAddress = shortAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	public final DLMSClassId getDlmsClassId() {
		return DLMSClassId.DATA_PROTECTION;
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
