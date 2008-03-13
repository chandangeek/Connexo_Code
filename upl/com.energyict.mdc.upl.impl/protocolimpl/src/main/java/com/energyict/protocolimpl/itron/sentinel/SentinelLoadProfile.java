/*
 * SentinelLoadProfile.java
 *
 * Created on 9 november 2005, 15:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.ansi.c12.procedures.*;
import com.energyict.protocolimpl.itron.sentinel.logicalid.*;
import com.energyict.protocolimpl.itron.sentinel.tables.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.logging.*;
import com.energyict.protocol.HalfDuplexEnabler;  
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocolimpl.ansi.c12.tables.*;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.meteridentification.*;
import com.energyict.cbo.*;
/**
 *
 * @author Koen
 */
public class SentinelLoadProfile {
    
    private static final int DEBUG=0;
    
    Sentinel sentinel;
    
    /** Creates a new instance of SentinelLoadProfile */
    public SentinelLoadProfile(Sentinel sentinel) {
       this.sentinel=sentinel;
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
            throw new UnsupportedException("Logbook reading is not supported in this protocolversion. Uncheck the read meter events checkbox!");
//            try {
//               buildHistoryLog(profileData,lastReading,to);
//               //buildEventLog(profileData,lastReading,to);
//               profileData.applyEvents(sentinel.getProfileInterval()/60);
//            }
//            catch(ResponseIOException e) {
//                if (e.getReason()==AbstractResponse.IAR) // table does not exist!
//                   sentinel.getLogger().warning("No Logging available. Respective tables do not exist in the meter.");
//                else 
//                   throw e;
//            }
        }
        
