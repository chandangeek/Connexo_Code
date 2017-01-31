/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

public class IllegalTableMappingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalTableMappingException(String message) {
        super(message);
    }
}
