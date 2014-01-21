package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

public class IllegalCurrencyCodeException extends BaseException {
	
	private static final long serialVersionUID = 1L;

	public IllegalCurrencyCodeException(Thesaurus thesaurus, int illegalCode) {
        super(ExceptionTypes.ILLEGAL_CURRENCY_CODE, buildMessage(thesaurus, illegalCode));
        set("illegalCode", illegalCode);
    }

    private static String buildMessage(Thesaurus thesaurus, int illegalCode) {
        return thesaurus.getFormat(MessageSeeds.ILLEGAL_CURRENCY_CODE).format(illegalCode);
    }
}
