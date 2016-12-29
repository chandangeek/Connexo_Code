package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class RecipientAddress extends AbstractParameter {

    String address;

    RecipientAddress(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
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
