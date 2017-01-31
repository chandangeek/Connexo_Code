/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeterEventParser {

    /**
     * Event code:
     * <p/>
     * 0   0   0   0   0   0   0   0
     * |   |   |   |   |   |   |   1		new interval because of power-down
     * |   |   |   |   |   |   |   2		new interval because of power-up and variable changed by setting
     * |   |   |   |   |   |   |   4		new time/date or daylight savings switch
     * |   |   |   |   |   |   |   8		new interval because of demand reset and 1-phase or 2-phase power outage
     * |   |   |   |   |   |   1		season change, i.e. dst switch (VDEW) and system reverse energy flow
     * |   |   |   |   |   |   2		values not reliable
     * |   |   |   |   |   |   4		carry over error (copy of errcovr, syserr)
     * |   |   |   |   |   |   8		fatal error ('OR' of some syserr flags)
     * |   |   |   |   |   1			input 2 event detected
     * |   |   |   |   |   2			load profile initialised
     * |   |   |   |   |   4			logbook initialised
     * |   |   |   |   |   8			input 1 event detected
     * |   |   |   |   1			reverse power in 1 or 2 phases detected
     * |   |   |   |   2			error or warning off
     * |   |   |   |   4			error or warning on ('OR' of syserr and syswarn flags)
     * |   |   |   |   8			variable changed by setting
     * |   |   |   |
     * |   |   |   1			phase L3 is missing
     * |   |   |   2			phase L2 is missing
     * |   |   |   4			phase L1 is missing
     * |   |   |   8			contactor switched off
     * |   |   1			wrong password was used
     * |   |   2			main cover is or was opened
     * |   |   4			terminal cover is or was opened
     * |   |   8			change of Impuls constant
     * |   |
     * |   1				demand rate 1
     * |   2				demand rate 2
     * |   4				demand rate 3
     * |   8				demand rate 4
     * 1				Binary coded energy rate (00 = T1, 10 = T2, 01 = T3, 11 = T4)
     * 2
     * 4				Binary coded season (00 = S1, 10 = S2, 01 = S3, 11 = S4)
     * 8
     */

    public static List<MeterEvent> parseEventCode(Date date, int meterEventCode) {


        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        if ((meterEventCode & 0x1) == 0x1) {
            meterEvents.add(new MeterEvent(date, MeterEvent.POWERDOWN, 0x1, "new interval because of power-down"));
        }
        if ((meterEventCode & 0x2) == 0x2) {
            meterEvents.add(new MeterEvent(date, MeterEvent.POWERUP, 0x2, "new interval because of power-up and variable changed by setting"));
        }
        if ((meterEventCode & 0x4) == 0x4) {
            meterEvents.add(new MeterEvent(date, MeterEvent.SETCLOCK, 0x4, "new time/date or daylight savings switch"));
        }
        if ((meterEventCode & 0x8) == 0x8) {
            meterEvents.add(new MeterEvent(date, MeterEvent.BILLING_ACTION, 0x8, "new interval because of demand reset and 1-phase or 2-phase power outage"));
        }

        if ((meterEventCode & 0x10) == 0x10) {
            meterEvents.add(new MeterEvent(date, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, 0x10, "season change, i.e. dst switch (VDEW) and system reverse energy flow"));
        }
        if ((meterEventCode & 0x20) == 0x20) {
            meterEvents.add(new MeterEvent(date, MeterEvent.MEASUREMENT_SYSTEM_ERROR, 0x20, "values not reliable"));
        }
        if ((meterEventCode & 0x40) == 0x40) {
            meterEvents.add(new MeterEvent(date, MeterEvent.MEASUREMENT_SYSTEM_ERROR, 0x40, "carry over error (copy of errcovr, syserr)"));
        }
        if ((meterEventCode & 0x80) == 0x80) {
            meterEvents.add(new MeterEvent(date, MeterEvent.FATAL_ERROR, 0x80, "fatal error ('OR' of some syserr flags)"));
        }

        if ((meterEventCode & 0x100) == 0x100) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x100, "input 2 event detected"));
        }
        if ((meterEventCode & 0x200) == 0x200) {
            meterEvents.add(new MeterEvent(date, MeterEvent.LOADPROFILE_CLEARED, 0x200, "load profile initialised"));
        }
        if ((meterEventCode & 0x400) == 0x400) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x400, "logbook initialised"));
        }
        if ((meterEventCode & 0x800) == 0x800) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x800, "input 1 event detected"));
        }

        if ((meterEventCode & 0x1000) == 0x1000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.REVERSE_RUN, 0x1000, "reverse power in 1 or 2 phases detected"));
        }
        if ((meterEventCode & 0x2000) == 0x2000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x2000, "error or warning off"));
        }
        if ((meterEventCode & 0x4000) == 0x4000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x4000, "error or warning on ('OR' of syserr and syswarn flags)"));
        }
        if ((meterEventCode & 0x8000) == 0x8000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x8000, "variable changed by setting"));
        }

        if ((meterEventCode & 0x10000) == 0x10000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 0x10000, "phase L3 is missing"));
        }
        if ((meterEventCode & 0x20000) == 0x20000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 0x20000, "phase L2 is missing"));
        }
        if ((meterEventCode & 0x40000) == 0x40000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.PHASE_FAILURE, 0x40000, "phase L1 is missing"));
        }
        if ((meterEventCode & 0x80000) == 0x80000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.REMOTE_DISCONNECTION, 0x80000, "contactor switched off"));
        }

        if ((meterEventCode & 0x100000) == 0x100000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x100000, "wrong password was used"));
        }
        if ((meterEventCode & 0x200000) == 0x200000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.COVER_OPENED, 0x200000, "main cover is or was opened"));
        }
        if ((meterEventCode & 0x400000) == 0x400000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.TERMINAL_OPENED, 0x400000, "terminal cover is or was opened"));
        }
        if ((meterEventCode & 0x800000) == 0x800000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x800000, "change of Impuls constant"));
        }

        StringBuffer sb = new StringBuffer();
        if ((meterEventCode & 0x1000000) == 0x1000000) {
            sb = append(sb, "demand rate 1");
        }
        if ((meterEventCode & 0x2000000) == 0x2000000) {
            sb = append(sb, "demand rate 2");
        }
        if ((meterEventCode & 0x4000000) == 0x4000000) {
            sb = append(sb, "demand rate 3");
        }
        if ((meterEventCode & 0x8000000) == 0x8000000) {
            sb = append(sb, "demand rate 4");
        }

        if ((meterEventCode & 0x30000000) == 0x00000000) {
            sb = append(sb, "energy rate T1");
        }
        if ((meterEventCode & 0x30000000) == 0x20000000) {
            sb = append(sb, "energy rate T2");
        }
        if ((meterEventCode & 0x30000000) == 0x10000000) {
            sb = append(sb, "energy rate T3");
        }
        if ((meterEventCode & 0x30000000) == 0x30000000) {
            sb = append(sb, "energy rate T4");
        }

        if ((meterEventCode & 0xC0000000) == 0x00000000) {
            sb = append(sb, "season S1");
        }
        if ((meterEventCode & 0xC0000000) == 0x80000000) {
            sb = append(sb, "season S2");
        }
        if ((meterEventCode & 0xC0000000) == 0x40000000) {
            sb = append(sb, "season S3");
        }
        if ((meterEventCode & 0xC0000000) == 0xC0000000) {
            sb = append(sb, "season S4");
        }

        if (sb.length() > 0) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, 0x1000000, sb.toString()));   //Extra information: current demand rate, tariff, season
        }
        return meterEvents;
    }

    private static StringBuffer append(StringBuffer sb, String text) {
        if (sb.length() > 0) {
            sb.append(", ");    //Separator
        }
        return sb.append(text);
    }
}