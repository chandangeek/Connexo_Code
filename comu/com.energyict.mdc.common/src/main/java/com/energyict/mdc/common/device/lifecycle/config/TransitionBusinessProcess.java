/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ConsumerType;

/**
 * Wraps an existing external business process that was designed
 * explicitly as an action that is available on devices that
 * are in a specific {@link State}.
 * <p>
 * The unique identifier of the device will be passed to the
 * external business process as well as the current State.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (09:39)
 */
@ConsumerType
public interface TransitionBusinessProcess extends HasId {

    /**
     * The name of the parameter of the external business process
     * that is used to pass the unique identifier of the device
     * that is the target of the execution.
     * Note that the parameter is passed as a Long
     * so make sure that your external business process
     * parameter is of type Long.
     */
    public static final String DEVICE_ID_BPM_PARAMETER_NAME = "deviceId";

    /**
     * The name of the parameter of the external business process
     * that is used to pass the unique identifier of the
     * current {@link State} of the Device.
     * Note that the parameter is passed as a Long
     * so make sure that your external business process
     * parameter is of type Long.
     */
    public static final String STATE_ID_BPM_PARAMETER_NAME = "stateId";

    public String getName();

    public String getDeploymentId();

    public String getProcessId();

    /**
     * Executes the related external process for the
     * device that is uniquely identified by the deviceId
     * and that is in the specified {@link State}.
     *
     * @param deviceId The unique identifier of the device
     * @param currentState The current State of the device
     */
    public void executeOn(long deviceId, State currentState);

}