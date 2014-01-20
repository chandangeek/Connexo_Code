package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

public class IllegalCurrencyCodeException extends BaseException {
	
	private static final long serialVersionUID = 1L;

	public IllegalCurrencyCodeException(int illegalCode, Thesaurus thesaurus) {
        super(ExceptionTypes.ILLEGAL_CURRENCY_CODE, buildMessage(illegalCode, thesaurus));
        set("illegalCode", illegalCode);
    }

    private static String buildMessage(int illegalCode, Thesaurus thesaurus) {
        return thesaurus.getFormat(MessageSeeds.ILLEGAL_CURRENCY_CODE).format(illegalCode);
    }
}
