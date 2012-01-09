package com.elster.protocolimpl.dlms.messaging;

import com.energyict.cbo.BusinessException;

/**
 * User: heuckeg
 * Date: 10.10.11
 * Time: 08:57
 */
@SuppressWarnings({"unused"})
public class FirmwareUpdateTimeoutException extends BusinessException {

    public FirmwareUpdateTimeoutException() {
        super();
    }

    public FirmwareUpdateTimeoutException(String msg) {
        super(msg);
    }

    public FirmwareUpdateTimeoutException(Throwable ex) {
        super(ex);
    }

}
