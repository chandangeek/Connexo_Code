package com.energyict.mdc.channels.ip.datagrams;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.protocol.ComChannel;

import com.energyict.protocol.exceptions.ConnectionSetupException;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * Straightforward version of an inbound UDP session
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:56
 */
public class InboundUdpSession extends AbstractUdpSession {

    /**
     * Default constructor for an InboundUdpSession
     *
     * @param bufferSize the bufferSize of the ByteArray that will be filled up by the DatagramPacket
     * @param portNumber the portNumber on which to listen on this machine for UDP packets
     */
    public InboundUdpSession(int bufferSize, int portNumber) {
        super(bufferSize);
        try {
            setDatagramSocket(ManagerFactory.getCurrent().getSocketFactory().newUDPSocket(portNumber));
        } catch (SocketException e) {
            throw ConnectionSetupException.setupOfInboundCallFailed(e);
        }
    }

    /**
     * Properly waits for an initial UDP packet and create an input- and outputStream for them.
     *
     * @return a DatagramComChannel modeled by the initial UDP packet.
     */
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
            throw ConnectionSetupException.setupOfInboundCallFailed(e);
        }
    }

}