/*
 * Metcom3FAF.java
 *
 * Created on 15 december 2004, 10:36
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.siemens7ED62.SCTMTimeData;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTMException;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class Metcom3FBC extends Metcom3 {

    @Override
    public String getProtocolDescription() {
        return "Metcom3 FBC SCTM";
    }

    private static final int DEBUG = 0;

    @Inject
    public Metcom3FBC(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected BufferStructure getBufferStructure(int bufferNr) throws IOException {
        try {
            byte[] data = getSCTMConnection().sendRequest(SiemensSCTM.TABENQ3,String.valueOf(20+bufferNr+1).getBytes());
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
           ProfileData profileData;
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

           SCTMProfileMultipleBufferFBC sctmp = new SCTMProfileMultipleBufferFBC(datas,getChannelMap(),bufferStructures);
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