        if (DEBUG>=3) System.out.println(profileData);
        profileData.sort();
        return profileData;
    }
    
    private void buildHistoryLog(ProfileData profileData, Date lastReading, Date to) throws IOException {
       // oredr = 0 oldest -> newest 
        
       if (DEBUG>=1) System.out.println("KV_DEBUG> "+sentinel.getStandardTableFactory().getHistoryLogControlTable());
       if (DEBUG>=1) System.out.println("KV_DEBUG> "+sentinel.getStandardTableFactory().getActualLogTable());
       if (DEBUG>=1) System.out.println("KV_DEBUG> "+sentinel.getStandardTableFactory().getHistoryLogDataTableHeader());
       //if (DEBUG>=1) System.out.println("KV_DEBUG> "+sentinel.getStandardTableFactory().getHistoryLogDataTableHeader());
        
       List meterHistorys = new ArrayList(); 
       int validHistoryCount=0;
       boolean futurelogcheck=true;
       boolean leaveLoop=false;
       
       while(!leaveLoop) {
           
           HistoryLog historyLog = sentinel.getStandardTableFactory().getHistoryLogDataTable().getHistoryLog();
           if (historyLog.getEntries() == null) break;
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
        
       if (DEBUG>=1) System.out.println("KV_DEBUG> "+sentinel.getStandardTableFactory().getEventLogControlTable());
       if (DEBUG>=1) System.out.println("KV_DEBUG> "+sentinel.getStandardTableFactory().getActualLogTable());
        
       List meterEvents = new ArrayList(); 
       int validEventCount=0;
       boolean futurelogcheck=true;
       boolean leaveLoop=false;
       
       while(!leaveLoop) {
           
           EventLog eventLog = sentinel.getStandardTableFactory().getEventLogDataTable().getEventLog();
           int nrOfValidEntries = eventLog.getNrOfValidentries();
                   
           if (DEBUG>=1) System.out.println("KV_DEBUG> eventLog="+eventLog);
           if (DEBUG>=1) System.out.println("KV_DEBUG> nrOfValidEntries="+nrOfValidEntries);
           
           if (futurelogcheck) {
               
               
               
               EventEntry[] eventEntries = eventLog.getEntries();
               
               if (DEBUG>=1) System.out.println("KV_DEBUG> eventLog.getEntries().length="+eventLog.getEntries().length);
               
               for (int i=0;i<eventEntries.length;i++) {
                   if ((validEventCount++ >=(nrOfValidEntries-1)) || (eventEntries[i].getEventTime().before(to))) {
                       futurelogcheck=false;
                       break;
                   }
                   if (DEBUG>=1) System.out.println("KV_DEBUG>(1) historys "+eventEntries[i].getEventTime()+" is before "+lastReading+" ?");
               }
               
               futurelogcheck=false;
               //break;
               
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
           
       } // while(!leaveLoop)
       
       profileData.setMeterEvents(meterEvents);
    }
    
    private MeterEvent createMeterEvent(HistoryEntry historyEntry) {
//        EventLogMfgCodeFactory eventFact = new EventLogMfgCodeFactory();
//        int eiCode = eventFact.getEICode(historyEntry.getHistoryCode().getProcedureNr(),historyEntry.getHistoryCode().isStdVsMfgFlag());
//        String text = eventFact.getEvent(historyEntry.getHistoryCode().getProcedureNr(),historyEntry.getHistoryCode().isStdVsMfgFlag())+", "+
//                      eventFact.getArgument(historyEntry.getHistoryCode().getProcedureNr(),historyEntry.getHistoryCode().isStdVsMfgFlag());
//        int protocolCode = historyEntry.getHistoryCode().getProcedureNr() | (historyEntry.getHistoryCode().isStdVsMfgFlag()?0x8000:0);
//        return new MeterEvent(historyEntry.getHistoryTime(),eiCode,protocolCode,text);
         return null;
    }
    
    
    private MeterEvent createMeterEvent(EventEntry eventEntry) {
//        EventLogMfgCodeFactory eventFact = new EventLogMfgCodeFactory();
//        int eiCode = eventFact.getEICode(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag());
//        String text = eventFact.getEvent(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag())+", "+
//                      eventFact.getArgument(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag());
//        int protocolCode = eventEntry.getEventCode().getProcedureNr() | (eventEntry.getEventCode().isStdVsMfgFlag()?0x8000:0);
//        return new MeterEvent(eventEntry.getEventTime(),eiCode,protocolCode,text);
        return null;
    }

    // using manufacturer tables...
//    private void buildIntervalData2(ProfileData profileData, Date lastReading, Date to) throws IOException {
//        
//        
//        LoadProfilePreliminaryDataRead lppdr = sentinel.getDataReadFactory().getLoadProfilePreliminaryDataRead();
//         
//        if (DEBUG>=1) System.out.println("KV_DEBUG> "+lppdr);        
//        int startBlockOffset = sentinel.getManufacturerProcedureFactory().getLoadProfileStartBlock(lastReading).getStartingBlockOffset();
//        int nrOfBlocks = lppdr.getIndexOfLastLoadProfileBlock()-startBlockOffset+1;
//        int blockSize = (264 * sentinel.getNumberOfChannels())+260;
//        int headersize = 8*sentinel.getNumberOfChannels()+4;
//        
//        if (DEBUG>=1) System.out.println("KV_DEBUG> startBlockOffset="+startBlockOffset+", nrOfBlocks="+nrOfBlocks+", blockSize="+blockSize);        
//
//        for (int block=startBlockOffset;block<(startBlockOffset+nrOfBlocks);block++) {
//            LoadProfileData lpd = sentinel.getManufacturerTableFactory().getLoadProfileDataHeaderOnly(block);
//            if (DEBUG>=1) System.out.println("KV_DEBUG> "+lpd); 
//        }
//        
//    }
    
    private final long MINUTES60=(60*60*1000);
    
    private void buildIntervalData(ProfileData profileData, Date lastReading, Date to) throws IOException {
        
        List loadProfileBlockDatas = new ArrayList();
        LoadProfileBlockData lpbd = null;
        
        int startBlock = sentinel.getManufacturerProcedureFactory().getLoadProfileStartBlock(lastReading).getStartingBlockOffset(); // start block ID using lastreading
        int nrOfValidIntervals = sentinel.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals(); // intervals in last block
        int lastBlock = sentinel.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getLastBlockElement(); // last block ID
        
        int intervalsPerBlock = sentinel.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlockIntervalsSet()[0]; // intervals per block
        int profileInterval = sentinel.getProfileInterval();
        Date newTo = new Date(to.getTime()+(long)(profileInterval*intervalsPerBlock*1000));
        boolean currentDayBlock=false;
        
        // ******************************************************************************************************************************* 
        // read the blocks
        long timeout = System.currentTimeMillis() + MINUTES60;
        while(true) {
            int block = startBlock;
            lpbd = null;
            loadProfileBlockDatas.clear();
            while(true) {

                lpbd = sentinel.getStandardTableFactory().getLoadProfileDataSetTable(0,block,1).getLoadProfileDataSet().getLoadProfileDataSets()[0];
                loadProfileBlockDatas.add(lpbd);
                if (DEBUG>=2) System.out.println("KV_DEBUG> save block "+block+", lpbd end time="+lpbd.getBlockEndTime());
                
                currentDayBlock=(block==lastBlock);
                if  (currentDayBlock || (lpbd.getBlockEndTime().after(newTo))) break;

                block++;
                
                if (((long) (System.currentTimeMillis() - timeout)) > 0) {
                    throw new IOException("SentinelLoadProfile, buildIntervalData(), 60 minutes limit exceed!");
                }                
            } // while(true)
            
            // ******************************************************************************************************************************* 
            // validate
            int lastBlockNew = sentinel.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getLastBlockElement();
            if (lastBlock == lastBlockNew) {
                int nrOfValidIntervalsNew = sentinel.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals(); // intervals in last block                
                if (nrOfValidIntervals <=nrOfValidIntervalsNew) {
                    // OK, continue with parsing
                    break;
                }
            }
            
            if (((long) (System.currentTimeMillis() - timeout)) > 0) {
                throw new IOException("SentinelLoadProfile, buildIntervalData(), 60 minutes limit exceed!");
            }                
            
        } // while(true)

        
        
        // ******************************************************************************************************************************* 
        // parse the blocks
        List intervalDatas = new ArrayList();
        
        for (int block=(loadProfileBlockDatas.size()-1);block >=0;block--) {
            lpbd = (LoadProfileBlockData)loadProfileBlockDatas.get(block);
            
//            if (DEBUG>=1) System.out.println("KV_DEBUG> "+lpbd);
            
            Calendar cal = Calendar.getInstance(sentinel.getTimeZone());
            cal.setTime(lpbd.getBlockEndTime());
if (DEBUG>=2) System.out.println("KV_DEBUG> ************************ Block "+block+", EndTime="+lpbd.getBlockEndTime());
            
            IntervalSet[] intervalSets = lpbd.getLoadProfileInterval();
            int nrOfIntervals = currentDayBlock?nrOfValidIntervals:intervalSets.length;
            for (int i=(nrOfIntervals-1);i>=0;i--) {
                

                IntervalSet intervalSet = intervalSets[i];
                IntervalFormat[] values = intervalSet.getIntervalData();
                IntervalData intervalData = new IntervalData(cal.getTime(),intervalSet.getCommon2EIStatus(),intervalSet.getCommonStatus());
//System.out.println("KV_DEBUG> "+cal.getTime()+", intervalSet.getCommonStatus=0x"+Integer.toHexString(intervalSet.getCommonStatus())+", ");                
                for (int channel=0;channel<sentinel.getNumberOfChannels();channel++) {
//System.out.print("channel="+channel+", intervalSet.getChannelStatus=0x"+Integer.toHexString(intervalSet.getChannelStatus(channel))+", ");
                    int protocolStatus = intervalSet.getChannelStatus(channel);
                    int eiStatus = intervalSet.getchannel2EIStatus(channel);
                    BigDecimal bd = (BigDecimal)values[channel].getValue(); // raw value
                    intervalData.addValue(bd, protocolStatus, eiStatus);
                } // for (int channel=0;channel<sentinel.getNumberOfChannels();channel++)
                intervalDatas.add(intervalData);
                
                
                cal.add(Calendar.SECOND,(-1)*sentinel.getProfileInterval());
if (DEBUG>=2) System.out.println("KV_DEBUG> interval "+i+", time="+cal.getTime());

            } // for (int i=0;i<intervalSets.length;i++)
            profileData.setIntervalDatas(intervalDatas);
            currentDayBlock=false;
        } // while(it.hasNext())
    }    
    
    
    private void buildChannelInfo(ProfileData profileData) throws IOException {
        // build channelunits
        LogicalID[] lids = sentinel.getDataReadFactory().getLoadProfileQuantitiesDataRead().getLogicalIDs();
        for (int channel=0;channel<sentinel.getNumberOfChannels();channel++) {
            com.energyict.protocol.ChannelInfo channelInfo = new com.energyict.protocol.ChannelInfo(channel, "Sentinel_channel_"+channel, lids[channel].getUnit());
            
            // use 6 decimals
            BigDecimal bd = BigDecimal.valueOf((long)sentinel.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[channel], 6).multiply(BigDecimal.valueOf(1000000));
            bd = bd.divide(BigDecimal.valueOf((long)sentinel.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[channel]),BigDecimal.ROUND_HALF_UP);
            channelInfo.setMultiplier(bd);
            profileData.addChannel(channelInfo);
        }
    }

    private void waitUntilTimeValid() throws IOException {
        Date date=null;
        long offset2IntervalBoundary=0;
        while(true) {
            date = sentinel.getTime();
            long profileInterval = sentinel.getProfileInterval();
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
    } // private void waitUntilTimeValid()    
}
