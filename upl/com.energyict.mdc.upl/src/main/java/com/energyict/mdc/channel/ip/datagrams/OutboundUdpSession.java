package com.energyict.mdc.channel.ip.datagrams;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Straightforward version of an Outbound UDP session
 * <p>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 11:41
 */
public class OutboundUdpSession extends AbstractUdpSession {

    /**
     * Default constructor for an OutboundUdpSession
     *
     * @param bufferSize the bufferSize of the ByteArray that will be filled up by the DatagramPacket
     * @param host       to remote host on which to connect
     * @param portNumber the portNumber of the remote host on which to connect
     */
    public OutboundUdpSession(int bufferSize, String host, int portNumber) throws IOException {
        super(bufferSize);
        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.connect(new InetSocketAddress(host, portNumber));
        setDatagramSocket(datagramSocket);
        setSocketAddress(datagramSocket.getRemoteSocketAddress());
        setInputStream(new DatagramInputStream(new PipedOutputStream(), bufferSize));
        setOutputStream(new DatagramOutputStream());
    }
}