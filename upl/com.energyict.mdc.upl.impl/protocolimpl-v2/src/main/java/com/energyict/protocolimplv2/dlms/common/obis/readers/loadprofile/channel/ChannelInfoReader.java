package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.channel;

import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

public interface ChannelInfoReader {
    CollectedLoadProfileConfiguration getChannelInfo(com.energyict.protocol.LoadProfileReader lpr, AbstractDlmsProtocol protocol);
}
