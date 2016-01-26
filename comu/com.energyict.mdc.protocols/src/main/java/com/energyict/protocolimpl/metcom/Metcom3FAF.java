/*
 * Metcom3FAF.java
 *
 * Created on 15 december 2004, 10:36
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;

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
public class Metcom3FAF extends Metcom3 {

    @Override
    public String getProtocolDescription() {
        return "Metcom3 FAF SCTM";
    }

    private static final int DEBUG = 0;
    protected final String REG_PROFILEINTERVAL="70300";
    protected final String DIGITS_PER_VALUE="82001";

    @Inject
    public Metcom3FAF(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected BufferStructure getBufferStructure(int bufferNr) throws IOException, UnsupportedException, NoSuchRegisterException {
        //BufferStructure bs = new BufferStructure(getNumberOfChannels(),getNrOfDecades(),getProfileInterval());
        BufferStructure bs=null;
        int nrOfChannels = getNumberOfChannels(); //Integer.parseInt(getRegister(REG_NR_OF_CHANNELS[bufferNr]).trim());
        byte[] data = getRegister(REG_PROFILEINTERVAL).getBytes();
        //if (getStrMeterClass().compareTo("20") == 0) {
        if (this instanceof com.energyict.protocolimpl.sctm.faf.FAF20) {
            int tm[] = new int[2];
            tm[0] = Integer.parseInt((new String(ProtocolUtils.getSubArray2(data, 0, 12))).trim());
            tm[1] = Integer.parseInt((new String(ProtocolUtils.getSubArray2(data, 12, 4))).trim());
            int profileInterval = tm[bufferNr];
            int digitsPerValue = Integer.parseInt(getRegister(DIGITS_PER_VALUE).trim());
            bs = new BufferStructure(getChannelMap().getBuffers()[bufferNr],digitsPerValue,profileInterval);
        }
        else if (this instanceof com.energyict.protocolimpl.sctm.faf.FAF10) {
            int profileInterval = Integer.parseInt((new String(data)).trim());
            int digitsPerValue = Integer.parseInt(getRegister(DIGITS_PER_VALUE).trim());
            bs = new BufferStructure(nrOfChannels,digitsPerValue,profileInterval);
        }
        else {
            throw new IOException("Metcom3FAF, invalid MeterClass property (" + getStrMeterClass() + ")");
        }

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
            ProfileData profileData = null;
            SCTMTimeData from = new SCTMTimeData(calendarFrom);
            SCTMTimeData to = new SCTMTimeData(calendarTo);
            List bufferStructures = new ArrayList();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            List datas = new ArrayList();
            byte[] profileid = new byte[2];
            byte[] data;
            byte[] last = {0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39, 0x39};

            for (int i = 0; i < getChannelMap().getNrOfBuffers(); i++) {

                BufferStructure bs = getBufferStructure(i);
                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG> " + bs);
                }
                bufferStructures.add(bs);
            }

            for (int i = 0; i < getChannelMap().getNrOfBuffers(); i++) {
                if (getChannelMap().useBuffer(i)) {

                    byte[] lastPeriod = new byte[12];
                    lastPeriod[0] = 0x30;
                    lastPeriod[1] = (byte) (0x30 + (byte) i + 1);
                    System.arraycopy(last, 0, lastPeriod, 2, 10);
                    byte[] latestResult = getSCTMConnection().sendRequest(SiemensSCTM.BUFENQ1, lastPeriod);
                    int profileSize = latestResult.length - 10 - 4; //the ten stands for the two dates, the 4 for the status
                    BufferStructure bs = (BufferStructure) bufferStructures.get(i);
                    int digits = (bs.getNrOfDecades() == -1 ? 4 : bs.getNrOfDecades());
                    if ((profileSize / (digits + 2)) != bs.getNrOfChannels()) { // the +2 is for the flags
                        throw new IOException("Profiledatabuffer " + i + " is misconfigured. ChannelMap has " + bs.getNrOfChannels() + ", meter has " + profileSize / (digits + 2) + " channels configured.");
                    }

                    profileid[0] = 0x30;
                    profileid[1] = (byte) (0x30 + (byte) i + 1);
                    baos.reset();
                    baos.write(profileid);
                    baos.write(from.getBUFENQData());
                    baos.write(to.getBUFENQData());
                    data = getSCTMConnection().sendRequest(SiemensSCTM.BUFENQ2, baos.toByteArray());
                    if (data == null) {
                        throw new IOException("Profiledatabuffer " + i + " is empty or not configured! ChannelMap property might be wrong!");
                    }
                    datas.add(data);
                }
            }

            SCTMProfileMultipleBufferFAF sctmp = new SCTMProfileMultipleBufferFAF(datas, getChannelMap(), bufferStructures);
            profileData = sctmp.getProfileData(getProfileInterval(), getTimeZone(), isRemovePowerOutageIntervals());

            if (includeEvents) {
                SCTMSpontaneousBuffer sctmSpontaneousBuffer = new SCTMSpontaneousBuffer(this); //getSCTMConnection(),getTimeZone());
                sctmSpontaneousBuffer.getEvents(calendarFrom, calendarTo, profileData);
                // Apply the events to the channel statusvalues
                profileData.applyEvents(getProfileInterval() / 60);
            }
            return profileData;
        }
        catch (SiemensSCTMException e) {
            throw new IOException("Siemens7ED62, doGetProfileData, SiemensSCTMException, " + e.getMessage());
        }
    }

}