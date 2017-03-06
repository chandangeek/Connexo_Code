package com.energyict.protocolimpl.edmi.common.connection;

import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edmi.common.core.ResponseData;

/**
 * @author sva
 * @since 23/02/2017 - 16:21
 */
public interface CommandLineConnection extends ProtocolConnection {

    ResponseData sendCommand(byte[] cmdData);

}