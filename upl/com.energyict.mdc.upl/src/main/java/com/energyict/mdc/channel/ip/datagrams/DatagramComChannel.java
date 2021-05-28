package com.energyict.mdc.channel.ip.datagrams;


import com.energyict.mdc.channel.SynchroneousComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.io.VirtualUdpSession;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.Logger;

/**
 * A DatagramComChannel is a {@link com.energyict.mdc.protocol.ComChannel} that will be used
 * for UDP sessions.
 * Note that this comChannel is marked to FIRST WRITE data, if reading is your first task,
 * make sure you call the proper {@link #startReading()} method.
 * <p>
 *
 * Date: 5/11/12
 * Time: 15:26
 */
public class DatagramComChannel extends SynchroneousComChannel {
    public static final Logger logger = Logger.getLogger(DatagramComChannel.class.getName());

    private AbstractUdpSession udpSession;
    private String remoteAddress;

    /**
     * Creates a new SynchroneousComChannel that uses the specified
     * InputStream and OutputStream as underlying communication mechanisms.
     *
     * @param udpSession the used virtual UDP session
     */
    public DatagramComChannel(VirtualUdpSession udpSession) {
        super(udpSession.getInputStream(), udpSession.getOutputStream());
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.DatagramComChannel;
    }

    public DatagramComChannel(DatagramInputStream is, DatagramOutputStream os, AbstractUdpSession udpSession, String remoteAddress) {
        super(is, os);
        this.udpSession = udpSession;
        this.remoteAddress = remoteAddress;
    }

    public void receive(DatagramPacket receivedPacket) throws IOException {
        if (udpSession != null) {
            int freeCapacity = udpSession.getBufferSize() - in.available();
            if (freeCapacity < receivedPacket.getLength()) {
                ((DatagramInputStream)in).write(receivedPacket.getData(), receivedPacket.getOffset(), freeCapacity);
                logger.info("Could not write full UDP packet, only the first " + freeCapacity + " of " + receivedPacket.getLength() + " bytes are written to the stream!");
            } else {
                ((DatagramInputStream) in).write(receivedPacket.getData(), receivedPacket.getOffset(), receivedPacket.getLength());
            }
        }
    }

    public void doClose() {
        super.doClose();
        if (udpSession != null) {
            udpSession.closeComChannel(remoteAddress);
        }
    }
}
