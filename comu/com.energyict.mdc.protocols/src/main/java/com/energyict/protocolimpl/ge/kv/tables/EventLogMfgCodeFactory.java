/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EventLogMfgCodeFactory.java
 *
 * Created on 22 november 2005, 11:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv.tables;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.ansi.c12.tables.EventLogCode;
import com.energyict.protocolimpl.ansi.c12.tables.EventLogCodeFactory;
/**
 *
 * @author Koen
 */
public class EventLogMfgCodeFactory extends EventLogCodeFactory {


    static {
        getMfgList().add(new EventLogCode(0,"Diagnostic 1 - Polarity, Cross Phase, Reverse Energy Flow","Angle out of tolerance (phase B or C voltage, phase A, B, or C current)"));
        getMfgList().add(new EventLogCode(1,"Diagnostic 1 Condition Cleared",""));
        getMfgList().add(new EventLogCode(2,"Diagnostic 2 - Voltage Imbalance","Voltage out of tolerance (phase B or C)",MeterEvent.PHASE_FAILURE));
        getMfgList().add(new EventLogCode(3,"Diagnostic 2 -Condition Cleared",""));
        getMfgList().add(new EventLogCode(4,"Diagnostic 3 - Inactive Phase Current","Current out of tolerance (phase A, B, or C)",MeterEvent.PHASE_FAILURE));
        getMfgList().add(new EventLogCode(5,"Diagnostic 3 Condition Cleared",""));
        getMfgList().add(new EventLogCode(6,"Diagnostic 4 - Phase Angle Alert","Angle out of tolerance (phase B or C voltage, phase A, B, or C current -- see table 3 below)"));
        getMfgList().add(new EventLogCode(7,"Diagnostic 4 Condition Cleared",""));
        getMfgList().add(new EventLogCode(8,"Diagnostic 5 - High Distortion","Bit 0 = phase ABit 1 = phase BBit 2 = phase CBit 6 = total"));
        getMfgList().add(new EventLogCode(9,"Diagnostic 5 -Condition Cleared",""));
        getMfgList().add(new EventLogCode(10,"Diagnostic 6 - Under Voltage, Phase A","Phase A voltage (see table 3 below)",MeterEvent.VOLTAGE_SAG));
        getMfgList().add(new EventLogCode(11,"Diagnostic 6 Condition Cleared",""));
        getMfgList().add(new EventLogCode(12,"Diagnostic 7 - Over Voltage, Phase A","Phase A voltage (see table 3 below)",MeterEvent.VOLTAGE_SWELL));
        getMfgList().add(new EventLogCode(13,"Diagnostic 7 Condition Cleared",""));
        getMfgList().add(new EventLogCode(14,"Diagnostic 8 - High Neutral Current",""));
        getMfgList().add(new EventLogCode(15,"Diagnostic 8 -Condition Cleared",""));
        getMfgList().add(new EventLogCode(16,"Caution 000400 - Under Voltage","Voltage out of tolerance (phase A, B, or C -- see table 3 below)",MeterEvent.VOLTAGE_SAG));
        getMfgList().add(new EventLogCode(17,"Caution 000400 Condition Cleared",""));
        getMfgList().add(new EventLogCode(18,"Caution 004000 - Demand Overload",""));
        getMfgList().add(new EventLogCode(19,"Caution 004000 Condition Cleared",""));
        getMfgList().add(new EventLogCode(20,"Caution 400000 - Received kWh",""));
        getMfgList().add(new EventLogCode(21,"Caution 400000 Condition Cleared",""));
        getMfgList().add(new EventLogCode(22,"Caution 040000 - Leading kvarh",""));
        getMfgList().add(new EventLogCode(23,"Caution 040000 Condition Cleared",""));
        getMfgList().add(new EventLogCode(24,"Real Time Pricing Activation",""));
        getMfgList().add(new EventLogCode(25,"Real Time Pricing Deactivation",""));
        getMfgList().add(new EventLogCode(26,"Test Mode Activation",""));
        getMfgList().add(new EventLogCode(27,"Test Mode Deactivation",""));
        getMfgList().add(new EventLogCode(28,"Calibration Mode Activated(kV meter does not advance time or date during calibration, so there is no need to record deactivation time)",""));
        getMfgList().add(new EventLogCode(29,"Self-Read",""));
    }

    /** Creates a new instance of EventLogMfgCodeFactory */
    public EventLogMfgCodeFactory() {
    }

}
