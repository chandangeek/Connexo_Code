package com.energyict.mdc.channels.ip.datagrams;


import com.energyict.mdc.channels.SynchroneousComChannel;
import com.energyict.mdc.io.VirtualUdpSession;

/**
 * A DatagramComChannel is a {@link com.energyict.mdc.protocol.ComChannel} that will be used
 * for UDP sessions.
 * Note that this comChannel is marked to FIRST WRITE data, if reading is your first task,
 * make sure you call the proper {@link #startReading()} method.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 15:26
 */
public class DatagramComChannel extends SynchroneousComChannel {

    /**
     * Creates a new SynchroneousComChannel that uses the specified
     * InputStream and OutputStream as underlying communication mechanisms.
     *
     * @param udpSession the used virtual UDP session
     */
    public DatagramComChannel(VirtualUdpSession udpSession) {
        super(udpSession.getInputStream(), udpSession.getOutputStream());
    }
}