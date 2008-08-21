package com.energyict.protocolimpl.CM32;
  
public class CommandFactory {
	
	static final int STATUS = 1;
	
	private CM32 cm32Protocol;
	
	public CommandFactory(CM32 cm32Protocol) {
		this.cm32Protocol = cm32Protocol;
	}
	
	public ReadCommand getReadStatusCommand() {
		return new ReadCommand(cm32Protocol, STATUS);
	}
	
	
}

