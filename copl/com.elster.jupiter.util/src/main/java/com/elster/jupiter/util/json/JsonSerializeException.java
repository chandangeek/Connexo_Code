/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import com.elster.jupiter.util.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when serialization of an Object tree to a Json String fails.
 */
public class JsonSerializeException extends BaseException {

	private static final long serialVersionUID = 1L;
	public static final String OBJECT = "object";

    public JsonSerializeException(Throwable cause, Object object) {
        super(MessageSeeds.JSON_SERIALIZATION_FAILED, cause, object.toString());
        set(OBJECT, object);
    }
}
