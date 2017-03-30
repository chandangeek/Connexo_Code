/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DiscreteSurvey.java
 *
 * Created on 11 maart 2004, 13:10
 */

package com.energyict.protocolimpl.pact.core.survey.discrete;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.base.ProfileDataReverser;
import com.energyict.protocolimpl.pact.core.common.ChannelMap;
import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;
import com.energyict.protocolimpl.pact.core.common.PACTProtocolException;
import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingsInterpreter;
import com.energyict.protocolimpl.pact.core.survey.LoadSurveyInterpreterImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 *
 * @author  Koen
 */
public class DiscreteSurvey extends LoadSurveyInterpreterImpl {

    private static final int DEBUG=0;

    // 3 load survey types type 38, 39 and 40
    public static final int[] MODULO={8000,64000,4000};
    public static final int[] VALUEMASK={0x1FFF,0xFFFF,0xFFF};


    private List surveyDays; // type SurveyDay;
    /** Creates a new instance of DiscreteSurvey */
    /** Creates a new instance of MeterReadingBlocks */
    public DiscreteSurvey(MeterReadingsInterpreter mri,TimeZone timeZone) {
        super(mri,timeZone);
        surveyDays = new ArrayList();
    }

    /*
     * parseData() builds a list of SurveyDay objects containing IntervalData objects for a whole day
     * <BR>
     * The structure of the survey data for discrete surveys is as follows:
     * A parameter is a channel.
     * All values are 16 bit from which some of the highest bits are status bits. The status bits appear only for the FIRST channel.
     * e.g. 2 channels, 2 days, profileinterval = 5 minutes
     * --------------- TODAY ---------------
     * 00:00 today, value channel 1
     * 00:05 today, value channel 1
     * ...
     * 23:55 today, value channel 1
     * EOD block today, value channel 1, contains register value at 0xFFFE, means day is still open, NOT CLOSED YET.
     * 00:00 today, value channel 0 + STATUS BITS FOR ALL CHANNELS of today
     * ...
     * 23:55 today, value channel 0 + STATUS BITS FOR ALL CHANNELS of today
     * EOD block today, value channel 0, contains register value at 0xFFFE, means day is still open, NOT CLOSED YET.
     * --------------- TODAY -1 DAY ---------------
     * 00:00 yesterday, value channel 1
     * 00:05 yesterday, value channel 1
     * ...
     * 23:55 yesterday, value channel 1
     * EOD block yesterday, value channel 1, contains register value at the same value as 00:00 today, value channel 1, means day is closed.
     * 00:00 yesterday, value channel 0 + STATUS BITS FOR ALL CHANNELS of yesterday
     * ...
     * 23:55 yesterday, value channel 0 + STATUS BITS FOR ALL CHANNELS of yesterday
     * EOD block yesterday, value channel 0, contains register value at the same value as 00:00 today, value channel 0, means day is closed.
     * <BR>
     * @param loadSurveyData byte array with survey blocks starting with most recent day, last parameter (channel)
     *                       to oldest day, first parameter (channel)
     */
    protected void parseData(byte[] loadSurveyData) throws IOException {
        buildSurveyDays(loadSurveyData);
        List channelInfos = buildChannelInfos();
        buildProfileData(channelInfos);
    } // void parseData(byte[] data)

    /*
     * Builds a list of SurveyDay objects starting with the most recent day
     *
     */
    private void buildSurveyDays(byte[] loadSurveyData) throws IOException {
        // get interval values and convert if necessary
        // get status flags and build intervalstatus
        int day=0;
        int processed=0;
        do {
            SurveyDay surveyDay = new SurveyDay(getMri(),getTimeZone(),isStatusFlagChannel());
            surveyDay.parseData(loadSurveyData,day);
            if (DEBUG>=1) {
				printSurveyDay(surveyDay);
			}
            surveyDays.add(surveyDay);
            processed += (getMri().getSurveyInfo().getBlocks()*8*getMri().getSurveyInfo().getNrOfChannels());
            day--; // try if there's data for another day...
        } while ((loadSurveyData.length-processed)>8); // we should use 0 but 8 is also OK. 8 bytes is the end of a MRI file taken with PACS.EXE

    } // private void buildSurveyDays(byte[] loadSurveyData)

    private void printSurveyDay(SurveyDay sd) {
        for (int eod=0;eod<sd.getEods().length;eod++) {
            System.out.println("      "+sd.getEods()[eod]);
        }
    }

    private SurveyDay getMostRecentSurveyDay() {
        if (getMri().getProtocolLink().getPACTMode().isPAKNET()) {
            return (SurveyDay)surveyDays.get(surveyDays.size()-1);
        }
        else {
            return (SurveyDay)surveyDays.get(0);
        }
    }

