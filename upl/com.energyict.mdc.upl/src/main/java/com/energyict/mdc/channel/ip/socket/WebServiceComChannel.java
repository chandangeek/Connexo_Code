/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channel.ip.socket;

import com.energyict.mdc.channel.AbstractComChannel;
import com.energyict.mdc.protocol.ComChannelType;

import java.io.IOException;

public class WebServiceComChannel extends AbstractComChannel {


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
        return false;
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
        return false;
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
        return ComChannelType.WebServiceComChannel;
    }
}