/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.math.BigDecimal;

public class OverFlowValueCanNotExceedNumberOfDigitsException extends LocalizedException {

    public OverFlowValueCanNotExceedNumberOfDigitsException(BigDecimal overflow, double max, int numberOfDigits, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, overflow, max, numberOfDigits);
        set("overflow", overflow);
        set("max", max);
        set("numberOfDigits", numberOfDigits);
    }

}