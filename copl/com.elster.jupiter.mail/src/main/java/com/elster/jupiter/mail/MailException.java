/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class MailException extends LocalizedException {

    // TODO
    public MailException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(thesaurus, messageSeed, cause, args);
    }
}
