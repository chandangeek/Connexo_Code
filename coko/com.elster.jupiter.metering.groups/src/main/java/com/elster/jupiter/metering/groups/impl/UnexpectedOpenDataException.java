package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.management.openmbean.OpenDataException;

/**
 * Models the exceptional situation that an OpenDataException occurred.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-09 (16:17)
 */
class UnexpectedOpenDataException extends LocalizedException {
    UnexpectedOpenDataException(Thesaurus thesaurus, MessageSeed messageSeed, OpenDataException cause) {
        super(thesaurus, messageSeed, cause);
    }
}