package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.loadprofile;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration.ChannelInfoReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration.MappableUnitChannelReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActarisSl7000ChannelReader extends MappableUnitChannelReader {

    private static final ObisCode obisCode = ObisCode.fromString("0.0.99.128.1.255");

    public ActarisSl7000ChannelReader() {
        super(obisCode);
    }

    @Override
    public List<ChannelInfo> getChannelInfo(AbstractDlmsProtocol protocol, LoadProfileReader loadProfileReader, ProfileGeneric profileGeneric) throws IOException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        List<CapturedObject> captureObjects = profileGeneric.getCaptureObjects();
        for (CapturedObject capturedObject : captureObjects.subList(4, captureObjects.size())) {
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

}
