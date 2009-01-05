/*
 * A1500Profile.java
 *
 * Created on 23 december 2004, 16:23
 */

package com.energyict.protocolimpl.iec1107.abba1500;

import java.io.*;
import java.util.*;
import com.energyict.cbo.*;
import java.math.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocolimpl.iec1107.vdew.*;
import java.text.*;
/**
 *
 * @author  Koen
 * Changes:
 * KV 20012005 Initial version
 */
public class ABBA1500Profile extends VDEWProfile {
    
    private static final int DEBUG=0;
	private String fwVersion = "";
    
    /** Creates a new instance of ABBA1500Profile */
    public ABBA1500Profile(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
        super(meterExceptionInfo,protocolLink,abstractVDEWRegistry,false);
    }
    
    
    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        fromCalendar.setTime(lastReading);
        
        ProfileData profileData =  doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getProtocolLink().getTimeZone()),1);
        if (includeEvents) {
           List meterEvents = doGetLogBook(fromCalendar,ProtocolUtils.getCalendar(getProtocolLink().getTimeZone())); 
           profileData.getMeterEvents().addAll(meterEvents);
           profileData.sort();
        }
                
        profileData.applyEvents(getProtocolLink().getProfileInterval()/60);
        
        // JME: filter flags for firmware 3.02
        if (getFirmwareVersion().equalsIgnoreCase("3.02")) {
            profileData = filterDisturbedIntervalFlag(profileData);
        }

        return profileData;
    }
    
    public ProfileData getProfileData(Date fromReading, Date toReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        fromCalendar.setTime(fromReading);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        toCalendar.setTime(toReading);
        
        ProfileData profileData =  doGetProfileData(fromCalendar,toCalendar,1);
        if (includeEvents) {
           List meterEvents = doGetLogBook(fromCalendar,toCalendar); 
           profileData.getMeterEvents().addAll(meterEvents);
           profileData.sort();
        }
                        
        profileData.applyEvents(getProtocolLink().getProfileInterval()/60);

        // JME: filter flags for firmware 3.02
        if (getFirmwareVersion().equalsIgnoreCase("3.02")) {
            profileData = filterDisturbedIntervalFlag(profileData);
        }
        
        return profileData;
    }

	public void setFirmwareVersion(String firmwareVersion) {
		this.fwVersion  = firmwareVersion;
	}

	private String getFirmwareVersion() {
		return this.fwVersion;
	}
	
	private ProfileData filterDisturbedIntervalFlag(ProfileData profileData) {
		ProfileData pd = profileData; 
		int numberOfIntervals = pd.getNumberOfIntervals();
		int numberOfChannels = pd.getNumberOfChannels();
		
		for (int i = 0; i < numberOfIntervals; i++) {

			//General statusFlags for all channels
			int statusFlags = pd.getIntervalData(i).getEiStatus();
			if (isEiStatusFlagSet(statusFlags, IntervalData.CORRUPTED) && (isEiStatusFlagSet(statusFlags, IntervalData.POWERUP) || isEiStatusFlagSet(statusFlags, IntervalData.POWERDOWN))) {
				pd.getIntervalData(i).setEiStatus(statusFlags & (~IntervalData.CORRUPTED));
			}

			//statusFlags for all specific channel
			for (int j = 0; j < numberOfChannels; j++) {
				statusFlags = pd.getIntervalData(i).getEiStatus(j);
				if (isEiStatusFlagSet(statusFlags, IntervalData.CORRUPTED) && (isEiStatusFlagSet(statusFlags, IntervalData.POWERUP) || isEiStatusFlagSet(statusFlags, IntervalData.POWERDOWN))) {
					pd.getIntervalData(i).setEiStatus(j, statusFlags & (~IntervalData.CORRUPTED));
				}
			}
			
		}
		
		return profileData;
	}
	
	private boolean isEiStatusFlagSet(int statusFlags, int eiFlag) {
		return ((statusFlags & eiFlag) == eiFlag);
	}
		
} // ABBA1500Profile

