package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 15:00
 */
public class OverFlowValueHasIncorrectFractionDigitsException extends LocalizedException{

    public OverFlowValueHasIncorrectFractionDigitsException(BigDecimal overflow, int overflowFractionDigits, int providedFractionDigits, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, overflow, overflowFractionDigits, providedFractionDigits);
        set("overflow", overflow);
        set("overflowFractionDigits", overflowFractionDigits);
        set("providedFractionDigits", providedFractionDigits);
    }

}