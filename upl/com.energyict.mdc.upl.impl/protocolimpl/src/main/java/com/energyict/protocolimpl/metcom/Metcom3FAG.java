/*
 * Metcom3FAF.java
 *
 * Created on 15 december 2004, 10:36
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
 */
public class Metcom3FAG extends Metcom3 {
    
    private static final int DEBUG = 0;
    
    /** Creates a new instance of Metcom3FAF */
    public Metcom3FAG() {
    }
    
    
    protected BufferStructure getBufferStructure(int bufferNr) throws IOException, UnsupportedException, NoSuchRegisterException {
        try {
            byte[] data = getSCTMConnection().sendRequest(getSCTMConnection().TABENQ3,String.valueOf(20+bufferNr+1).getBytes());
            data = ProtocolUtils.getSubArray2(data, 1, 6);
            return new BufferStructure(data);
        }
        catch(SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, getTime, SiemensSCTMException, "+e.getMessage());   
        }        
    }
    
    public String buildDefaultChannelMap() throws IOException {
        return String.valueOf(getBufferStructure().getNrOfChannels());
    }
  
    public String getDefaultChannelMap() {
        return null;
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

           SCTMProfileMultipleBufferFAG sctmp = new SCTMProfileMultipleBufferFAG(datas,getChannelMap(),bufferStructures);
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
    } // protected ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException
    
}
