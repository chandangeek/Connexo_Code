package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 15:00
 */
public class OverFlowValueHasIncorrectFractionDigitsException extends LocalizedException{

    public OverFlowValueHasIncorrectFractionDigitsException(Thesaurus thesaurus, BigDecimal overflow, int overflowFractionDigits, int providedFractionDigits) {
        super(thesaurus, MessageSeeds.REGISTER_SPEC_OVERFLOW_INCORRECT_FRACTION_DIGITS, overflow, overflowFractionDigits, providedFractionDigits);
        set("overflow", overflow);
        set("overflowFractionDigits", overflowFractionDigits);
        set("providedFractionDigits", providedFractionDigits);
    }
}
