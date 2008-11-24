/*
 * A1500Profile.java
 *
 * Created on 23 december 2004, 16:23
 */

package com.energyict.protocolimpl.iec1107.abba1350;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfileHeader;

/**
 *
 * @author Koen
 * @author fbo
 * 
 */

public class ABBA1350Profile extends VDEWProfile {
    
	private ABBA1350ProfileHeader abba1350ProfileHeader = null;
	
    /** Creates a new instance of ABBA1500Profile */
    public ABBA1350Profile(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
        super(meterExceptionInfo,protocolLink,abstractVDEWRegistry,false);
    }
    
    public ProfileData getProfileData(Date lastReading,boolean includeEvents, int profileNumber) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        fromCalendar.setTime(lastReading);
        
        ProfileData profileData =  doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getProtocolLink().getTimeZone()), profileNumber);
        if (includeEvents) {
           List meterEvents = doGetLogBook(fromCalendar,ProtocolUtils.getCalendar(getProtocolLink().getTimeZone())); 
           profileData.getMeterEvents().addAll(meterEvents);
           profileData.sort();
        }
        
        profileData.applyEvents(getProtocolLink().getProfileInterval()/60);
        return profileData;
    }
    
    public ProfileData getProfileData(Date fromReading, Date toReading, boolean includeEvents, int profileNumber) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        fromCalendar.setTime(fromReading);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        toCalendar.setTime(toReading);
        
        ProfileData profileData =  doGetProfileData(fromCalendar,toCalendar, profileNumber);
        if (includeEvents) {
           List meterEvents = doGetLogBook(fromCalendar,toCalendar); 
           profileData.getMeterEvents().addAll(meterEvents);
           profileData.sort();
        }
        
        profileData.applyEvents(getProtocolLink().getProfileInterval()/60);
        return profileData;
    }
    
    public ABBA1350ProfileHeader getProfileHeader(int profileNumber) throws IOException {
    	if (abba1350ProfileHeader == null) {
    		abba1350ProfileHeader = new ABBA1350ProfileHeader(getProtocolLink().getFlagIEC1107Connection(), profileNumber);
    	}
    	return abba1350ProfileHeader;
    }
    
    /* Overrides VDEWProfile#getMeterEvent().
     * 
     * Difference: 
     * map bit 4 (reset of cumulation) to MeterEvent.MAXIMUM_DEMAND_RESET 
     * 
     * (non-Javadoc)
     * @see VDEWProfile#getMeterEvent(Date, long, String)
     */
    protected MeterEvent getMeterEvent(Date date, long logcode, String msg) {
        return new MeterEvent(date,getMeterEvent(logcode),(int)logcode);
    }
    
    private int getMeterEvent(long logcode){
        switch((int)logcode) {
            case CLEAR_LOADPROFILE:             return MeterEvent.CLEAR_DATA;
            case CLEAR_LOGBOOK:                 return MeterEvent.CLEAR_DATA;
            case END_OF_ERROR:                  return MeterEvent.METER_ALARM;
            case BEGIN_OF_ERROR:                return MeterEvent.METER_ALARM;
            case VARIABLE_SET:                  return MeterEvent.CONFIGURATIONCHANGE;
            case DEVICE_CLOCK_SET_INCORRECT:    return MeterEvent.SETCLOCK;
            case SEASONAL_SWITCHOVER:           return MeterEvent.OTHER;
            case FATAL_DEVICE_ERROR:            return MeterEvent.FATAL_ERROR;
            case DISTURBED_MEASURE:             return MeterEvent.OTHER;
            case POWER_FAILURE:                 return MeterEvent.POWERDOWN;
            case POWER_RECOVERY:                return MeterEvent.POWERUP;
            case DEVICE_RESET:                  return MeterEvent.MAXIMUM_DEMAND_RESET;
            case RUNNING_RESERVE_EXHAUSTED:     return MeterEvent.OTHER;
            default:                            return MeterEvent.OTHER;
        } 
        
    }
    
    /* Overrides VDEWProfile#mapStatus2IntervalStateBits().
     * 
     * Difference: 
     * map bit 2 (Measure value disturbed) to IntervalStateBits.SHORTLONG 
     * 
     * (non-Javadoc)
     * @see VDEWProfile#mapStatus2IntervalStateBits(int)
     */
    protected int mapStatus2IntervalStateBits(int status) {
        switch(status) {
            case CLEAR_LOADPROFILE:         return IntervalStateBits.OTHER;
            case CLEAR_LOGBOOK:             return IntervalStateBits.OTHER;
            case END_OF_ERROR:              return IntervalStateBits.OTHER;
            case BEGIN_OF_ERROR:            return IntervalStateBits.OTHER;
            case VARIABLE_SET:              return IntervalStateBits.CONFIGURATIONCHANGE;
            case DEVICE_CLOCK_SET_INCORRECT:return IntervalStateBits.SHORTLONG;
            case SEASONAL_SWITCHOVER:       return IntervalStateBits.SHORTLONG;
            case FATAL_DEVICE_ERROR:        return IntervalStateBits.OTHER;
            case DISTURBED_MEASURE:         return IntervalStateBits.SHORTLONG;
            case POWER_FAILURE:             return IntervalStateBits.POWERDOWN;
            case POWER_RECOVERY:            return IntervalStateBits.POWERUP;
            case DEVICE_RESET:              return IntervalStateBits.OTHER;
            case RUNNING_RESERVE_EXHAUSTED: return IntervalStateBits.OTHER;
            default:                        return IntervalStateBits.OTHER;
        } 
        
    } 
    
} 

