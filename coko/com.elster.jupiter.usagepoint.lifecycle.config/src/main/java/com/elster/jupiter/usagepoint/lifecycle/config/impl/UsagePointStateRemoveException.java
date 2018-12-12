/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.List;
import java.util.stream.Collectors;

public class UsagePointStateRemoveException extends LocalizedException {
    private UsagePointStateRemoveException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointStateRemoveException stateHasLinkedTransitions(Thesaurus thesaurus, List<UsagePointTransition> linkedTransitions) {
        UsagePointStateRemoveException exception = new UsagePointStateRemoveException(thesaurus, MessageSeeds.CAN_NOT_REMOVE_STATE_HAS_TRANSITIONS, linkedTransitions.stream()
                .map(UsagePointTransition::getName).collect(Collectors.joining(", ")));
        return exception;
    }

    public static UsagePointStateRemoveException stateIsTheLastState(Thesaurus thesaurus) {
        UsagePointStateRemoveException exception = new UsagePointStateRemoveException(thesaurus, MessageSeeds.CAN_NOT_REMOVE_LAST_STATE);
        return exception;
    }

    public static UsagePointStateRemoveException stateIsInitial(Thesaurus thesaurus) {
        UsagePointStateRemoveException exception = new UsagePointStateRemoveException(thesaurus, MessageSeeds.CAN_NOT_REMOVE_INITIAL_STATE);
        return exception;
    }
}
