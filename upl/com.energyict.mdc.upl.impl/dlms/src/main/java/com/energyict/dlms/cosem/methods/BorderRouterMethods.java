package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Border Router Setup IC actions
 * class id = 20020, version = 0, logical name = 0-168:96.176.0.255 (00A860B000FF)
 * The border router setup IC allows for configuring the G3-PLC layer 3 router functionality.
 */
public enum BorderRouterMethods implements DLMSClassMethods {
    /**
     * Adds a new routing entry to the routing setup
     */
    ADD_ROUTING_ENTRY(1, 0x00),

    /**
     * Removed the routing entry associated with the given ID
     */
    REMOVE_ROUTING_ENTRY(2, 0x10),

    /**
     * Drops all routing entries and restarts the border router
     */
    RESET_ROUTER(3, 0x18);


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
    private BorderRouterMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.BORDER_ROUTER;
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
