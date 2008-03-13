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
        return profileData;
    }
    
} // ABBA1500Profile

