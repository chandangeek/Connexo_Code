/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import com.elster.jupiter.util.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when deserialization of an Object tree from a Json String fails.
 */
public class JsonDeserializeException extends BaseException {

	private static final long serialVersionUID = 1L;
	public static final String JSON_STRING = "jsonString";
    public static final String TARGET_CLASS = "targetClass";

    public JsonDeserializeException(Throwable cause, String jsonString, Class<?> targetClass) {
        super(MessageSeeds.JSON_DESERIALIZATION_FAILED, cause, jsonString, targetClass.getName());
        set(JSON_STRING, jsonString);
        set(TARGET_CLASS, targetClass);
    }
}
