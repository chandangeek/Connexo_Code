/*
 * EventNumber.java
 *
 * Created on 18 oktober 2004, 14:41
 */

package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.protocol.MeterEvent;

import java.util.*;


/**
 *
 * @author  Koen
 */
public class EventNumber {
    
    static final int ALERT=0;
    static final int ERROR=1;
    static final int UNDEF=2;
    static final int ERRORALERT=3;
    
    static List events = new ArrayList();
    static {
        events.add(new EventNumber(2,"EnergyRegistersCleared","Indicates that tariff energy registers were cleared cleared (but not the total energy registers).",UNDEF));
        events.add(new EventNumber(3,"LoadStoredProfileCleared","Indicates that the load profile and/or the stored and/or stored      value profile was cleared.  value profile cleared",UNDEF));
        events.add(new EventNumber(5,"BatteryLow","Indicates that the battery voltage fell below a set threshold.",ALERT));
        events.add(new EventNumber(7,"BatteryOk","Indicates that the battery voltage returned to a level above a set threshold.",UNDEF));
        events.add(new EventNumber(8,"BillingPeriodReset","Indicates that a billing period reset has occurred. reset",ALERT));
        events.add(new EventNumber(9,"DSTSwitched","Indicates the change from and to daylight saving time enabled or time. The time stamp shows the time before the disabled change.",UNDEF));
        events.add(new EventNumber(10,"ClockAdjustedOldDateTime","Indicates that the date/time has been adjusted. (old date/time) The time that is stored in the event log is the old time before adjusting the time.",UNDEF));
        events.add(new EventNumber(11,"ClockAdjustedNewDateTime","Indicates that the date/time has been adjusted. (new date/time) The time that is stored in the event log is the new time after adjusting the time.",UNDEF));
        events.add(new EventNumber(13,"ControlInputStatusChanged","Indicates that the status of the input control signals inputs changed have changed (B21 and higher).",UNDEF));
        events.add(new EventNumber(17,"UndervoltageL1","Indicates that an undervoltage on phase 1 occurred.",UNDEF));
        events.add(new EventNumber(18,"UndervoltageL2","Indicates that an undervoltage on phase 2 occurred.",UNDEF));
        events.add(new EventNumber(19,"UndervoltageL3","Indicates that an undervoltage on phase 3 occurred.",UNDEF));
        events.add(new EventNumber(20,"OvervoltageL1","Indicates that an overvoltage on phase 1 occurred.",UNDEF));
        events.add(new EventNumber(21,"OvervoltageL2","Indicates that an overvoltage on phase 2 occurred.",UNDEF));
        events.add(new EventNumber(22,"OvervoltageL3","Indicates that an overvoltage on phase 3 occurred.",UNDEF));
        events.add(new EventNumber(23,"PowerDown","Indicates that a power failure occurred.",UNDEF));
        events.add(new EventNumber(24,"PowerUp","Indicates that a power up returned.",UNDEF));
        events.add(new EventNumber(25,"OvercurrentL1","Indicates that an overcurrent on phase 1 has occurred.",UNDEF));
        events.add(new EventNumber(26,"OvercurrentL2","Indicates that an overcurrent on phase 2 has occurred.",UNDEF));
        events.add(new EventNumber(27,"OvercurrentL3","Indicates that an overcurrent on phase 3 has occurred.",UNDEF));
        events.add(new EventNumber(28,"OvercurrentN","Indicates that an overcurrent in the neutral neutral conductor has occurred.",UNDEF));
        events.add(new EventNumber(31,"PowerFactorMonitor1","Indicates that the power factor 1 is below a set limit.",UNDEF));
        events.add(new EventNumber(32,"PowerFactorMonitor2","Indicates that the power factor 2 is below a set limit.",UNDEF));
        events.add(new EventNumber(33,"DemandMonitor1","Indicates that demand 1 is above a set limit.",UNDEF));
        events.add(new EventNumber(34,"DemandMonitor2","Indicates that demand 2 is above a set limit.",UNDEF));
        events.add(new EventNumber(35,"DemandMonitor3","Indicates that demand 3 is above a set limit.",UNDEF));
        events.add(new EventNumber(36,"DemandMonitor4","Indicates that demand 4 is above a set limit.",UNDEF));
        events.add(new EventNumber(37,"DemandMonitor5","Indicates that demand 5 is above a set limit.",UNDEF));
        events.add(new EventNumber(38,"DemandMonitor6","Indicates that demand 6 is above a set limit.",UNDEF));
        events.add(new EventNumber(39,"DemandMonitor7","Indicates that demand 7 is above a set limit.",UNDEF));
        events.add(new EventNumber(40,"DemandMonitor8","Indicates that demand 8 is above a set limit.",UNDEF));
        events.add(new EventNumber(49,"MissingVoltageL1","Indicates that the voltage U1 dropped below 20 V L1 and remained below 20 V for a period of time defined by parameterisation (B21 and higher).",ALERT));
        events.add(new EventNumber(50,"MissingVoltageL2","Indicates that the voltage U2 dropped below 20 V L2 and remained below 20 V for a period of time defined by parameterisation (B21 and higher).",ALERT));
        events.add(new EventNumber(51,"MissingVoltageL3","Indicates that the voltage U3 dropped below 20 V L3 and remained below 20 V for a period of time defined by parameterisation (B21 and higher).",ALERT));
        events.add(new EventNumber(66,"DateTimeInvalid","FF 02000000 (see 16.3.1 Time-Base Errors (Clock))",ERROR));
        events.add(new EventNumber(75,"MeasuringSystemAccessError","FF 00040000 access error (see 16.3.2 Read/Write Access Errors)",ERROR));
        events.add(new EventNumber(76,"TimeBaseError","FF 00080000 (CTS) (see 16.3.2 Read/Write Access Errors)",ERROR));
        events.add(new EventNumber(79,"CommunicationUnitError","FF 00400000 unit error (see 16.3.2 Read/Write Access Errors) ",ERROR));
        events.add(new EventNumber(80,"MMIBoardError,","FF 00800000 (see 16.3.2 Read/Write Access Errors)",ERROR));
        events.add(new EventNumber(89,"StartupSequenceInvalid","FF 00000001 sequence invalid (see 16.3.4 Other Errors)",ERRORALERT));
        events.add(new EventNumber(90,"MeasuringSystemError","FF 00000002 error (see 16.3.4 Other Errors)",ERROR));
        events.add(new EventNumber(93,"GeneralSystemError","FF 00000010 error (see 16.3.4 Other Errors)",ERROR));
        events.add(new EventNumber(94,"CommunicationLocked","FF 00000020 locked (see 16.3.4 Other Errors)",ERRORALERT));
        events.add(new EventNumber(106,"AlertOccurred","Indicates that an alert has occurred.",UNDEF));
        events.add(new EventNumber(524288, "FatalErrorOccurred", "Indicates that an fatal error has occurred.", ERROR));
    }
    
    
    
