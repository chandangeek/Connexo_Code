/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Created by bvn on 3/7/16.
 */
public class NoTransitionException extends LocalizedException {

    public NoTransitionException(Thesaurus thesaurus, MessageSeed messageSeed, String fromState, String toState) {
        super(thesaurus, messageSeed, fromState, toState);
    }
}
