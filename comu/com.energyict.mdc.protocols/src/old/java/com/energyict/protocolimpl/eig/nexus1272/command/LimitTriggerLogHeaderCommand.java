package com.energyict.protocolimpl.eig.nexus1272.command;


public class LimitTriggerLogHeaderCommand extends AbstractReadCommand {

	public LimitTriggerLogHeaderCommand(int transID) {
		super(transID);
		startAddress =  new byte[] {(byte) 0x90,(byte) 0x80};
		numRegisters =  new byte[] {0x00, 0x12};
	}
	

}
