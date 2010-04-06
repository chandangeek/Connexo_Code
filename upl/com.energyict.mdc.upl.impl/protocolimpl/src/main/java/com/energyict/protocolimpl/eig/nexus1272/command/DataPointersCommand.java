package com.energyict.protocolimpl.eig.nexus1272.command;

public class DataPointersCommand extends AbstractReadCommand {

	public DataPointersCommand(int transID) {
		super(transID);
		//FIXME SET ONLY FOR HIST2
		startAddress = new byte[] {(byte) 0xB1, (byte) 0x14};
		numRegisters = new byte[] {0x00, (byte) 0x79};
	}

}
