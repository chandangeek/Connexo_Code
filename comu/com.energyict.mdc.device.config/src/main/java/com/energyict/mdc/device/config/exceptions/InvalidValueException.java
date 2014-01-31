package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to set the value of a property to a value that is not allowed. Detailed
 * explanation what values are allowed should be provided by the Message.
 *
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 12:22
 */
public class InvalidValueException extends LocalizedException {

    private InvalidValueException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static InvalidValueException registerSpecNumberOfDigitsShouldBeLargerThanOne(Thesaurus thesaurus){
        return new InvalidValueException(thesaurus, MessageSeeds.REGISTER_SPEC_NUMBER_OF_DIGITS_LARGER_THAN_ONE);
    }
}
