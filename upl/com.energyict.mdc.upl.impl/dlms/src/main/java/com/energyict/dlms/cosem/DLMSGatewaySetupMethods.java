package com.energyict.dlms.cosem;

import com.energyict.dlms.cosem.methods.DLMSClassMethods;

/**
 * Methods for the {@link com.energyict.dlms.cosem.DLMSGatewaySetup} IC.
 */
public enum DLMSGatewaySetupMethods implements DLMSClassMethods {

	/** Drops all virtual logical devices from the gateway and kicks the associated meters from the network. */
	RESET_GATEWAY(0x01, -1),

    /** Removes the logical device with given ID from the gateway and kicks the associated meter from the network. */
	REMOVE_LOGICAL_DEVICE(0x02, -1);



	/** The ID of the method. */
	private final int methodId;

	/** The short name. */
	private final int shortName;

	/**
	 * Create a new instance.
		return 0;
	 * @param 	methodId		The ID of the method.
	 */
	private DLMSGatewaySetupMethods(final int methodId, final int shortName) {
		this.methodId = methodId;
		this.shortName = shortName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DLMSClassId getDlmsClassId() {
		return DLMSClassId.DLMS_GATEWAY_SETUP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getShortName() {
		return this.shortName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getMethodNumber() {
		return this.methodId;
	}
}
