/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding;

import java.io.IOException;

public class InvalidBooleanStateException extends IOException {

    /**
     * Single constructor for this exception. User must know what caused the exception
     *
     * @param message the message to clarify the exception
     */
    public InvalidBooleanStateException(String message) {
        super(message);
    }

}
