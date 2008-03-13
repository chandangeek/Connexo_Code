package com.energyict.protocolimpl.instromet.connection;

import com.energyict.protocolimpl.instromet.core.InstrometProtocol;

public class WriteCommand extends AbstractCommand {

	private int startAddress;
	private byte[] data;
	
	public void setStartAddress(int startAddress) {
		this.startAddress = startAddress;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public WriteCommand(InstrometProtocol instrometProtocol) {
		super(instrometProtocol);
	}
	
	protected Command preparebuild() {
		Command command = new Command('W');
		command.setStartAddress(startAddress); // only write for table switch
		command.setLength(data.length); // only write for table switch
		command.setData(data);
		return command;
	}

}
