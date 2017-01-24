package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

public enum G3NetworkManagementMethods implements DLMSClassMethods {

    // We don't use short addresses, RTU+Server uses LN references.
    PATH_REQUEST(1),
    ROUTE_REQUEST(2),
    PING(3),
    DETACH(4),
    ENABLE(5),
    PROVIDE_PSK(9);
	
	private final int methodId;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	methodId			The ID of the method.
	 */
	private G3NetworkManagementMethods(final int methodId) {
		this.methodId = methodId;
	}

	/**
	 * {@inheritDoc}
	 */
	public DLMSClassId getDlmsClassId() {
		return DLMSClassId.G3_NETWORK_MANAGEMENT;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getShortName() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getMethodNumber() {
		return this.methodId;
	}
}
