/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AS230Event.java
 *
 * Created on 17 February 2006, 14:26
 *
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

/**@author fbo */

public class ABBA230Event {

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

    static ABBA230Event createReverseRun( Date date ) {
        return new ABBA230Event( date, REVERSE_RUN );
    }

    static ABBA230Event createPhaseFailure( Date date ) {
        return new ABBA230Event( date, PHASE_FAILURE );
    }

    static ABBA230Event createPowerFail( Date date ) {
        ABBA230Event event = new ABBA230Event();
        event.date = date;
        event.meterEventEiCode = MeterEvent.POWERDOWN;
        return event;
    }

    static ABBA230Event createConfigurationChange(Date date, String description) {
        ABBA230Event event = new ABBA230Event();
        event.date = date;
        event.meterEventEiCode = MeterEvent.CONFIGURATIONCHANGE;
        event.description = description;
        return event;
    }

    static ABBA230Event createBilling(Date date) {
        ABBA230Event event = new ABBA230Event();
        event.date = date;
        event.meterEventEiCode = MeterEvent.BILLING_ACTION;
        return event;
    }


    private ABBA230Event() { }

    /** Creates new AS230Event */
    ABBA230Event(Date date, int meterCode) {
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
            if( description == null ){
                meterEvent = new MeterEvent(date, meterEventEiCode, meterCode);
            } else {
				meterEvent = new MeterEvent(date, meterEventEiCode, meterCode, description);
			}
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
        if( description != null ) {
			result += ", " + description;
		}
        result += "]";
        return result;
    }

}
