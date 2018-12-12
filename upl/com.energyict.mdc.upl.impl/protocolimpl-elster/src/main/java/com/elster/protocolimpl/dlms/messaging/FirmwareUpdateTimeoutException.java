package com.elster.protocolimpl.dlms.messaging;

import java.io.IOException;

/**
 * User: heuckeg
 * Date: 10.10.11
 * Time: 08:57
 */
@SuppressWarnings({"unused"})
public class FirmwareUpdateTimeoutException extends IOException {

    public FirmwareUpdateTimeoutException() {
        super();
    }

    public FirmwareUpdateTimeoutException(String message) {
        super(message);
    }

    public FirmwareUpdateTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public FirmwareUpdateTimeoutException(Throwable cause) {
        super(cause);
    }
}
