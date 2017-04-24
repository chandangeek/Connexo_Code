package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.profile;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

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

    private static final ChannelConfig[] QUANTITY = new ChannelConfig[] {
            new ChannelConfig(0, Unit.get(BaseUnit.WATTHOUR), "active Energy imp."),
            new ChannelConfig(1, Unit.get(BaseUnit.WATTHOUR), "active Energy exp."),
            new ChannelConfig(2, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy imp."),
            new ChannelConfig(3, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy exp."),
            new ChannelConfig(4, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy ind."),
            new ChannelConfig(5, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy cap."),
            new ChannelConfig(6, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QI"),
            new ChannelConfig(7, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QII"),
            new ChannelConfig(8, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QIII"),
            new ChannelConfig(9, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR), "reactive Energy QIV."),
            new ChannelConfig(10, Unit.get(BaseUnit.VOLTAMPEREHOUR), "apparent energy imp."),
            new ChannelConfig(11, Unit.get(BaseUnit.VOLTAMPEREHOUR), "apparent energy exp."),
            new ChannelConfig(12, Unit.get(BaseUnit.VOLT), "phase voltage"),
            new ChannelConfig(13, Unit.get(BaseUnit.VOLT), "line to line voltage"),
            new ChannelConfig(14, Unit.get(BaseUnit.AMPERE), "current."),
            new ChannelConfig(15, Unit.get(BaseUnit.WATT), "active power"),
            new ChannelConfig(16, Unit.get(BaseUnit.WATT), "reactive power"),
            new ChannelConfig(17, Unit.get(BaseUnit.WATT), "apparent power"),
            new ChannelConfig(18, Unit.get(BaseUnit.HERTZ), "frequency"),
            new ChannelConfig(19, Unit.getUndefined(), "phase angle."),
            new ChannelConfig(20, Unit.getUndefined(), "power factor."),
            new ChannelConfig(21, Unit.getUndefined(), "THD voltage"),
            new ChannelConfig(22, Unit.getUndefined(), "THD current"),

            new ChannelConfig(23, Unit.getUndefined(), "external reg. 0"),
            new ChannelConfig(24, Unit.getUndefined(), "external reg. 1"),
            new ChannelConfig(25, Unit.getUndefined(), "external reg. 2"),
            new ChannelConfig(26, Unit.getUndefined(), "external reg. 3"),
            new ChannelConfig(27, Unit.getUndefined(), "external reg. 4"),
            new ChannelConfig(28, Unit.getUndefined(), "external reg. 5"),
            new ChannelConfig(29, Unit.getUndefined(), "external reg. 6"),
            new ChannelConfig(30, Unit.getUndefined(), "external reg. 7"),

            new ChannelConfig(31, Unit.get("THDVIEEE"), "Voltage harmonic. 1"),
            new ChannelConfig(32, Unit.get("THDVIEEE"), "Voltage harmonic. 2"),
            new ChannelConfig(33, Unit.get("THDVIEEE"), "Voltage harmonic. 3"),
            new ChannelConfig(34, Unit.get("THDVIEEE"), "Voltage harmonic. 4"),
            new ChannelConfig(35, Unit.get("THDVIEEE"), "Voltage harmonic. 5"),
            new ChannelConfig(36, Unit.get("THDVIEEE"), "Voltage harmonic. 6"),
            new ChannelConfig(37, Unit.get("THDVIEEE"), "Voltage harmonic. 7"),
            new ChannelConfig(38, Unit.get("THDVIEEE"), "Voltage harmonic. 8"),
            new ChannelConfig(39, Unit.get("THDVIEEE"), "Voltage harmonic. 9"),
            new ChannelConfig(40, Unit.get("THDVIEEE"), "Voltage harmonic. 10"),
            new ChannelConfig(41, Unit.get("THDVIEEE"), "Voltage harmonic. 11"),
            new ChannelConfig(42, Unit.get("THDVIEEE"), "Voltage harmonic. 12"),
            new ChannelConfig(43, Unit.get("THDVIEEE"), "Voltage harmonic. 13"),
            new ChannelConfig(44, Unit.get("THDVIEEE"), "Voltage harmonic. 14"),
            new ChannelConfig(45, Unit.get("THDVIEEE"), "Voltage harmonic. 15"),
            new ChannelConfig(46, Unit.get("THDVIEEE"), "Voltage harmonic. 16"),
            new ChannelConfig(47, Unit.get("THDVIEEE"), "Voltage harmonic. 17"),
            new ChannelConfig(48, Unit.get("THDVIEEE"), "Voltage harmonic. 18"),
            new ChannelConfig(49, Unit.get("THDVIEEE"), "Voltage harmonic. 19"),
            new ChannelConfig(50, Unit.get("THDVIEEE"), "Voltage harmonic. 20"),
            new ChannelConfig(51, Unit.get("THDVIEEE"), "Voltage harmonic. 21"),
            new ChannelConfig(52, Unit.get("THDVIEEE"), "Voltage harmonic. 22"),
            new ChannelConfig(53, Unit.get("THDVIEEE"), "Voltage harmonic. 23"),
            new ChannelConfig(54, Unit.get("THDVIEEE"), "Voltage harmonic. 24"),
            new ChannelConfig(55, Unit.get("THDVIEEE"), "Voltage harmonic. 25"),
            new ChannelConfig(56, Unit.get("THDVIEEE"), "Voltage harmonic. 26"),
            new ChannelConfig(57, Unit.get("THDVIEEE"), "Voltage harmonic. 27"),
            new ChannelConfig(58, Unit.get("THDVIEEE"), "Voltage harmonic. 28"),
            new ChannelConfig(59, Unit.get("THDVIEEE"), "Voltage harmonic. 29"),
            new ChannelConfig(60, Unit.get("THDVIEEE"), "Voltage harmonic. 30"),
            new ChannelConfig(61, Unit.get("THDVIEEE"), "Voltage harmonic. 31"),

            new ChannelConfig(62, Unit.get("THDIIEEE"), "Current harmonic. 1"),
            new ChannelConfig(63, Unit.get("THDIIEEE"), "Current harmonic. 2"),
            new ChannelConfig(64, Unit.get("THDIIEEE"), "Current harmonic. 3"),
            new ChannelConfig(65, Unit.get("THDIIEEE"), "Current harmonic. 4"),
            new ChannelConfig(66, Unit.get("THDIIEEE"), "Current harmonic. 5"),
            new ChannelConfig(67, Unit.get("THDIIEEE"), "Current harmonic. 6"),
            new ChannelConfig(68, Unit.get("THDIIEEE"), "Current harmonic. 7"),
            new ChannelConfig(69, Unit.get("THDIIEEE"), "Current harmonic. 8"),
            new ChannelConfig(70, Unit.get("THDIIEEE"), "Current harmonic. 9"),
            new ChannelConfig(71, Unit.get("THDIIEEE"), "Current harmonic. 10"),
            new ChannelConfig(72, Unit.get("THDIIEEE"), "Current harmonic. 11"),
            new ChannelConfig(73, Unit.get("THDIIEEE"), "Current harmonic. 12"),
            new ChannelConfig(74, Unit.get("THDIIEEE"), "Current harmonic. 13"),
            new ChannelConfig(75, Unit.get("THDIIEEE"), "Current harmonic. 14"),
            new ChannelConfig(76, Unit.get("THDIIEEE"), "Current harmonic. 15"),
            new ChannelConfig(77, Unit.get("THDIIEEE"), "Current harmonic. 16"),
            new ChannelConfig(78, Unit.get("THDIIEEE"), "Current harmonic. 17"),
            new ChannelConfig(79, Unit.get("THDIIEEE"), "Current harmonic. 18"),
            new ChannelConfig(80, Unit.get("THDIIEEE"), "Current harmonic. 19"),
            new ChannelConfig(81, Unit.get("THDIIEEE"), "Current harmonic. 20"),
            new ChannelConfig(82, Unit.get("THDIIEEE"), "Current harmonic. 21"),
            new ChannelConfig(83, Unit.get("THDIIEEE"), "Current harmonic. 22"),
            new ChannelConfig(84, Unit.get("THDIIEEE"), "Current harmonic. 23"),
            new ChannelConfig(85, Unit.get("THDIIEEE"), "Current harmonic. 24"),
            new ChannelConfig(86, Unit.get("THDIIEEE"), "Current harmonic. 25"),
            new ChannelConfig(87, Unit.get("THDIIEEE"), "Current harmonic. 26"),
            new ChannelConfig(88, Unit.get("THDIIEEE"), "Current harmonic. 27"),
            new ChannelConfig(89, Unit.get("THDIIEEE"), "Current harmonic. 28"),
            new ChannelConfig(90, Unit.get("THDIIEEE"), "Current harmonic. 29"),
            new ChannelConfig(91, Unit.get("THDIIEEE"), "Current harmonic. 30"),
            new ChannelConfig(92, Unit.get("THDIIEEE"), "Current harmonic. 31"),

            new ChannelConfig(93, Unit.getUndefined(), "Voltage unbalance")

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
                unit = QUANTITY[bit1To5].getUnit();
                name = QUANTITY[bit1To5].getDescription() + " ";
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
