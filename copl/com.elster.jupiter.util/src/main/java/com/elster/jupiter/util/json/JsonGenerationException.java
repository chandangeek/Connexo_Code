package com.elster.jupiter.util.json;

import com.elster.jupiter.util.ExceptionTypes;
import com.elster.jupiter.util.exception.BaseException;

public class JsonGenerationException extends BaseException {

    public static final String OBJECT_TO_SERIALIZE = "objectToSerialize";

    public JsonGenerationException(Throwable cause, Object toSerialize) {
        super(ExceptionTypes.JSON_GENERATION_FAILED, cause);
        set(OBJECT_TO_SERIALIZE, toSerialize);
    }

}
