package com.energyict.protocolimpl.edmi.common.command;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CommandResponseException extends IOException {

    private int responseCANCode;

    public CommandResponseException(String str) {
        super(str);
    }

    public CommandResponseException(int responseCANCode, String str) {
        super(str);
        this.responseCANCode = responseCANCode;
    }

    public int getResponseCANCode() {
        return responseCANCode;
    }
}