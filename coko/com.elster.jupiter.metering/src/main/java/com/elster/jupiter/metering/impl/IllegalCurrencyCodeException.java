/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class IllegalCurrencyCodeException extends LocalizedException {

	private static final long serialVersionUID = 1L;

	public IllegalCurrencyCodeException(Thesaurus thesaurus, int illegalCode) {
        super(thesaurus, PrivateMessageSeeds.ILLEGAL_CURRENCY_CODE, illegalCode);
        set("illegalCode", illegalCode);
    }
}