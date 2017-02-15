/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.profile;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 * Event ID's and data
 *
 * The message Get next event (102200) returns event ID'zs as well as additional
 * event data.  The event id is returned as a decimal integer and the additional
 * data as 6 hexadecimal bytes.
 *
 *
 * EventParser contains a List of all EventTypes.  The EventParser first
 * identifies the type of the message, and then dispatches the rest of the
 * parsing to the EventType.
 *
 * </pre>
 *
 * @author fbo
 */

public class EventParser {

    private final CewePrometer prometer;
    private Map map = new LinkedHashMap();

    public EventParser(CewePrometer prometer){
        this.prometer = prometer;
        init();
    }

    void init( ){

        String dscr = null;

        dscr = "Single phase reverse energy direction";
        put( 1, new EventType(MeterEvent.OTHER, dscr){

            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", phase L" + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }

        });

        dscr = "Time set";
        put( 2, new EventType(MeterEvent.SETCLOCK, dscr){
            MeterEvent toMeterEvent(Date date, String data) {

                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.setTimeZone(prometer.getTimeZone());

                String ds = sdf.format(new Date(parseLong(data.substring(0, 8))*1000));
                String dscr = description + " " + ds;

                return new MeterEvent(date, eiCode, protocolCode, dscr);

            }
        });

        dscr = "Registers cleared";
        put( 3, new EventType(MeterEvent.CLEAR_DATA, dscr) );

