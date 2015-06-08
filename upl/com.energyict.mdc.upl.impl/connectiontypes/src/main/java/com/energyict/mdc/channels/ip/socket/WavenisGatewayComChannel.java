package com.energyict.mdc.channels.ip.socket;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;

import java.io.IOException;
import java.net.Socket;

/**
 * Extension of the socket comchannel, it adds a reference to a Wavenis stack instance.
 * <p/>
 * Copyrights EnergyICT
 * Date: 30/05/13
 * Time: 9:45
 * Author: khe
 */
public class WavenisGatewayComChannel extends SocketComChannel implements ServerWavenisGatewayComChannel {

    /**
     * Stack instance that represents the interface to the Wavecard of the MUC
     */
    private WavenisStack wavenisStack;

    /**
     * Creates a new SocketComChannel that uses the specified
     * InputStream and OutputStream as underlying communication mechanisms.
     * The ComChannel is open for writing.
     *
     * @param socket the used Socket for the ComChannel
     */
    public WavenisGatewayComChannel(Socket socket, WavenisStack wavenisStack) throws IOException {
        super(socket);
        this.wavenisStack = wavenisStack;
    }

    public WavenisStack getWavenisStack() {
        return wavenisStack;
    }

    @Override
    public void doClose() {
        if (wavenisStack != null) {
            wavenisStack.stop();
        }
        super.doClose();
    }
}