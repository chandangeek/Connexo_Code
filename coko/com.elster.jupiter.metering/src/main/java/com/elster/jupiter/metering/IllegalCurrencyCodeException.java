package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class IllegalCurrencyCodeException extends LocalizedException {
	
	private static final long serialVersionUID = 1L;

	public IllegalCurrencyCodeException(Thesaurus thesaurus, int illegalCode) {
        super(thesaurus, MessageSeeds.ILLEGAL_CURRENCY_CODE, thesaurus, illegalCode);
        set("illegalCode", illegalCode);
    }
}
