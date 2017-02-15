/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.core;


import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.util.logging.Logger;


public interface ProtocolStackLink {
	Logger getLogger();
	public byte[] sendEscapeData(byte[] bs) throws NestedIOException, ConnectionException;
}
