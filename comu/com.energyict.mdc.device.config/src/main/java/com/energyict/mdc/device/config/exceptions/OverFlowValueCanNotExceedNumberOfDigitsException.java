package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.math.BigDecimal;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link com.energyict.mdc.device.config.RegisterSpec}
 * without an OverFlow value which is larger then the <i>power of ten</i>
 * of the provided NumberOfDigits
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 14:25
 */
public class OverFlowValueCanNotExceedNumberOfDigitsException extends LocalizedException {

    public OverFlowValueCanNotExceedNumberOfDigitsException(BigDecimal overflow, double max, int numberOfDigits, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, overflow, max, numberOfDigits);
        set("overflow", overflow);
        set("max", max);
        set("numberOfDigits", numberOfDigits);
    }

}