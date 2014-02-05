package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link com.energyict.mdc.device.config.LogBookType}
 * of an existing {@link com.energyict.mdc.device.config.LogBookSpec}
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 13:42
 */
public class CannotChangeLogbookTypeOfLogbookSpecException extends LocalizedException {

    public CannotChangeLogbookTypeOfLogbookSpecException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.LOGBOOK_SPEC_CANNOT_CHANGE_LOGBOOK_TYPE);
    }
}
