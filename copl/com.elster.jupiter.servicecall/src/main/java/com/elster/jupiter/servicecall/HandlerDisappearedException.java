/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Created by bvn on 2/25/16.
 */
public class HandlerDisappearedException extends LocalizedException {
    public HandlerDisappearedException(Thesaurus thesaurus, MessageSeed messageSeed, String name) {
        super(thesaurus, messageSeed, name);
    }
}
