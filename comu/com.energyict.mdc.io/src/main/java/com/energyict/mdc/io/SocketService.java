package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.protocol.ComChannel;

import java.io.IOException;
import java.net.Socket;

/**
 * Provides services to create sockets.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-13 (11:55)
 */
@ProviderType
public interface SocketService extends com.energyict.mdc.io.UPLSocketService {

    InboundUdpSession newInboundUdpSession(int bufferSize, int port);

    ComChannel newSocketComChannel(Socket socket) throws IOException;

}