package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.loadprofile;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration.MappableUnitChannelReader;

import java.io.IOException;
import java.util.List;

public class ActarisSl7000ChannelReader extends MappableUnitChannelReader {

    public ActarisSl7000ChannelReader() {
        super(ObisCode.fromString("0.0.99.128.1.255"));
    }

    @Override
    public List<ChannelInfo> getChannelInfo(AbstractDlmsProtocol protocol, LoadProfileReader loadProfileReader, ProfileGeneric profileGeneric) throws IOException {
        List<ChannelInfo> channelInfo = super.getChannelInfo(protocol, loadProfileReader, profileGeneric);
        return channelInfo.subList(4,channelInfo.size());
    }
}
