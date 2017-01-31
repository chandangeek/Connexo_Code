/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when an instance that should exist, is not found to.
 */
public class DoesNotExistException extends BaseException {
	private static final long serialVersionUID = 1L;

	public DoesNotExistException(String identification) {
		super(MessageSeeds.DOES_NOT_EXIST, identification);
        set("identification", identification);
	}
}
