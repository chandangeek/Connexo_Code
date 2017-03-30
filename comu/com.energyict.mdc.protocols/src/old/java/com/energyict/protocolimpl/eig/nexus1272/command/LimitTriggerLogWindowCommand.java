/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;

public class LimitTriggerLogWindowCommand extends AbstractReadCommand implements ReadCommand{

	public LimitTriggerLogWindowCommand(int transID) {
		super(transID);
		startAddress =  new byte[] {(byte) 0x96,(byte) 0x00};
		numRegisters =  new byte[] {0x00, 0x40};
	}

	public byte[] getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(byte[] sa) {
		startAddress = sa;
		
	}

}
