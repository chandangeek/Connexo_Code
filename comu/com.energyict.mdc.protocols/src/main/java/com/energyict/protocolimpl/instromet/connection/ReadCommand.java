package com.energyict.protocolimpl.instromet.connection;

import com.energyict.protocolimpl.instromet.core.InstrometProtocol;

public class ReadCommand extends AbstractCommand {

	private int startAddress;
	private int length;

	public ReadCommand(InstrometProtocol instrometProtocol) {
		super(instrometProtocol);
	}

	public void setStartAddress(int startAddress) {
		this.startAddress = startAddress;
	}

	public void setLength(int length) {
		this.length = length;
	}

	protected Command preparebuild() {
		Command command = new Command('R');
		command.setStartAddress(startAddress);
		command.setLength(length);
		return command;
	}

}
