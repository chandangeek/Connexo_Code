package com.elster.protocolimpl.dlms.messaging;

import java.io.IOException;

/**
 * User: heuckeg
 * Date: 10.10.11
 * Time: 09:03
 */
public class FWUpdateTimeoutException extends IOException {

    public FWUpdateTimeoutException(String msg) {
        super(msg);
    }
}
