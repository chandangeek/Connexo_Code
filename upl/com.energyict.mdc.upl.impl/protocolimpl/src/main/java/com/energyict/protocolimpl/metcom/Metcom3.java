/*
 * Metcom3.java
 *
 * Created on 20 februari 2003, 16:52
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.siemens7ED62.SCTMTimeData;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTMException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
@Deprecated //Never released, technical class
public class Metcom3 extends Metcom {

    private static final int DEBUG = 0;

    public Metcom3(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getChannelMap().getTotalNrOfChannels();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendarFrom=ProtocolUtils.getCleanCalendar(getTimeZone());
        calendarFrom.add(Calendar.YEAR,-10);
        return doGetProfileData(calendarFrom,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom=ProtocolUtils.getCleanCalendar(getTimeZone());
        calendarFrom.setTime(lastReading);
        return doGetProfileData(calendarFrom,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar calendarFrom=ProtocolUtils.getCleanCalendar(getTimeZone());
        calendarFrom.setTime(from);
        Calendar calendarTo=ProtocolUtils.getCleanCalendar(getTimeZone());
        calendarTo.setTime(to);
        return doGetProfileData(calendarFrom,calendarTo,includeEvents);
    }

    protected ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException {
       try {
           ProfileData profileData;
           SCTMTimeData from = new SCTMTimeData(calendarFrom);
           SCTMTimeData to = new SCTMTimeData(calendarTo);
           List<BufferStructure> bufferStructures = new ArrayList<>();
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           List<byte[]> datas = new ArrayList<>();
           byte[] profileid = new byte[2];
           byte[] data;

           for (int i=0;i<getChannelMap().getNrOfBuffers();i++) {

               BufferStructure bs = getBufferStructure(i);

               if (getChannelMap().getBuffers()[i]>=0) {
                   if (getChannelMap().getBuffers()[i] != bs.getNrOfChannels()) {
                       throw new IOException("doGetProfileData(), nr of channels configured (" + getChannelMap().getBuffers()[i] + ") != nr of channels read from meter (" + bs.getNrOfChannels() + ")!");
                   }
               }

               if (DEBUG >= 1) {
                   System.out.println("KV_DEBUG> " + bs);
               }
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
                   if (data==null) {
                       throw new IOException("Profiledatabuffer " + i + " is empty or not configured! ChannelMap property might be wrong!");
                   }
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
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public String buildDefaultChannelMap() throws IOException {
        return null;
    }

    @Override
    public String getDefaultChannelMap() {
        return "1,1,1,1";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-20 14:07:47 +0200 (Fri, 20 Jun 2014) $";
    }

    @Override
    public String getRegistersInfo(int extendedLogging) throws IOException {
        return null;
    }

}