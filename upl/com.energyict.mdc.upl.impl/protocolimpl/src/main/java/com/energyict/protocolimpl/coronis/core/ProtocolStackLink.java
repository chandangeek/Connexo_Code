package com.energyict.protocolimpl.coronis.core;


import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;

import java.util.logging.Logger;


public interface ProtocolStackLink {
	Logger getLogger();
	public byte[] sendEscapeData(byte[] bs) throws NestedIOException, ConnectionException;
}
