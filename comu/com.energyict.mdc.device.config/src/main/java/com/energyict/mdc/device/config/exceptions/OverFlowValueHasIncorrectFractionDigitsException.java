/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.math.BigDecimal;

public class OverFlowValueHasIncorrectFractionDigitsException extends LocalizedException{

    public OverFlowValueHasIncorrectFractionDigitsException(BigDecimal overflow, int overflowFractionDigits, int providedFractionDigits, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, overflow, overflowFractionDigits, providedFractionDigits);
        set("overflow", overflow);
        set("overflowFractionDigits", overflowFractionDigits);
        set("providedFractionDigits", providedFractionDigits);
    }

}