    private List buildChannelInfos() {
        List channelInfos = new ArrayList();
        SurveyDay surveyDay = getMostRecentSurveyDay();
        int channelId=0;

        for (int channel = 0; channel < surveyDay.getEods().length ; channel++) {

           // if !statusflagchannel, continue
           if (!(isStatusFlagChannel() || ((!(EnergyTypeCode.isStatusFlagsChannel(surveyDay.getEods()[channel].getEtype()))) && (!isStatusFlagChannel())))) {
			continue;
		}

           ChannelInfo channelInfo = null;
           if (getChannelMap().getChannelFunction(channelId)==ChannelMap.FUNCTION_DEMAND) {
			channelInfo = new ChannelInfo(channelId,"PACS_CHANNEL_"+(channel+1),EnergyTypeCode.getUnit(surveyDay.getEods()[channel].getEtype(),false));
		} else {
			channelInfo = new ChannelInfo(channelId,"PACS_CHANNEL_"+(channel+1),EnergyTypeCode.getUnit(surveyDay.getEods()[channel].getEtype(),true));
		}

           if (getChannelMap().getChannelFunction(channelId)==ChannelMap.FUNCTION_CUMULATIVE) {
              int modulo = DiscreteSurvey.MODULO[getMri().getSurveyFlagsInfo().getSurtyp()-38];
              channelInfo.setCumulativeWrapValue(new BigDecimal(modulo));
           }

           channelInfos.add(channelInfo);
           channelId++;


        } // for (int channel = 0; channel < surveyDay.getEods().length ; channel++)

        return channelInfos;
    }

    /*
     *  Assemble all intervaldatas from different surverDays together into one intervaldatas list.
     *  Set profiledata's intervaldatas and sort.
     */
    private void buildProfileData(List channelInfos) throws PACTProtocolException {
        SurveyProcessor surveyProcessor = new SurveyProcessor(surveyDays, getChannelMap(), getMri(),isStatusFlagChannel());
        surveyProcessor.process();
        getProfileData().setChannelInfos(channelInfos);
        getProfileData().setIntervalDatas(surveyProcessor.getIntervalDatas());
        getProfileData().sort();
        // In case of PAKNET mode, flip the channels and interval data!
        if (getMri().getProtocolLink().getPACTMode().isPAKNET()) {
             ProfileDataReverser profileDataReverser = new ProfileDataReverser(getProfileData());

             // KV 07082006
             // If a calmu, sprint or premier meter have E4 starting their CLEM program, we still have the possibility to overrule by
             // setting MeterType to 2
             if (!((getMri().getClemProgramName().startsWith("E4")) || getMri().getProtocolLink().isMeterTypeICM200())) {
				profileDataReverser.reverse();
			} else if (getMri().getProtocolLink().isMeterTypeCSP()) {
				profileDataReverser.reverse();
			}

             // create ProfileData again, otherwise, the XML serializer have problems serializing ProfileDataReverser
             ProfileData pd = new ProfileData();
             pd.setIntervalDatas(profileDataReverser.getIntervalDatas());
             pd.setChannelInfos(profileDataReverser.getChannelInfos());
             pd.setMeterEvents(profileDataReverser.getMeterEvents());
             setProfileData(pd);
        }

    } // private void buildProfileData()

    protected int[] doGetEnergyTypeCodes() {
        if ((surveyDays == null) || (surveyDays.size() == 0)) {
			return null;
		}
        SurveyDay surveyDay = getMostRecentSurveyDay();
        int[] energyTypeCodes = new int[surveyDay.getEods().length];
        for (int i=0;i<energyTypeCodes.length;i++) {
            energyTypeCodes[i] = surveyDay.getEods()[i].getEtype();
        }
        return energyTypeCodes;
    }

    protected int doGetNrOfDays(Date from, Date to) throws IOException {
        if (to.getTime() < from.getTime()) {
			throw new IOException("DiscreteSurvey, doGetNrOfBlocks, error ("+from+") > ("+to+")");
		}
        final long ONEDAY=24*60*60*1000;
        long tostd = to.getTime() + (long)getMri().getTimeZone().getOffset(to.getTime());
        long fromstd = from.getTime() + (long)getMri().getTimeZone().getOffset(from.getTime());
        long nrOfDaysToRetrieve = ((tostd/ONEDAY) - (fromstd/ONEDAY)) + 1;
        return (int)nrOfDaysToRetrieve; // +1  // KV 25082004 add 1 day to retrieve extra!!
                                               // done in another way. Ask for 2 days more profiledata is from < oldest date...
    }

    protected int doGetNrOfBlocks(Date from, Date to) throws IOException {
        return (int)doGetNrOfDays(from,to)*getMri().getSurveyInfo().getBlocks()*getMri().getSurveyInfo().getNrOfChannels();
    }

} // public class DiscreteSurvey extends LoadSurveyInterpreterImpl
