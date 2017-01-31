/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.InboundCommunicationException;
import com.energyict.mdc.io.InboundUdpSession;
import com.energyict.mdc.io.SocketService;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.SocketException;

public class InboundUdpSessionImpl extends AbstractUdpSession implements InboundUdpSession {

    /**
     * Default constructor for an InboundUdpSession.
     *
     * @param bufferSize the bufferSize of the ByteArray that will be filled up by the DatagramPacket
     * @param portNumber the portNumber on which to listen on this machine for UDP packets
     * @param socketService The SocketService
     */
    public InboundUdpSessionImpl(int bufferSize, int portNumber, SocketService socketService) {
        super(bufferSize);
        try {
            setDatagramSocket(socketService.newInboundUDPSocket(portNumber));
        } catch (SocketException e) {
            throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
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
            throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
        }
    }

}