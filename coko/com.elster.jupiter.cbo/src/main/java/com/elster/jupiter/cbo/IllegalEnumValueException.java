package com.elster.jupiter.cbo;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

public class IllegalEnumValueException extends BaseException {

    public IllegalEnumValueException(Class<?> enumClass, int value) {
        super(ExceptionTypes.ILLEGAL_ENUM_VALUE, MessageFormat.format("{0} is not a value matching an instance of enum {1}", value, enumClass.getName()));
        set("enumClass", enumClass);
        set("value", value);
    }
}
