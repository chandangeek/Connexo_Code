/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import com.elster.jupiter.util.exception.BaseException;
import com.elster.jupiter.util.exception.MessageSeed;

public abstract class LocalizedException extends BaseException {

	private static final long serialVersionUID = 1L;
	private final Thesaurus thesaurus;
    private final Object[] messageArgs;

    protected LocalizedException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(messageSeed);
        this.thesaurus = thesaurus;
        messageArgs = new Object[] {};
    }

    protected LocalizedException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(messageSeed, args);
        this.thesaurus = thesaurus;
        this.messageArgs = args;
    }

    protected LocalizedException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause) {
        super(messageSeed, cause);
        this.thesaurus = thesaurus;
        messageArgs = new Object[] {};
    }

    protected LocalizedException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(messageSeed, cause, args);
        this.thesaurus = thesaurus;
        this.messageArgs = args;
    }

    protected final Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected final NlsMessageFormat getFormat() {
        return getThesaurus().getFormat(getMessageSeed());
    }

    @Override
    public String getLocalizedMessage() {
        return getFormat().format(messageArgs);
    }

}
