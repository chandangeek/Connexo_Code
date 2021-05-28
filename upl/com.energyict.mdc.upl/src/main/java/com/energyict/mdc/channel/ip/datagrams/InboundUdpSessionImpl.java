package com.energyict.mdc.channel.ip.datagrams;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.io.InboundUdpSession;
import com.energyict.mdc.upl.io.UPLSocketService;

import com.energyict.protocol.exceptions.ConnectionSetupException;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.logging.Logger;

/**
 * Straightforward version of an inbound UDP session.
 * <p>
 * <p>
 * Date: 9/11/12
 * Time: 10:56
 */
public class InboundUdpSessionImpl extends AbstractUdpSession implements InboundUdpSession {

    public static final Logger logger = Logger.getLogger(InboundUdpSession.class.getName());

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
            datagramSocket.setSoTimeout(0);
        } catch (SocketException e) {
            throw new ConnectionSetupException(e, MessageSeeds.SETUP_OF_INBOUND_CALL_FAILED);
        }
    }

    @Override
    public ComChannel accept() {
        byte[] receiveData = new byte[getBufferSize()];
        try {
            final DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
            logger.info("InboundUdpSession - waiting for data");
            datagramSocket.receive(datagramPacket);
            logger.info("InboundUdpSession - " + datagramPacket.getLength() + " bytes received: " + new String(datagramPacket.getData()));
            String remoteAddress = datagramPacket.getAddress() + ":" + datagramPacket.getPort();
            DatagramComChannel datagramComChannel = sessions.get(remoteAddress);
            boolean existingComChannel = false;
            if (datagramComChannel == null) {
                DatagramInputStream datagramInputStream = new DatagramInputStream(new PipedOutputStream(), getBufferSize());
                datagramComChannel = new DatagramComChannel(datagramInputStream,
                        new DatagramOutputStream(datagramSocket, datagramPacket.getSocketAddress(), getBufferSize()),
                        this, remoteAddress);
                synchronized (sessions) {
                    sessions.put(remoteAddress, datagramComChannel);
                }
                logger.info("New UDP session created for remote address " + remoteAddress + " #sessions=" + sessions.size());
            } else {
                existingComChannel = true;
                logger.info("Retrieved existing UDP session for " + remoteAddress);
            }

            datagramComChannel.receive(datagramPacket);
            datagramComChannel.startReading();   // need to set the startReading because we are listening for data!
            // prevent starting a new Worker thread if the session already exists - see MultiThreadedComPortListener.doRun()
            // return existingComChannel ? new VoidComChannel() : datagramComChannel;
            return datagramComChannel;
        } catch (IOException e) {
            throw new ConnectionSetupException(e, MessageSeeds.SETUP_OF_INBOUND_CALL_FAILED);
        }
    }

}
