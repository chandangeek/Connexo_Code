package com.elster.jupiter.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Thrown when an access path cannot be resolved on any of the traveled beans.
 */
public class CorruptAccessPathException extends LocalizedException {

    public CorruptAccessPathException(Thesaurus thesaurus, Throwable cause) {
        super(thesaurus, MessageSeeds.CORRUPT_ACCESSPATH, cause);
    }
}
