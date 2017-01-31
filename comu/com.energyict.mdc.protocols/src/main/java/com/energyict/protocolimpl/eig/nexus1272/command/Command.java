/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;

import java.io.IOException;

public interface Command {

	public abstract byte[] build() throws IOException;
	public abstract int getTransactionID();
	public abstract int getProtocolID() ;
	public abstract int getLength();
	public abstract int getUnitID();
	
}
