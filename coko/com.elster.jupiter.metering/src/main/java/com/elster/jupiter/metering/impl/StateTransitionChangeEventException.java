/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class StateTransitionChangeEventException extends LocalizedException {

    protected StateTransitionChangeEventException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static class UnableToChangeDeviceStateDueToTimeNotAfterLastStateChangeException extends StateTransitionChangeEventException{
        public UnableToChangeDeviceStateDueToTimeNotAfterLastStateChangeException(Thesaurus thesaurus, String stateName, String endDeviceName,  String effectiveTimestamp, String lastStateChange) {
            super(thesaurus, MessageSeeds.UNABLE_TO_CHANGE_DEVICE_STATE_DUE_TO_TIME_NOT_AFTER_LAST_STATE_CHANGE, stateName, endDeviceName, effectiveTimestamp, lastStateChange);
        }
    }
}