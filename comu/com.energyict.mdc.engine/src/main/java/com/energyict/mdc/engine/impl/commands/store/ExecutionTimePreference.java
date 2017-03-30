/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

/**
 * Models the timing preference of a {@link DeviceCommand}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (11:50)
 */
public enum ExecutionTimePreference {

    /**
     * Indicates that the {@link DeviceCommand} wants to be executed first.
     * Note that there can be only one DeviceCommand with this preference.
     */
    FIRST,

    /**
     * Indicates that the {@link DeviceCommand} wants to be executed last.
     * Note that there can be only one DeviceCommand with this preference.
     */
    LAST,

    /**
     * Indicates that the {@link DeviceCommand} does not really care
     * when it is executed.
     */
    WHENEVER;

}