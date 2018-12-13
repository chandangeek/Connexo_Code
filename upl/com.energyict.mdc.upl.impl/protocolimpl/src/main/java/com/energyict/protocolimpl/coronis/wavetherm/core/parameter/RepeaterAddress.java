package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class RepeaterAddress extends AbstractParameter {

    String address;
    int id;

    RepeaterAddress(WaveTherm waveTherm) {
        super(waveTherm);
    }

    public RepeaterAddress(WaveTherm waveTherm, int id) {
        super(waveTherm);
        this.id = id;
    }

    @Override
    ParameterId getParameterId() {
        switch (id) {
            default:
            case 1:
                return ParameterId.RepeaterAddress1;
            case 2:
                return ParameterId.RepeaterAddress2;
            case 3:
                return ParameterId.RepeaterAddress3;
        }
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        address = ProtocolTools.getHexStringFromBytes(data);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return ProtocolTools.getBytesFromHexString(address, "");
    }

    public void setAddress(String address) {
        this.address = address;
    }
}