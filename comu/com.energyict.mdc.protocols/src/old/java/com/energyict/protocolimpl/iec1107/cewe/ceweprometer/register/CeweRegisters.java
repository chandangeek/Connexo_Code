/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register;

import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.utils.ProtocolTools;

public class CeweRegisters {

    /**
     * TOU-registers select 00: active energy imp.
     */
    public static final int TOU_ACTIVE_ENERGY_IMP = 0x0;
    /**
     * TOU-registers select 01: active energy exp.
     */
    public static final int TOU_ACTIVE_ENERGY_EXP = 0x1;
    /**
     * TOU-registers select 02: reactive energy imp.
     */
    public static final int TOU_REACTIVE_ENERGY_IMP = 0x2;
    /**
     * TOU-registers select 03: reactive energy exp.
     */
    public static final int TOU_REACTIVE_ENERGY_EXP = 0x3;
    /**
     * TOU-registers select 04: reactive energy ind.
     */
    public static final int TOU_REACTIVE_ENERGY_IND = 0x4;
    /**
     * TOU-registers select 05: reactive energy cap.
     */
    public static final int TOU_REACTIVE_ENERGY_CAP = 0x5;
    /**
     * TOU-registers select 06: reactive energy QI
     */
    public static final int TOU_REACTIVE_ENERGY_QI = 0x6;
    /**
     * TOU-registers select 07: reactive energy QII
     */
    public static final int TOU_REACTIVE_ENERGY_QII = 0x7;
    /**
     * TOU-registers select 08: reactive energy QIII
     */
    public static final int TOU_REACTIVE_ENERGY_QIII = 0x8;
    /**
     * TOU-registers select 09: reactive energy QIV
     */
    public static final int TOU_REACTIVE_ENERGY_QIV = 0x9;
    /**
     * TOU-registers select 10: apparent energy imp.
     */
    public static final int TOU_APPARENT_ENERGY_IMP = 0xA;
    /**
     * TOU-registers select 11: apparent energy exp.
     */
    public static final int TOU_APPARENT_ENERGY_EXP = 0xB;
    /**
     * TOU-registers select 23: active energy imp.
     */
    public static final int TOU_EXTERNAL_REG_1 = 0x17;
    /**
     * TOU-registers select 24: active energy imp.
     */
    public static final int TOU_EXTERNAL_REG_2 = 0x18;
    /**
     * TOU-registers select 25: active energy imp.
     */
    public static final int TOU_EXTERNAL_REG_3 = 0x19;
    /**
     * TOU-registers select 26: active energy imp.
     */
    public static final int TOU_EXTERNAL_REG_4 = 0x1A;
    /**
     * TOU-registers select 27: active energy imp.
     */
    public static final int TOU_EXTERNAL_REG_5 = 0x1B;
    /**
     * TOU-registers select 28: active energy imp.
     */
    public static final int TOU_EXTERNAL_REG_6 = 0x1C;
    /**
     * TOU-registers select 29: active energy imp.
     */
    public static final int TOU_EXTERNAL_REG_7 = 0x1D;
    /**
     * TOU-registers select 30: active energy imp.
     */
    public static final int TOU_EXTERNAL_REG_8 = 0x1E;
    /**
     * Maximum demand for phenomenon: active power import
     */
    public static final int MD_ACTIVE_POWER_IMP = 0x0;
    /**
     * Maximum demand for phenomenon: active power export
     */
    public static final int MD_ACTIVE_POWER_EXP = 0x1;
    /**
     * Maximum demand for phenomenon: reactive power import
     */
    public static final int MD_REACTIVE_POWER_IMP = 0x2;
    /**
     * Maximum demand for phenomenon: reactive power export
     */
    public static final int MD_REACTIVE_POWER_EXP = 0x3;
    /**
     * Maximum demand for phenomenon: reactive power inductive
     */
    public static final int MD_REACTIVE_POWER_IND = 0x4;
    /**
     * Maximum demand for phenomenon: reactive power capacitive
     */
    public static final int MD_REACTIVE_POWER_CAP = 0x5;
    /**
     * Maximum demand for phenomenon: reactive power QI
     */
    public static final int MD_REACTIVE_POWER_QI = 0x6;
    /**
     * Maximum demand for phenomenon: reactive power QII
     */
    public static final int MD_REACTIVE_POWER_QII = 0x7;
    /**
     * Maximum demand for phenomenon: reactive power QIII
     */
    public static final int MD_REACTIVE_POWER_QIII = 0x8;
    /**
     * Maximum demand for phenomenon: reactive power QIV
     */
    public static final int MD_REACTIVE_POWER_QIV = 0x9;
    /**
     * Maximum demand for phenomenon: apparent power import
     */
    public static final int MD_APPARENT_POWER_IMP = 0xA;
    /**
     * Maximum demand for phenomenon: apparent power export
     */
    public static final int MD_APPARENT_POWER_EXP = 0xB;
    private final CewePrometer cewePrometer;

