/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

import java.io.IOException;

public class InvalidCommandException extends IOException {
	
	public InvalidCommandException(String description) {
		super(description);
	}

}
