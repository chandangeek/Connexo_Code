package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class RecipientAddress extends AbstractParameter {

    String address;
    
    RecipientAddress(WaveTherm waveTherm) {
        super(waveTherm);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.RecipientAddress;
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
