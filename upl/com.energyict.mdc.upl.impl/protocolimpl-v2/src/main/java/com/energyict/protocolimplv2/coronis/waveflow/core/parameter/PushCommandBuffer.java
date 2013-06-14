package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class PushCommandBuffer extends AbstractParameter {

    byte[] buffer = ProtocolTools.getBytesFromHexString("$01$03$00$00$00$00$00", "$");
    int length = 1;

    PushCommandBuffer(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public void replaceCommand(int cmd) {
        length = 1;
        buffer = new byte[7];
        buffer[0] = 1;
        buffer[1] = (byte) cmd;
    }

    public void clearBuffer() {
        this.buffer = ProtocolTools.getBytesFromHexString("$00$00$00$00$00$00$00", "$");
        this.length = 1;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.PushCommandBuffer;
    }

    @Override
    protected void parse(byte[] data) {
        buffer = data;
        length = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() {
        return buffer;
    }
}