        dscr = "Logger has been reset";
        put( 4, new EventType(MeterEvent.CLEAR_DATA, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", Logger " + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Supply lost";
        put( 5, new EventType(MeterEvent.POWERDOWN, dscr) );

        dscr = "Historical registers cleared (all billing periods cleared)";
        put( 6, new EventType(MeterEvent.CLEAR_DATA, dscr) );

        dscr = "Historical period finished and MD-registers reset (Billing " +
                "period reset)";
        put( 7, new EventType(MeterEvent.BILLING_ACTION, dscr) );

        dscr = "All MD-registers cleared due to configuration changed";
        put( 8, new EventType(MeterEvent.CLEAR_DATA, dscr) );

        dscr = "Logger cleared due to configuration changed";
        put( 9, new EventType(MeterEvent.CONFIGURATIONCHANGE, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", Logger " + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Voltage interruption in seconds";
        put( 14, new EventType(MeterEvent.POWERDOWN, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + " " + parseLong(data) + " s";
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Voltage sag";
        put( 15, new EventType(MeterEvent.VOLTAGE_SAG, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + " " + parseLong(data) + " s";
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Voltage swell";
        put( 16, new EventType(MeterEvent.VOLTAGE_SWELL, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + " " + parseLong(data) + " s";
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Voltage below limit";
        put( 17, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Voltage exceeding limit";
        put( 18, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Power factor below limit";
        put( 19, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Voltage unbalance";
        put( 20, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Current unbalance";
        put( 21, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Active power below limit";
        put( 22, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Active power exceeding limit";
        put( 23, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Voltage THD exceeding limit";
        put( 24, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Current THD exceeding limit";
        put( 25, new EventType(MeterEvent.OTHER, dscr) );


        dscr = "Single harmonic on voltage exceeding limit";
        put( 26, new EventType(MeterEvent.OTHER, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d =
                    description + " Phase L" + parseFirstIndex(data) +
                    " Harmonic nr " + parseSecondIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Single harmonic on current exceeding limit";
        put( 27, new EventType(MeterEvent.OTHER, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d =
                    description + " Phase L" + parseFirstIndex(data) +
                    " Harmonic nr " + parseSecondIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Digital input pulse length too long";
        put( 28, new EventType(MeterEvent.OTHER, dscr){

            MeterEvent toMeterEvent(Date date, String data) {
                String d =
                    description + " Inp. " + parseFirstIndex(data) +
                    ", " + parseLong(data.substring(2));
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Digital input pulse lenth too short";
        put( 29, new EventType(MeterEvent.OTHER, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String a = data.substring(0,2) + "." + data.substring(2, data.length() );
                String d = description + " " + Double.parseDouble(a) + "ms";
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Voltage phase failure. 2-element meters will always have phases = 00";
        put( 30, new EventType(MeterEvent.OTHER, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", phase L" + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Meter configuration altered";
        put( 42, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Meter calibration altered";
        put( 43, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Meter initialised";
        put( 44, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Reverse running. 2-element meters will always have bit 1 (L2)=0";
        put( 45, new EventType(MeterEvent.OTHER, dscr){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", phase L" + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        dscr = "Meter firmware upgrade";
        put( 46, new EventType(MeterEvent.OTHER, dscr) );

        dscr = "Time to change battery";
        put( 47, new EventType(MeterEvent.BATTERY_VOLTAGE_LOW, dscr) );

        dscr = "Energy registers corrupt";
        put( 1000, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "Communication module config. corrupt";
        put( 1001, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "IO module config. corrupt";
        put( 1002, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "Measuring module config. corrupt";
        put( 1003, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "Measuring module initialisation corrupt";
        put( 1004, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "Measurig module calibration corrupt";
        put( 1005, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "Main module config. corrupt";
        put( 1006, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "Historical period corrupt";
        put( 1007, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "MD-register corrupt";
        put( 1008, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

        dscr = "Measuring module faulty";
        put( 1009, new EventType(MeterEvent.HARDWARE_ERROR, dscr) );

    }

    private void put(int id, EventType type){
        type.setProtocolCode(id);
        map.put(new Integer(id), type);
    }

    class EventType {

        int protocolCode;
        int eiCode;
        String description;

        EventType(int eiCode, String description){
            this.eiCode = eiCode;
            this.description = description;
        }

        void setProtocolCode(int id){
            this.protocolCode = id;
        }

        MeterEvent toMeterEvent(Date date, String data) {
            return new MeterEvent(date, eiCode, protocolCode, description );
        }

        int parseFirstIndex(String data){
            return Integer.parseInt(data.substring(0, 2))+1;
        }

        int parseSecondIndex(String data){
            return Integer.parseInt(data.substring(2, 4))+1;
        }

        long parseLong(String data){
            StringBuffer buffer = new StringBuffer();
            for(int i=data.length(); i > 1; i=i-2){
                buffer.append( data.substring(i-2,i) );
            }
            return Long.parseLong(buffer.toString(),16);
        }

        public String toString(){
            return new StringBuffer()
                .append("EventType [")
                .append(protocolCode).append("")
                .append( "\t\t" ).append(eiCode).append("")
                .append( "\t" ).append( eventToString(eiCode) ).append( ", ")
                .append( "\t" ).append(description).append("")

                .toString();
        }
    }

    private EventType getEventType(Integer id){
        EventType result = (EventType) map.get(id);

        if( result != null)     /* short circuit */
            return (EventType) map.get(id);

        return
            new EventType(MeterEvent.OTHER, "Unknown event id: " + id ){
                MeterEvent toMeterEvent(Date date, String data) {
                    String d = description;
                    return new MeterEvent(date, eiCode, protocolCode, d);
                } };

    }

    private MeterEvent getMeterEvent(Date date, String data){
        int komma = data.indexOf(',');

        Integer id = new Integer( data.substring(0, komma) );
        String d = data.substring(komma+1, data.length());

        return getEventType( id ).toMeterEvent(date, d);
    }

    public MeterEvent parse(String data) throws NestedIOException {
        Date d;
        try {
            d = prometer.getDateFormats().getEventDateFormat().parse(data.substring(1, 13));
        } catch (ParseException e) {
            throw new NestedIOException(e);
        }
        return getMeterEvent(d, data.substring(14,27));
    }

    private String eventToString(int eiCode){

        switch(eiCode)
        {
            case  MeterEvent.POWERDOWN:
                     return("Power down.");
            case  MeterEvent.POWERUP:
                     return("Power up.");
            case  MeterEvent.CONFIGURATIONCHANGE:
                     return("Change in configuration.");
            case  MeterEvent.REGISTER_OVERFLOW:
                     return("Register overflow.");
            case  MeterEvent.PROGRAM_FLOW_ERROR:
                     return("Program flow error.");
            case  MeterEvent.RAM_MEMORY_ERROR:
                     return("Ram memory error.");
            case  MeterEvent.SETCLOCK:
                     return("Clock set.");
            case  MeterEvent.SETCLOCK_AFTER:
                     return("Clock set after.");
            case  MeterEvent.SETCLOCK_BEFORE:
                     return("Clock set before");
            case  MeterEvent.WATCHDOGRESET:
                     return("Watchdog reset.");
            case  MeterEvent.OTHER:
                     return("Other event.");
           case  MeterEvent.FATAL_ERROR:
                     return("Fatal error.");
           case  MeterEvent.CLEAR_DATA:
                     return("Clear data.");
           case  MeterEvent.HARDWARE_ERROR:
                     return("Hardware error.");
           case  MeterEvent.METER_ALARM:
                     return("Meter alarm.");
           case  MeterEvent.ROM_MEMORY_ERROR:
                     return("Rom memory error.");
           case  MeterEvent.MAXIMUM_DEMAND_RESET:
                     return("Maximum demand reset.");
           case  MeterEvent.BILLING_ACTION:
                     return("Billing action.");
           case  MeterEvent.PHASE_FAILURE:
                     return("Phase failure.");
           case  MeterEvent.VOLTAGE_SAG:
                       return("Voltage sage.");
           case  MeterEvent.VOLTAGE_SWELL:
                       return("Voltage swell.");

            default:
                return("Unknown event."+eiCode);

        }

    }

    public String toString(){
        StringBuilder rslt = new StringBuilder( );

        Iterator i = map.values().iterator();
        while(i.hasNext()) {
            rslt.append(i.next()).append("\n");
        }

        return rslt.toString();
    }

}