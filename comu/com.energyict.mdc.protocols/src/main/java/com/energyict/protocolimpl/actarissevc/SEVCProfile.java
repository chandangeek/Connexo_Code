/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SEVCProfile.java
 *
 * Created on 22 januari 2003, 13:47
 */

package com.energyict.protocolimpl.actarissevc;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class SEVCProfile {

    int frameSize;
    int[] fieldOffsets = new int[6];
    int[] fieldLengths= new int[6];
    SEVC sevc;

    private Unit[] sevcProfileUnits;

    private static final int LOG_FRAMESIZE=6;

    /** Creates a new instance of SEVCProfile */
    public SEVCProfile(SEVC sevc) throws IOException {
        this.sevc = sevc;
        sevcProfileUnits = new Unit[sevc.getNumberOfChannels()];

        if ((sevc.getProfileInterval() == 300) || (sevc.getProfileInterval() == 900) || (sevc.getProfileInterval() == 1800)) {
            frameSize = 11;
            fieldLengths[0]=3;fieldLengths[1]=5;fieldLengths[2]=3;fieldLengths[3]=4;fieldLengths[4]=5;fieldLengths[5]=2;
            fieldOffsets[0]=0;fieldOffsets[1]=3;fieldOffsets[2]=8;fieldOffsets[3]=11;fieldOffsets[4]=15;fieldOffsets[5]=20;
        }
        else if (sevc.getProfileInterval() == 3600) {
            frameSize = 12;
            fieldLengths[0]=4;fieldLengths[1]=6;fieldLengths[2]=3;fieldLengths[3]=4;fieldLengths[4]=5;fieldLengths[5]=2;
            fieldOffsets[0]=0;fieldOffsets[1]=4;fieldOffsets[2]=10;fieldOffsets[3]=13;fieldOffsets[4]=17;fieldOffsets[5]=22;
        }
        else if (sevc.getProfileInterval() == 86400) {
            frameSize = 13;
            fieldLengths[0]=5;fieldLengths[1]=7;fieldLengths[2]=3;fieldLengths[3]=4;fieldLengths[4]=5;fieldLengths[5]=2;
            fieldOffsets[0]=0;fieldOffsets[1]=5;fieldOffsets[2]=12;fieldOffsets[3]=15;fieldOffsets[4]=19;fieldOffsets[5]=24;
        }
        else throw new IOException("SEVCProfile, wring sevc.getInterval() "+sevc.getProfileInterval());
    }

    public int getFrameSize() {
        return frameSize;
    }

    private static final int GROSS_CONSUMPTION=0;
    private static final int GROSS_CONSUMPTION_CORRECTED=1;
    private static final int AVERAGE_TEMPERATURE=2;
    private static final int AVERAGE_PRESSURE=3;
    private static final int DATE=4;
    private static final int TIME=5;

    public ProfileData getProfile(byte[] intervalData, byte[] logbookData) throws IOException {

        ProfileData profileData=new ProfileData();
        profileData = parseIntervalData(intervalData,profileData);
        profileData = parseLogbookData(logbookData,profileData);

        return profileData;
    }

    private ProfileData parseIntervalData(byte[] data, ProfileData profileData) throws IOException {
        if (data == null) return profileData;
        int nrOfIntervals = data.length/getFrameSize();
        long date,time;
        int i,t;
        int pim = sevc.getSEVCRegisterFactory().getValue("PIM",sevc.getIEC1107Connection()).intValue();

        sevcProfileUnits[GROSS_CONSUMPTION] = Unit.get(BaseUnit.CUBICMETER,pim);
        sevcProfileUnits[GROSS_CONSUMPTION_CORRECTED] = Unit.get(BaseUnit.NORMALCUBICMETER,pim);
        sevcProfileUnits[AVERAGE_TEMPERATURE] = Unit.get(BaseUnit.KELVIN,-1);
        sevcProfileUnits[AVERAGE_PRESSURE] = Unit.get(BaseUnit.BAR,-2);

        for (t=0;t<sevc.getNumberOfChannels();t++)
               profileData.addChannel(new ChannelInfo(t,"sevc_channel_"+t,sevcProfileUnits[t]));

        Calendar calendar = ProtocolUtils.getCleanCalendar(sevc.getTimeZone());

        for (i=0; i<nrOfIntervals ; i++) {

           // Get date time of record
           date = ProtocolUtils.getLongFromNibblesLE(data,
                                                     fieldOffsets[DATE]+getFrameSize()*2*i,
                                                     fieldLengths[DATE]).longValue();
           time = ProtocolUtils.getLongFromNibblesLE(data,
                                                     fieldOffsets[TIME]+getFrameSize()*2*i,
                                                     fieldLengths[TIME]).longValue();
           if ((date != 0) || (time != 0)) {
               calendar.set(Calendar.YEAR,(int)((date/10000)+2000));
               calendar.set(Calendar.MONTH,(int)((date%10000)/100)-1);
               calendar.set(Calendar.DATE,(int)(date%100));
               calendar.set(Calendar.HOUR_OF_DAY,(int)(time/4));
               calendar.set(Calendar.MINUTE,(int)((time%4)*15));
               calendar.set(Calendar.SECOND,0);

               IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()));

               intervalData.addValue(ProtocolUtils.getLongFromNibblesLE(data,
                                                                     fieldOffsets[GROSS_CONSUMPTION]+getFrameSize()*2*i,
                                                                     fieldLengths[GROSS_CONSUMPTION]));
               intervalData.addValue(ProtocolUtils.getLongFromNibblesLE(data,
                                                                              fieldOffsets[GROSS_CONSUMPTION_CORRECTED]+getFrameSize()*2*i,
                                                                              fieldLengths[GROSS_CONSUMPTION_CORRECTED]));
               intervalData.addValue(ProtocolUtils.getLongFromNibblesLE(data,
                                                                fieldOffsets[AVERAGE_TEMPERATURE]+getFrameSize()*2*i,
                                                                fieldLengths[AVERAGE_TEMPERATURE]));
               intervalData.addValue(ProtocolUtils.getLongFromNibblesLE(data,
                                                                    fieldOffsets[AVERAGE_PRESSURE]+getFrameSize()*2*i,
                                                                    fieldLengths[AVERAGE_PRESSURE]));
               profileData.addInterval(intervalData);
           }
        } // for (int i=0; i<nrOfIntervals ; i++)


        return profileData;

    } // parseIntervalData(...)

    private ProfileData parseLogbookData(byte[] data, ProfileData profileData) throws IOException {
        if (data == null) return profileData;
        int nrOfLogs= data.length/LOG_FRAMESIZE;
        int i,t;
        long date,time;
        int log;

        Calendar calendar = ProtocolUtils.getCleanCalendar(sevc.getTimeZone());

        for (i=0;i<nrOfLogs;i++) {
            date = ProtocolUtils.getLongFromNibblesLE(data,LOG_FRAMESIZE*2*i,5).longValue();
            time = ProtocolUtils.getLongFromNibblesLE(data,5+LOG_FRAMESIZE*2*i,5).longValue();

            if ((date != 0) || (time != 0)) {
                calendar.set(Calendar.YEAR,(int)((date/10000)+2000));
                calendar.set(Calendar.MONTH,(int)((date%10000)/100)-1);
                calendar.set(Calendar.DATE,(int)(date%100));
                calendar.set(Calendar.HOUR_OF_DAY,(int)(time/10000));
                calendar.set(Calendar.MINUTE,(int)((time%10000)/100));
                calendar.set(Calendar.SECOND,(int)(time%100));

                log = ProtocolUtils.getLongFromNibblesLE(data,10+LOG_FRAMESIZE*2*i,2).intValue();

                MeterEvent meterEvent = new MeterEvent(new Date(calendar.getTime().getTime()),mapLogCode(log),log);
                profileData.addEvent(meterEvent);
            }

        }

        // Apply the events to the channel statusvalues
        profileData.applyEvents(sevc.getProfileInterval()/60);

        return profileData;

    } // parseLogbookData(...)

    private int mapLogCode(int log) {

       switch(log) {

           case 18: // power up van de SEVC, reset SEVC
               return MeterEvent.POWERUP;
           case 10: // date time change
               return MeterEvent.SETCLOCK;
           case 13:
           case 14:
           case 15:
               return MeterEvent.CONFIGURATIONCHANGE;
           default:
               return MeterEvent.HARDWARE_ERROR;

       }
    }



}
