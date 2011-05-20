package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.profile;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class ChannelConfigurationParser {

    private static final String[] PHASE = {
            /* 00 */ "All phases",
            /* 01 */ "L1",
            /* 02 */ "L2",
            /* 03 */ "L3"
    };

    private static final Object[][] QUANTITY = new Object[][]{
            /* 00 */ {Unit.get(BaseUnit.WATTHOUR), "active Energy imp."},
            /* 01 */ {Unit.get(BaseUnit.WATTHOUR), "active Energy exp."},
            /* 02 */ {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy imp."},
            /* 03 */ {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy exp."},
            /* 04 */ {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy ind."},
            /* 05 */ {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy cap."},
            /* 06 */ {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QI"},
            /* 07 */ {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QII"},
            /* 08 */ {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QIII"},
            /* 09 */ {Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QIV."},
            /* 0A */ {Unit.get(BaseUnit.VOLTAMPEREHOUR), "apparent energy imp."},
            /* 0B */ {Unit.get(BaseUnit.VOLTAMPEREHOUR), "apparent energy exp."},
            /* 0C */ {Unit.get(BaseUnit.VOLT), "phase voltage"},
            /* 0D */ {Unit.get(BaseUnit.VOLT), "line to line voltage"},
            /* 0E */ {Unit.get(BaseUnit.AMPERE), "current."},
            /* 0F */ {Unit.get(BaseUnit.WATT), "active power"},
            /* 10 */ {Unit.get(BaseUnit.WATT), "reactive power"},
            /* 11 */ {Unit.get(BaseUnit.WATT), "apparent power"},
            /* 12 */ {Unit.get(BaseUnit.HERTZ), "frequency"},
            /* 13 */ {Unit.getUndefined(), "phase angle."},
            /* 14 */ {Unit.getUndefined(), "power factor."},
            /* 15 */ {Unit.getUndefined(), "THD voltage"},
            /* 16 */ {Unit.getUndefined(), "THD current"},
            /* 17 */ {Unit.getUndefined(), "external reg. 0"},
            /* 18 */ {Unit.getUndefined(), "external reg. 1"},
            /* 19 */ {Unit.getUndefined(), "external reg. 2"},
            /* 1A */ {Unit.getUndefined(), "external reg. 3"},
            /* 1B */ {Unit.getUndefined(), "external reg. 4"},
            /* 1C */ {Unit.getUndefined(), "external reg. 5"},
            /* 1D */ {Unit.getUndefined(), "external reg. 6"},
            /* 1E */ {Unit.getUndefined(), "external reg. 7"},

            // TODO: Add the latest quantities of FW 3.1
    };


    public static List<ChannelInfo> toChannelInfoOldFw(String data, int nrChannels) {

        data = data.substring(1, data.length() - 1);
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        for (int idx = 0; idx < nrChannels; idx++) {

            String s = data.substring(idx * 2, (idx * 2) + 2);

            byte bit1To5 = (byte) (Integer.parseInt(s, 16) & 0x3F);
            byte bit6To7 = (byte) ((Integer.parseInt(s, 16) >> 6) & 0x03);

            Unit unit = Unit.getUndefined();
            String name = "Unknown ";
            if (bit1To5 < QUANTITY.length) {
                unit = (Unit) QUANTITY[bit1To5][0];
                name = QUANTITY[bit1To5][1] + " ";
            }
            name += (bit6To7 < PHASE.length) ? PHASE[bit6To7] : "Unknown phase!";

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
        String channelInfo = rawChannelInfo.replaceAll("\\(", "");
        channelInfo = channelInfo.replaceAll("\\)", "");
        channelInfo = channelInfo.trim();
        String[] infoParts = channelInfo.split(",");
        if (infoParts.length == 3) {
            int quantity = Integer.valueOf(infoParts[0]);
            int phase = Integer.valueOf(infoParts[0]);
            int value = quantity & 0x3F;
            value |= ((phase << 6) & 0x0C0);
            String hexValue = Integer.toHexString(value);
            return hexValue.length() == 1 ? "0" + hexValue : hexValue;
        }
        return "FF";
    }

}
