/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.cosem.methods.LifeCycleManagementMethods;

import java.io.IOException;

public class LifeCycleManagement extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public LifeCycleManagement(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.LIFE_CYCLE_MANAGEMENT.getClassId();
    }

    /**
     * Reboots the device
     *
     * @throws java.io.IOException
     */
    public final void rebootDevice() throws IOException {
        methodInvoke(LifeCycleManagementMethods.REBOOT_DEVICE);
    }

    /**
     * Restarts the application
     *
     * @throws java.io.IOException
     */
    public final void restartApplication() throws IOException {
        methodInvoke(LifeCycleManagementMethods.RESTART_APPLICATION);
    }

    /**
     * Restarts the application
     *
     * @throws java.io.IOException
     */
    public final void hardReset() throws IOException {
        methodInvoke(LifeCycleManagementMethods.HARD_RESET);
    }

}
