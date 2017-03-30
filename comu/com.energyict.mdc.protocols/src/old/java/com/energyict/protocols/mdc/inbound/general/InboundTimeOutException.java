/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.inbound.general;

import java.io.IOException;

public class InboundTimeOutException extends IOException {

    public InboundTimeOutException() {
        super();    
    }

    public InboundTimeOutException(String message) {
        super(message);    
    }

    public InboundTimeOutException(String message, Throwable cause) {
        super(message, cause);    
    }

    public InboundTimeOutException(Throwable cause) {
        super(cause);    
    }
}
