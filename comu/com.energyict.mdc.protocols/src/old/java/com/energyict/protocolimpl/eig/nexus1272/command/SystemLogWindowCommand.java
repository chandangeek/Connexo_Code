package com.energyict.protocolimpl.eig.nexus1272.command;

public class SystemLogWindowCommand extends AbstractReadCommand implements ReadCommand{

	public SystemLogWindowCommand(int transID) {
		super(transID);
		startAddress =  new byte[] {(byte) 0x98,(byte) 0x00};;
		numRegisters =  new byte[] {0x00, 0x40};
	}

	public byte[] getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(byte[] sa) {
		startAddress = sa;
		
	}

}
