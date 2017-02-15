/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.CM32;

import java.io.IOException;


public abstract class AbstractCommand {
	
	private CM32 cm32Protocol;
	
	protected Command preparebuild() {
		throw new RuntimeException("No implementation provided");
	}
	
	protected void parse(Response response) throws IOException {
		throw new RuntimeException("No implementation provided");
	}
    
    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(CM32 cm32Protocol) {
        this.setCM32Protocol(cm32Protocol);
    }

    protected CM32 getInstrometProtocol() {
        return cm32Protocol;
    }

    private void setCM32Protocol(CM32 cm32Protocol) {
        this.cm32Protocol = cm32Protocol;
    }
    
    
    public Response invoke() throws IOException {
    	Command command = preparebuild();
        Response response = 
        	cm32Protocol.getCM32Connection().sendCommand(command);
        return response;
    }


}

