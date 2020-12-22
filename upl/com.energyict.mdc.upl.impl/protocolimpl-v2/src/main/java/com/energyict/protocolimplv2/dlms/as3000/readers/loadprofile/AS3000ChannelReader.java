package com.energyict.protocolimplv2.dlms.as3000.readers.loadprofile;

import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.channel.GenericChannelInfoReader;

public class AS3000ChannelReader extends GenericChannelInfoReader {

    public AS3000ChannelReader(CollectedDataFactory collectedDataFactory) {
        super(collectedDataFactory);
    }

    @Override
    public CollectedLoadProfileConfiguration getChannelInfo(com.energyict.protocol.LoadProfileReader lpr, AbstractDlmsProtocol protocol) {
        CollectedLoadProfileConfiguration channelInfo = super.getChannelInfo(lpr, protocol);
        // removing first 2 channels since they are timestamp and status
        channelInfo.setChannelInfos(channelInfo.getChannelInfos().subList(2,channelInfo.getChannelInfos().size()));
        return channelInfo;
    }
}
