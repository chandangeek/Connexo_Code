/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;

public class CommBootVersionCommand extends AbstractReadCommand {

	public CommBootVersionCommand(int transID) {
		super(transID);
		startAddress = new byte[] {(byte) 0x00,(byte) 0x48};
		numRegisters = new byte[] {0x00, 0x02};
	}

}
