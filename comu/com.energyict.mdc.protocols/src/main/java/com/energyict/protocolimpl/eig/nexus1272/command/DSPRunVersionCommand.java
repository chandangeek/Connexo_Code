package com.energyict.protocolimpl.eig.nexus1272.command;

public class DSPRunVersionCommand extends AbstractReadCommand {

	public DSPRunVersionCommand(int transID) {
		super(transID);
		startAddress = new byte[] {(byte) 0x00,(byte) 0x4E};
		numRegisters = new byte[] {0x00, 0x02};
	}

}
