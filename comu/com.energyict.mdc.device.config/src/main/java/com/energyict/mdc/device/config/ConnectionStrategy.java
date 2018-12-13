/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

/**
 * Models the algorithm that calculates the next time
 * a connection will be established by a ConnectionTask.
 * This can as simple as a fixed calculation based on
 * the frequency attributes of the ConnectionTask.
 * That in effect will minimize the number of connections
 * that will be established in the end.
 * This can also be as soon as possible, taking the ComTaskExecutions
 * of devices that use the ConnectionTask.
 * With that strategy, a connection will be established as soon
 * as a ScheduledComTask has expired and needs to be executed.
 * <p/>
 * The default is "as soon as possible".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (12:05)
 */
public enum ConnectionStrategy {

    AS_SOON_AS_POSSIBLE,
    MINIMIZE_CONNECTIONS

}