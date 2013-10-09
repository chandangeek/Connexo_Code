package com.elster.jupiter.util.json;

import com.elster.jupiter.util.ExceptionTypes;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when deserialization of an Object tree from a Json String fails.
 */
public class JsonDeserializeException extends BaseException {

    public static final String JSON_STRING = "jsonString";
    public static final String TARGET_CLASS = "targetClass";

    public JsonDeserializeException(Throwable cause, String jsonString, Class<?> targetClass) {
        super(ExceptionTypes.JSON_DESERIALIZATION_FAILED, cause);
        set(JSON_STRING, jsonString);
        set(TARGET_CLASS, targetClass);
    }
}