    /**
     * Meter serial number: 0 to 16 char string
     */
    private final ProRegister rSerial;
    private final ProRegister[] rGeneralInfo;
    private final ProRegister rInternalTemp;
    private final ProRegister rBatteryLeft;

    /**
     * Meter firmware version: as 3 comma separated ints (major, minor, rev)
     */
    private final ProRegister rFirmwareVersionOld;

    /**
     * Meter firmware version: as 3 comma separated ints (major, minor, rev)
     */
    private final ProRegister rFirmwareVersionNew;

    /**
     * Date and time (yyyymmdd,hhmmss)
     */
    private final ProRegister rReadDate;

    /**
     * Slide time (S,P)
     * <p/>
     * S: -28800...28800
     * P: 1...40
     * <p/>
     * The meter time is adjusted every minute by P percent of a minute until
     * the time has been adjusted S seconds.
     */
    private final ProRegister rSlideTime;

    /**
     * TOU-registers select.  (8 hex bytes)
     * Defines what each TOU-register represents.
     */
    private final ProRegister rTouRegisterSelect;

    /**
     * Returns single precision value. The example 0.1 represents 10% voltage unbalance.
     */
    private final ProRegister rVoltageUnbalance;

    /**
     * Returned as 46 comma separated floats with single precision
     */
    private final ProRegister rInstantValues;

    /**
     * Time stamp registers
     */
    private final ProRegister[] rTimestamp;

    /**
     * Energy registers
     */
    private final ProRegister[] rEenergy;

    /**
     * Historical external registers (8 floats)
     */
    private final ProRegister[] rExternal;

    /* rows: billing points (not in chronological order), 
    * columns: MD registers
    * rMD[BILLING POINT][MAXIMUM DEMAND]
    */
    private final String[][] md;

    /* rows: billing points (not in chronological order), 
    * columns: TOU registers
    * rMD[BILLING POINT][TOU]
    */
    private final String[][] tou;

    /**
     * Maximum demand registers
     */
    private final ProRegister[][] rMaximumDemand;

    /**
     * Tou registers
     */
    private final ProRegister[][] rTou;

    /**
     * Logger channel count
     */
    private final ProRegister[] rLogChannelCount;

    /**
     * Logger channel interval (seconds)
     */
    private final ProRegister[] rLogChannelInterval;

    /**
     * Log Read offset: before fetching the load profile set start date
     */
    private final ProRegister[] rLogOffset;

    /**
     * what is stored in each channel, for fw < 2.1.0
     */
    private final ProRegister[] rLogChannelConfigOld;

    /**
     * what is stored in each channel, for fw >= 2.1.0
     */
    private final ProRegister[][] rLogChannelConfigNew;

    /**
     * Read next Log record
     */
    private final ProRegister[] rLogNextRecord;

    /*
    * "Event logbook" related registers
    */

    private final ProRegister rEventLogReadOffset;
    private final ProRegister rEventLogNextEvent;

