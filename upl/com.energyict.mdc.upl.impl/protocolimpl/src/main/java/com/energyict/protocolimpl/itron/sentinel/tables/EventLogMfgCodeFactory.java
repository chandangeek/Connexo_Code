package com.energyict.protocolimpl.itron.sentinel.tables;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.ansi.c12.tables.EventLogCode;
import com.energyict.protocolimpl.ansi.c12.tables.EventLogCodeFactory;

public class EventLogMfgCodeFactory extends EventLogCodeFactory {
    static {
        getMfgList().add(new EventLogCode(3,"Clear billing data","", MeterEvent.CLEAR_DATA));
        getMfgList().add(new EventLogCode(5,"DST time adjusted","New time", MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED));
        getMfgList().add(new EventLogCode(7,"Demand threshold exceeded","Threshold", MeterEvent.LIMITER_THRESHOLD_EXCEEDED));
        getMfgList().add(new EventLogCode(8,"Demand threshold restore","Threshold", MeterEvent.LIMITER_THRESHOLD_OK));
        getMfgList().add(new EventLogCode(9,"Self read pointers updated","Number of buffers"));
        getMfgList().add(new EventLogCode(10,"ANSI logon","UserID"));
        getMfgList().add(new EventLogCode(11,"New battery","", MeterEvent.REPLACE_BATTERY));
        getMfgList().add(new EventLogCode(12,"ANSI security successful","Security level"));
        getMfgList().add(new EventLogCode(13,"ANSI security failed",""));
        getMfgList().add(new EventLogCode(14,"Load profile cleared","", MeterEvent.CLEAR_DATA));
        getMfgList().add(new EventLogCode(15,"Load profile pointers updated","Number of items"));
        getMfgList().add(new EventLogCode(22,"Input channel hi","Channel"));
        getMfgList().add(new EventLogCode(23,"Input channel low","Channel"));
        getMfgList().add(new EventLogCode(26,"Option board event",""));
        getMfgList().add(new EventLogCode(29,"SiteScan error",""));
        getMfgList().add(new EventLogCode(30,"Voltage quality log pointers updated","Number of events"));
        getMfgList().add(new EventLogCode(31,"Voltage quality log nearly full",""));
        getMfgList().add(new EventLogCode(34,"ABC phase rotation active",""));
        getMfgList().add(new EventLogCode(35,"CBA phase rotation active",""));
        getMfgList().add(new EventLogCode(38,"Metrology communication error",""));
        getMfgList().add(new EventLogCode(39,"Metrology communication restore",""));
        getMfgList().add(new EventLogCode(40,"Clear voltage quality log","", MeterEvent.CLEAR_DATA));
        getMfgList().add(new EventLogCode(41,"TOU schedule error","Bit0: Season, Bit1: Year, Bit2, No clock"));
        getMfgList().add(new EventLogCode(42,"Mass memory error","", MeterEvent.PROGRAM_MEMORY_ERROR));
        getMfgList().add(new EventLogCode(43,"Loss of phase restore","", MeterEvent.PHASE_FAILURE));
        getMfgList().add(new EventLogCode(47,"Reverse power flow restore","", MeterEvent.REVERSE_RUN));
        getMfgList().add(new EventLogCode(49,"SS diag1 active",""));
        getMfgList().add(new EventLogCode(50,"SS diag2 active",""));
        getMfgList().add(new EventLogCode(51,"SS diag3 active","Bit0: Phase A,Bit 1: Phase B, Bit2: Phase C"));
        getMfgList().add(new EventLogCode(52,"SS diag4 active",""));
        getMfgList().add(new EventLogCode(53,"SS diag5 active","Phase"));
        getMfgList().add(new EventLogCode(54,"SS diag1 inactive",""));
        getMfgList().add(new EventLogCode(55,"SS diag2 inactive",""));
        getMfgList().add(new EventLogCode(56,"SS diag3 inactive",""));
        getMfgList().add(new EventLogCode(57,"SS diag4 inactive",""));
        getMfgList().add(new EventLogCode(58,"SS diag5 inactive","Phase"));
        getMfgList().add(new EventLogCode(59,"SS diag6 active",""));
        getMfgList().add(new EventLogCode(60,"SS diag6 inactive",""));
        getMfgList().add(new EventLogCode(61,"Self reads cleared",""));
    }
    
    /** Creates a new instance of EventLogMfgCodeFactory */
    public EventLogMfgCodeFactory() {
    }
}
