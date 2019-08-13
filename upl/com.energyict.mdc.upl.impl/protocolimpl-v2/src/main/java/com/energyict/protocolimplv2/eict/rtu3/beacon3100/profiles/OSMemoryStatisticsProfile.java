package com.energyict.protocolimplv2.eict.rtu3.beacon3100.profiles;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.Clock;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OSMemoryStatisticsProfile extends AbstractHardcodedProfileParser {

    public static final ObisCode OS_MEMORY_TOTAL_VALUE          = ObisCode.fromString("0.194.96.130.1.255");
    public static final ObisCode OS_MEMORY_FREE_VALUE           = ObisCode.fromString("0.194.96.130.2.255");
    public static final ObisCode OS_MEMORY_USED_VALUE           = ObisCode.fromString("0.194.96.130.3.255");
    public static final ObisCode OS_MEMORY_ACTUAL_USED_VALUE    = ObisCode.fromString("0.194.96.130.4.255");

    public OSMemoryStatisticsProfile(byte[] encodedData) throws IOException {
        super(encodedData);
    }


    /** Hardcoded channels for OS Memory Statistics
     * @param lpc
     */
    public static List<ChannelInfo> getHardcodedChannelInfos(CollectedLoadProfileConfiguration lpc) {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        int ch = 0;
        channelInfos.add(new ChannelInfo(ch++, Clock.getDefaultObisCode().toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), true));

        channelInfos.add(new ChannelInfo(ch++, OS_MEMORY_TOTAL_VALUE.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, OS_MEMORY_FREE_VALUE.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, OS_MEMORY_USED_VALUE.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, OS_MEMORY_ACTUAL_USED_VALUE.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));


        return channelInfos;
    }

}