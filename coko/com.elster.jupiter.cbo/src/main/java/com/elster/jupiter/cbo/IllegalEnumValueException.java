/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.util.exception.BaseException;

public class IllegalEnumValueException extends BaseException {
	
	private static final long serialVersionUID = 1L;

	public IllegalEnumValueException(Class<?> enumClass, int value) {
        super(MessageSeeds.ILLEGAL_ENUM_VALUE, value, enumClass.getName());
        set("enumClass", enumClass);
        set("value", value);
    }
}
