/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class PseudoBubbleUpCommandBuffer extends AbstractParameter {

    byte[] buffer = ProtocolTools.getBytesFromHexString("$01$01$00$00$00$00$00", "$");
    int length = 1;

    PseudoBubbleUpCommandBuffer(RTM rtm) {
        super(rtm);
    }

    public String getBuffer() {
        return ProtocolTools.getHexStringFromBytes(buffer, "");
    }

    public void replaceCommand(int cmd, int portMask, int numberOfReadings, int offset) {
        length = (portMask == 0) ? 1 : 6;
        byte[] numberOfReadingsBytes = ProtocolTools.getBytesFromInt(numberOfReadings, 2);
        byte[] offsetBytes = ProtocolTools.getBytesFromInt(offset, 2);

        buffer = new byte[7];
        buffer[0] = (byte) length;
        buffer[1] = (byte) cmd;
        buffer[2] = (byte) portMask;
        buffer[3] = numberOfReadingsBytes[0];
        buffer[4] = numberOfReadingsBytes[1];
        buffer[5] = offsetBytes[0];
        buffer[6] = offsetBytes[1];
    }

    public void clearBuffer() {
        this.buffer = ProtocolTools.getBytesFromHexString("$00$00$00$00$00$00$00", "$");
        this.length = 1;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.PushCommandBuffer;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        buffer = data;
        length = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return buffer;
    }
}