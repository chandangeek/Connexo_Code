/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when a query that should return at most one instance, returns more than one.
 */
public class NotUniqueException extends BaseException {
	private static final long serialVersionUID = 1L;
	
	public NotUniqueException(String identification) {
		super(MessageSeeds.NOT_UNIQUE, identification);
	}

}
