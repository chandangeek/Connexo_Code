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

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.ansi.c12.tables.EventEntry;
import com.energyict.protocolimpl.ansi.c12.tables.EventLog;
import com.energyict.protocolimpl.ansi.c12.tables.HistoryEntry;
import com.energyict.protocolimpl.ansi.c12.tables.HistoryLog;
import com.energyict.protocolimpl.ansi.c12.tables.IntervalFormat;
import com.energyict.protocolimpl.ansi.c12.tables.IntervalSet;
import com.energyict.protocolimpl.ansi.c12.tables.LoadProfileBlockData;
import com.energyict.protocolimpl.itron.sentinel.logicalid.LoadProfilePreliminaryDataRead;
import com.energyict.protocolimpl.itron.sentinel.logicalid.LogicalID;
import com.energyict.protocolimpl.itron.sentinel.tables.LoadProfileData;
import com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables.EventLogMfgCodeFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 * @author James Fox
 */
public class SentinelLoadProfile {

    Sentinel sentinel;

    boolean readLoadProfilesChunked = true;
    int chunkSize = 19;

    /** Creates a new instance of SentinelLoadProfile */
    public SentinelLoadProfile(Sentinel sentinel, boolean readLoadProfilesChunked, int chunkSize) {
        this.sentinel=sentinel;
        this.readLoadProfilesChunked = readLoadProfilesChunked;
        this.chunkSize = chunkSize;
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

        if (isReadLoadProfilesChunked()) {
            buildIntervalDataChunked(profileData, lastReading, to);
        } else {
            buildIntervalData(profileData, lastReading, to);
        }

        if (includeEvents) {
            sentinel.getLogger().warning("Not retrieving events, not supported by the protocol");
            //throw new UnsupportedException("Logbook reading is not supported in this protocolversion. Uncheck the read meter events checkbox!");

            /*
            System.out.println("Including events");
            try {
               //buildHistoryLog(profileData,lastReading,to);
               buildEventLog(profileData,lastReading,to);
               profileData.applyEvents(sentinel.getProfileInterval()/60);
            }
            catch(ResponseIOException e) {
                System.out.println("Events exception: " + e);
                e.printStackTrace();
                if (e.getReason()==AbstractResponse.IAR) // table does not exist!
                   sentinel.getLogger().warning("No Logging available. Respective tables do not exist in the meter.");
                else
                   throw e;
            } catch (Throwable t) {
                System.out.println("Events exception: " + t);
                t.printStackTrace();
            }
            */
        }

        getLogger().info(profileData.toString());
        profileData.sort();
        return profileData;
    }

    private Logger getLogger() {
        return sentinel.getLogger();
    }

