package com.energyict.protocolimpl.cm10;

import java.io.IOException;


public abstract class AbstractCommand {
	
	private CM10 cm10Protocol;
	private byte[] arguments;
	
	protected Command preparebuild() {
		throw new RuntimeException("No implementation provided");
	}
	
	protected void parse(Response response) throws IOException {
		throw new RuntimeException("No implementation provided");
	}
    
    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(CM10 cm10Protocol) {
        this.setCM10Protocol(cm10Protocol);
    }

    protected CM10 getCM10Protocol() {
        return cm10Protocol;
    }

    private void setCM10Protocol(CM10 cm10Protocol) {
        this.cm10Protocol = cm10Protocol;
    }
    
    
    public Response invoke() throws IOException {
    	Command command = preparebuild();
        Response response = 
        	cm10Protocol.getCM10Connection().sendCommand(command);
        return response;
    }

	public byte[] getArguments() {
		return arguments;
	}

	public void setArguments(byte[] arguments) {
		this.arguments = arguments;
	}


}

