/*
 * EictRtuVdewProfile.java
 *
 * Created on 10 januari 2005, 09:21
 */

package com.energyict.protocolimpl.iec1107.eictrtuvdew;

import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 *
 * @author  Koen
 * Changes:
 * KV 10012005 Initial version
 */
public class EictRtuVdewProfile extends VDEWProfile {
    
    private static final int DEBUG=0;
    
    /** Creates a new instance of EictRtuVdewProfile */
    public EictRtuVdewProfile(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
        super(meterExceptionInfo,protocolLink,abstractVDEWRegistry);
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
    
} // EictRtuVdewProfile