    private static final String[] strTypes={" (Alert)"," (Error)",""," (Error/Alert)"};
    
    int type;
    int id;
    String idDescription;
    String eventDescription;
    
    /** Creates a new instance of EventLog */
    private EventNumber(int id, String idDescription, String eventDescription, int type) {
        this.id=id;
        this.idDescription=idDescription;
        this.eventDescription=eventDescription;
        this.type=type;
    }

    static private EventNumber getEventNumber(int id) {
        Iterator it = events.iterator();
        while(it.hasNext()) {
            EventNumber en = (EventNumber)it.next();
            if (en.getId() == id)
                return en;
        }
        return null; 
    }
    
//    static private String getEventDescr(int id) {
//        Iterator it = events.iterator();
//        while(it.hasNext()) {
//            EventNumber en = (EventNumber)it.next();
//            if (en.getId() == id)
//                return en.getEventDescription();
//        }
//        return "Event description not found in list, id "+id+" is an unknown event";
//    }
//    
//    static private String getIdDescr(int id) {
//        Iterator it = events.iterator();
//        while(it.hasNext()) {
//            EventNumber en = (EventNumber)it.next();
//            if (en.getId() == id)
//                return en.getIdDescription();
//        }
//        return "Id description not found in list, id "+id+" is an unknown event";
//    }
    
