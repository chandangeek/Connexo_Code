package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class InvalidNodeException extends LocalizedException {

    public InvalidNodeException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    public InvalidNodeException(Thesaurus thesaurus, MessageSeed messageSeed, int value) {
        super(thesaurus, messageSeed, value);
    }

    public InvalidNodeException(Thesaurus thesaurus, MessageSeed messageSeed, String value1, String value2) {
        super(thesaurus, messageSeed, value1, value2);
    }

    public InvalidNodeException(Thesaurus thesaurus, MessageSeed messageSeed, String value1, String value2, String value3) {
        super(thesaurus, messageSeed, value1, value2, value3);
    }

}

