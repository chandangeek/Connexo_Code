package com.energyict.protocolimplv2.dlms.as3000.readers.loadprofile;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration.UnalteredUnitChannelReader;

import java.io.IOException;
import java.util.List;

public class AS3000ChannelReader extends UnalteredUnitChannelReader {

    public AS3000ChannelReader() {
        super();
    }

    @Override
    public List<ChannelInfo> getChannelInfo(AbstractDlmsProtocol protocol, LoadProfileReader loadProfileReader, ProfileGeneric profileGeneric) throws IOException {
        List<ChannelInfo> channelInfo = super.getChannelInfo(protocol, loadProfileReader, profileGeneric);
        // removing first 2 channels since they are timestamp and status
        return channelInfo.subList(2,channelInfo.size());
    }
}