    static public MeterEvent toMeterEvent(int id,Date dateTime) {
        
        EventNumber eventNumber = EventNumber.getEventNumber(id);
        if (eventNumber==null) 
            return null;
        
        String idDescr = eventNumber.getIdDescription();
        int eiCode=MeterEvent.OTHER;
        
        if (idDescr.compareTo("EnergyRegistersCleared") == 0) {
            eiCode=MeterEvent.CLEAR_DATA;
        }
        else if (idDescr.compareTo("LoadStoredProfileCleared") == 0) {
            eiCode=MeterEvent.CLEAR_DATA;
        }
        else if (idDescr.compareTo("BatteryLow") == 0) {
            eiCode=MeterEvent.BATTERY_VOLTAGE_LOW;
        }
        else if (idDescr.compareTo("BatteryOk") == 0) {
            eiCode=MeterEvent.OTHER;
        }
        else if (idDescr.compareTo("BillingPeriodReset") == 0) {
            eiCode=MeterEvent.BILLING_ACTION;
        }
        else if (idDescr.compareTo("DSTSwitched") == 0) {
            eiCode=MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED;
        }
        else if (idDescr.compareTo("ClockAdjustedOldDateTime") == 0) {
            eiCode=MeterEvent.SETCLOCK_BEFORE;
        }
        else if (idDescr.compareTo("ClockAdjustedNewDateTime") == 0) {
            eiCode=MeterEvent.SETCLOCK_AFTER;
        }
        else if (idDescr.compareTo("ControlInputStatusChanged") == 0) {
            eiCode=MeterEvent.OTHER;
        }
        else if (idDescr.compareTo("UndervoltageL1") == 0) {
            eiCode=MeterEvent.VOLTAGE_SAG;
        }
        else if (idDescr.compareTo("UndervoltageL2") == 0) {
            eiCode=MeterEvent.VOLTAGE_SAG;
        }
        else if (idDescr.compareTo("UndervoltageL3") == 0) {
            eiCode=MeterEvent.VOLTAGE_SAG;
        }
        else if (idDescr.compareTo("OvervoltageL1") == 0) {
            eiCode=MeterEvent.VOLTAGE_SWELL;
        }
        else if (idDescr.compareTo("OvervoltageL2") == 0) {
            eiCode=MeterEvent.VOLTAGE_SWELL;
        }
        else if (idDescr.compareTo("OvervoltageL3") == 0) {
            eiCode=MeterEvent.VOLTAGE_SWELL;
        }
        else if (idDescr.compareTo("PowerDown") == 0) {
            eiCode=MeterEvent.POWERDOWN;
        }
        else if (idDescr.compareTo("PowerUp") == 0) {
            eiCode=MeterEvent.POWERUP;
        }
        else if (idDescr.compareTo("OvercurrentL1") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("OvercurrentL2") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("OvercurrentL3") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("OvercurrentN") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("PowerFactorMonitor1") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("PowerFactorMonitor2") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("DemandMonitor1") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("DemandMonitor2") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("DemandMonitor3") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("DemandMonitor4") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("DemandMonitor5") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("DemandMonitor6") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("DemandMonitor7") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("DemandMonitor8") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        }
        else if (idDescr.compareTo("MissingVoltageL1") == 0) {
            eiCode=MeterEvent.PHASE_FAILURE;
        }
        else if (idDescr.compareTo("MissingVoltageL2") == 0) {
            eiCode=MeterEvent.PHASE_FAILURE;
        }
        else if (idDescr.compareTo("MissingVoltageL3") == 0) {
            eiCode=MeterEvent.PHASE_FAILURE;
        }
        else if (idDescr.compareTo("DateTimeInvalid") == 0) {
            eiCode=MeterEvent.CLOCK_INVALID;
        }
        else if (idDescr.compareTo("MeasuringSystemAccessError") == 0) {
            eiCode=MeterEvent.FATAL_ERROR;
        }
        else if (idDescr.compareTo("TimeBaseError") == 0) {
            eiCode=MeterEvent.FATAL_ERROR;
        }
        else if (idDescr.compareTo("CommunicationUnitError") == 0) {
            eiCode=MeterEvent.FATAL_ERROR;
        }
        else if (idDescr.compareTo("MMIBoardError,") == 0) {
            eiCode=MeterEvent.FATAL_ERROR;
        }
        else if (idDescr.compareTo("StartupSequenceInvalid") == 0) {
            eiCode=MeterEvent.PROGRAM_FLOW_ERROR;
        }
        else if (idDescr.compareTo("MeasuringSystemError") == 0) {
            eiCode=MeterEvent.FATAL_ERROR;
        }
        else if (idDescr.compareTo("GeneralSystemError") == 0) {
            eiCode=MeterEvent.FATAL_ERROR;
        }
        else if (idDescr.compareTo("CommunicationLocked") == 0) {
            eiCode=MeterEvent.FATAL_ERROR;
        }
        else if (idDescr.compareTo("AlertOccurred") == 0) {
            eiCode=MeterEvent.METER_ALARM;
        } else if (idDescr.compareTo("FatalErrorOccurred") == 0) {
            eiCode = MeterEvent.FATAL_ERROR;
        }

        return new MeterEvent(dateTime,eiCode,id,eventNumber.getEventDescription());
    }
    
    /**
     * Getter for property id.
     * @return Value of property id.
     */
    private int getId() {
        return id;
    }
    
    private int getType() {
        return type;
    }
    
    private String getStrType() {
        return strTypes[getType()];
    }
    
    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    private void setId(int id) {
        this.id = id;
    }
    
    /**
     * Getter for property idDescription.
     * @return Value of property idDescription.
     */
    private java.lang.String getIdDescription() {
        return idDescription;
    }
    
    /**
     * Setter for property idDescription.
     * @param idDescription New value of property idDescription.
     */
    private void setIdDescription(java.lang.String idDescription) {
        this.idDescription = idDescription;
    }
    
    /**
     * Getter for property eventDescription.
     * @return Value of property eventDescription.
     */
    private java.lang.String getEventDescription() {
        return eventDescription;
    }
    
    /**
     * Setter for property eventDescription.
     * @param eventDescription New value of property eventDescription.
     */
    private void setEventDescription(java.lang.String eventDescription) {
        this.eventDescription = eventDescription;
    }
    
}
