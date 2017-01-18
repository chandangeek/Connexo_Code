package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.upl.Services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Provides an implementation for the {@link SocketService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (13:19)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.socket", service = {SocketService.class})
@SuppressWarnings("unused")
public class SocketServiceImpl implements SocketService {

    @Activate
    public void activate() {
        Services.socketService(this);
    }

    @Override
    public ServerSocket newTCPSocket (int portNumber) throws IOException {
        return new ServerSocket(portNumber);
    }

    @Override
    public DatagramSocket newUDPSocket (int portNumber) throws SocketException {
        return new DatagramSocket(portNumber);
    }

}