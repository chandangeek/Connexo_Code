package com.energyict.mdc.upl.io;

import com.energyict.mdc.protocol.ComChannel;

import aQute.bnd.annotation.ProviderType;

import java.io.IOException;
import java.net.Socket;

/**
 * Provides services to create sockets.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-13 (11:55)
 */
@ProviderType
public interface SocketService extends UPLSocketService {

    InboundUdpSession newInboundUdpSession(int bufferSize, int port);

    ComChannel newSocketComChannel(Socket socket) throws IOException;

}