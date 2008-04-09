/*
 * Metcom3.java
 *
 * Created on 20 februari 2003, 16:52
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
 * @author Koen
 * @beginchanges KV|18032004|add ChannelMap
KV|07052004|Extend for multibuffer with more then 1 channel per buffer. Also extend ChannelMap
KV|07032005|changes for setTime and use of 8 character SCTM ID * 
KV|23032005|Changed header to be compatible with protocol version tool
KV|23092005|Changed intervalstate bits behaviour (EDP)
 @endchanges
 */
public class Metcom3 extends Metcom {
    
    private static final int DEBUG = 0;
    
    /** Creates a new instance of Metcom3 */ 
    public Metcom3() {
    }
    
    public int getNumberOfChannels() throws IOException {
        return getChannelMap().getTotalNrOfChannels();
    }
    
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendarFrom=ProtocolUtils.getCleanCalendar(getTimeZone());
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
    
    
    
    protected ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException {
       try { 
           ProfileData profileData=null;
           SCTMTimeData from = new SCTMTimeData(calendarFrom);
           SCTMTimeData to = new SCTMTimeData(calendarTo);
           List bufferStructures = new ArrayList();
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           List datas = new ArrayList();
           byte[] profileid = new byte[2];
           byte[] data;
           
           for (int i=0;i<getChannelMap().getNrOfBuffers();i++) {
               
               BufferStructure bs = getBufferStructure(i);
               
               if (getChannelMap().getBuffers()[i]>=0) {
                   if (getChannelMap().getBuffers()[i] != bs.getNrOfChannels())               
                       throw new IOException("doGetProfileData(), nr of channels configured ("+getChannelMap().getBuffers()[i]+") != nr of channels read from meter ("+bs.getNrOfChannels()+")!");
               }
               
               if (DEBUG >= 1) System.out.println("KV_DEBUG> "+bs);
               bufferStructures.add(bs);
           }
           
           for (int i=0;i<getChannelMap().getNrOfBuffers();i++) {
               if (getChannelMap().useBuffer(i)) {
                   profileid[0] = 0x30;
                   profileid[1] = (byte)(0x30+(byte)i+1);
                   baos.reset();
                   baos.write(profileid);
                   baos.write(from.getBUFENQData());
                   baos.write(to.getBUFENQData());
                   data = getSCTMConnection().sendRequest(SiemensSCTM.BUFENQ2, baos.toByteArray());
                   if (data==null)
                       throw new IOException("Profiledatabuffer "+i+" is empty or not configured! ChannelMap property might be wrong!");
                   datas.add(data);
               }
           }

           SCTMProfileMultipleBufferMetcom3 sctmp = new SCTMProfileMultipleBufferMetcom3(datas,getChannelMap(),bufferStructures);
           profileData = sctmp.getProfileData(getProfileInterval(),getTimeZone(),isRemovePowerOutageIntervals());
           
           if (includeEvents) {
               SCTMSpontaneousBuffer sctmSpontaneousBuffer = new SCTMSpontaneousBuffer(this); //getSCTMConnection(),getTimeZone());
               sctmSpontaneousBuffer.getEvents(calendarFrom,calendarTo,profileData);
               // Apply the events to the channel statusvalues
               profileData.applyEvents(getProfileInterval()/60); 
           }
           return profileData;
       }
       catch(SiemensSCTMException e) {
          throw new IOException("Siemens7ED62, doGetProfileData, SiemensSCTMException, "+e.getMessage());
       }
    } // private ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException
    
    public void release() throws IOException {
    }
    
    public String buildDefaultChannelMap() throws IOException {
        return null;
    }
    public String getDefaultChannelMap() {
        return "1,1,1,1";
    }
    public List getOptionalKeys() { 
        List result = new ArrayList(); 
        result.add("Timeout");
        result.add("Retries");
        result.add("HalfDuplex");
        result.add("ChannelMap");
        result.add("RemovePowerOutageIntervals");
        result.add("LogBookReadCommand");
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
