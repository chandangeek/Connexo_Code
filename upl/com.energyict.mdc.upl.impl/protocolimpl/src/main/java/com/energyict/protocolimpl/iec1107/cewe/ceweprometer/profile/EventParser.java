package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.profile;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
    private Map<Integer, EventType> map = new HashMap<>();

    public EventParser(CewePrometer prometer){
        this.prometer = prometer;
        init();
    }

    void init( ){
        put( 1, new EventType(MeterEvent.OTHER, "Single phase reverse energy direction"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", phase L" + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }

        });

        put( 2, new EventType(MeterEvent.SETCLOCK, "Time set"){
            MeterEvent toMeterEvent(Date date, String data) {

                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.setTimeZone(prometer.getTimeZone());

                String ds = sdf.format(new Date(parseLong(data.substring(0, 8))*1000));
                String dscr = description + " " + ds;

                return new MeterEvent(date, eiCode, protocolCode, dscr);

            }
        });

        put( 3, new EventType(MeterEvent.CLEAR_DATA, "Registers cleared") );

        put( 4, new EventType(MeterEvent.CLEAR_DATA, "Logger has been reset"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", Logger " + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 5, new EventType(MeterEvent.POWERDOWN, "Supply lost") );

        put( 6, new EventType(MeterEvent.CLEAR_DATA, "Historical registers cleared (all billing periods cleared)") );

        put( 7, new EventType(MeterEvent.BILLING_ACTION, "Historical period finished and MD-registers reset (Billing period reset)") );

        put( 8, new EventType(MeterEvent.CLEAR_DATA, "All MD-registers cleared due to configuration changed") );

        put( 9, new EventType(MeterEvent.CONFIGURATIONCHANGE, "Logger cleared due to configuration changed"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", Logger " + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 14, new EventType(MeterEvent.POWERDOWN, "Voltage interruption in seconds"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + " " + parseLong(data) + " s";
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 15, new EventType(MeterEvent.VOLTAGE_SAG, "Voltage sag"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + " " + parseLong(data) + " s";
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 16, new EventType(MeterEvent.VOLTAGE_SWELL, "Voltage swell"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + " " + parseLong(data) + " s";
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 17, new EventType(MeterEvent.OTHER, "Voltage below limit") );

        put( 18, new EventType(MeterEvent.OTHER, "Voltage exceeding limit") );

        put( 19, new EventType(MeterEvent.OTHER, "Power factor below limit") );

        put( 20, new EventType(MeterEvent.OTHER, "Voltage unbalance") );

        put( 21, new EventType(MeterEvent.OTHER, "Current unbalance") );

        put( 22, new EventType(MeterEvent.OTHER, "Active power below limit") );

        put( 23, new EventType(MeterEvent.OTHER, "Active power exceeding limit") );

        put( 24, new EventType(MeterEvent.OTHER, "Voltage THD exceeding limit") );

        put( 25, new EventType(MeterEvent.OTHER, "Current THD exceeding limit") );

        put( 26, new EventType(MeterEvent.OTHER, "Single harmonic on voltage exceeding limit"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d =
                    description + " Phase L" + parseFirstIndex(data) +
                    " Harmonic nr " + parseSecondIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 27, new EventType(MeterEvent.OTHER, "Single harmonic on current exceeding limit"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d =
                    description + " Phase L" + parseFirstIndex(data) +
                    " Harmonic nr " + parseSecondIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 28, new EventType(MeterEvent.OTHER, "Digital input pulse length too long"){

            MeterEvent toMeterEvent(Date date, String data) {
                String d =
                    description + " Inp. " + parseFirstIndex(data) +
                    ", " + parseLong(data.substring(2));
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 29, new EventType(MeterEvent.OTHER, "Digital input pulse lenth too short"){
            MeterEvent toMeterEvent(Date date, String data) {
                String a = data.substring(0,2) + "." + data.substring(2, data.length() );
                String d = description + " " + Double.parseDouble(a) + "ms";
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 30, new EventType(MeterEvent.OTHER, "Voltage phase failure. 2-element meters will always have phases = 00"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", phase L" + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 42, new EventType(MeterEvent.OTHER, "Meter configuration altered") );

        put( 43, new EventType(MeterEvent.OTHER, "Meter calibration altered") );

        put( 44, new EventType(MeterEvent.OTHER, "Meter initialised") );

        put( 45, new EventType(MeterEvent.OTHER, "Reverse running. 2-element meters will always have bit 1 (L2)=0"){
            MeterEvent toMeterEvent(Date date, String data) {
                String d = description + ", phase L" + parseFirstIndex(data);
                return new MeterEvent(date, eiCode, protocolCode, d);
            }
        });

        put( 46, new EventType(MeterEvent.OTHER, "Meter firmware upgrade") );

        put( 47, new EventType(MeterEvent.BATTERY_VOLTAGE_LOW, "Time to change battery") );

        put( 1000, new EventType(MeterEvent.HARDWARE_ERROR, "Energy registers corrupt") );

        put( 1001, new EventType(MeterEvent.HARDWARE_ERROR, "Communication module config. corrupt") );

        put( 1002, new EventType(MeterEvent.HARDWARE_ERROR, "IO module config. corrupt") );

        put( 1003, new EventType(MeterEvent.HARDWARE_ERROR, "Measuring module config. corrupt") );

        put( 1004, new EventType(MeterEvent.HARDWARE_ERROR, "Measuring module initialisation corrupt") );

        put( 1005, new EventType(MeterEvent.HARDWARE_ERROR, "Measurig module calibration corrupt") );

        put( 1006, new EventType(MeterEvent.HARDWARE_ERROR, "Main module config. corrupt") );

        put( 1007, new EventType(MeterEvent.HARDWARE_ERROR, "Historical period corrupt") );

        put( 1008, new EventType(MeterEvent.HARDWARE_ERROR, "MD-register corrupt") );

        put( 1009, new EventType(MeterEvent.HARDWARE_ERROR, "Measuring module faulty") );

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
            StringBuilder builder = new StringBuilder();
            for(int i=data.length(); i > 1; i=i-2){
                builder.append( data.substring(i-2,i) );
            }
            return Long.parseLong(builder.toString(),16);
        }

        public String toString(){
            return "EventType [" +
                    protocolCode + "" +
                    "\t\t" + eiCode + "" +
                    "\t" + eventToString(eiCode) + ", " +
                    "\t" + description + "";
        }
    }

    private EventType getEventType(Integer id){
        EventType result = map.get(id);

        if( result != null)     /* short circuit */ {
            return map.get(id);
        }

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
        StringBuilder builder = new StringBuilder( );
        for (Object o : map.values()) {
            builder.append(o).append("\n");
        }
        return builder.toString();
    }

}