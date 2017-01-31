/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class RecipientAddress extends AbstractParameter {

    String address;
    
    RecipientAddress(WaveSense waveSense) {
        super(waveSense);
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

    public String getAddress() {
        return address;
    }
}
