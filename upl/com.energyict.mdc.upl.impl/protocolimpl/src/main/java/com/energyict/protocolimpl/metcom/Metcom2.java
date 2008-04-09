/*
 * Metcom2.java
 *
 * Created on 8 april 2003, 16:35
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.protocolimpl.siemens7ED62.*;
import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.*;
import java.util.logging.*;
import com.energyict.cbo.*;
/**
 *
 * @author  Koen
 * @beginchanges
KV|18032004|add ChannelMap. ChannelMap is not used here!
KV|19102004|test data!=null in getProfileData()
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|23032005|Changed header to be compatible with protocol version tool
KV|23092005|Changed intervalstate bits behaviour (EDP)
KV|06042006|Add IntervalStatusBehaviour custom property to correct power fail status
 @endchanges
 */
public class Metcom2 extends Metcom{
    
    
    // TABENQ1(E1) list numbers
    protected final String REG_NR_OF_CHANNELS="60200";
    protected final String REG_PROFILEINTERVAL="70101";
    protected final String DIGITS_PER_VALUE="82001";
    
    protected int iNROfChannels=-1;
    protected int iMeterProfileInterval=-1;
    protected int digitsPerDecade=-1;
    
    /** Creates a new instance of Metcom2 */
    public Metcom2() {
    }
    
    
    public int getDigitsPerDecade() throws UnsupportedException, IOException {
        if (digitsPerDecade == -1)
            digitsPerDecade = Integer.parseInt(getRegister(DIGITS_PER_VALUE).trim());
        return digitsPerDecade;
    }
    
//    private static final String[]  meterReadings = {"8.1","8.2","8.3","8.4","9.1","9.2","10.1","10.2","6.1","6.2","6.3","6.4","2.1","2.2","2.3","2.4"}; // KV at KP 27032003
    
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
        //if (channelId > meterReadings.length) throw new IOException("Siemens7ED62, getMeterReading, invalid channelId");
        //return getDumpData().getRegister(meterReadings[channelId]);
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
        //return getDumpData().getRegister(name);
    }
    
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (iNROfChannels == -1) {
           iNROfChannels = Integer.parseInt(getRegister(REG_NR_OF_CHANNELS).trim());
        }
        return iNROfChannels;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        if (iMeterProfileInterval == -1) {
           iMeterProfileInterval = (Integer.parseInt(getRegister(REG_PROFILEINTERVAL).trim())*60);
        }
        return iMeterProfileInterval;
    }
    
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        // lazy initializing
       iNROfChannels = -1;
       iMeterProfileInterval = -1;
       super.init(inputStream, outputStream, timeZone, logger);
    }
     
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCleanCalendar(getTimeZone());
        calendarFrom.add(Calendar.YEAR,-10);
        return doGetProfileData(calendarFrom,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom=ProtocolUtils.getCleanCalendar(getTimeZone());
        calendarFrom.setTime(lastReading);
        return doGetProfileData(calendarFrom,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
           Calendar calendarFrom=ProtocolUtils.getCleanCalendar(getTimeZone());
           calendarFrom.setTime(from);
           Calendar calendarTo=ProtocolUtils.getCleanCalendar(getTimeZone());
           calendarTo.setTime(to);
           return doGetProfileData(calendarFrom,calendarTo,includeEvents);
    }
    
    
    private ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException {
       try { 
           ProfileData profileData=null;
           SCTMTimeData from = new SCTMTimeData(calendarFrom);
           SCTMTimeData to = new SCTMTimeData(calendarTo);
            
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           baos.write(getSCTMConnection().PERIODICBUFFERS);
           baos.write(from.getBUFENQData());
           baos.write(to.getBUFENQData());
           byte[] data = getSCTMConnection().sendRequest(getSCTMConnection().BUFENQ2, baos.toByteArray());
           if (data != null) {
               SCTMProfileSingleBufferMetcom2 sctmp = new SCTMProfileSingleBufferMetcom2(data);
               profileData = sctmp.getProfileData(getProfileInterval(),getTimeZone(), getNumberOfChannels(), getDigitsPerDecade(), isRemovePowerOutageIntervals(), getIntervalStatusBehaviour());
               if (includeEvents) {
                   SCTMSpontaneousBuffer sctmSpontaneousBuffer = new SCTMSpontaneousBuffer(this); //getSCTMConnection(),getTimeZone());
                   sctmSpontaneousBuffer.getEvents(calendarFrom,calendarTo,profileData);
                   // Apply the events to the channel statusvalues
                   profileData.applyEvents(getProfileInterval()/60); 
               }
           }
           return profileData;
       }
       catch(SiemensSCTMException e) {
          throw new IOException("Siemens7ED62, doGetProfileData, SiemensSCTMException, "+e.getMessage());
       }
    }
    

    public void release() throws IOException {
    }
    
    public String buildDefaultChannelMap() throws IOException {
        return null;
    }
    public String getDefaultChannelMap() {
        return "";
    }
    public List getOptionalKeys() { 
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("HalfDuplex");
        result.add("RemovePowerOutageIntervals");
        result.add("LogBookReadCommand");
        result.add("IntervalStatusBehaviour");
        result.add("TimeSetMethod");
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.22 $";
    }    
    
    public String getRegistersInfo(int extendedLogging) throws IOException {
        return null;
    }
    
}
