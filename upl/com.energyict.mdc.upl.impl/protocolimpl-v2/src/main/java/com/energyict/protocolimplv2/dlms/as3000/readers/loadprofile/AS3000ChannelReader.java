package com.energyict.protocolimplv2.dlms.as3000.readers.loadprofile;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration.UnalteredUnitChannelReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AS3000ChannelReader extends UnalteredUnitChannelReader {
    private final ObisCode timestampObisCode = ObisCode.fromString("0.0.1.0.0.255");
    private final ObisCode statusObisCode = ObisCode.fromString("1.0.94.49.2.24");

    public AS3000ChannelReader() {
        super();
    }

    @Override
    public List<ChannelInfo> getChannelInfo(AbstractDlmsProtocol protocol, LoadProfileReader loadProfileReader, ProfileGeneric profileGeneric) throws IOException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : profileGeneric.getCaptureObjects()) {
            ObisCode capturedObisCode = capturedObject.getLogicalName().getObisCode();
            if (!timestampObisCode.equals(capturedObisCode) && !statusObisCode.equals(capturedObisCode)) {
                // add everything but timestamp and status channels
                channelObisCodes.add(capturedObisCode);
            }
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