    public CeweRegisters(CewePrometer cewePrometer) {
        this.cewePrometer = cewePrometer;
        this.rSerial = new ProRegister(getCewePrometer(), "108700");

        this.rGeneralInfo = new ProRegister[] {
                new ProRegister(getCewePrometer(), "107100"),
                new ProRegister(getCewePrometer(), "107101"),
                new ProRegister(getCewePrometer(), "107102"),
                new ProRegister(getCewePrometer(), "107103")
        };

        this.rInternalTemp = new ProRegister(getCewePrometer(), "100700");
        this.rBatteryLeft = new ProRegister(getCewePrometer(), "10D800");

        this.rFirmwareVersionOld = new ProRegister(getCewePrometer(), "102500");
        this.rFirmwareVersionNew = new ProRegister(getCewePrometer(), "10DA00");

        /** Date and time (yyyymmdd,hhmmss) */
        this.rReadDate = new ProRegister(getCewePrometer(), "100C00", false);

        /** Slide time (S,P)
         *
         * S: -28800...28800
         * P: 1...40
         *
         * The meter time is adjusted every minute by P percent of a minute until
         * the time has been adjusted S seconds.
         */
        this.rSlideTime = new ProRegister(getCewePrometer(), "108A00", false);

        /** TOU-registers select.  (8 hex bytes)
         * Defines what each TOU-register represents. */
        this.rTouRegisterSelect = new ProRegister(getCewePrometer(), "10D200");

        this.rVoltageUnbalance = new ProRegister(getCewePrometer(), "015400");

        this.rInstantValues = new ProRegister(getCewePrometer(), "015200");


        /** Time stamp registers */
        this.rTimestamp = new ProRegister[] {
            rReadDate,                        /* 255 */
            new ProRegister(getCewePrometer(), "103700"),  /* VZ  */
            new ProRegister(getCewePrometer(), "103701"),  /* VZ-1 */
            new ProRegister(getCewePrometer(), "103702"),  /* VZ-2 */
            new ProRegister(getCewePrometer(), "103703"),  /* VZ-3 */
            new ProRegister(getCewePrometer(), "103704"),  /* VZ-4 */
            new ProRegister(getCewePrometer(), "103705"),  /* VZ-5 */
            new ProRegister(getCewePrometer(), "103706"),  /* VZ-6 */
            new ProRegister(getCewePrometer(), "103707"),  /* VZ-7 */
            new ProRegister(getCewePrometer(), "103708"),  /* VZ-8 */
            new ProRegister(getCewePrometer(), "103709"),  /* VZ-9 */
            new ProRegister(getCewePrometer(), "10370A"),  /* VZ-10*/
            new ProRegister(getCewePrometer(), "10370B"),  /* VZ-11*/
            new ProRegister(getCewePrometer(), "10370C"),  /* VZ-12*/
            new ProRegister(getCewePrometer(), "10370D"),  /* VZ-13*/
        };

        this.rEenergy = new ProRegister[] {
            new ProRegister(getCewePrometer(), "100800"), /* 255 */
            new ProRegister(getCewePrometer(), "103800"), /* VZ  */
            new ProRegister(getCewePrometer(), "103801"), /* VZ-1 */
            new ProRegister(getCewePrometer(), "103802"), /* VZ-2 */
            new ProRegister(getCewePrometer(), "103803"), /* VZ-3 */
            new ProRegister(getCewePrometer(), "103804"), /* VZ-4 */
            new ProRegister(getCewePrometer(), "103805"), /* VZ-5 */
            new ProRegister(getCewePrometer(), "103806"), /* VZ-6 */
            new ProRegister(getCewePrometer(), "103807"), /* VZ-7 */
            new ProRegister(getCewePrometer(), "103808"), /* VZ-8 */
            new ProRegister(getCewePrometer(), "103809"), /* VZ-9 */
            new ProRegister(getCewePrometer(), "10380A"), /* VZ-10*/
            new ProRegister(getCewePrometer(), "10380B"), /* VZ-11*/
            new ProRegister(getCewePrometer(), "10380C"), /* VZ-12*/
            new ProRegister(getCewePrometer(), "10380D"), /* VZ-13*/
        };

        /** Historical external registers (8 floats) */
        this.rExternal = new ProRegister[] {
            new ProRegister(getCewePrometer(), "10A100"), /*  255 */
            new ProRegister(getCewePrometer(), "103900"), /* VZ  */
            new ProRegister(getCewePrometer(), "103901"), /* VZ-1 */
            new ProRegister(getCewePrometer(), "103902"), /* VZ-2 */
            new ProRegister(getCewePrometer(), "103903"), /* VZ-3 */
            new ProRegister(getCewePrometer(), "103904"), /* VZ-4 */
            new ProRegister(getCewePrometer(), "103905"), /* VZ-5 */
            new ProRegister(getCewePrometer(), "103906"), /* VZ-6 */
            new ProRegister(getCewePrometer(), "103907"), /* VZ-7 */
            new ProRegister(getCewePrometer(), "103908"), /* VZ-8 */
            new ProRegister(getCewePrometer(), "103909"), /* VZ-9 */
            new ProRegister(getCewePrometer(), "10390A"), /* VZ-10*/
            new ProRegister(getCewePrometer(), "10390B"), /* VZ-11*/
            new ProRegister(getCewePrometer(), "10390C"), /* VZ-12*/
            new ProRegister(getCewePrometer(), "10390D"), /* VZ-13*/
        };

        this.md = new String[][] {
            /*               MD 0,     MD 1,     MD 2,     MD 3,     MD 4,     MD 5,     MD 6,     MD 7              */
            new String[] { "106500", "106501", "106502", "106503", "106504", "106505", "106506", "106507" }, /* 255  */
            new String[] { "104000", "104100", "104200", "104300", "104400", "104500", "104600", "104700" }, /* VZ   */
            new String[] { "104001", "104101", "104201", "104301", "104401", "104501", "104601", "104701" }, /* VZ-1 */
            new String[] { "104002", "104102", "104202", "104302", "104402", "104502", "104602", "104702" }, /* VZ-2 */
            new String[] { "104003", "104103", "104203", "104303", "104403", "104503", "104603", "104703" }, /* VZ-3 */
            new String[] { "104004", "104104", "104204", "104304", "104404", "104504", "104604", "104704" }, /* VZ-4 */
            new String[] { "104005", "104105", "104205", "104305", "104405", "104505", "104605", "104705" }, /* VZ-5 */
            new String[] { "104006", "104106", "104206", "104306", "104406", "104506", "104606", "104706" }, /* VZ-6 */
            new String[] { "104007", "104107", "104207", "104307", "104407", "104507", "104607", "104707" }, /* VZ-7 */
            new String[] { "104008", "104108", "104208", "104308", "104408", "104508", "104608", "104708" }, /* VZ-8 */
            new String[] { "104009", "104109", "104209", "104309", "104409", "104509", "104609", "104709" }, /* VZ-9 */
            new String[] { "10400A", "10410A", "10420A", "10430A", "10440A", "10450A", "10460A", "10470A" }, /* VZ-10*/
            new String[] { "10400B", "10410B", "10420B", "10430B", "10440B", "10450B", "10460B", "10470B" }, /* VZ-11*/
            new String[] { "10400C", "10410C", "10420C", "10430C", "10440C", "10450C", "10460C", "10470C" }, /* VZ-12*/
            new String[] { "10400D", "10410D", "10420D", "10430D", "10440D", "10450D", "10460D", "10470D" }  /* VZ-13*/
        };

        this.tou = new String[][] {
            /*                tou 1,     tou 2,   tou 3,    tou 4,    tou 5,    tou 6,    tou 7,    tou 8            */
            new String[] { "10D100", "10D101", "10D102", "10D103", "10D104", "10D105", "10D106", "10D107" }, /* 255  */
            new String[] { "105000", "105100", "105200", "105300", "105400", "105500", "105600", "105700" }, /* VZ   */
            new String[] { "105001", "105101", "105201", "105301", "105401", "105501", "105601", "105701" }, /* VZ-1 */
            new String[] { "105002", "105102", "105202", "105302", "105402", "105502", "105602", "105702" }, /* VZ-2 */
            new String[] { "105003", "105103", "105203", "105303", "105403", "105503", "105603", "105703" }, /* VZ-3 */
            new String[] { "105004", "105105", "105204", "105304", "105404", "105504", "105604", "105704" }, /* VZ-4 */
            new String[] { "105005", "105105", "105205", "105305", "105405", "105505", "105605", "105705" }, /* VZ-5 */
            new String[] { "105006", "105106", "105206", "105306", "105406", "105506", "105606", "105706" }, /* VZ-6 */
            new String[] { "105007", "105107", "105207", "105307", "105407", "105507", "105607", "105707" }, /* VZ-7 */
            new String[] { "105008", "105108", "105208", "105308", "105408", "105508", "105608", "105708" }, /* VZ-8 */
            new String[] { "105009", "105109", "105209", "105309", "105409", "105509", "105609", "105709" }, /* VZ-9 */
            new String[] { "10500A", "10510A", "10520A", "10530A", "10540A", "10550A", "10560A", "10570A" }, /* VZ-10*/
            new String[] { "10500B", "10510B", "10520B", "10530B", "10540B", "10550B", "10560B", "10570B" }, /* VZ-11*/
            new String[] { "10500C", "10510C", "10520C", "10530C", "10540C", "10550C", "10560C", "10570C" }, /* VZ-12*/
            new String[] { "10500D", "10510D", "10520D", "10530D", "10540D", "10550D", "10560D", "10570D" }  /* VZ-13*/
        };

        rMaximumDemand = new ProRegister[md.length][];
        for( int row = 0; row < md.length; row ++ ) {
            rMaximumDemand[row] = new ProRegister[md[row].length];
            for( int col = 0; col < md[row].length; col ++ ) {
                rMaximumDemand[row][col] = new ProRegister(getCewePrometer(), md[row][col]);
            }
        }

        rTou = new ProRegister[tou.length][];
        for( int row = 0; row < tou.length; row ++ ) {
            rTou[row] = new ProRegister[tou[row].length];
            for( int col = 0; col < tou[row].length; col ++ ) {
                rTou[row][col] = new ProRegister(getCewePrometer(), tou[row][col]);
            }
        }

        this.rLogChannelCount = new ProRegister[] {
            new ProRegister(getCewePrometer(), "100D00", true),                  // count log 1
            new ProRegister(getCewePrometer(), "101300", true)                   // count log 2
        };

        this.rLogChannelInterval = new ProRegister[] {
            new ProRegister(getCewePrometer(), "100E00", true),                  // interval 1
            new ProRegister(getCewePrometer(), "101400", true)                   // interval 2
        };

        this.rLogOffset = new ProRegister[] {
            new ProRegister(getCewePrometer(), "101100", true),                  // offset log 1
            new ProRegister(getCewePrometer(), "101700", true)                   // offset log 2
        };

        this.rLogChannelConfigOld = new ProRegister[] {
            new ProRegister(getCewePrometer(), "100F00"),                        // log 1 config for fw < 2.1.0
            new ProRegister(getCewePrometer(), "101500")                         // log 2 config for fw < 2.1.0
        };

        this.rLogChannelConfigNew = new ProRegister[2][50];                      // log config for fw >= 2.1.0
        for (int logger = 0; logger < rLogChannelConfigNew.length; logger++) {
            for (int channel = 0; channel < rLogChannelConfigNew[logger].length; channel++) {
                Long address = Long.valueOf("114000", 16);
                address += logger;
                address += channel * 256;
                String registerAddress = ProtocolTools.addPaddingAndClip(Long.toHexString(address), '0', 6, false);
                rLogChannelConfigNew[logger][channel] = new ProRegister(getCewePrometer(), registerAddress.toUpperCase(), true);
            }
        }

        this.rLogNextRecord = new ProRegister[] {
                new ProRegister(getCewePrometer(), "101200", false, 16),             // read log 1
                new ProRegister(getCewePrometer(), "101800", false, 16)              // read log 2
        };

        this.rEventLogReadOffset = new ProRegister(getCewePrometer(), "102100", false);
        this.rEventLogNextEvent = new ProRegister(getCewePrometer(), "102200", false);

    }

