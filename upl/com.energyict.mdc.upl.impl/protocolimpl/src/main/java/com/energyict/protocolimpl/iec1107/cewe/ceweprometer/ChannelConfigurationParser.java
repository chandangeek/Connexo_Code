package com.energyict.protocolimpl.iec1107.cewe.ceweprometer;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class ChannelConfigurationParser {

    private static final String[] PHASE = {"All phases", "L1", "L2", "L3"};

    private static final Object[][] QUANTITY = new Object[][]{
            {Unit.get(BaseUnit.WATTHOUR), "active Energy imp."},
            {Unit.get(BaseUnit.WATTHOUR), "active Energy exp."},
            {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy imp."},
            {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy exp."},
            {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy ind."},
            {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy cap."},
            {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QI"},
            {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QII"},
            {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QIII"},
            {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QIV."},
            {Unit.get(BaseUnit.VOLTAMPEREHOUR), "apparent energy imp."},
            {Unit.get(BaseUnit.VOLTAMPEREHOUR), "apparent energy exp."},
            {Unit.get(BaseUnit.VOLT), "phase voltage"},
            {Unit.get(BaseUnit.VOLT), "line to line voltage"},
            {Unit.get(BaseUnit.AMPERE), "current."},
            {Unit.get(BaseUnit.WATT), "active power"},
            {Unit.get(BaseUnit.WATT), "reactive power"},
            {Unit.get(BaseUnit.WATT), "apparent power"},
            {Unit.get(BaseUnit.HERTZ), "frequency"},
            {Unit.getUndefined(), "phase angle."},
            {Unit.getUndefined(), "power factor."},
            {Unit.getUndefined(), "THD voltage"},
            {Unit.getUndefined(), "THD current"},
            {Unit.getUndefined(), "external reg. 0"},
            {Unit.getUndefined(), "external reg. 1"},
            {Unit.getUndefined(), "external reg. 2"},
            {Unit.getUndefined(), "external reg. 3"},
            {Unit.getUndefined(), "external reg. 4"},
            {Unit.getUndefined(), "external reg. 5"},
            {Unit.getUndefined(), "external reg. 6"},
            {Unit.getUndefined(), "external reg. 7"},
    };


    public static List<ChannelInfo> toChannelInfoOldFw(String data, int nrChannels) {

        data = data.substring(1, data.length() - 1);
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        for (int idx = 0; idx < nrChannels; idx++) {

            String s = data.substring(idx * 2, (idx * 2) + 2);

            byte bit1To5 = (byte) (Byte.parseByte(s, 16) & 0x3F);
            byte bit6To7 = (byte) (Byte.parseByte(s, 16) & 0xC0);

            Unit unit = (Unit) QUANTITY[bit1To5][0];
            String name = QUANTITY[bit1To5][1] + " " + PHASE[bit6To7];

            ChannelInfo ci = new ChannelInfo(idx, name, unit);

            /* for energy & external registers the current reading of the
             * register is logged, this is cumulative ... */

            if (bit1To5 < 12 || bit1To5 > 22) {
                ci.setCumulative();
                ci.setCumulativeWrapValue(new BigDecimal("1000000000"));
            }

            channelInfos.add(ci);

        }

        return channelInfos;

    }

    public static List<ChannelInfo> toChannelInfoNewFw(String[] rawData) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < rawData.length; i++) {
            String rawChannelInfo = rawData[i];
            sb.append(getConfigBytesForChannelinfo(rawChannelInfo));
        }
        sb.append(")");
        return toChannelInfoOldFw(sb.toString(), rawData.length);
    }

    private static String getConfigBytesForChannelinfo(String rawChannelInfo) {
        String channelInfo = rawChannelInfo.replaceAll("(", "");
        channelInfo = channelInfo.replaceAll(")", "");
        channelInfo = channelInfo.trim();
        String[] infoParts = channelInfo.split(",");
        if (infoParts.length == 3) {
            int quantity = Integer.valueOf(infoParts[0]);
            int phase = Integer.valueOf(infoParts[0]);
            int value = quantity & 0x3F;
            value |= ((phase & 0x03) << 6);
            String hexValue = Integer.toHexString(value);
            return hexValue.length() == 1 ? "0" + hexValue : hexValue;
        }
        return "00";
    }

}
