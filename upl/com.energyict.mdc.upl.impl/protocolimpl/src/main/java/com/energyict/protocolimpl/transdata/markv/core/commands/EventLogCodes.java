/*
 * EventLogCodes.java
 *
 * Created on 12 augustus 2005, 9:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.io.*;
import java.util.*;
import com.energyict.protocol.MeterEvent;
/**
 *
 * @author koen
 */
public class EventLogCodes {
    
    static Map map = new HashMap();
    
    static {
        
        map.put(new Integer(0x01),new EventLogCodeMapping("Voltage Sag - Phase A",MeterEvent.VOLTAGE_SAG));
        map.put(new Integer(0x02),new EventLogCodeMapping("Voltage Sag - Phase C",MeterEvent.VOLTAGE_SAG)); 
        map.put(new Integer(0x03),new EventLogCodeMapping("Voltage Sag - Phase B",MeterEvent.VOLTAGE_SAG)); 
        map.put(new Integer(0x04),new EventLogCodeMapping("Voltage Swell - Phase A",MeterEvent.VOLTAGE_SWELL));
        map.put(new Integer(0x05),new EventLogCodeMapping("Voltage Swell - Phase C",MeterEvent.VOLTAGE_SWELL));
        map.put(new Integer(0x06),new EventLogCodeMapping("Voltage Swell - Phase B",MeterEvent.VOLTAGE_SWELL));
        map.put(new Integer(0x20),new EventLogCodeMapping("Voltage Outage - Phase A",MeterEvent.PHASE_FAILURE)); 
        map.put(new Integer(0x21),new EventLogCodeMapping("Voltage Outage - Phase C",MeterEvent.PHASE_FAILURE)); 
        map.put(new Integer(0x22),new EventLogCodeMapping("Voltage Outage - Phase B",MeterEvent.PHASE_FAILURE)); 
        map.put(new Integer(0x26),new EventLogCodeMapping("Breaker Operation-Phase A",MeterEvent.OTHER));
        map.put(new Integer(0x27),new EventLogCodeMapping("Breaker Operation-Phase C",MeterEvent.OTHER));     
        map.put(new Integer(0x28),new EventLogCodeMapping("Breaker Operation-Phase B",MeterEvent.OTHER));     
        map.put(new Integer(0x30),new EventLogCodeMapping("Volt. Angle Outside Limit-Phase A",MeterEvent.OTHER)); 
        map.put(new Integer(0x31),new EventLogCodeMapping("Volt. Angle Outside Limit-Phase C",MeterEvent.OTHER));  
        map.put(new Integer(0x32),new EventLogCodeMapping("Volt. Angle Outside Limit-Phase B",MeterEvent.OTHER)); 
        map.put(new Integer(0x33),new EventLogCodeMapping("Current Angle Outside Limit-Ph. A",MeterEvent.OTHER));  
        map.put(new Integer(0x34),new EventLogCodeMapping("Current Angle Outside Limit-Ph. C",MeterEvent.OTHER));
        map.put(new Integer(0x35),new EventLogCodeMapping("Current Angle Outside Limit-Ph. B",MeterEvent.OTHER)); 
        map.put(new Integer(0x36),new EventLogCodeMapping("Cur. Below Limit-Phase A",MeterEvent.OTHER));    
        map.put(new Integer(0x37),new EventLogCodeMapping("Cur. Below Limit-Phase C",MeterEvent.OTHER));     
        map.put(new Integer(0x38),new EventLogCodeMapping("Cur. Below Limit-Phase B",MeterEvent.OTHER)); 
        map.put(new Integer(0x39),new EventLogCodeMapping("PF Outside Limit -Phase A",MeterEvent.OTHER)); 
        map.put(new Integer(0x3A),new EventLogCodeMapping("PF Outside Limit -Phase C",MeterEvent.OTHER)); 
        map.put(new Integer(0x3B),new EventLogCodeMapping("PF Outside Limit -Phase B",MeterEvent.OTHER));  
        map.put(new Integer(0x50),new EventLogCodeMapping("Ext. Input 1 Open (High)",MeterEvent.OTHER));
        map.put(new Integer(0x51),new EventLogCodeMapping("Ext. Input 1 Closed (Low)",MeterEvent.OTHER));
        map.put(new Integer(0x52),new EventLogCodeMapping("Ext. Input 2 Open (High)",MeterEvent.OTHER));
        map.put(new Integer(0x53),new EventLogCodeMapping("Ext. Input 2 Closed (Low)",MeterEvent.OTHER));
        map.put(new Integer(0x60),new EventLogCodeMapping("Control Output 1 Open",MeterEvent.OTHER));
        map.put(new Integer(0x61),new EventLogCodeMapping("Control Output 1 Closed",MeterEvent.OTHER));
        map.put(new Integer(0x62),new EventLogCodeMapping("Control Output 2 Open",MeterEvent.OTHER));
        map.put(new Integer(0x63),new EventLogCodeMapping("Control Output 2 Closed",MeterEvent.OTHER));
        map.put(new Integer(0x64),new EventLogCodeMapping("Control Output 3 Open",MeterEvent.OTHER));
        map.put(new Integer(0x65),new EventLogCodeMapping("Control Output 3 Closed",MeterEvent.OTHER));
        map.put(new Integer(0x66),new EventLogCodeMapping("Control Output 4 Open",MeterEvent.OTHER));
        map.put(new Integer(0x67),new EventLogCodeMapping("Control Output 4 Closed",MeterEvent.OTHER));
        map.put(new Integer(0x68),new EventLogCodeMapping("Control Output 5 Open",MeterEvent.OTHER));
        map.put(new Integer(0x69),new EventLogCodeMapping("Control Output 5 Closed",MeterEvent.OTHER));
        map.put(new Integer(0x70),new EventLogCodeMapping("Battery Time Over Limit",MeterEvent.OTHER));
        map.put(new Integer(0x71),new EventLogCodeMapping("EPROM Diagnostics Error",MeterEvent.ROM_MEMORY_ERROR));
        map.put(new Integer(0x72),new EventLogCodeMapping("RAM Diagnostics Error",MeterEvent.RAM_MEMORY_ERROR));
        map.put(new Integer(0x73),new EventLogCodeMapping("Register WatchDog Tripped",MeterEvent.WATCHDOGRESET));
        map.put(new Integer(0x74),new EventLogCodeMapping("A/D WatchDog Activated",MeterEvent.WATCHDOGRESET));
        map.put(new Integer(0x75),new EventLogCodeMapping("Communications Error",MeterEvent.OTHER));
        map.put(new Integer(0x76),new EventLogCodeMapping("Dial Out Unsuccessful",MeterEvent.OTHER));
        map.put(new Integer(0x77),new EventLogCodeMapping("Meter Power Turned Off",MeterEvent.POWERDOWN));
        map.put(new Integer(0x78),new EventLogCodeMapping("Maximum Demand Reset",MeterEvent.OTHER));
        map.put(new Integer(0x79),new EventLogCodeMapping("Recorder Data Retrieved",MeterEvent.OTHER));
        map.put(new Integer(0x7A),new EventLogCodeMapping("Register Programmed",MeterEvent.OTHER));
        map.put(new Integer(0x7B),new EventLogCodeMapping("Time Changed",MeterEvent.SETCLOCK));
        map.put(new Integer(0x7C),new EventLogCodeMapping("Register Reading Changed",MeterEvent.OTHER));
        map.put(new Integer(0x7D),new EventLogCodeMapping("A/D Programmed or Re-Cal.",MeterEvent.OTHER));
        map.put(new Integer(0x7E),new EventLogCodeMapping("Test Mode Initiated",MeterEvent.TEST_MODE_START));
        map.put(new Integer(0x7F),new EventLogCodeMapping("Test Mode Exited",MeterEvent.TEST_MODE_STOP));
        map.put(new Integer(0x80),new EventLogCodeMapping("Day Light Savings Time",MeterEvent.OTHER));
        map.put(new Integer(0x81),new EventLogCodeMapping("Recorder Time Sync.",MeterEvent.OTHER));
        map.put(new Integer(0x82),new EventLogCodeMapping("TOU Season Change",MeterEvent.OTHER));
        map.put(new Integer(0x83),new EventLogCodeMapping("Register Rollover",MeterEvent.REGISTER_OVERFLOW));
        map.put(new Integer(0x84),new EventLogCodeMapping("Demand Register Overflow",MeterEvent.REGISTER_OVERFLOW));
        map.put(new Integer(0x85),new EventLogCodeMapping("Recorder Overflow",MeterEvent.OTHER));
        map.put(new Integer(0x86),new EventLogCodeMapping("Demand Over Limit - Start",MeterEvent.OTHER));
        map.put(new Integer(0x87),new EventLogCodeMapping("Demand Over Limit - End",MeterEvent.OTHER));
        map.put(new Integer(0x88),new EventLogCodeMapping("Harmonic Alarm - Start",MeterEvent.METER_ALARM));
        map.put(new Integer(0x89),new EventLogCodeMapping("Harmonic Alarm - End",MeterEvent.METER_ALARM));
        map.put(new Integer(0x8A),new EventLogCodeMapping("Event Log Data Retrieved",MeterEvent.OTHER));
        map.put(new Integer(0x8B),new EventLogCodeMapping("All Meter Data Reset",MeterEvent.CLEAR_DATA));
        map.put(new Integer(0x8C),new EventLogCodeMapping("Scheduled Dial Out Failure (SSR6000)",MeterEvent.HARDWARE_ERROR));
        map.put(new Integer(0x8D),new EventLogCodeMapping("Wireless No Activity Timeout (SSR6000)",MeterEvent.OTHER));
        map.put(new Integer(0x8E),new EventLogCodeMapping("Backup Battery Voltage Low",MeterEvent.OTHER));
        map.put(new Integer(0x8F),new EventLogCodeMapping("Backup Battery Acuated",MeterEvent.OTHER));
        map.put(new Integer(0x90),new EventLogCodeMapping("Power Restore Phase A",MeterEvent.OTHER));
        map.put(new Integer(0x91),new EventLogCodeMapping("Power Restore Phase C",MeterEvent.OTHER));
        map.put(new Integer(0x92),new EventLogCodeMapping("Power Restore Phase B",MeterEvent.OTHER));
        map.put(new Integer(0x93),new EventLogCodeMapping("Main Power Restored",MeterEvent.POWERUP));
        map.put(new Integer(0x9A),new EventLogCodeMapping("No Pulse Inputs Timeout (SSR6000)",MeterEvent.OTHER));
        map.put(new Integer(0x9B),new EventLogCodeMapping("No Pulse Outputs Timeout (SSR6000)",MeterEvent.OTHER));
        map.put(new Integer(0x9C),new EventLogCodeMapping("Zero Crossing Loss Detected (SSR6000)",MeterEvent.OTHER));
    }
    
    static public EventLogCodeMapping getEventLogMapping(int logCode) {
        EventLogCodeMapping elcm = (EventLogCodeMapping)map.get(new Integer(logCode));
        if (elcm==null) {
            return new EventLogCodeMapping("Invalid log code (not found in protocoldescription, "+logCode+")", MeterEvent.OTHER);
        }
        else return elcm;
        
    }

    /** Creates a new instance of EventLogCodes */
    private EventLogCodes() {
    }


    
}
