package com.energyict.mdc.channels;

import com.energyict.mdc.protocol.ComChannel;

import java.io.IOException;

/**
 * Provides an implementation of a {@link ComChannel} that can be handled by the ComServer framework,
 * but that basically does nothing. It will mostly be created when exceptions occur during the creation
 * of Inbound connections.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/12
 * Time: 14:06
 */
public class VoidComChannel extends AbstractComChannel {

    @Override
    public boolean doStartReading () {
        //nothing to do
        return true;
    }

    @Override
    public int doRead() {
        return 0;
    }

    @Override
    public int doRead(byte[] buffer) {
        return 0;
    }

    @Override
    public int doRead(byte[] buffer, int offset, int length) {
        return 0;
    }

    @Override
    protected int doAvailable() {
        return 0;
    }

    @Override
    public boolean doStartWriting () {
        //nothing to do
        return true;
    }

    @Override
    public int doWrite(int b) {
        return 0;
    }

    @Override
    public int doWrite(byte[] bytes) {
        return 0;
    }

    @Override
    public void doClose() {
        // nothing to close
    }

    @Override
    public void doFlush() throws IOException {
        // nothing to do
    }

    @Override
    public boolean isVoid() {
        return true;
    }

}