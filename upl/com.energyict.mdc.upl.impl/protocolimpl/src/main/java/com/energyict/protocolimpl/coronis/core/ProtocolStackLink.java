package com.energyict.protocolimpl.coronis.core;

import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;

import java.util.logging.Logger;


public interface ProtocolStackLink {
	Logger getLogger();
	byte[] sendEscapeData(byte[] bs) throws NestedIOException, ConnectionException;
}
