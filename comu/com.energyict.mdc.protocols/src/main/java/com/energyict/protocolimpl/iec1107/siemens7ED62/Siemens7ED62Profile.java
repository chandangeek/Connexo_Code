/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Siemens7ED62Profile.java
 *
 * Created on 5 juli 2004, 17:57
 */

package com.energyict.protocolimpl.iec1107.siemens7ED62;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.Channel;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWLogbook;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class Siemens7ED62Profile extends VDEWProfile {

    /** Creates a new instance of Siemens7ED62Profile */
    public Siemens7ED62Profile(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
        super(meterExceptionInfo,protocolLink,abstractVDEWRegistry);
    }

    public ProfileData getProfileData(Calendar fromCalendar, Calendar toCalendar, int nrOfChannels, int profileId, boolean includeEvents) throws IOException {
        byte[] data = readRawData(fromCalendar, toCalendar, profileId);
        byte[] logbook = null;
        if (includeEvents) logbook = new VDEWLogbook(getMeterExceptionInfo(),getProtocolLink()).readRawLogbookData(fromCalendar, toCalendar, 98);
        return parse(logbook, data, nrOfChannels);
    }

    private ProfileData parse(byte[] logbook, byte[] data, int nrOfChannels) throws IOException {
        ProfileData profileData = buildProfileData(data,nrOfChannels);
        if (logbook != null) addLogbookEvents(logbook,profileData);
        return profileData;
    }

    protected int gotoNextOpenBracket(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == '(') break;
            i++;
            if (i>=responseData.length) break;
        }
        return i;
    }

    protected int gotoNextCR(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == '\r') break;
            i++;
            if (i>=responseData.length) break;
        }
        return i;
    }

    private Calendar parseDateTime(byte[] data,int iOffset) throws IOException {
        // data at 0 offset 0 is the DST indicator?????
        int dst;
        dst = ProtocolUtils.hex2nibble(data[iOffset]);
        Calendar calendar = ProtocolUtils.initCalendar(dst==0x01, getProtocolLink().getTimeZone());
        calendar.clear();
        calendar.set(calendar.YEAR,(int)(2000+(int)ProtocolUtils.bcd2byte(data,1+iOffset)));
        calendar.set(calendar.MONTH,(int)((int)ProtocolUtils.bcd2byte(data,3+iOffset)-1));
        calendar.set(calendar.DAY_OF_MONTH,(int)ProtocolUtils.bcd2byte(data,5+iOffset));
        calendar.set(calendar.HOUR_OF_DAY,(int)ProtocolUtils.bcd2byte(data,7+iOffset));
        calendar.set(calendar.MINUTE,(int)ProtocolUtils.bcd2byte(data,9+iOffset));
        return calendar;
    }

    private Calendar parseLogbookDateTime(byte[] data,int iOffset) throws IOException {
        int dst;
        dst = ProtocolUtils.hex2nibble(data[iOffset]);
        Calendar calendar = ProtocolUtils.initCalendar(dst==0x01, getProtocolLink().getTimeZone());
        calendar.set(calendar.YEAR,(int)(2000+(int)ProtocolUtils.bcd2byte(data,1+iOffset)));
        calendar.set(calendar.MONTH,(int)((int)ProtocolUtils.bcd2byte(data,3+iOffset)-1));
        calendar.set(calendar.DAY_OF_MONTH,(int)ProtocolUtils.bcd2byte(data,5+iOffset));
        calendar.set(calendar.HOUR_OF_DAY,(int)ProtocolUtils.bcd2byte(data,7+iOffset));
        calendar.set(calendar.MINUTE,(int)ProtocolUtils.bcd2byte(data,9+iOffset));
        calendar.set(calendar.SECOND,(int)ProtocolUtils.bcd2byte(data,11+iOffset));
        return calendar;
    }

    private String parseFindString(byte[] data,int iOffset) {
        int start=0,stop=0,i=0;
        if (iOffset >= data.length) return null;
        for (i=0;i<data.length;i++) {
            if (data[i+iOffset]=='(')  start = i;
            if (data[i+iOffset]==')') {
                stop = i;
                break;
            }
        }
        byte[] strparse=new byte[stop-start-1];
        for (i=0;i<(stop-start-1);i++) strparse[i]=data[i+start+1+iOffset];
        return new String(strparse);
    } // private String parseFindString(byte[] data,int iOffset)


    private static final int LOGGER_CLEARED = 0x4000;
    private static final int LOGBOOK_CLEARED = 0x2000;
    private static final int WATCHDOG = 0x0600;
    private static final int CPU_ERROR = 0x0400;
    private static final int PARAMETER_SETTING = 0x0100;
    private static final int POWER_FAILURE = 0x80;
    private static final int POWER_RECOVERY = 0x40;
    private static final int DEVICE_CLOCK_SET = 0x20;
    private static final int DEVICE_RESET = 0x10;
    private static final int SEASONAL_SWITCHOVER = 0x08;
    private static final int DISTURBED_MEASURE = 0x04;
    private static final int RUNNING_RESERVE_EXHAUSTED = 0x02;
    private static final int FATAL_DEVICE_ERROR = 0x01;

    private long mapLogCodes2MeterEvent(long lLogCode) {
        // Phase errors...
        if ((lLogCode >= 0x111) && (lLogCode<=0x303)) {
            return MeterEvent.METER_ALARM;
        }

        if ((lLogCode >= 0x81) && (lLogCode<=0x83)) {
            return MeterEvent.METER_ALARM;
        }

        if ((lLogCode >= 0x41) && (lLogCode<=0x43)) {
            return MeterEvent.METER_ALARM;
        }

        switch((int)lLogCode) {
            case PARAMETER_SETTING:
                return(MeterEvent.CONFIGURATIONCHANGE);

            case DEVICE_CLOCK_SET:
                return(MeterEvent.SETCLOCK);

            case WATCHDOG:
                return(MeterEvent.WATCHDOGRESET);

            case FATAL_DEVICE_ERROR:
                return(MeterEvent.FATAL_ERROR);
            case CPU_ERROR:
                return(MeterEvent.HARDWARE_ERROR);
            case POWER_FAILURE:
                return(MeterEvent.POWERDOWN);
            case POWER_RECOVERY:
                return(MeterEvent.POWERUP);

            case LOGGER_CLEARED:
                return MeterEvent.CLEAR_DATA;

            case DEVICE_RESET:
            case LOGBOOK_CLEARED:
            case SEASONAL_SWITCHOVER:
            case DISTURBED_MEASURE:
            case RUNNING_RESERVE_EXHAUSTED:
            default:
                return MeterEvent.OTHER;

        } // switch(lLogCode)

    } // private void mapLogCodes2MeterEvent(long lLogCode)

    private long mapStatus2IntervalStatus(long lLogCode) {

        switch((int)lLogCode) {
            case DISTURBED_MEASURE:
                return IntervalData.CORRUPTED;

            case PARAMETER_SETTING:
                return IntervalData.CONFIGURATIONCHANGE;

            case DEVICE_CLOCK_SET:
                return IntervalData.SHORTLONG;

            case POWER_FAILURE:
                return IntervalData.POWERDOWN;

            case POWER_RECOVERY:
                return IntervalData.POWERUP;

        } // switch(lLogCode)

        return 0;

    } // private void mapStatus2IntervalStatus(long lLogCode)

    private void verifyChannelMap(ChannelMap channelMap) throws IOException {
        if (!getProtocolLink().getChannelMap().hasEqualRegisters(channelMap))
            throw new IOException("verifyChannelMap() profile channelmap registers ("+channelMap.getChannelRegisterMap()+") different from configuration channelmap registers ("+getProtocolLink().getChannelMap().getChannelRegisterMap()+")");
    }

    ProfileData buildProfileData(byte[] responseData, int nrOfChannels) throws IOException {
        ProfileData profileData;
        Calendar calendar;
        int status=0,eiStatus=0;
        byte bNROfValues=0;
        byte bInterval=0;
        String response;
        int channelMask=0;
        int t;

        // We suppose that the profile contains nr of channels!!
        try {
            calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
            profileData = new ProfileData();
            for (t=0;t<nrOfChannels;t++) {
                ChannelInfo chi = new ChannelInfo(t,"iskraEmeco_channel_"+t, Unit.get(""));
                Channel channel = getProtocolLink().getChannelMap().getChannel(t);
                if (channel.isCumul()) {
                   chi.setCumulativeWrapValue(channel.getWrapAroundValue());
                }
                profileData.addChannel(chi);
            }

            int i=0;
            while(true) {
                if (responseData[i] == 'P') {
                    i+=4; // skip P.0x
                    i=gotoNextOpenBracket(responseData,i);

                    response = parseFindString(responseData,i);
//                    if (response.compareTo("ER38") == 0)
//                        throw new IOException("No entries in object list.");
//                    else if (response.indexOf("ER") != -1)
//                        throw new IOException("Error retrieving profile, "+response);

                    calendar = parseDateTime(responseData,i+1);
                    i=gotoNextOpenBracket(responseData,i+1);
                    status = Integer.parseInt(parseFindString(responseData,i),16);
                    eiStatus = parseStatus2IntervalStatus(status);
                    i=gotoNextOpenBracket(responseData,i+1);
                    bInterval = (byte)Integer.parseInt(parseFindString(responseData,i));
                    i=gotoNextOpenBracket(responseData,i+1);
                    bNROfValues = ProtocolUtils.bcd2nibble(responseData,i+1);
                    if (bNROfValues > nrOfChannels)
                        throw new IOException("buildProfileData() error, mismatch between nrOfChannels and profile columns! Adjust the ChannelMap!");

                    List channels=new ArrayList();
                    for (t=0;t<bNROfValues;t++) {
                        i=gotoNextOpenBracket(responseData,i+1);
                        // add channel to list
                        channels.add(new Channel(parseFindString(responseData,i)));

                        i=gotoNextOpenBracket(responseData,i+1);
                        // set channel unit
                        ((ChannelInfo)profileData.getChannel(t)).setUnit(Unit.get(parseFindString(responseData,i)));
                    }

                    verifyChannelMap(new ChannelMap(channels));

                    i= gotoNextCR(responseData,i+1);
                }
                else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
                    i+=1; // skip
                }
                else {
                    // Fill profileData
                    IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()));
                    for (t=0;t<bNROfValues;t++) {
                        i=gotoNextOpenBracket(responseData,i);
                        intervalData.addValue(new BigDecimal(parseFindString(responseData,i)));
                        i++;
                    }
                    intervalData.addStatus(eiStatus);
                    profileData.addInterval(intervalData);
                    calendar.add(calendar.MINUTE,bInterval);
                    i= gotoNextCR(responseData,i+1);
                    eiStatus=0;
                }
                if (i>=responseData.length) break;
            } // while(true)
        }
        catch(IOException e) {
            throw new IOException("buildProfileData> "+e.getMessage());
        }

        return profileData;
    } // ProfileData buildProfileData(byte[] responseData) throws IOException

    private int parseStatus2IntervalStatus(int status) {
        int t,eiStatus=0;
        status &= (SEASONAL_SWITCHOVER^0xFFFF);
        for (t=0;t<16;t++) {
            if ((status & (0x01<<t)) != 0) {
                eiStatus |= (int)mapStatus2IntervalStatus((long)(status&(0x01<<t))&0xFF);
            }
        }
        return eiStatus;
    }

    private void addLogbookEvents(byte[] logBook, ProfileData profileData) throws IOException {
        Calendar calendar;
        int status=0;
        try {
            int i=0;
            while(true) {
                i=gotoNextOpenBracket(logBook,i);
                if (i>=logBook.length) break;
                i++;
                calendar = parseLogbookDateTime(logBook,i);
                i=gotoNextOpenBracket(logBook,i);
                i++;
                status = Integer.parseInt(parseFindString(logBook,i),16);
                //System.out.println(calendar.getTime()+" 0x"+Integer.toHexString(status));
                profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                     (int)mapLogCodes2MeterEvent((long)status),
                                     status&0xffff));
            } // while(true) {
        }
        catch(IOException e) {
            throw new IOException("addLogbookEvents> "+e.getMessage());
        }
    }

}
