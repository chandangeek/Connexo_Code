/*
 * S4LoadProfile.java
 *
 * Created on 9 november 2005, 15:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.ansi.c12.AbstractResponse;
import com.energyict.protocolimpl.ansi.c12.ResponseIOException;
import com.energyict.protocolimpl.ansi.c12.tables.EventEntry;
import com.energyict.protocolimpl.ansi.c12.tables.EventLog;
import com.energyict.protocolimpl.ansi.c12.tables.HistoryEntry;
import com.energyict.protocolimpl.ansi.c12.tables.HistoryLog;
import com.energyict.protocolimpl.ansi.c12.tables.IntervalFormat;
import com.energyict.protocolimpl.ansi.c12.tables.IntervalSet;
import com.energyict.protocolimpl.ansi.c12.tables.LoadProfileBlockData;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables.EventLogMfgCodeFactory;
import com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables.UnitOfMeasure;
import com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables.UnitOfMeasureFactory;

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
public class S4LoadProfile {

    private static final int DEBUG=0;

    S4 s4;

    /** Creates a new instance of S4LoadProfile */
    public S4LoadProfile(S4 s4) {
       this.s4=s4;
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
               buildHistoryLog(profileData,lastReading,to);
               //buildEventLog(profileData,lastReading,to);
               profileData.applyEvents(s4.getProfileInterval()/60);
            }
            catch(ResponseIOException e) {
                if (e.getReason()==AbstractResponse.IAR) // table does not exist!
                   s4.getLogger().warning("No Logging available. Respective tables do not exist in the meter.");
                else
                   throw e;
            }
        }

        if (DEBUG>=3) System.out.println(profileData);
        profileData.sort();
        return profileData;
    }

    private void buildHistoryLog(ProfileData profileData, Date lastReading, Date to) throws IOException {
       // oredr = 0 oldest -> newest

       if (DEBUG>=1) System.out.println("KV_DEBUG> "+s4.getStandardTableFactory().getHistoryLogControlTable());
       if (DEBUG>=1) System.out.println("KV_DEBUG> "+s4.getStandardTableFactory().getActualLogTable());

       List meterHistorys = new ArrayList();
       int validHistoryCount=0;
       boolean futurelogcheck=true;
       boolean leaveLoop=false;

       while(!leaveLoop) {

           HistoryLog historyLog = s4.getStandardTableFactory().getHistoryLogDataTable().getHistoryLog();
           int nrOfValidEntries = historyLog.getNrOfValidentries();

           if (futurelogcheck) {

               HistoryEntry[] historyEntries = historyLog.getEntries();
               for (int i=0;i<historyEntries.length;i++) {
                   if ((validHistoryCount++ >=(nrOfValidEntries-1)) || (historyEntries[i].getHistoryTime().before(to))) {
                       futurelogcheck=false;
                       break;
                   }
                   if (DEBUG>=1) System.out.println("KV_DEBUG>(1) historys "+historyEntries[i].getHistoryTime()+" is before "+lastReading+" ?");
               }
           }


           if (!futurelogcheck) {
               HistoryEntry[] historyEntries = historyLog.getEntries();
               for (int i=0;i<historyEntries.length;i++) {
                   if (DEBUG>=1) System.out.println("KV_DEBUG>(2) historys "+historyEntries[i].getHistoryTime()+" is before "+lastReading+" ?");
                   if ((validHistoryCount++ >=(nrOfValidEntries-1)) || (historyEntries[i].getHistoryTime().before(lastReading))) {
                       leaveLoop=true;
                       break;
                   }

                   meterHistorys.add(createMeterEvent(historyEntries[i]));
                   if (DEBUG>=1) System.out.println("KV_DEBUG>(3) historys "+historyEntries[i].getHistoryTime()+" is before "+lastReading+" ?");
               }

               //break; // KV_DEBUG

           }

       } // while(true)

       profileData.setMeterEvents(meterHistorys);

    } // private void buildHistoryLog(ProfileData profileData, Date lastReading, Date to) throws IOException

    private void buildEventLog(ProfileData profileData, Date lastReading, Date to) throws IOException {
       // oredr = 0 oldest -> newest

       if (DEBUG>=1) System.out.println("KV_DEBUG> "+s4.getStandardTableFactory().getEventLogControlTable());
       if (DEBUG>=1) System.out.println("KV_DEBUG> "+s4.getStandardTableFactory().getActualLogTable());

       List meterEvents = new ArrayList();
       int validEventCount=0;
       boolean futurelogcheck=true;
       boolean leaveLoop=false;

       while(!leaveLoop) {

           EventLog eventLog = s4.getStandardTableFactory().getEventLogDataTable().getEventLog();
           int nrOfValidEntries = eventLog.getNrOfValidentries();

           if (futurelogcheck) {

               EventEntry[] eventEntries = eventLog.getEntries();
               for (int i=0;i<eventEntries.length;i++) {
                   if ((validEventCount++ >=(nrOfValidEntries-1)) || (eventEntries[i].getEventTime().before(to))) {
                       futurelogcheck=false;
                       break;
                   }
                   if (DEBUG>=1) System.out.println("KV_DEBUG>(1) historys "+eventEntries[i].getEventTime()+" is before "+lastReading+" ?");
               }
           } // if (futurelogcheck)


           if (!futurelogcheck) {
               EventEntry[] eventEntries = eventLog.getEntries();
               for (int i=0;i<eventEntries.length;i++) {
                   if (DEBUG>=1) System.out.println("KV_DEBUG>(2) historys "+eventEntries[i].getEventTime()+" is before "+lastReading+" ?");
//                   if ((validEventCount++ >=(nrOfValidEntries-1)) || (eventEntries[i].getEventTime().before(lastReading))) {
//                       leaveLoop=true;
//                       break;
//                   }

                   meterEvents.add(createMeterEvent(eventEntries[i]));
                   if (DEBUG>=1) System.out.println("KV_DEBUG>(3) historys "+eventEntries[i].getEventTime()+" is before "+lastReading+" ?");
               }

               break; // KV_DEBUG

           } // if (!futurelogcheck)

       } // while(true)

       profileData.setMeterEvents(meterEvents);
    }

    private MeterEvent createMeterEvent(HistoryEntry historyEntry) {
        EventLogMfgCodeFactory eventFact = new EventLogMfgCodeFactory();
        int eiCode = eventFact.getEICode(historyEntry.getHistoryCode().getProcedureNr(),historyEntry.getHistoryCode().isStdVsMfgFlag());
        String text = eventFact.getEvent(historyEntry.getHistoryCode().getProcedureNr(),historyEntry.getHistoryCode().isStdVsMfgFlag())+", "+
                      eventFact.getArgument(historyEntry.getHistoryCode().getProcedureNr(),historyEntry.getHistoryCode().isStdVsMfgFlag());
        int protocolCode = historyEntry.getHistoryCode().getProcedureNr() | (historyEntry.getHistoryCode().isStdVsMfgFlag()?0x8000:0);
        return new MeterEvent(historyEntry.getHistoryTime(),eiCode,protocolCode,text);
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
        int nrOfValidBlocks=s4.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidBlocks();
        int nrOfValidIntervals=s4.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals();
        int block2read = s4.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getLastBlockElement();
        int maxNrOfBlocks=s4.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlocksSet()[0];
        int nrOfIntervalsPerBlock=s4.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlockIntervalsSet()[0];
        int blockOrder = s4.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getBlockOrder();
        int intervalOrder = s4.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getIntervalOrder();
        boolean currentDayBlock=true;
        int profileInterval = s4.getProfileInterval();

        if (DEBUG>=3) System.out.println("KV_DEBUG> "+s4.getStandardTableFactory().getLoadProfileStatusTable());
        if (DEBUG>=3) System.out.println("KV_DEBUG> "+s4.getStandardTableFactory().getActualLoadProfileTable());

        // read the block headers
        lpbd = null;
        // **********************************************************************************************************************************
        // read the blocks
        while(true) {
            s4.getManufacturerProcedureFactory().setLoadProfileReadControl(block2read==0?0:1,block2read, 1);
            lpbd = s4.getStandardTableFactory().getLoadProfileDataSetTableNormalRead(0).getLoadProfileDataSet().getLoadProfileDataSets()[0];

        if (DEBUG>=3) System.out.println("KV_DEBUG> lastReading="+lastReading+", block2read "+block2read+", 2 lpbd.getBlockEndTime()="+lpbd.getBlockEndTime());

            if ((validBlockCount++ >=(nrOfValidBlocks-1)) || (lpbd.getBlockEndTime().before(lastReading))) break;
            loadProfileBlockDatas.add(lpbd);
            if (block2read++ >= (maxNrOfBlocks-1))
                block2read = 0;
        } // while(true)


        List intervalDatas = new ArrayList();
        Iterator it = loadProfileBlockDatas.iterator();
        while(it.hasNext()) {
            lpbd = (LoadProfileBlockData)it.next();

            Calendar cal = Calendar.getInstance(s4.getTimeZone());
            cal.setTime(lpbd.getBlockEndTime());

            if (DEBUG>=3) System.out.println("KV_DEBUG> cal="+cal.getTime());

            IntervalSet[] intervalSets = lpbd.getLoadProfileInterval();
            int nrOfIntervals = currentDayBlock?nrOfValidIntervals:intervalSets.length;
            int startInterval = currentDayBlock?nrOfIntervalsPerBlock-nrOfValidIntervals:0;
            if (DEBUG>=3) System.out.println("KV_DEBUG> nrOfIntervals="+nrOfIntervals+", startInterval="+startInterval+", nrOfIntervalsPerBlock="+nrOfIntervalsPerBlock+", nrOfValidIntervals="+nrOfValidIntervals);

            for (int i=startInterval;i<nrOfIntervalsPerBlock;i++) {



                //if (!ParseUtils.isOnIntervalBoundary(cal,s4.getProfileInterval())) {

                if (i==(nrOfIntervals-1)) {
                     ParseUtils.roundDown2nearestInterval(cal,s4.getProfileInterval());
                     continue;
                }
                //}

                IntervalSet intervalSet = intervalSets[i];
                IntervalFormat[] values = intervalSet.getIntervalData();
                IntervalData intervalData = new IntervalData(cal.getTime(),intervalSet.getCommon2EIStatus(),intervalSet.getCommonStatus());
                for (int channel=0;channel<s4.getNumberOfChannels();channel++) {
                    int protocolStatus = intervalSet.getChannelStatus(channel);
                    int eiStatus = intervalSet.getchannel2EIStatus(channel);


                    BigDecimal bd = (BigDecimal)values[channel].getValue(); // raw value
                    intervalData.addValue(bd, protocolStatus, eiStatus);
                } // for (int channel=0;channel<s4.getNumberOfChannels();channel++)
                intervalDatas.add(intervalData);

if (DEBUG>=3) System.out.println("KV_DEBUG> cal interval="+cal.getTime());
                cal.add(Calendar.SECOND,(-1)*s4.getProfileInterval());


            } // for (int i=0;i<intervalSets.length;i++)
            profileData.setIntervalDatas(intervalDatas);
            currentDayBlock=false;
        } // while(it.hasNext())
    }

    private void buildChannelInfo(ProfileData profileData) throws IOException {
        // build channelunits
        for (int channel=0;channel<s4.getNumberOfChannels();channel++) {
            int sourceIndex = s4.getStandardTableFactory().getLoadProfileControlTable().getLoadProfileSelectionSet1()[channel].getLoadProfileSourceSelect();
            UnitOfMeasure uom = UnitOfMeasureFactory.findUnitOfMeasure(sourceIndex);
            com.energyict.protocol.ChannelInfo channelInfo = new com.energyict.protocol.ChannelInfo(channel, "S4_channel_"+channel, uom.getUnit());
            if (uom.isPOWERMultiplier())
                channelInfo.setMultiplier(s4.getManufacturerTableFactory().getMeterFactors().getEnergyMultiplier());
            else if (uom.isVOLTMultiplier())
                channelInfo.setMultiplier(s4.getManufacturerTableFactory().getMeterStatus().getVoltageMultiplier());
            else if (uom.isCURRENTMultiplier())
                channelInfo.setMultiplier(s4.getManufacturerTableFactory().getServiceTypeTable().getCurrentMultiplier());
            profileData.addChannel(channelInfo);
        }
    }

    private void waitUntilTimeValid() throws IOException {
        Date date=null;
        long offset2IntervalBoundary=0;
        while(true) {
            date = s4.getTime();
            long profileInterval = s4.getProfileInterval();
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
