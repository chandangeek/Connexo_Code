package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public final class UnsupportedCommandException extends LocalizedException {

    public UnsupportedCommandException(Thesaurus thesaurus, String command, String name) {
        super(thesaurus, MessageSeeds.UNSUPPORTED_COMMAND, command, name);
    }
}
