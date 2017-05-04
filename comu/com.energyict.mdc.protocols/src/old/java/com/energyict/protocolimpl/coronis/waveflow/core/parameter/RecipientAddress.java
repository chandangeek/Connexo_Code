/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

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

    public String getAddress() {
        return address;
    }
}
