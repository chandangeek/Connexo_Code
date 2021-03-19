package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractChannelInfoReader implements ChannelInfoReader {

    public List<ChannelInfo> getChannelInfo(AbstractDlmsProtocol protocol, LoadProfileReader loadProfileReader, ProfileGeneric profileGeneric) throws IOException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : profileGeneric.getCaptureObjects()) {
            channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
        }
        List<ChannelInfo> channelInfos = new ArrayList<>();
        int counter = 0;
        for (ObisCode obisCode : channelObisCodes) {
            Unit unit = this.getUnit(protocol, obisCode);
            String newOBIS = obisCode.toString();
            ChannelInfo channelInfo = new ChannelInfo(counter, newOBIS, unit, loadProfileReader.getMeterSerialNumber());
            channelInfos.add(channelInfo);
            counter++;
        }
        return channelInfos;
    }

    protected abstract Unit getUnit(AbstractDlmsProtocol protocol, ObisCode obisCode) throws  IOException;

}
