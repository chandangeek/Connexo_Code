/*
 * Metcom3FAF.java
 *
 * Created on 15 december 2004, 10:36
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
public class Metcom3FAF extends Metcom3 {

    private static final int DEBUG = 0;
    protected static final String REG_PROFILEINTERVAL = "70300";
    protected static final String DIGITS_PER_VALUE = "82001";

    public Metcom3FAF(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected BufferStructure getBufferStructure(int bufferNr) throws IOException {
        throw new IOException("Metcom3FAF, invalid MeterClass property (" + getStrMeterClass() + ")");
    }

    @Override
    public String getDefaultChannelMap() {
        return "4";
    }

    @Override
    protected ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException {
        try {
            ProfileData profileData = null;
            SCTMTimeData from = new SCTMTimeData(calendarFrom);
            SCTMTimeData to = new SCTMTimeData(calendarTo);
            List<BufferStructure> bufferStructures = new ArrayList<>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            List<byte[]> datas = new ArrayList<>();
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