/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;


public class ReadSingleRegisterCommand extends AbstractReadCommand implements ReadCommand{

	public ReadSingleRegisterCommand(int transID) {
		super(transID);
		numRegisters = new byte[] {0x00, 0x01};
	}

	public byte[] getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(byte[] sa) {
		startAddress = sa;
		
	}

}
