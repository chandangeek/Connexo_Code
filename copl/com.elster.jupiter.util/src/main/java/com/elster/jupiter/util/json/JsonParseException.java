package com.elster.jupiter.util.json;

import com.elster.jupiter.util.ExceptionTypes;
import com.elster.jupiter.util.exception.BaseException;

public class JsonParseException extends BaseException {

    public static final String JSON_STRING = "jsonString";
    public static final String TARGET_CLASS = "targetClass";

    public JsonParseException(Throwable cause, String jsonString, Class<?> targetClass) {
        super(ExceptionTypes.JSON_PARSING_FAILED, cause);
        set(JSON_STRING, jsonString);
        set(TARGET_CLASS, targetClass);
    }
}
