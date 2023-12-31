/*
 * KVLoadProfile.java
 *
 * Created on 9 november 2005, 15:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv2;

import com.energyict.cbo.Unit;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.ansi.c12.AbstractResponse;
import com.energyict.protocolimpl.ansi.c12.ResponseIOException;
import com.energyict.protocolimpl.ansi.c12.tables.EventEntry;
import com.energyict.protocolimpl.ansi.c12.tables.EventLog;
import com.energyict.protocolimpl.ansi.c12.tables.IntervalFormat;
import com.energyict.protocolimpl.ansi.c12.tables.IntervalSet;
import com.energyict.protocolimpl.ansi.c12.tables.LoadProfileBlockData;
import com.energyict.protocolimpl.ge.kv2.tables.EventLogMfgCodeFactory;
import com.energyict.protocolimpl.ge.kv2.tables.SourceInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class GEKV2LoadProfile {

    private static final int DEBUG=0;

    GEKV2 gekv2;

    /** Creates a new instance of KVLoadProfile */
    public GEKV2LoadProfile(GEKV2 gekv2) {
       this.gekv2=gekv2;
    }


    public ProfileData getProfileData(Date lastReading, Date to, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();

        /*
         * GET PROFILEDATA ONLY FOR SET 0! The Ansi C12 standard has 4 sets of load profile. KV meters only use set 0, table 64!
         */

        // wait to request profile data until 10 seconds before or after crossboundary to avoid unsynchronized table read!
        // lpstatustable could be read with other actual values than lpdatasettable
        waitUntilTimeValid();

        buildChannelInfo(profileData);
        buildIntervalData(profileData,lastReading,to);
        if (includeEvents) {
            try {
               buildEventLog(profileData,lastReading,to);
               profileData.applyEvents(gekv2.getProfileInterval()/60);
            }
            catch(ResponseIOException e) {
                if (e.getReason()==AbstractResponse.IAR) // table does not exist!
                   gekv2.getLogger().warning("No Logging available. Respective tables do not exist in the meter.");
                else
                   throw e;
            }
        }

        if (DEBUG>=2) System.out.println(profileData);
        profileData.sort();
        return profileData;
    }

    private void buildEventLog(ProfileData profileData, Date lastReading, Date to) throws IOException {
       // oredr = 0 oldest -> newest
       List meterEvents = new ArrayList();
       EventLog header = gekv2.getStandardTableFactory().getEventLogDataTableHeader().getEventLog();
       int order = header.getEventFlags().getOrder();
       int event2Read = order==0?header.getLastEntryElement():0;
       int nrOfValidEntries = header.getNrOfValidentries();
       int validEventCount=0;
       boolean futurelogcheck=true;
       while(true) {
//System.out.println("KV_DEBUG> events "+eventEntry.getEventTime()+" is before "+lastReading+" ?");
           if (futurelogcheck) {
               EventEntry eventEntry = gekv2.getStandardTableFactory().getEventLogDataTableEventEntryHeader(event2Read).getEventLog().getEntries()[0];
               if ((validEventCount++ >=(nrOfValidEntries-1)) || (eventEntry.getEventTime().before(to)))
                   futurelogcheck=false;
           }
           if (!futurelogcheck) {
               EventEntry eventEntry = gekv2.getStandardTableFactory().getEventLogDataTableEventEntries(event2Read, 1).getEventLog().getEntries()[0];
               if ((validEventCount++ >=(nrOfValidEntries-1)) || (eventEntry.getEventTime().before(lastReading))) break;
               meterEvents.add(createMeterEvent(eventEntry));
           }
           if (order == 0) {
               if (event2Read-- == 0)
                   event2Read = nrOfValidEntries-1;
           }
           else {
               if (event2Read++ == (nrOfValidEntries-1))
                      event2Read = 0;
           }
       }
       profileData.setMeterEvents(meterEvents);
    }

    private MeterEvent createMeterEvent(EventEntry eventEntry) {
        EventLogMfgCodeFactory eventFact = new EventLogMfgCodeFactory();
        int eiCode = eventFact.getEICode(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag());
        String text = eventFact.getEvent(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag())+", "+
                      eventFact.getArgument(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag());
        int protocolCode = eventEntry.getEventCode().getProcedureNr() | (eventEntry.getEventCode().isStdVsMfgFlag()?0x8000:0);
        return new MeterEvent(eventEntry.getEventTime(),eiCode,protocolCode,text);
    }

    private void buildIntervalData(ProfileData profileData, Date lastReading, Date to) throws IOException {
        // get blocks until last interval enddate < lastreading
        // parse blocks to load profile data
        List loadProfileBlockDatas = new ArrayList();
        LoadProfileBlockData lpbd = null;
        int validBlockCount=0;
        int nrOfValidBlocks=gekv2.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidBlocks();
        int nrOfValidIntervals=gekv2.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals();
        int block2read = gekv2.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getLastBlockElement();
        int maxNrOfBlocks=gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlocksSet()[0];

        boolean currentDayBlock=true;

        if (DEBUG>=1) System.out.println(gekv2.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status());

        // calc blocksize
        int intervalsPerBlock = gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlockIntervalsSet()[0];
        int profileInterval = gekv2.getProfileInterval();
        Date newTo = new Date(to.getTime()+(long)(profileInterval*intervalsPerBlock*1000));

        // read the block headers
        while(true) {
            lpbd = gekv2.getStandardTableFactory().getLoadProfileDataSetTableBlockHeader(0,block2read).getLoadProfileDataSet().getLoadProfileDataSets()[0];
            if ((validBlockCount++ >=(nrOfValidBlocks-1)) || (lpbd.getBlockEndTime().before(newTo))) break;
            if (DEBUG>=1) System.out.println("KV_DEBUG> skip "+lpbd);
            if (block2read-- <= 0)
                block2read = maxNrOfBlocks-1;
            currentDayBlock=false; // no currentday block anymore!
        }
        lpbd = null;
        // read the blocks
        while(true) {
            lpbd = gekv2.getStandardTableFactory().getLoadProfileDataSetTableBlockHeader(0,block2read).getLoadProfileDataSet().getLoadProfileDataSets()[0];
            if (DEBUG>=1) System.out.println("KV_DEBUG> save "+lpbd);
            if ((validBlockCount++ >=(nrOfValidBlocks-1)) || (lpbd.getBlockEndTime().before(lastReading))) break;
            lpbd = gekv2.getStandardTableFactory().getLoadProfileDataSetTable(0,block2read,1).getLoadProfileDataSet().getLoadProfileDataSets()[0];
            loadProfileBlockDatas.add(lpbd);
            if (block2read-- <= 0)
                block2read = maxNrOfBlocks-1;
        }


        List intervalDatas = new ArrayList();
        Iterator it = loadProfileBlockDatas.iterator();
        while(it.hasNext()) {
            lpbd = (LoadProfileBlockData)it.next();


//System.out.println("KV_DEBUG> "+lpbd);
            Calendar cal = Calendar.getInstance(gekv2.getTimeZone());
            cal.setTime(lpbd.getBlockEndTime());
            IntervalSet[] intervalSets = lpbd.getLoadProfileInterval();
            int nrOfIntervals = currentDayBlock?gekv2.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals():intervalSets.length;
            for (int i=(nrOfIntervals-1);i>=0;i--) {


                IntervalSet intervalSet = intervalSets[i];
                IntervalFormat[] values = intervalSet.getIntervalData();
                IntervalData intervalData = new IntervalData(cal.getTime(),intervalSet.getCommon2EIStatus(),intervalSet.getCommonStatus());
                for (int channel=0;channel<gekv2.getNumberOfChannels();channel++) {
                    int protocolStatus = intervalSet.getChannelStatus(channel);
                    int eiStatus = intervalSet.getchannel2EIStatus(channel);


                    BigDecimal bd = (BigDecimal)values[channel].getValue(); // raw value
//                    if (gekv2.getProtocolChannelMap() != null) {
//                        // conversion to engineering units
//
//                        // KV_TO_DO
//                        // Depending on the UON for the profile data quantity, the engineering value calculation differs!
//                        // See KV2(c) document with all explanation about that. For the moment we only use
//                        // KVAh load profile calculation!
//
//                        if (gekv2.getProtocolChannelMap().isProtocolChannel(channel)) {
//                            if (gekv2.getProtocolChannelMap().getProtocolChannel(channel).getValue()==1) { // engineering values
//
//
//                                SourceInfo si = new SourceInfo(gekv2);
//                                bd = si.basic2engineering(bd,channel);
//
////                                bd = bd.multiply(BigDecimal.valueOf((long)gekv2.getManufacturerTableFactory().getScaleFactorTable().getEnergyScaleFactorVA()));
////                                if (gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
////                                    bd = bd.multiply(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[channel]));
////                                    if (gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[channel] != 1)
////                                        bd = bd.divide(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[channel]),BigDecimal.ROUND_HALF_UP);
////                                }
////                                bd = bd.movePointLeft(6+3); // u -> k // see kv doc page 97
//                                //bd = bd.setScale(4, BigDecimal.ROUND_HALF_UP);
//                            }
//                        }
//                    }
//System.out.println("KV_DEBUG> interval "+i+", endtime "+cal.getTime()+", value channel "+channel+" = "+bd);

                    intervalData.addValue(bd, protocolStatus, eiStatus);
                } // for (int channel=0;channel<gekv2.getNumberOfChannels();channel++)
                intervalDatas.add(intervalData);

                cal.add(Calendar.SECOND,(-1)*gekv2.getProfileInterval());

            } // for (int i=0;i<intervalSets.length;i++)
            profileData.setIntervalDatas(intervalDatas);
            currentDayBlock=false;
        } // while(it.hasNext())
    }

    private void buildChannelInfo(ProfileData profileData) throws IOException {
        // build channelunits
        SourceInfo si = new SourceInfo(gekv2);
        for (int channel=0;channel<gekv2.getNumberOfChannels();channel++) {
            int sourceIndex = gekv2.getStandardTableFactory().getLoadProfileControlTable().getLoadProfileSelectionSet1()[channel].getLoadProfileSourceSelect();
//            Unit unit=Unit.get("");
//            if (gekv2.getProtocolChannelMap() != null) {
//                // conversion to engineering units
//                if (gekv2.getProtocolChannelMap().isProtocolChannel(channel)) {
//                    if (gekv2.getProtocolChannelMap().getProtocolChannel(channel).getValue()==1) { // engineering values
//                        SourceInfo sourceUnits = new SourceInfo(gekv2);
//                        unit = sourceUnits.getChannelUnit(sourceIndex);
//                    }
//                }
//            }
            SourceInfo sourceUnits = new SourceInfo(gekv2);
            Unit unit = sourceUnits.getChannelUnit(sourceIndex);
            com.energyict.protocol.ChannelInfo channelInfo = new com.energyict.protocol.ChannelInfo(channel, "GEKV_channel_"+channel, unit);

            channelInfo.setMultiplier(si.getMultiplier(channel));

            profileData.addChannel(channelInfo);
        }
    }

    private void waitUntilTimeValid() throws IOException {
        Date date=null;
        long offset2IntervalBoundary=0;
        while(true) {
            date = gekv2.getTime();
            long profileInterval = gekv2.getProfileInterval();
            long seconds = date.getTime()/1000;
            offset2IntervalBoundary = seconds%profileInterval;
            if ((offset2IntervalBoundary<5) || (offset2IntervalBoundary>(profileInterval-10))) {
                try {
                    Thread.sleep(5000);
                }
                catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }
            }
            else break;
        } // while(true)
    } // private void waitUntilTimeValid()


}
