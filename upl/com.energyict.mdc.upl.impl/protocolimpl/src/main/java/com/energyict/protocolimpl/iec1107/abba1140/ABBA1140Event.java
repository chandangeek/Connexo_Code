/*
 * A1140Event.java
 *
 * Created on 17 February 2006, 14:26
 *
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MeterEvent;
import java.util.Date;

/**@author fbo */

public class ABBA1140Event {
    
    /** protocol events */
    private static final int TRANSIENT_RESET=0x01;
    private static final int TIME_SYNC=0x02;
    private static final int DATA_CHANGE=0x04;
    private static final int BATTERY_FAIL=0x08;
    private static final int CT_RATIO_CHANGE = 0x10;
    private static final int REVERSE_RUN=0x20;
    private static final int PHASE_FAILURE=0x40;
    
    private Date date;
    private int meterCode;
    private int meterEventEiCode;
    private String description;
    
    private MeterEvent meterEvent;
    
    static ABBA1140Event createReverseRun( Date date ) {
        return new ABBA1140Event( date, REVERSE_RUN );
    }
    
    static ABBA1140Event createPhaseFailure( Date date ) {
        return new ABBA1140Event( date, PHASE_FAILURE );
    }
    
    static ABBA1140Event createPowerFail( Date date ) {
        ABBA1140Event event = new ABBA1140Event();
        event.date = date; 
        event.meterEventEiCode = MeterEvent.POWERDOWN;     
        return event;
    }
    
    static ABBA1140Event createConfigurationChange(Date date, String description) {
        ABBA1140Event event = new ABBA1140Event();
        event.date = date; 
        event.meterEventEiCode = MeterEvent.CONFIGURATIONCHANGE;     
        event.description = description;
        return event;
    }
    
    static ABBA1140Event createBilling(Date date) {
        ABBA1140Event event = new ABBA1140Event();
        event.date = date; 
        event.meterEventEiCode = MeterEvent.BILLING_ACTION;     
        return event;
    }
    
    
    private ABBA1140Event() { }
    
    /** Creates new A1140Event */
    ABBA1140Event(Date date, int meterCode) {
        this.date = date;
        this.meterCode = meterCode;
        
        switch( meterCode ) {
            case TRANSIENT_RESET:
                break;
            case TIME_SYNC:
                break;
            case DATA_CHANGE:
                break;
            case BATTERY_FAIL:
                break;
            case CT_RATIO_CHANGE:
                break;
            case REVERSE_RUN:
                break;
            case PHASE_FAILURE:
                break;
        }
    }
    
    MeterEvent toMeterEvent(){
        if( meterEvent == null ) {
            if( description == null )
                meterEvent = new MeterEvent(date, meterEventEiCode, meterCode);
            else
                meterEvent = new MeterEvent(date, meterEventEiCode, meterCode, description);
        }
        return meterEvent;
    }
    
    public String toString(){
        String result = 
            "MeterEvent [date " + date + ", " + meterEventEiCode + ", ";
        for( int i = 0; i < IntervalStateBits.states.length; i ++ ){
            if ((meterEventEiCode & (0x00000001<<i)) != 0) {
                result += IntervalStateBits.states[i] + ", ";
            }
        }
        result += meterCode + ", " + description + "]";
        if( description != null )
            result += ", " + description;
        result += "]";
        return result;
    }
    
}
