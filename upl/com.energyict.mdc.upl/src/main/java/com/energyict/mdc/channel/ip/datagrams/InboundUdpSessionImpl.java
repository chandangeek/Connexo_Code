package com.energyict.mdc.channel.ip.datagrams;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.io.InboundUdpSession;
import com.energyict.mdc.upl.io.UPLSocketService;

import com.energyict.protocol.exceptions.ConnectionSetupException;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * Straightforward version of an inbound UDP session.
 * <p>
 * <p>
 * Date: 9/11/12
 * Time: 10:56
 */
public class InboundUdpSessionImpl extends AbstractUdpSession implements InboundUdpSession {

    /**
     * Default constructor for an InboundUdpSession
     *
     * @param socketService The SocketService that will actually create the UDP socket
     * @param bufferSize the bufferSize of the ByteArray that will be filled up by the DatagramPacket
     * @param portNumber the portNumber on which to listen on this machine for UDP packets
     */
    public InboundUdpSessionImpl(int bufferSize, int portNumber, UPLSocketService socketService) {
        super(bufferSize);
        try {
            setDatagramSocket(socketService.newUDPSocket(portNumber));
        } catch (SocketException e) {
            throw new ConnectionSetupException(e, MessageSeeds.SETUP_OF_INBOUND_CALL_FAILED);
        }
    }

    @Override
    public ComChannel accept() {
        byte[] receiveData = new byte[getBufferSize()];
        try {
            final DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
            getDatagramSocket().receive(datagramPacket);
            setSocketAddress(datagramPacket.getSocketAddress());
            DatagramInputStream datagramInputStream = new DatagramInputStream(new PipedOutputStream(), getBufferSize());
            datagramInputStream.write(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());
            setInputStream(datagramInputStream);
            setOutputStream(new DatagramOutputStream());
            final DatagramComChannel datagramComChannel = new DatagramComChannel(this);
            datagramComChannel.startReading();   // need to set the startReading because we are listening for data!
            return datagramComChannel;
        } catch (IOException e) {
            throw new ConnectionSetupException(e, MessageSeeds.SETUP_OF_INBOUND_CALL_FAILED);
        }
    }

}