/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class CannotRemoveStateException extends LocalizedException {

    public CannotRemoveStateException(Thesaurus thesaurus, MessageSeed messageSeed, DefaultState state) {
        super(thesaurus, messageSeed, thesaurus.getString(state.getKey(), state.name()));
    }
}
