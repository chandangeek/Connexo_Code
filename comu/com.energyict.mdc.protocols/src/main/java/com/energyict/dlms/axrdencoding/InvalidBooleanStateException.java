package com.energyict.dlms.axrdencoding;

import java.io.IOException;

/**
 * Exception can be thrown when the state from a Boolean can not be determined
 * <p/>
 * Copyrights EnergyICT
 * Date: 8-nov-2010
 * Time: 16:43:43
 */
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
