package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class RecipientAddress extends AbstractParameter {

    String address;
    
    RecipientAddress(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.RecipientAddress;
    }

    @Override
    protected void parse(byte[] data) {
        address = ProtocolTools.getHexStringFromBytes(data);
    }

    @Override
    protected byte[] prepare() {
        return ProtocolTools.getBytesFromHexString(address, "");
    }


    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
