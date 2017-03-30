/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

/**
 * Models the different types of communication errors that can occur.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-02 (13:52)
 */
public enum CommunicationErrorType {

    /**
     * Produced when a connection to an outbound device could not be established.
     * The failure can obviously have multiple causes, see examples below.
     * Examples:
     * <ul>
     * <li>network problems</li>
     * <li>connection information incomplete</li>
     * <li>connection information incorrect</li>
     * <li>problem at the device itself</li>
     * </ul>
     */
    CONNECTION_SETUP_FAILURE,

    /**
     * Produced when a connection that was established before
     * (either inbound or outbound) is dropped mid air,
     * i.e. the connection no longer works for some reason.
     */
    CONNECTION_FAILURE,

    /**
     * Produced when a communication failure with a device occurred.
     * The connection was successfully established.
     */
    COMMUNICATION_FAILURE;

}