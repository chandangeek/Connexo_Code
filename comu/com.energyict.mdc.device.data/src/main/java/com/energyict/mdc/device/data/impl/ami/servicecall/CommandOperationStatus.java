/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall;

/**
 * Enumeration of all possible steps a 'command operation' can go through
 *
 * @author sva
 * @since 14/06/2016 - 10:49
 */
public enum CommandOperationStatus {

    SEND_OUT_DEVICE_MESSAGES,
    READ_STATUS_INFORMATION,
    UNKNOWN;

    public static CommandOperationStatus fromName(String name) {
        for (CommandOperationStatus commandOperationStatus : values()) {
            if (commandOperationStatus.name().equalsIgnoreCase(name)) {
                return commandOperationStatus;
            }
        }
        return UNKNOWN;
    }
}