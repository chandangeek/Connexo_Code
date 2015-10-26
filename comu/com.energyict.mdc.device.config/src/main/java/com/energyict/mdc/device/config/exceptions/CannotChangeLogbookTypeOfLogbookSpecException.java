package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.masterdata.LogBookType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link LogBookType}
 * of an existing {@link com.energyict.mdc.device.config.LogBookSpec}
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 13:42
 */
public class CannotChangeLogbookTypeOfLogbookSpecException extends LocalizedException {

    public CannotChangeLogbookTypeOfLogbookSpecException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

}
