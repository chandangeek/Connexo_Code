/*
 * KVLoadProfile.java
 *
 * Created on 9 november 2005, 15:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv;

import com.energyict.protocol.*;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureFactory;
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
import com.energyict.protocolimpl.ge.kv.tables.*;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.meteridentification.*;
import com.energyict.cbo.*;
import com.energyict.protocolimplv2.MdcManager;

/**
 *
 * @author Koen
 */
public class GEKVLoadProfile {
    
    private static final int DEBUG=0;
    
    GEKV gekv;
    
    /** Creates a new instance of KVLoadProfile */
    public GEKVLoadProfile(GEKV gekv) {
       this.gekv=gekv;
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
               profileData.applyEvents(gekv.getProfileInterval()/60);
            }
            catch(ResponseIOException e) {
                if (e.getReason()==AbstractResponse.IAR) // table does not exist!
                   gekv.getLogger().warning("No Logging available. Respective tables do not exist in the meter.");
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
       EventLog header = gekv.getStandardTableFactory().getEventLogDataTableHeader().getEventLog(); 
       int order = header.getEventFlags().getOrder();
       int event2Read = order==0?header.getLastEntryElement():0;
       int nrOfValidEntries = header.getNrOfValidentries();
       int validEventCount=0;
       boolean futurelogcheck=true;
       while(true) {
//System.out.println("KV_DEBUG> events "+eventEntry.getEventTime()+" is before "+lastReading+" ?");
           if (futurelogcheck) {
               EventEntry eventEntry = gekv.getStandardTableFactory().getEventLogDataTableEventEntryHeader(event2Read).getEventLog().getEntries()[0];
               if ((validEventCount++ >=(nrOfValidEntries-1)) || (eventEntry.getEventTime().before(to)))
                   futurelogcheck=false;
           }
           if (!futurelogcheck) {
               EventEntry eventEntry = gekv.getStandardTableFactory().getEventLogDataTableEventEntries(event2Read, 1).getEventLog().getEntries()[0];
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
    
    private void checkForReLogon() throws IOException {
        if ((System.currentTimeMillis() - gekv.getTimeStampAtLogon()) > 150000) {
            gekv.disconnect();
            gekv.connect();
        }
    }
    
    private void buildIntervalData(ProfileData profileData, Date lastReading, Date to) throws IOException {
        // get blocks until last interval enddate < lastreading
        // parse blocks to load profile data
        List loadProfileBlockDatas = new ArrayList();
        LoadProfileBlockData lpbd = null;
        int validBlockCount=0;
        int nrOfValidBlocks=gekv.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidBlocks();
        int nrOfValidIntervals=gekv.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals();
        int block2read = gekv.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getLastBlockElement();
        int maxNrOfBlocks=gekv.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlocksSet()[0];

        boolean currentDayBlock=true;

if (DEBUG>=1) System.out.println("KV_DEBUG> "+gekv.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status());        
        
        // calc blocksize
        int intervalsPerBlock = gekv.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlockIntervalsSet()[0];
        int profileInterval = gekv.getProfileInterval();
        Date newTo = new Date(to.getTime());
        
        // read the block headers
        while(true) {
            
            checkForReLogon();            
            
            lpbd = gekv.getStandardTableFactory().getLoadProfileDataSetTableBlockHeader(0,block2read).getLoadProfileDataSet().getLoadProfileDataSets()[0];
            if ((validBlockCount++ >=(nrOfValidBlocks-1)) || (lpbd.getBlockEndTime().before(newTo))) break;
if (DEBUG>=1) System.out.println("KV_DEBUG> skip "+lpbd);
            if (block2read-- <= 0)
                block2read = maxNrOfBlocks-1;
            currentDayBlock=false; // no currentday block anymore! 
        } 
        lpbd = null;
        // read the blocks
        while(true) {
            
            checkForReLogon();            
            
            lpbd = gekv.getStandardTableFactory().getLoadProfileDataSetTableBlockHeader(0,block2read).getLoadProfileDataSet().getLoadProfileDataSets()[0];
if (DEBUG>=1) System.out.println("KV_DEBUG> save "+lpbd);
            if ((validBlockCount++ >=(nrOfValidBlocks-1)) || (lpbd.getBlockEndTime().before(lastReading))) break;
            lpbd = gekv.getStandardTableFactory().getLoadProfileDataSetTable(0,block2read,1).getLoadProfileDataSet().getLoadProfileDataSets()[0];
            loadProfileBlockDatas.add(lpbd);
            if (block2read-- <= 0)
                block2read = maxNrOfBlocks-1;
        } 
        
                
        List intervalDatas = new ArrayList();
        Iterator it = loadProfileBlockDatas.iterator();
        while(it.hasNext()) {
            lpbd = (LoadProfileBlockData)it.next();
            

if (DEBUG>=1) System.out.println("KV_DEBUG> "+lpbd);
            Calendar cal = Calendar.getInstance(gekv.getTimeZone());
            cal.setTime(lpbd.getBlockEndTime());
            IntervalSet[] intervalSets = lpbd.getLoadProfileInterval();
            int nrOfIntervals = currentDayBlock?gekv.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals():intervalSets.length;
            for (int i=(nrOfIntervals-1);i>=0;i--) {
                

                IntervalSet intervalSet = intervalSets[i];
                IntervalFormat[] values = intervalSet.getIntervalData();
                IntervalData intervalData = new IntervalData(cal.getTime(),intervalSet.getCommon2EIStatus(),intervalSet.getCommonStatus());
                for (int channel=0;channel<gekv.getNumberOfChannels();channel++) {
                    int protocolStatus = intervalSet.getChannelStatus(channel);
                    int eiStatus = intervalSet.getchannel2EIStatus(channel);
                    
                    
                    BigDecimal bd = (BigDecimal)values[channel].getValue(); // raw value
//                    if (gekv.getProtocolChannelMap() != null) {
//                        // conversion to engineering units
//                        if (gekv.getProtocolChannelMap().isProtocolChannel(channel)) {
//                            if (gekv.getProtocolChannelMap().getProtocolChannel(channel).getValue()==1) { // engineering values
//                                bd = bd.multiply(BigDecimal.valueOf((long)gekv.getManufacturerTableFactory().getScaleFactorTable().getEnergyScaleFactorVA()));
//                                if (gekv.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
//                                    bd = bd.multiply(BigDecimal.valueOf((long)gekv.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[channel]));
//                                    if (gekv.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[channel] != 1)
//                                        bd = bd.divide(BigDecimal.valueOf((long)gekv.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[channel]),BigDecimal.ROUND_HALF_UP);
//                                }
//                                
//                                bd = bd.movePointLeft(6+3); // u -> k // see kv doc page 97
//                                //bd = bd.setScale(4, BigDecimal.ROUND_HALF_UP);
//                            }
//                        }
//                    }
if (DEBUG>=1) System.out.println("KV_DEBUG> interval "+i+", endtime "+cal.getTime()+", value channel "+channel+" = "+bd);                                                
                    
                    intervalData.addValue(bd, protocolStatus, eiStatus);
                } // for (int channel=0;channel<gekv.getNumberOfChannels();channel++)
                intervalDatas.add(intervalData);
                
                cal.add(Calendar.SECOND,(-1)*gekv.getProfileInterval());

            } // for (int i=0;i<intervalSets.length;i++)
            profileData.setIntervalDatas(intervalDatas);
            currentDayBlock=false;
        } // while(it.hasNext())
    }
    
    private void buildChannelInfo(ProfileData profileData) throws IOException {
        // build channelunits
        for (int channel=0;channel<gekv.getNumberOfChannels();channel++) {
            int sourceIndex = gekv.getStandardTableFactory().getLoadProfileControlTable().getLoadProfileSelectionSet1()[channel].getLoadProfileSourceSelect();
            Unit unit=Unit.get("");
//            if (gekv.getProtocolChannelMap() != null) {
//                // conversion to engineering units
//                if (gekv.getProtocolChannelMap().isProtocolChannel(channel)) {
//                    if (gekv.getProtocolChannelMap().getProtocolChannel(channel).getValue()==1) { // engineering values
//                        SourceInfo sourceUnits = new SourceInfo(gekv);
//                        unit = sourceUnits.getChannelUnit(sourceIndex);
//                    }
//                }
//            }
            
            SourceInfo sourceUnits = new SourceInfo(gekv);
            unit = sourceUnits.getChannelUnit(sourceIndex);
            
            com.energyict.protocol.ChannelInfo channelInfo = new com.energyict.protocol.ChannelInfo(channel, "GEKV_channel_"+channel, unit);
            
            channelInfo.setMultiplier(getMultiplier(channel));
            
            profileData.addChannel(channelInfo);
        }
    }
    
    private BigDecimal getMultiplier(int channel) throws IOException {
        
        BigDecimal bd = BigDecimal.valueOf((long)gekv.getManufacturerTableFactory().getScaleFactorTable().getEnergyScaleFactorVA());
        if (gekv.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
            bd = bd.multiply(BigDecimal.valueOf((long)gekv.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[channel]));
            if (gekv.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[channel] != 1)
                bd = bd.divide(BigDecimal.valueOf((long)gekv.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[channel]),BigDecimal.ROUND_HALF_UP);
        }

        bd = bd.movePointLeft(6+3); // u -> k // see kv doc page 97
        return bd;
    }
    
    
    private void waitUntilTimeValid() throws IOException {
        Date date=null;
        long offset2IntervalBoundary=0;
        while(true) {
            date = gekv.getTime();
            long profileInterval = gekv.getProfileInterval();
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
