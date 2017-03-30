/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;

public class DataPointersCommand extends AbstractReadCommand {

	public DataPointersCommand(int transID) {
		super(transID);
		//TODO These are set only for historical log 2, if we want to support reading
		//		historical log 1 then we need those addresses and a way to differentiate
		startAddress = new byte[] {(byte) 0xB1, (byte) 0x14};
		numRegisters = new byte[] {0x00, (byte) 0x79};
	}

}
