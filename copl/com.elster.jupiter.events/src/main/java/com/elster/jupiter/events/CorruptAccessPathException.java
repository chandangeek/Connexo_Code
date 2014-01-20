package com.elster.jupiter.events;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when an access path cannot be resolved on any of the traveled beans.
 */
public class CorruptAccessPathException extends BaseException {

    public CorruptAccessPathException(Throwable cause, Thesaurus thesaurus) {
        super(ExceptionTypes.CORRUPT_ACCESSPATH, buildMessage(thesaurus), cause);
    }

    private static String buildMessage(Thesaurus thesaurus) {
        return thesaurus.getFormat(MessageSeeds.CORRUPT_ACCESSPATH).format();
    }
}
