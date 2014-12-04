package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Methods used in the NetworkManagement class id
 */
public enum GatewaySetupMethods implements DLMSClassMethods {


    CLEAR_WHITELIST(3, 30),
    ACTIVATE_WHITELIST(4, 38),
    DEACTIVATE_WHITELIST(5, 40),
    ACTIVATE_OPERATING_WINDOW(6, 48),
    DEACTIVATE_OPERATING_WINDOW(7, 50);

    private final int methodNumber;

    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    GatewaySetupMethods(final int methodNumber, final int shortName) {
        this.methodNumber = methodNumber;
        this.shortName = shortName;
    }

    /**
     * Getter for the method number
     *
     * @return the method number as int
     */
    public int getMethodNumber() {
        return this.methodNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.GATEWAY_SETUP;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }
}
