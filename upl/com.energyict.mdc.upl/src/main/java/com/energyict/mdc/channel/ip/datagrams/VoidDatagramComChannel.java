package com.energyict.mdc.channel.ip.datagrams;


import com.energyict.mdc.channel.AbstractComChannel;
import com.energyict.mdc.protocol.ComChannelType;

import java.io.IOException;

/**
 * A VoidDatagramComChannel is a {@link com.energyict.mdc.protocol.ComChannel} that will be used for UDP sessions.
 *
 * It will be returned by InboundUdpSessionImpl when communication is received for an already established Inbound connection
 * In this case, the InboundUdpSessionImpl will inform the already established DatagramComChannel
 *
 * The ComPortListener will only start a new Inbound communication-session Worker for a ComChannel when isVoid() == false
 *
 * See MultiThreadedComPortListener's doRun() method
 * <p>
 *
 * Date: 5/11/12
 * Time: 15:26
 */
public class VoidDatagramComChannel extends AbstractComChannel {

    @Override
    protected void doClose() {

    }

    @Override
    protected void doFlush() throws IOException {

    }

    @Override
    protected int doAvailable() {
        return 0;
    }

    @Override
    protected boolean doStartReading() {
        return true;
    }

    @Override
    protected int doRead() {
        return 0;
    }

    @Override
    protected int doRead(byte[] buffer) {
        return 0;
    }

    @Override
    protected int doRead(byte[] buffer, int offset, int length) {
        return 0;
    }

    @Override
    protected boolean doStartWriting() {
        return true;
    }

    @Override
    protected int doWrite(int b) {
        return 0;
    }

    @Override
    protected int doWrite(byte[] bytes) {
        return 0;
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.DatagramComChannel;
    }

    @Override
    public boolean isVoid() {
        return true;
    }
}
