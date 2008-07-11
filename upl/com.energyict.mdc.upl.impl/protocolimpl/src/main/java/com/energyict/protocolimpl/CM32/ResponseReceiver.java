package com.energyict.protocolimpl.CM32;

import java.io.IOException;


public class ResponseReceiver {
	
	private static final int DEBUG=0;
	private static final long TIMEOUT=60000;
    
    private CM32Connection cm32Connection;
    private int timeout;
    
    public ResponseReceiver(CM32Connection cm32Connection, int timeout) {
    	this.cm32Connection = cm32Connection;
    	this.timeout =timeout;
    }

	protected Response receiveResponse(Command command, long timeoutEnq) throws IOException {
		return null;
	}


}
