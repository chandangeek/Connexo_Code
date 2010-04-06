package com.energyict.protocolimpl.eig.nexus1272.command;

import java.io.IOException;

import com.energyict.protocolimpl.eig.nexus1272.AbstractLogReader;

public class Historical2LogHeaderCommand extends AbstractReadCommand implements
		Command {

	public Historical2LogHeaderCommand(int transID) {
		super(transID);
		startAddress =  new byte[] {(byte) 0x90,(byte) 0x40};
		numRegisters =  new byte[] {0x00, 0x12};
	}


}