    private void buildHistoryLog(ProfileData profileData, Date lastReading, Date to) throws IOException {
       // oredr = 0 oldest -> newest

        getLogger().info("KV_DEBUG> "+sentinel.getStandardTableFactory().getHistoryLogControlTable());
        getLogger().info("KV_DEBUG> "+sentinel.getStandardTableFactory().getActualLogTable());
        getLogger().info("KV_DEBUG> "+sentinel.getStandardTableFactory().getHistoryLogDataTableHeader());
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
                   getLogger().info("KV_DEBUG>(1) historys "+historyEntries[i].getHistoryTime()+" is before "+lastReading+" ?");
               }
           }


           if (!futurelogcheck) {
               HistoryEntry[] historyEntries = historyLog.getEntries();
               for (int i=0;i<historyEntries.length;i++) {
                   getLogger().info("KV_DEBUG>(2) historys "+historyEntries[i].getHistoryTime()+" is before "+lastReading+" ?");
                   if ((validHistoryCount++ >=(nrOfValidEntries-1)) || (historyEntries[i].getHistoryTime().before(lastReading))) {
                       leaveLoop=true;
                       break;
                   }

                   meterHistorys.add(createMeterEvent(historyEntries[i]));
                   getLogger().info("KV_DEBUG>(3) historys "+historyEntries[i].getHistoryTime()+" is before "+lastReading+" ?");
               }

               //break; // KV_DEBUG

           }

       } // while(true)

       profileData.setMeterEvents(meterHistorys);

    } // private void buildHistoryLog(ProfileData profileData, Date lastReading, Date to) throws IOException

    private void buildEventLog(ProfileData profileData, Date lastReading, Date to) throws IOException {
       // oredr = 0 oldest -> newest

        getLogger().info("KV_DEBUG> "+sentinel.getStandardTableFactory().getEventLogControlTable());
        getLogger().info("KV_DEBUG> "+sentinel.getStandardTableFactory().getActualLogTable());

       List meterEvents = new ArrayList();
       int validEventCount=0;
       boolean futurelogcheck=true;
       boolean leaveLoop=false;

       while(!leaveLoop) {

           EventLog eventLog = sentinel.getStandardTableFactory().getEventLogDataTable().getEventLog();
           int nrOfValidEntries = eventLog.getNrOfValidentries();

           getLogger().info("KV_DEBUG> events eventLog="+eventLog);
           getLogger().info("KV_DEBUG> events nrOfValidEntries="+nrOfValidEntries);

           if (futurelogcheck) {

               EventEntry[] eventEntries = eventLog.getEntries();

               getLogger().info("KV_DEBUG> events eventLog.getEntries().length="+eventLog.getEntries().length);

               for (int i=0;i<eventEntries.length;i++) {
                   if ((validEventCount++ >=(nrOfValidEntries-1)) || (eventEntries[i].getEventTime().before(to))) {
                       futurelogcheck=false;
                       break;
                   }
                   getLogger().info("KV_DEBUG>(1) events "+eventEntries[i].getEventTime()+" is before "+lastReading+" ?");
               }

               futurelogcheck=false;
               //break;

           } // if (futurelogcheck)


           if (!futurelogcheck) {
               EventEntry[] eventEntries = eventLog.getEntries();
               for (int i=0;i<eventEntries.length;i++) {
                   getLogger().info("KV_DEBUG>(2) events "+eventEntries[i].getEventTime()+" is before "+lastReading+" ?");
//                   if ((validEventCount++ >=(nrOfValidEntries-1)) || (eventEntries[i].getEventTime().before(lastReading))) {
//                       leaveLoop=true;
//                       break;
//                   }

                   meterEvents.add(createMeterEvent(eventEntries[i]));
                   getLogger().info("KV_DEBUG>(3) events "+eventEntries[i].getEventTime()+" is before "+lastReading+" ?");
               }

               break; // KV_DEBUG

           } // if (!futurelogcheck)

       } // while(!leaveLoop)

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

    // using manufacturer tables...
    private void buildIntervalData2(ProfileData profileData, Date lastReading, Date to) throws IOException {


        LoadProfilePreliminaryDataRead lppdr = sentinel.getDataReadFactory().getLoadProfilePreliminaryDataRead();

        getLogger().info("KV_DEBUG> "+lppdr);
        int startBlockOffset = sentinel.getManufacturerProcedureFactory().getLoadProfileStartBlock(lastReading).getStartingBlockOffset();
        int nrOfBlocks = lppdr.getIndexOfLastLoadProfileBlock()-startBlockOffset+1;
        int blockSize = (264 * sentinel.getNumberOfChannels())+260;
        int headersize = 8*sentinel.getNumberOfChannels()+4;

        getLogger().info("KV_DEBUG> startBlockOffset="+startBlockOffset+", nrOfBlocks="+nrOfBlocks+", blockSize="+blockSize);

        for (int block=startBlockOffset;block<(startBlockOffset+nrOfBlocks);block++) {
            LoadProfileData lpd = sentinel.getManufacturerTableFactory().getLoadProfileDataHeaderOnly(block);
            getLogger().info("KV_DEBUG> "+lpd);
        }

    }

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
                getLogger().info("KV_DEBUG> save block "+block+", lpbd end time="+lpbd.getBlockEndTime());

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
            getLogger().info("KV_DEBUG> ************************ Block "+block+", EndTime="+lpbd.getBlockEndTime());

            IntervalSet[] intervalSets = lpbd.getLoadProfileInterval();
            int nrOfIntervals = currentDayBlock?nrOfValidIntervals:intervalSets.length;
            for (int i=(nrOfIntervals-1);i>=0;i--) {
                addIntervalData(intervalDatas, cal, intervalSets[i], i);
            } // for (int i=0;i<intervalSets.length;i++)
            profileData.setIntervalDatas(intervalDatas);
            currentDayBlock=false;
        } // while(it.hasNext())
    }

    private void addIntervalData(List intervalDatas, Calendar cal, IntervalSet intervalSet1, int i) throws IOException {
        IntervalSet intervalSet = intervalSet1;
        IntervalFormat[] values = intervalSet.getIntervalData();
        IntervalData intervalData = new IntervalData(cal.getTime(),intervalSet.getCommon2EIStatus(),intervalSet.getCommonStatus());
        for (int channel=0;channel<sentinel.getNumberOfChannels();channel++) {
            int protocolStatus = intervalSet.getChannelStatus(channel);
            int eiStatus = intervalSet.getchannel2EIStatus(channel);
            BigDecimal bd = (BigDecimal)values[channel].getValue(); // raw value
            intervalData.addValue(bd, protocolStatus, eiStatus);
        } // for (int channel=0;channel<sentinel.getNumberOfChannels();channel++)
        intervalDatas.add(intervalData);
        cal.add(Calendar.SECOND,(-1)*sentinel.getProfileInterval());
        getLogger().info("KV_DEBUG> interval "+i+", time="+cal.getTime());
    }

    public com.energyict.protocolimpl.itron.sentinel.tables.AbstractLoadProfileDataSetTable getLoadProfileDataSetTable(int blockNrOffset, int nrOfBlocksToRequest, int intervalsets, int count, int chunkSize) throws IOException {
        com.energyict.protocolimpl.itron.sentinel.tables.LoadProfileDataSet1Table loadProfileDataSet1Table = new com.energyict.protocolimpl.itron.sentinel.tables.LoadProfileDataSet1Table(sentinel.getStandardTableFactory());
        loadProfileDataSet1Table.setBlockNrOffset(blockNrOffset);
        loadProfileDataSet1Table.setNrOfBlocksToRequest(nrOfBlocksToRequest);
        loadProfileDataSet1Table.setIntervalsets(intervalsets);
        loadProfileDataSet1Table.setCount(count);
        loadProfileDataSet1Table.setChunkSize(chunkSize);
        loadProfileDataSet1Table.build();
        return loadProfileDataSet1Table;
    }

    boolean isReadLoadProfilesChunked() {
        return readLoadProfilesChunked;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    private void buildIntervalDataChunked(ProfileData profileData, Date lastReading, Date to) throws IOException {

        int startBlock = sentinel.getManufacturerProcedureFactory().getLoadProfileStartBlock(lastReading).getStartingBlockOffset(); // start block ID using lastreading
        int lastBlock = sentinel.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getLastBlockElement(); // last block ID
        int nrOfValidIntervals = sentinel.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals(); // intervals in last block
        int intervalsPerBlock = sentinel.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlockIntervalsSet()[0]; // intervals per block
        int currentBlock = lastBlock;

        Date currentDate = new Date();

        //int profileInterval = sentinel.getProfileInterval();
        //Date newTo = new Date(to.getTime() + (long)(profileInterval*intervalsPerBlock*1000));

        List<List<LoadProfileBlockData>> blockData = new ArrayList<List<LoadProfileBlockData>>();

        try {
            while (true) {
                LoadProfileBlockData lpbd = null;
                boolean read = false;

                int numIntervals;

                if (currentBlock == lastBlock) {
                    numIntervals = nrOfValidIntervals;
                } else {
                    numIntervals = intervalsPerBlock;
                }

                List<LoadProfileBlockData> aList = new ArrayList<LoadProfileBlockData>();
                blockData.add(aList);

                int count = 0;
                while (!read) {
                    try {
                        if (numIntervals <= getChunkSize()) {
                            lpbd = getLoadProfileDataSetTable(currentBlock, 1, numIntervals, count, getChunkSize()).getLoadProfileDataSet().getLoadProfileDataSets()[0];
                            aList.add(lpbd);
                            read = true;
                            count++;

                            if (currentBlock == lastBlock) {
                                currentBlock = startBlock;
                            } else {
                                currentBlock++;
                            }
                        } else {
                            while (numIntervals > getChunkSize()) {
                                // read chunk size intervals
                                lpbd = getLoadProfileDataSetTable(currentBlock, 1, getChunkSize(), count, getChunkSize()).getLoadProfileDataSet().getLoadProfileDataSets()[0];
                                aList.add(lpbd);
                                count++;
                                numIntervals -= getChunkSize();
                            }
                        }
                    } catch (Throwable t) {
                        sentinel.getLogger().severe("Exception when reading load profiles: " + t.getMessage());
                        t.printStackTrace();
                        throw t;
                    }
                }
                if (currentBlock == lastBlock) {
                    break;
                }
            }
        } catch (Throwable t) {
            sentinel.getLogger().severe("JAMES: exception (2) when reading load profiles: " + t.getMessage());
            t.printStackTrace();
            throw t;
        }

        List<List<LoadProfileBlockData>> filtered = new ArrayList<List<LoadProfileBlockData>>();

        Calendar lastReadingCal = Calendar.getInstance();
        lastReadingCal.setTime(lastReading);
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(currentDate);

        // Filter out any that are outside of the required range
        for (List<LoadProfileBlockData> aList : blockData) {
            List<LoadProfileBlockData> blah = new ArrayList<LoadProfileBlockData>();
            inner: for (LoadProfileBlockData lpbd : aList) {
                Calendar cal = Calendar.getInstance(sentinel.getTimeZone());
                cal.setTime(lpbd.getBlockEndTime());
                if (cal.before(lastReadingCal) || cal.after(currentCal)) {
                    // Remove this, it is outside the time frame we are interested in
                    String msg = "Sentinel serial no. " + sentinel.getSerialNumber() + ", dropping block with end time out of read range: " + lpbd.getBlockEndTime();
                    getLogger().severe(msg);
                    System.out.println(msg);
                    continue inner;
                }
                blah.add(lpbd);
            }
            if (!blah.isEmpty()) {
                filtered.add(blah);
            }
        }

        try {
            List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
            for (List<LoadProfileBlockData> aList : filtered) {
                Calendar cal = Calendar.getInstance(sentinel.getTimeZone());
                sentinel.getLogger().info("Set timezone to " + sentinel.getTimeZone());
                boolean calSet = false;
                for (int loop = (aList.size() - 1); loop >= 0; loop--) {
                    LoadProfileBlockData lpbd = aList.get(loop);
                    if (!calSet) {
                        cal.setTime(lpbd.getBlockEndTime());
                        calSet = true;
                    }

                    IntervalSet[] intervalSets = lpbd.getLoadProfileInterval();
                    for (int i = (intervalSets.length - 1); i >= 0; i--) {
                        addIntervalData(intervalDatas, cal, intervalSets[i], i);

                    } // for (int i=0;i<intervalSets.length;i++)
                }
            }
            profileData.setIntervalDatas(intervalDatas);
        } catch (Throwable t) {
            sentinel.getLogger().severe("Exception (3) when reading load profiles" + t.getMessage());
            t.printStackTrace();
            throw t;
        }
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
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }
            }
            else break;
        } // while(true)
    } // private void waitUntilTimeValid()    
}
