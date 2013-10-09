package com.elster.jupiter.util.json;

import com.elster.jupiter.util.ExceptionTypes;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when serialization of an Object tree to a Json String fails.
 */
public class JsonSerializeException extends BaseException {

    public static final String OBJECT = "object";

    public JsonSerializeException(Throwable cause, Object object) {
        super(ExceptionTypes.JSON_SERIALIZATION_FAILED, cause);
        set(OBJECT, object);
    }
}
