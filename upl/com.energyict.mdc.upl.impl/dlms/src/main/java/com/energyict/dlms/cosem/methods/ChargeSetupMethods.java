package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * CHARGE setup IC, methods
 * class id = 100, version = 0, logical name = 0-0:25.10.0.255 (0000190A00FF)
 * Instances of the "CHARGE setup" IC allow setting up time synchronisation using the CHARGE protocol as specified in RFC 5905.
 * One or several instances may be configured to support multiple time servers.
 */
public enum ChargeSetupMethods implements DLMSClassMethods {

    UPDATE_UNIT_CHARGE(1, 0x01),

    ACTIVATE_PASSIVE_UNIT_CHARGE(2, 0x02);


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
    private ChargeSetupMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.CHARGE_SETUP;
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
