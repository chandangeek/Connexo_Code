package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying a {@link com.energyict.mdc.device.config.LogBookType}
 * where it is required
 *
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 13:26
 */
public class LogbookTypeIsRequiredException extends LocalizedException {

    private LogbookTypeIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new LogbookTypeIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.LogBookSpec} without a
     * {@link com.energyict.mdc.device.config.LogBookType}
     * @param thesaurus The Thesaurus
     * @return the newly create LogbookTypeIsRequiredException
     */
    public static LogbookTypeIsRequiredException logBookSpecRequiresLoadProfileType(Thesaurus thesaurus){
        return new LogbookTypeIsRequiredException(thesaurus, MessageSeeds.LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED);
    }
}
