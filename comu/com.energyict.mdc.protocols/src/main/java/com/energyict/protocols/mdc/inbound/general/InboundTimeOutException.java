package com.energyict.protocols.mdc.inbound.general;

import java.io.IOException;

/**
 * Serves as a indication that a TimeOut occurred during inbound Communication
 * 
 * Copyrights EnergyICT
 * Date: 14/05/13
 * Time: 10:26
 */
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
