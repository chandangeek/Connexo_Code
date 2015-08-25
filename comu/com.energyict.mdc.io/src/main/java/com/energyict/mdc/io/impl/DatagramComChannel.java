package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.VirtualUdpSession;

import java.net.DatagramSocket;

/**
 * A DatagramComChannel is a {@link ComChannel} that will be used
 * for UDP sessions.
 * Note that this comChannel is marked to FIRST WRITE data, if reading is your first task,
 * make sure you call the proper {@link #startReading()} method.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 15:26
 */
public class DatagramComChannel extends SynchronousComChannel {

    private final DatagramSocket datagramSocket;

    /**
     * Creates a new SynchronousComChannel that uses the specified
     * InputStream and OutputStream as underlying communication mechanisms.
     *
     * @param udpSession the used virtual UDP session
     */
    public DatagramComChannel(VirtualUdpSession udpSession) {
        super(udpSession.getInputStream(), udpSession.getOutputStream());
        this.datagramSocket = udpSession.getDatagramSocket();
    }


    @Override
    public void doClose() {
        try {
            super.doClose(); // will close the in- and outputstreams
        } finally {
            if (this.datagramSocket != null) {
                this.datagramSocket.close();
            }
        }
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.DATAGRAM_COM_CHANNEL;
    }
}
