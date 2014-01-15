package com.elster.jupiter.metering;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

public class IllegalCurrencyCodeException extends BaseException {
	
	private static final long serialVersionUID = 1L;

	public IllegalCurrencyCodeException(int illegalCode) {
        super(ExceptionTypes.ILLEGAL_CURRENCY_CODE, MessageFormat.format("Invalid currency code : ''{0}''", illegalCode));
        set("illegalCode", illegalCode);
    }
}