    public CewePrometer getCewePrometer() {
        return cewePrometer;
    }

    public String[][] getMd() {
        return md;
    }

    public ProRegister[] getrEenergy() {
        return rEenergy;
    }

    public ProRegister getrEventLogNextEvent() {
        return rEventLogNextEvent;
    }

    public ProRegister getrEventLogReadOffset() {
        return rEventLogReadOffset;
    }

    public ProRegister[] getrExternal() {
        return rExternal;
    }

    public ProRegister getrFirmwareVersionOld() {
        return rFirmwareVersionOld;
    }

    public ProRegister getrFirmwareVersionNew() {
        return rFirmwareVersionNew;
    }

    public ProRegister[] getrLogChannelConfigOld() {
        return rLogChannelConfigOld;
    }

    public ProRegister[][] getrLogChannelConfigNew() {
        return rLogChannelConfigNew;
    }

    public ProRegister[] getrLogChannelCount() {
        return rLogChannelCount;
    }

    public ProRegister[] getrLogChannelInterval() {
        return rLogChannelInterval;
    }

    public ProRegister[] getrLogNextRecord() {
        return rLogNextRecord;
    }

    public ProRegister[] getrLogOffset() {
        return rLogOffset;
    }

    public ProRegister[][] getrMaximumDemand() {
        return rMaximumDemand;
    }

    public ProRegister getrReadDate() {
        return rReadDate;
    }

    public ProRegister getrSerial() {
        return rSerial;
    }

    public ProRegister getrSlideTime() {
        return rSlideTime;
    }

    public ProRegister[] getrTimestamp() {
        return rTimestamp;
    }

    public ProRegister[][] getrTou() {
        return rTou;
    }

    public ProRegister getrTouRegisterSelect() {
        return rTouRegisterSelect;
    }

    public String[][] getTou() {
        return tou;
    }

    public ProRegister getrVoltageUnbalance() {
        return rVoltageUnbalance;
    }

    public ProRegister getrInstantValues() {
        return rInstantValues;
    }

    public ProRegister getrBatteryLeft() {
        return rBatteryLeft;
    }

    public ProRegister getrGeneralInfo(int i) {
        return rGeneralInfo[i];
    }

    public ProRegister getrInternalTemp() {
        return rInternalTemp;
    }
}
