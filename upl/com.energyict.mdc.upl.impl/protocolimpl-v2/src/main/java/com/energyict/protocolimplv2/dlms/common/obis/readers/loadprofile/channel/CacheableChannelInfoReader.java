package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.channel;

import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

public class CacheableChannelInfoReader implements ChannelInfoReader {

    private final ChannelInfoReader actualReader;
    private  CollectedLoadProfileConfiguration channelInfo;

    public CacheableChannelInfoReader(ChannelInfoReader actualReader) {
        this.actualReader = actualReader;
    }

    @Override
    public CollectedLoadProfileConfiguration getChannelInfo(LoadProfileReader lpr, AbstractDlmsProtocol protocol) {
        if (channelInfo == null) {
            channelInfo = actualReader.getChannelInfo(lpr, protocol);
        }
        return channelInfo;
    }
}
