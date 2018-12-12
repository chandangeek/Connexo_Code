/*
 * S200Profile.java
 *
 * Created on 1 augustus 2006, 9:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.landisgyr.sentry.s200.core.DataDumpFactory;
import com.energyict.protocolimpl.landisgyr.sentry.s200.core.S200EventsFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Koen
 */
public class S200Profile {

    final int DEBUG=0;

    S200 s200;

    /** Creates a new instance of S200Profile */
    public S200Profile(S200 s200) {
        this.s200=s200;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();

        profileData.setChannelInfos(buildChannelInfo());
        profileData.setIntervalDatas(buildIntervalData(lastReading));
        if (includeEvents) {
            profileData.setMeterEvents(buildMeterEvents());
            profileData.applyEvents(s200.getProfileInterval()/60);
        }


//System.out.println("KV_DEBUG> "+profileData);
        return profileData;
    }

    private List<ChannelInfo> buildChannelInfo() throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (int channelNr=0;channelNr<s200.getNumberOfChannels();channelNr++) {
            channelInfos.add(new ChannelInfo(channelNr,"S200 channel "+(channelNr+1),Unit.get("")));
        }
        return channelInfos;
    }

    private List<MeterEvent> buildMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        DataDumpFactory ddf = new DataDumpFactory(s200.getCommandFactory());
        byte[] rawData = ddf.collectHistoryLogDataBlocks();

        int offset=0;

        while((rawData.length-offset)>=5) {
            int s200EventCode = (int)rawData[offset++]&0xFF;
            if (s200EventCode==0) {
                continue;
            }
            Calendar cal = ProtocolUtils.getCleanCalendar(s200.getTimeZone());
            cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(rawData[offset++])-1);
            cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(rawData[offset++]));
            cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(rawData[offset++]));
            cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(rawData[offset++]));

            if (DEBUG>=1) {
                System.out.println("KV_DEBUG> 1 cal=" + cal.getTime());
            }

            Calendar now = ProtocolUtils.getCalendar(s200.getTimeZone());
            ParseUtils.adjustYear(now,cal);

            if (cal.get(Calendar.YEAR) > now.get(Calendar.YEAR)) {
                cal.set(Calendar.YEAR,now.get(Calendar.YEAR));
            }

            if (DEBUG>=1) {
                System.out.println("KV_DEBUG> 2 cal=" + cal.getTime());
            }

            meterEvents.add(new MeterEvent(cal.getTime(),S200EventsFactory.findEventMapping(s200EventCode).getEiMeterEventCode(),s200EventCode, S200EventsFactory.findEventMapping(s200EventCode).getDescription()));
        }
        return meterEvents;
    }

    private List<IntervalData> buildIntervalData(Date lastReading) throws IOException {

        // calculate nr of blocks
        long lastReadingInSeconds = lastReading.getTime()/1000;
        long nowInSeconds = (new Date()).getTime()/1000;

        int nrOfIntervalsPerChannel = (int)(((nowInSeconds - lastReadingInSeconds) / s200.getProfileInterval())+1);
        int segmentDataSize = s200.getCommandFactory().getLookAtCommand().getSegmentDataSize();

        int nrOfBytesPerChannel=0;
        int nrOfNibbles=0;
        if (segmentDataSize == 0) {
            nrOfBytesPerChannel = nrOfIntervalsPerChannel; // 1 byte
            nrOfNibbles=2;
        }
        else if (segmentDataSize == 1) {
            nrOfBytesPerChannel = (nrOfIntervalsPerChannel+nrOfIntervalsPerChannel/2); // 1.5 byte (12 bit)
            nrOfNibbles=3;
        }
        else if (segmentDataSize == 2) {
            nrOfBytesPerChannel = nrOfIntervalsPerChannel*2; // 2 bytes
            nrOfNibbles=4;
        }
        else if (segmentDataSize == 3) {
            nrOfBytesPerChannel = nrOfIntervalsPerChannel*2; // 2 bytes signed
            nrOfNibbles=4;
        }

        int nrOfBlocks = ((nrOfBytesPerChannel * s200.getNumberOfChannels())/256)+1;

        DataDumpFactory ddf = new DataDumpFactory(s200.getCommandFactory());
        byte[] rawData = ddf.collectLoadProfileDataBlocks(nrOfBlocks);

// KV_DEBUG
        if (DEBUG>=1) {
            System.out.println("KV_DEBUG> nrOfBlocks=" + nrOfBlocks);
        }

        if (DEBUG>=2) {
            ProtocolUtils.printResponseData(rawData);
        }

        List<IntervalData> intervalDatas = new ArrayList<>();
        Calendar cal = ddf.getDumpCommand().getLastEndingInterval();
        int offset=0;
        for (int interval=0;interval<nrOfIntervalsPerChannel;interval++) {
            IntervalData intervalData = new IntervalData(new Date(cal.getTime().getTime()));

            List<Number> numbers = new ArrayList<>(s200.getNumberOfChannels());
            for (int channelNr=0;channelNr<s200.getNumberOfChannels();channelNr++) {
                int value=0;
                for (int nibbleNr=0;nibbleNr<nrOfNibbles;nibbleNr++) {
                    value <<= 4;
                    int nibbleVal = (int)ProtocolUtils.getNibble(rawData, offset++) & 0x0F;
                    value |= nibbleVal;
                }
                if (DEBUG>=1) {
                    System.out.println("KV_DEBUG> interval=" + interval + ", offset=" + offset + ", rawData.length=" + rawData.length + " \n");
                }
                if (s200.getProtocolChannelMap()==null) {
                    numbers.set((s200.getNumberOfChannels()-1)-channelNr, new BigDecimal("" + value));
                }
                else {
                    numbers.set(s200.getProtocolChannelMap().getProtocolChannel(channelNr).getValue(), new BigDecimal("" + value));
                }
            }
            intervalData.addValues(numbers);

            intervalDatas.add(intervalData);
            cal.add(Calendar.SECOND, -1*s200.getProfileInterval());
        }

        return intervalDatas;
    }

}