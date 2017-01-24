/*
 * Metcom3FCL.java
 *
 * Created on 14 december 2004, 18:19
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
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
public class Metcom3FCL extends Metcom3 {

    @Override
    public String getProtocolDescription() {
        return "Metcom3 FCL SCTM";
    }

    private static final int DEBUG=0;

    protected final String[] REG_PROFILEINTERVAL={"70101","70102"};
    protected final String[] DIGITS_PER_VALUE={"62200","63200"};

    @Inject
    public Metcom3FCL(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected BufferStructure getBufferStructure(int bufferNr) throws IOException, UnsupportedException, NoSuchRegisterException {
        //BufferStructure bs = new BufferStructure(getNumberOfChannels(),getNrOfDecades(),getProfileInterval());

        int nrOfChannels = getNumberOfChannels(); //Integer.parseInt(getRegister(REG_NR_OF_CHANNELS[bufferNr]).trim());
        int profileInterval = Integer.parseInt(getRegister(REG_PROFILEINTERVAL[bufferNr]).trim());

        // KV_DEBUG_EXTRA
        //System.out.println("KV_DEBUG_EXTRA> digitsPerValue nrOfChannels="+nrOfChannels+", profileInterval="+profileInterval);
        int digitsPerValue=4; // default
        try {
           digitsPerValue = Integer.parseInt(getRegister(DIGITS_PER_VALUE[bufferNr]).trim());
        }
        catch(IOException e) {
            if (e.toString().indexOf("data == null")<0)
                throw e;
        }



        BufferStructure bs = new BufferStructure(nrOfChannels,digitsPerValue,profileInterval);

        // Nr of channels can be found with
        //System.out.println("KV_DEBUG> "+Integer.parseInt(getRegister(REG_NR_OF_CHANNELS8[0]).trim()));
        //System.out.println("KV_DEBUG> "+Integer.parseInt(getRegister(REG_NR_OF_CHANNELS16[0]).trim()));
        //System.out.println("KV_DEBUG> "+Integer.parseInt(getRegister(REG_NR_OF_CHANNELS8[1]).trim()));
        //System.out.println("KV_DEBUG> "+Integer.parseInt(getRegister(REG_NR_OF_CHANNELS16[1]).trim()));
        //System.out.println("KV_DEBUG> "+bs);
        return bs;
    }

    public String getDefaultChannelMap() {
        return "4";
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
    } // private ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException

}
