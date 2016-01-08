package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class RepeaterAddress extends AbstractParameter {

    String address;
    int id;

    RepeaterAddress(RTM rtm) {
        super(rtm);
    }

    public RepeaterAddress(RTM rtm, int id) {
        super(rtm);
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
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
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
