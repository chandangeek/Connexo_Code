/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.rest.impl;

import com.energyict.mdc.device.command.CommandRulePendingUpdate;

public enum PendingChangesType {
    UPDATE,
    REMOVAL,
    ACTIVATION,
    DEACTIVATION;

    public static PendingChangesType getCorrectType(CommandRulePendingUpdate pendingUpdate) {
        if(pendingUpdate.isRemoval()) {
            return REMOVAL;
        } else if (pendingUpdate.isActivation()) {
            return ACTIVATION;
        } else if (pendingUpdate.isDeactivation()) {
            return DEACTIVATION;
        } else {
            return UPDATE;
        }
    }
}
