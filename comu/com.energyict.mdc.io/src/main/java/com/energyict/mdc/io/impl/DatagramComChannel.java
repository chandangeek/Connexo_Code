/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.VirtualUdpSession;

import java.net.DatagramSocket;

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
