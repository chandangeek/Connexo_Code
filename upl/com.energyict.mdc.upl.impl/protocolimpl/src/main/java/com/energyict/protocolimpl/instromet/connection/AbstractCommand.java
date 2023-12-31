package com.energyict.protocolimpl.instromet.connection;

import java.io.IOException;

import com.energyict.protocolimpl.instromet.core.InstrometProtocol;


public abstract class AbstractCommand {
	
	private InstrometProtocol instrometProtocol;
	
	protected Command preparebuild() {
		throw new RuntimeException("No implementation provided");
	}
	
	protected void parse(Response response) throws IOException {
		throw new RuntimeException("No implementation provided");
	}
    
    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(InstrometProtocol instrometProtocol) {
        this.setInstrometProtocol(instrometProtocol);
    }

    protected InstrometProtocol getInstrometProtocol() {
        return instrometProtocol;
    }

    private void setInstrometProtocol(InstrometProtocol instrometProtocol) {
        this.instrometProtocol = instrometProtocol;
    }
    
    
    public Response invoke() throws IOException {
    	Command command = preparebuild();
        Response response = 
        	instrometProtocol.getInstrometConnection().sendCommand(command);
        return response;
    }


}
