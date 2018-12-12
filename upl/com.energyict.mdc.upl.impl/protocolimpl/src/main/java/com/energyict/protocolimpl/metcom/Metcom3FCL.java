/*
 * Metcom3FCL.java
 *
 * Created on 14 december 2004, 18:19
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.siemens7ED62.SCTMTimeData;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTMException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
/**
 *
 * @author  Koen
 */
@Deprecated //Never released, technical class
public class Metcom3FCL extends Metcom3 {

    private static final int DEBUG=0;

    //protected final String[] REG_NR_OF_CHANNELS8={"62300","63300"}; Can be used but i prefer the channelmap entry for nr of channels with the buffer id
    //protected final String[] REG_NR_OF_CHANNELS16={"62308","63308"}; Can be used but i prefer the channelmap entry for nr of channels with the buffer id
    private static final String[] REG_PROFILEINTERVAL={"70101","70102"};
    private static final String[] DIGITS_PER_VALUE={"62200","63200"};

    public Metcom3FCL(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected BufferStructure getBufferStructure(int bufferNr) throws IOException {
        int nrOfChannels = getNumberOfChannels();
        int profileInterval = Integer.parseInt(getRegister(REG_PROFILEINTERVAL[bufferNr]).trim());

        // KV_DEBUG_EXTRA
        //System.out.println("KV_DEBUG_EXTRA> digitsPerValue nrOfChannels="+nrOfChannels+", profileInterval="+profileInterval);
        int digitsPerValue=4; // default
        try {
           digitsPerValue = Integer.parseInt(getRegister(DIGITS_PER_VALUE[bufferNr]).trim());
        }
        catch(IOException e) {
            if (!e.toString().contains("data == null")) {
                throw e;
            }
        }
        return new BufferStructure(nrOfChannels,digitsPerValue,profileInterval);
    }

    @Override
    public String getDefaultChannelMap() {
        return "4";
    }

    @Override
    protected ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException {
       try {
           ProfileData profileData=null;
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
                   if (data==null)
                       throw new IOException("Profiledatabuffer "+i+" is empty or not configured! ChannelMap property might be wrong!");
                   datas.add(data);
               }
           }

           SCTMProfileMultipleBufferFCL sctmp = new SCTMProfileMultipleBufferFCL(datas,getChannelMap(),bufferStructures);
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

}