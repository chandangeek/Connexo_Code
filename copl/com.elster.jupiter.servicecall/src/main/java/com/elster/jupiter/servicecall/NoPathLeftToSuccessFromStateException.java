/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class NoPathLeftToSuccessFromStateException extends LocalizedException {

    public NoPathLeftToSuccessFromStateException(Thesaurus thesaurus, MessageSeed messageSeed, DefaultState state) {
        super(thesaurus, messageSeed, thesaurus.getString(state.getKey(), state.name()));
    }
}
