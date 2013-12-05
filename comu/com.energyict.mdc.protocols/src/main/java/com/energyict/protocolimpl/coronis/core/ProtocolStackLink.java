package com.energyict.protocolimpl.coronis.core;


import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdc.common.NestedIOException;

import java.util.logging.Logger;


public interface ProtocolStackLink {
	Logger getLogger();
	public byte[] sendEscapeData(byte[] bs) throws NestedIOException, ConnectionException;
}
