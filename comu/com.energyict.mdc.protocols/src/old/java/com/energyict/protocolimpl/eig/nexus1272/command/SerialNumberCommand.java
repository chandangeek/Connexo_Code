package com.energyict.protocolimpl.eig.nexus1272.command;


public class SerialNumberCommand extends AbstractReadCommand {

	public SerialNumberCommand(int transID) {
		super(transID);
		startAddress = new byte[] {(byte) 0xFF,(byte) 0xFE};
		numRegisters = new byte[] {0x00, 0x02};
	}

}
