/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json;

import com.elster.jupiter.util.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when the generation of a json String fails.
 */
public class JsonGenerationException extends BaseException {

	private static final long serialVersionUID = 1L;
	public static final String OBJECT_TO_SERIALIZE = "objectToSerialize";

    public JsonGenerationException(Throwable cause, Object toSerialize) {
        super(MessageSeeds.JSON_GENERATION_FAILED, cause, toSerialize.toString());
        set(OBJECT_TO_SERIALIZE, toSerialize);
    }

}
