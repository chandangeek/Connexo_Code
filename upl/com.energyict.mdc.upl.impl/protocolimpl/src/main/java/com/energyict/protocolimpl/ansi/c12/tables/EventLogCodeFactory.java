/*
 * EventLogCodeFactory.java
 *
 * Created on 18 november 2005, 13:50
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author Koen
 */
abstract public class EventLogCodeFactory {
    
    static List stdList = new ArrayList();
    static List mfgList = new ArrayList();
    
    static {
        // C12.19
        stdList.add(new EventLogCode(0, "No event",""));
        stdList.add(new EventLogCode(1, "Primary power down","",MeterEvent.POWERDOWN));
        stdList.add(new EventLogCode(2, "Primary power up","",MeterEvent.POWERUP));
        stdList.add(new EventLogCode(3, "Time changed (old time)","None. Time tag if used equals old time.",MeterEvent.SETCLOCK_BEFORE));
        stdList.add(new EventLogCode(4, "Time changed (new time)","None. Time tag if used equals new time.",MeterEvent.SETCLOCK_AFTER));
        stdList.add(new EventLogCode(5, "Time changed (old time)","Old time in STIME_DATE format",MeterEvent.SETCLOCK_BEFORE));
        stdList.add(new EventLogCode(6, "Time changed (new time)","New time in STIME_DATE format",MeterEvent.SETCLOCK_AFTER));
        stdList.add(new EventLogCode(7, "End device accessed for read",""));
        stdList.add(new EventLogCode(8, "End device accessed for write",""));
        stdList.add(new EventLogCode(9, "Procedure invoked","Bit field of procedure number and standard/manufacturer flag (UINT16) TABLE_IDC_FIELD format"));
        stdList.add(new EventLogCode(10, "Table written to","Bit field of table number and standard/manufacturer flag (UINT16) TABLE_IDA_FIELD format"));
        stdList.add(new EventLogCode(11, "End device programmed",""));
        stdList.add(new EventLogCode(12, "Communication terminated normally",""));
        stdList.add(new EventLogCode(13, "Communication terminated abnormally",""));
        stdList.add(new EventLogCode(14, "Reset list pointers","LIST (INT8) {Reference procedure 4,9.1.8.1.5}"));
        stdList.add(new EventLogCode(15, "Update list pointers","LIST (UINT8) {Reference procedure 5,9.1.8.1.6}"));
        stdList.add(new EventLogCode(16, "History log cleared","",MeterEvent.CLEAR_DATA));
        stdList.add(new EventLogCode(17, "History log pointers updated","Value of procedure parameter (UINT16)"));
        stdList.add(new EventLogCode(18, "Event log cleared",""));
        stdList.add(new EventLogCode(19, "History log pointers updated","Value of procedure parameter (UINT16)"));
        stdList.add(new EventLogCode(20, "Demand reset occurred","",MeterEvent.BILLING_ACTION));
        stdList.add(new EventLogCode(21, "Self read occurred",""));
        stdList.add(new EventLogCode(22, "Daylight Savings Time On",""));
        stdList.add(new EventLogCode(23, "Daylight Savings Time Off",""));
        stdList.add(new EventLogCode(24, "Season change","New season number (UINT8)"));
        stdList.add(new EventLogCode(25, "Rate change","New rate (UINT8)"));
        stdList.add(new EventLogCode(26, "Special schedule activation","New special schedule (UINT8)"));
        stdList.add(new EventLogCode(27, "Tier switch change","New current tier (UINT8) followed by new demand tier (UINT8)"));
        stdList.add(new EventLogCode(28, "Pending table activation","Table number (TABLE_IDA_BFLD)"));
        stdList.add(new EventLogCode(29, "Pending table clear","Table number (TABLE_IDA_BFLD). Table removed from end device prior to activation."));
        
        // C12.21 addendum
        stdList.add(new EventLogCode(30, "Metering mode started",""));
        stdList.add(new EventLogCode(31, "Metering mode stopped",""));
        stdList.add(new EventLogCode(32, "Test mode started",""));
        stdList.add(new EventLogCode(33, "Test mode stopped",""));
        stdList.add(new EventLogCode(34, "Meter shop mode started",""));
        stdList.add(new EventLogCode(35, "Meter shop mode stopped",""));
        stdList.add(new EventLogCode(36, "Meter reprogrammed","",MeterEvent.CONFIGURATIONCHANGE));
        stdList.add(new EventLogCode(37, "Configuration error detected",""));
        stdList.add(new EventLogCode(38, "Self check error detected",""));
        stdList.add(new EventLogCode(39, "RAM failure detected","",MeterEvent.RAM_MEMORY_ERROR));
        stdList.add(new EventLogCode(40, "ROM failure detected","",MeterEvent.ROM_MEMORY_ERROR));
        stdList.add(new EventLogCode(41, "Nonvolatile memory failure detected","",MeterEvent.ROM_MEMORY_ERROR));
        stdList.add(new EventLogCode(42, "Clock error detected",""));
        stdList.add(new EventLogCode(43, "Measurement error detected",""));
        stdList.add(new EventLogCode(44, "Low battery detected",""));
        stdList.add(new EventLogCode(45, "Low loss potential detected",""));
        stdList.add(new EventLogCode(46, "Demand overload detected","",MeterEvent.REGISTER_OVERFLOW));
        stdList.add(new EventLogCode(47, "Tamper attempt detected",""));
        stdList.add(new EventLogCode(48, "Reverse rotation detected",""));
        stdList.add(new EventLogCode(32, "Test mode started","", MeterEvent.TEST_MODE_START));
        stdList.add(new EventLogCode(33, "Test mode stopped","", MeterEvent.TEST_MODE_STOP));
    }
    
    /** Creates a new instance of EventLogCodeFactory */
    public EventLogCodeFactory() {
    
    }
    
    static public List getMfgList() {
       return mfgList;    
    }
    
    
    public int getEICode(int code, boolean manufacturer) {
        return findEventLogCode(code,manufacturer).getEiCode();
    }
    
    public String getEvent(int code, boolean manufacturer) {
        return findEventLogCode(code,manufacturer).getEvent();
    } 
    
    public String getArgument(int code, boolean manufacturer) {
        return findEventLogCode(code,manufacturer).getArgument();
    } 
    
    private EventLogCode findEventLogCode(int code, boolean manufacturer) {
        if (manufacturer) {
            Iterator it = mfgList.iterator();
            while(it.hasNext()) {
                EventLogCode elc = (EventLogCode)it.next();
                if (elc.getCode() == code) return elc;
            }
            return new EventLogCode(code, "Unknown manufacturer log code! Code:" + code,"");
        }
        else {
            Iterator it = stdList.iterator();
            while(it.hasNext()) {
                EventLogCode elc = (EventLogCode)it.next();
                if (elc.getCode() == code) return elc;
            }
            return new EventLogCode(code, "Unknown standard log code! Code:" + code,"");
        }
    }
    
    
}
