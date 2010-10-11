package com.energyict.protocolimpl.dlms.as220.powerquality;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.emeter.LoadProfileCompactArrayEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 11-okt-2010
 * Time: 13:05:21
 * To change this template use File | Settings | File Templates.
 */
public class PowerQualityProfileBuilder {

    private final PowerQuality powerQuality;

    public PowerQualityProfileBuilder(PowerQuality powerQuality) {
        this.powerQuality = powerQuality;
    }

    public ScalerUnit[] buildScalerUnits(byte numberOfChannels) throws IOException {
        ScalerUnit[] scalerUnits = new ScalerUnit[numberOfChannels];

        List<CapturedObject> co = this.powerQuality.getGenericPowerQualityProfile().getCaptureObjects();
        int index = 0;
        for (CapturedObject capturedObject : co) {
            ObisCode obis = capturedObject.getLogicalName().getObisCode();
            if (obis.getA() != 0) {
                if (index <= numberOfChannels) {
                    scalerUnits[index] = this.powerQuality.getAs220().getCosemObjectFactory().getCosemObject(obis).getScalerUnit();
                    index++;
                } else {
                    throw new IOException("There are more channels in the captured objects [ PowerQualityLoadProfile ] than needed [" + numberOfChannels + "].");
                }
            }
        }

        return scalerUnits;
    }

    public List<ChannelInfo> buildChannelInfos(ScalerUnit[] scalerunit) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (int i = 0; i < scalerunit.length; i++) {
            ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + this.powerQuality.getAs220().getDeviceID() + "_PQ-channel_" + i, scalerunit[i].getUnit());
            channelInfos.add(channelInfo);
        }
        return channelInfos;
    }

    public List<IntervalData> buildIntervalData(ScalerUnit[] scalerunit, List<LoadProfileCompactArrayEntry> loadProfileCompactArrayEntries) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
