/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MarkVProfile.java
 *
 * Created on 2 september 2005, 13:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.transdata.markv.MarkV;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class MarkVProfile {

    MarkV markV;


    /** Creates a new instance of MarkVProfile */
    public MarkVProfile(MarkV markV) {
        this.markV=markV;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData=null;
        int nrOfDays = ParseUtils.getNrOfDays(lastReading,new Date(),markV.getTimeZone());
        int nrOfIntervalsParDay = ((24*3600) / markV.getProfileInterval());

        ProtocolChannelMap pcm = markV.getCommandFactory().getDCCommand().getProtocolChannelMap();

        int nrOfRecords = nrOfDays * nrOfIntervalsParDay * pcm.getNrOfUsedProtocolChannels();

        Calendar cal = checkIfTimeValid();


        List intervals = markV.getCommandFactory().getRCCommand(nrOfRecords).getIntervals();
        profileData = new ProfileData();

        profileData.setChannelInfos(getChannelInfos());
        int eiStatus=0,protocolStatus=0;
        boolean intervalTimeCorrection=false;
        Iterator it = intervals.iterator();
        while(it.hasNext()) {
            int[] channelValues = (int[])it.next();
            eiStatus=protocolStatus=0; // KV_TO_DO
            IntervalData intervalData = new IntervalData(((Calendar)cal.clone()).getTime(),eiStatus,protocolStatus);
            for(int channel=0;channel<channelValues.length;channel++) {
                if (markV.getCommandFactory().getISCommand().isRecordingType2()) {
                    protocolStatus=(channelValues[channel]&0xE000)>>12;
                    eiStatus=intervalTimeCorrection?mapIntervalStatus(protocolStatus)| IntervalStateBits.SHORTLONG:mapIntervalStatus(protocolStatus);
                    channelValues[channel] &= 0x1FFF;
                }
                intervalData.addValue(new Integer(channelValues[channel]), protocolStatus, eiStatus);
            }

            // all interval tuime correction marked intervals are informative
            // see communications manual for the MarkV meter at page 6
            if (protocolStatus != INTERVAL_TIME_CORRECTION) {
                intervalTimeCorrection=false;
                profileData.addInterval(intervalData);
                cal.add(Calendar.SECOND,(-1)*markV.getProfileInterval());
            }
            else intervalTimeCorrection=true;
        }

        if (includeEvents) {
            profileData.setMeterEvents(getMeterEvents());
            profileData.applyEvents(markV.getProfileInterval()/60);
        }

        profileData.sort();

        return profileData;
    }

    private List getMeterEvents() throws IOException {
        return markV.getCommandFactory().getRVCommand(256).getMeterEvents();
    }

    static private final int END_OF_INTERVAL_WITH_POWEROUTAGE = 0x6;
    static private final int ENTIRE_INTERVAL_POWERDOWN = 0x8;
    static private final int END_OF_INTERVAL_DURING_DEMAND_DEFERRAL = 0xA;
    static private final int INTERVAL_TIME_CORRECTION = 0x2;
    static private final int END_OF_INTERVAL_WITH_DIAGNOSTIC_ALARM = 0xE;
    static private final int PHASE_FAILURE_ALARM = 0xC;
    static private final int HARMONIC_DISTORTION_ALARM = 0x4;

    private int mapIntervalStatus(int protocolStatus) {
        switch(protocolStatus) {
            case END_OF_INTERVAL_WITH_POWEROUTAGE: {
                return IntervalStateBits.POWERDOWN|IntervalStateBits.POWERUP;
            } // END_OF_INTERVAL_WITH_POWERDOWN
            case ENTIRE_INTERVAL_POWERDOWN: {
                return IntervalStateBits.MISSING;
            } // ENTIRE_INTERVAL_POWERDOWN
            case END_OF_INTERVAL_DURING_DEMAND_DEFERRAL: {
                return IntervalStateBits.OTHER;
            } // END_OF_INTERVAL_DURING_DEMAND_DEFERRAL
            case INTERVAL_TIME_CORRECTION: {
                return IntervalStateBits.SHORTLONG;
            } // INTERVAL_TIME_CORRECTION
            case END_OF_INTERVAL_WITH_DIAGNOSTIC_ALARM: {
                return IntervalStateBits.OTHER;
            } // END_OF_INTERVAL_WITH_DIAGNOSTIC_ALARM
            case PHASE_FAILURE_ALARM: {
                return IntervalStateBits.PHASEFAILURE;
            } // PHASE_FAILURE_ALARM
            case HARMONIC_DISTORTION_ALARM: {
                return IntervalStateBits.OTHER;
            } // HARMONIC_DISTORTION_ALARM
        }
        return 0;
    }


    private List getChannelInfos() throws IOException {
        // KV_TO_DO
        ProtocolChannelMap pcm = markV.getCommandFactory().getDCCommand().getProtocolChannelMap();
        List channelInfos = new ArrayList();
        int count=0;
        for(int channel=0;channel<pcm.getNrOfProtocolChannels();channel++) {
            if (!pcm.isProtocolChannelZero(channel)) {
                MeterIdentification mi = new MeterIdentification(markV.getMeterType());
                if (mi.isMeter()) {
                    // KV_TO_DO
                }
                else {
                    // KV_TO_DO obiscode aanpassen volgens energy or demand...
                    channelInfos.add(new ChannelInfo(count++,"1."+(channel+1)+".82.8.0.255", Unit.get(""))); //,channel+1));
                }
            }
        }
        return channelInfos;
    }

    private Calendar checkIfTimeValid() throws IOException {
        Date date=null;
        long offset2IntervalBoundary=0;
        while(true) {
            date = markV.getTime();
            long profileInterval = markV.getProfileInterval();
            long seconds = date.getTime()/1000;
            offset2IntervalBoundary = seconds%profileInterval;
            if ((offset2IntervalBoundary<5) || (offset2IntervalBoundary>(profileInterval-10))) {
                try {
                    Thread.sleep(5000);
                }
                catch(InterruptedException e) {
                    // absorb
                }
            }
            else break;
        } // while(true)

        Calendar cal = ProtocolUtils.getCleanCalendar(markV.getTimeZone());
        cal.setTime(date);
        cal.add(Calendar.SECOND,(-1)*(int)offset2IntervalBoundary);
        return cal;

    }
}
