/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.ConcentratorSetup;
import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Methods for the {@link ConcentratorSetup} IC.
 *
 * @author alex
 */
public enum ConcentratorSetupMethods implements DLMSClassMethods {

    /** Enumerate the methods. */
    RESET_CONCENTRATOR(0x01, -1),
    RESET_LOGICAL_DEVICE(0x02, -1),
    REMOVE_LOGICAL_DEVICE(0x03, -1),
    FETCH_DEVICE_STATISTICS(0x04, -1),
    TRIGGER_PRELIMINARY_PROTOCOL(0x05, -1);

    /** The ID of the method. */
    private final int methodId;

    /** The short name. */
    private final int shortName;

    /**
     * Create a new instance.
     * // TODO Auto-generated method stub
     return 0;
     * @param 	methodId		The ID of the method.
     */
    private ConcentratorSetupMethods(final int methodId, final int shortName) {
        this.methodId = methodId;
        this.shortName = shortName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.CONCENTRATOR_SETUP;
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
