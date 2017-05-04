/*
 * Metcom2.java
 *
 * Created on 8 april 2003, 16:35
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.protocolimpl.siemens7ED62.SCTMTimeData;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTMException;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
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

    @Override
    public String getProtocolDescription() {
        return "Metcom2 SCTM";
    }

    // TABENQ1(E1) list numbers
    protected final String REG_NR_OF_CHANNELS="60200";
    protected final String REG_PROFILEINTERVAL="70101";
    protected final String DIGITS_PER_VALUE="82001";

    protected int iNROfChannels=-1;
    protected int iMeterProfileInterval=-1;
    protected int digitsPerDecade=-1;

    @Inject
    public Metcom2(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public int getDigitsPerDecade() throws IOException {
        if (digitsPerDecade == -1) {
            digitsPerDecade = Integer.parseInt(getRegister(DIGITS_PER_VALUE).trim());
        }
        return digitsPerDecade;
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }


    public int getNumberOfChannels() throws IOException {
        if (iNROfChannels == -1) {
           iNROfChannels = Integer.parseInt(getRegister(REG_NR_OF_CHANNELS).trim());
        }
        return iNROfChannels;
    }

    public int getProfileInterval() throws IOException {
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

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
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
           baos.write(SiemensSCTM.PERIODICBUFFERS);
           baos.write(from.getBUFENQData());
           baos.write(to.getBUFENQData());
           byte[] data = getSCTMConnection().sendRequest(SiemensSCTM.BUFENQ2, baos.toByteArray());
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

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    "Timeout",
                    "Retries",
                    "HalfDuplex",
                    "RemovePowerOutageIntervals",
                    "LogBookReadCommand",
                    "IntervalStatusBehaviour",
                    "TimeSetMethod",
                    "Software7E1");
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getRegistersInfo(int extendedLogging) throws IOException {
        return null;
    }

}