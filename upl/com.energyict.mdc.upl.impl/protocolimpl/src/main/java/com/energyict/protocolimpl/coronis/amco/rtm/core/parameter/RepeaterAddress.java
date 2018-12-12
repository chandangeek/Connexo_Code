package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class RepeaterAddress extends AbstractParameter {

    private String address;
    private int id;

    RepeaterAddress(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    public RepeaterAddress(PropertySpecService propertySpecService, RTM rtm, int id, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
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
