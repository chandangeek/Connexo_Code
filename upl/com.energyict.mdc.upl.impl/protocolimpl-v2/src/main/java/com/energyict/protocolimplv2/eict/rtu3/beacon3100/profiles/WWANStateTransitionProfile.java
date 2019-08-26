package com.energyict.protocolimplv2.eict.rtu3.beacon3100.profiles;

import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WWANStateTransitionProfile extends AbstractHardcodedProfileParser {

    public static final ObisCode WWAN_STATE_TRANSITION_1_TRANSITION_TIME = ObisCode.fromString("0.162.96.192.1.255");
    public static final ObisCode WWAN_STATE_TRANSITION_2_FROM_STATE = ObisCode.fromString("0.162.96.192.2.255");
    public static final ObisCode WWAN_STATE_TRANSITION_3_TO_STATE = ObisCode.fromString("0.162.96.192.3.255");
    public static final ObisCode WWAN_STATE_TRANSITION_4_REASON = ObisCode.fromString("0.162.96.192.4.255");


    public WWANStateTransitionProfile(byte[] encodedData) throws IOException {
        super(encodedData);
    }

    /** Hardcoded channels
     transition_time: date-time, // -> date-time
     from_state: enumeration,
     to_state: enumeration,
     reason: octet-string
     */
    public static List<ChannelInfo> getHardcodedChannelInfos(CollectedLoadProfileConfiguration lpc) {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        int ch = 0;
        channelInfos.add(new ChannelInfo(ch++, WWAN_STATE_TRANSITION_1_TRANSITION_TIME.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), true));
        channelInfos.add(new ChannelInfo(ch++, WWAN_STATE_TRANSITION_2_FROM_STATE.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, WWAN_STATE_TRANSITION_3_TO_STATE.toString(),  Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        // this is a string, so cannot put it on a profile, will wait until Connexo will support this
        //  channelInfos.add(new ChannelInfo(ch++, WWAN_STATE_TRANSITION_4_REASON.toString(),  Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        return channelInfos;
    }

}