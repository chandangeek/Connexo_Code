package com.energyict.mdc.engine.impl.core;

import com.energyict.protocols.mdc.channels.AbstractComChannel;

import java.io.IOException;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.api.ComChannel} interface
 * that writes all bytes to System.out.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-19 (10:42)
 */
public class SystemOutComChannel extends AbstractComChannel {

    @Override
    public boolean doStartReading () {
        return true;
    }

    @Override
    public int doRead () {
        return 0;
    }

    @Override
    public int doRead (byte[] buffer) {
        return 0;
    }

    @Override
    public int doRead (byte[] buffer, int offset, int length) {
        return 0;
    }

    @Override
    public int available () {
        return 0;
    }

    @Override
    public boolean doStartWriting () {
        return true;
    }

    @Override
    public int doWrite (int b) {
        System.out.println(b);
        return 1;
    }

    @Override
    public int doWrite (byte[] bytes) {
        System.out.println(new String(bytes));
        return bytes.length;
    }

    @Override
    public void doClose () {
        // nothing to do
    }

    @Override
    public void doFlush() throws IOException {
        // nothing to do
    }

}