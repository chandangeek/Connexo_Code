/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;

import java.io.IOException;

public abstract class AbstractCommand implements Command {

	protected int transactionID;
	protected int protocolID;
	protected int length;
	protected int unitID;

	
	public int getProtocolID() {
		return protocolID;
	}

	public int getLength() {
		return length;
	}

	public int getUnitID() {
		return unitID;
	}
	
	public int getTransactionID() {
		return transactionID;
	}
	public AbstractCommand(int transID) {
		transactionID = transID;
		protocolID = 0;
		unitID = 1;
	}
	
	public abstract byte[] build() throws IOException;
	
	public static byte[] intToByteArray(int value) {
		byte[] b = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
}
