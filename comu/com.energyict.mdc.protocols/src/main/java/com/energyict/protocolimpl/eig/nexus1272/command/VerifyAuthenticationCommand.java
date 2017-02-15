/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;


public class VerifyAuthenticationCommand extends AbstractReadCommand {

	public VerifyAuthenticationCommand(int transID) {
		super(transID);
		startAddress = new byte[] {(byte) 0xFF, 0x28};
		numRegisters = new byte[] {0x00, 0x01};
	}

}
