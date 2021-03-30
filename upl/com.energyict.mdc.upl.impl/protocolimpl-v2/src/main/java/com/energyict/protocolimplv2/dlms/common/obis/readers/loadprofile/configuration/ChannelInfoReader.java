package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.List;

/**
 * This interface should define contract on how to obtain channel under a load profile
 */
public interface ChannelInfoReader {

    List<ChannelInfo> getChannelInfo(AbstractDlmsProtocol protocol, LoadProfileReader loadProfileReader, ProfileGeneric profileGeneric) throws IOException;
}
