package com.energyict.protocolimpl.eig.nexus1272.command;


public class SystemLogHeaderCommand extends AbstractReadCommand {

	public SystemLogHeaderCommand(int transID) {
		super(transID);
		startAddress =  new byte[] {(byte) 0x92,(byte) 0x80};;
		numRegisters =  new byte[] {0x00, 0x12};
	}
	

}
