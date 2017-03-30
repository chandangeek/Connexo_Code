/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;

public interface ReadCommand extends Command {

	public void setStartAddress(byte[] startAddress);
	public byte[] getNumRegisters();
	public byte[] getStartAddress();
	public void setNumRegisters(byte[] numRegisters);
}
