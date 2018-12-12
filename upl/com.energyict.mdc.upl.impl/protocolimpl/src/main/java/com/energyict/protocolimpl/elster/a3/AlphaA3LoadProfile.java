/*
 * KVLoadProfile.java
 *
 * Created on 9 november 2005, 15:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3;

import com.energyict.cbo.Unit;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
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
import com.energyict.protocolimpl.ansi.c12.tables.LoadProfileStatusTable;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.elster.a3.tables.EventLogMfgCodeFactory;
import com.energyict.protocolimpl.elster.a3.tables.SourceInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Koen
 */
public class AlphaA3LoadProfile {

    protected static final int DEBUG = 0; //-1;ff;k;

    private AlphaA3 alphaA3;

    public AlphaA3LoadProfile(AlphaA3 alphaA3) {
        this.alphaA3 = alphaA3;
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
        buildIntervalData(profileData, lastReading, to);
        if (includeEvents) {
            try {
                buildHistoryLog(profileData, lastReading, to);
                buildEventLog(profileData, lastReading, to);
                buildPQMLog(profileData, lastReading, to);      //KHE 25.01.2011
                profileData.applyEvents(alphaA3.getProfileInterval() / 60);
            }
            catch (ResponseIOException e) {
                if (e.getReason() == AbstractResponse.IAR) // table does not exist!
                {
                    alphaA3.getLogger().warning("No Logging available. Respective tables do not exist in the meter.");
                } else {
                    throw e;
                }
            }
        }

        if (DEBUG >= 2) {
            System.out.println(profileData);
        }
        profileData.sort();
        return profileData;
    }

    private void buildPQMLog(ProfileData profileData, Date from, Date to) throws IOException {
        if (alphaA3.getManufacturerTableFactory().getPowerQualityMonitorTests().hasPQMFunctionality()) {
            List<MeterEvent> meterEvents = alphaA3.getManufacturerTableFactory().getPowerQualityMonitorLog().getMeterEvents();
            meterEvents = filterEvents(meterEvents, from, to);
            if (!meterEvents.isEmpty()) {
                profileData.getMeterEvents().addAll(meterEvents);
            }
        } else {
            alphaA3.getLogger().info("PQM functionality is disabled in device. Skipping readout of PQM logbook.");
        }
    }

    private List<MeterEvent> filterEvents(List<MeterEvent> meterEvents, Date from, Date to) {
        if (to == null) {
            to = new Date();
        }
        List<MeterEvent> result = new ArrayList<>();
        for (MeterEvent event : meterEvents) {
            if (event.getTime().after(from) && event.getTime().before(to)) {
                result.add(event);
            }
        }
        return result;
    }

    protected void buildHistoryLog(ProfileData profileData, Date lastReading,
                                   Date to) throws IOException {
        // TODO Auto-generated method stub

    }


    private void buildEventLog(ProfileData profileData, Date lastReading, Date to) throws IOException {
        // order = 0 oldest -> newest
        List<MeterEvent> meterEvents = new ArrayList<>();
        EventLog header = alphaA3.getStandardTableFactory().getEventLogDataTableHeader().getEventLog();
        int order = header.getEventFlags().getOrder();
        int event2Read = order == 0 ? header.getLastEntryElement() : 0;
        if (DEBUG >= 1) {
            System.out.println("KV_DEBUG> event2Read=" + event2Read);
        }
        int nrOfValidEntries = header.getNrOfValidentries();
        if (DEBUG >= 1) {
            System.out.println("KV_DEBUG> nrOfValidEntries=" + nrOfValidEntries);
        }
        int validEventCount = 0;
        boolean futurelogcheck = true;
        while (true) {
            if (futurelogcheck) {
                EventEntry eventEntry = alphaA3.getStandardTableFactory().getEventLogDataTableEventEntryHeader(event2Read).getEventLog().getEntries()[0];
                if ((validEventCount >= nrOfValidEntries) || (eventEntry.getEventTime().before(to))) {
                    futurelogcheck = false;
                } else {
                    validEventCount++;
                }
                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG>(1) events " + eventEntry.getEventTime() + " is before " + lastReading + " ?");
                }
            }
            if (!futurelogcheck) {
                EventEntry eventEntry = alphaA3.getStandardTableFactory().getEventLogDataTableEventEntries(event2Read, 1).getEventLog().getEntries()[0];

                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG>(2) eventEntry=" + eventEntry);
                }

                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG>(2) events " + eventEntry.getEventTime() + " is before " + lastReading + " ?");
                }
                if ((validEventCount >= nrOfValidEntries) || (eventEntry.getEventTime().before(lastReading))) {
                    break;
                }
                validEventCount++;
                meterEvents.add(createMeterEvent(eventEntry));
                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG>(2) events " + eventEntry.getEventTime() + " is before " + lastReading + " ?");
                }
            }
            if (order == 0) {
                if (event2Read-- == 0) {
                    event2Read = nrOfValidEntries - 1;
                }
            } else {
                if (event2Read++ == (nrOfValidEntries - 1)) {
                    event2Read = 0;
                }
            }
        }
        profileData.getMeterEvents().addAll(meterEvents);
//       profileData.setMeterEvents(meterEvents);
    }

    protected MeterEvent createMeterEvent(EventEntry eventEntry) {
        EventLogMfgCodeFactory eventFact = new EventLogMfgCodeFactory();
        int eiCode = eventFact.getEICode(eventEntry.getEventCode().getProcedureNr(), eventEntry.getEventCode().isStdVsMfgFlag());
        String text = eventFact.getEvent(eventEntry.getEventCode().getProcedureNr(), eventEntry.getEventCode().isStdVsMfgFlag()) + ", " +
                eventFact.getArgument(eventEntry.getEventCode().getProcedureNr(), eventEntry.getEventCode().isStdVsMfgFlag());
        int protocolCode = eventEntry.getEventCode().getProcedureNr() | (eventEntry.getEventCode().isStdVsMfgFlag() ? 0x8000 : 0);
        return new MeterEvent(eventEntry.getEventTime(), eiCode, protocolCode, text);
    }

//    // KV 02112005
//    protected boolean inDSTGreyZone(Date date, TimeZone timeZone) {
//        Date testdate;
//        if (timeZone.inDaylightTime(date)) {
//            testdate = new Date(date.getTime()+3600000);
//            if (timeZone.inDaylightTime(testdate))
//                return false;
//            else
//                return true;
//        }
//        else
//            testdate = new Date(date.getTime()-3600000);
//
//        if (timeZone.inDaylightTime(testdate))
//            return true;
//        else
//            return false;
//    }

    private void buildIntervalData(ProfileData profileData, Date lastReading, Date to) throws IOException {
        // get blocks until last interval enddate < lastreading
        // parse blocks to load profile data
        List<LoadProfileBlockData> loadProfileBlockDatas = new ArrayList<>();
        LoadProfileBlockData lpbd;
        int validBlockCount = 0;
        LoadProfileStatusTable lpst = alphaA3.getStandardTableFactory().getLoadProfileStatusTable();
        int nrOfValidBlocks = lpst.getLoadProfileSet1Status().getNrOfValidBlocks();
        int nrOfValidIntervals = lpst.getLoadProfileSet1Status().getNrOfValidIntervals();
        int lastBlock2read = lpst.getLoadProfileSet1Status().getLastBlockElement();
        int block2read = lastBlock2read;
        int blockOrder = lpst.getLoadProfileSet1Status().getBlockOrder();
        int maxNrOfBlocks = alphaA3.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlocksSet()[0];
        int nrOfIntervalsPerBlock = alphaA3.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlockIntervalsSet()[0];


        //boolean dstApplied = alphaA3.getStandardTableFactory().getClockStateTable().getTimeDateQualifier().isDstAppliedFlag();

        boolean currentDayBlock = true;

        // calc blocksize
        //int intervalsPerBlock =   alphaA3.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().getNrOfBlockIntervalsSet()[0];
        int profileInterval = alphaA3.getProfileInterval();

        if (DEBUG >= 1) {
            System.out.println("KV_DEBUG> nrOfIntervalsPerBlock=" + nrOfIntervalsPerBlock + ", maxNrOfBlocks=" + maxNrOfBlocks);
        }
        if (DEBUG >= 1) {
            System.out.println("KV_DEBUG> block2read=" + block2read + ", nrOfValidIntervals=" + nrOfValidIntervals + ", nrOfValidBlocks=" + nrOfValidBlocks);
        }

        Date newTo = new Date(to.getTime() + (long) (profileInterval * nrOfIntervalsPerBlock * 1000));

        /**************************************************************************************************************************************************
         C H E C K  F O R  V A L I D  F I R S T  B L O C K
         **************************************************************************************************************************************************/

        if (DEBUG >= 2) {
            System.out.println("KV_DEBUG> (1) validBlockCount=" + validBlockCount);
        }
        // read the block headers
        while (true) {
            lpbd = alphaA3.getStandardTableFactory().getLoadProfileDataSetTableBlockHeader(0, block2read).getLoadProfileDataSet().getLoadProfileDataSets()[0];

            if (DEBUG >= 2) {
                System.out.println("KV_DEBUG> (1) header lpbd=" + lpbd);
            }

            if ((validBlockCount >= nrOfValidBlocks) || (lpbd.getBlockEndTime().before(newTo))) {
                break;
            }
            validBlockCount++;
            if (block2read++ >= (maxNrOfBlocks - 1)) {
                block2read = 0;
            }
            currentDayBlock = false; // no currentday block anymore!
        }
        lpbd = null;

        if (DEBUG >= 2) {
            System.out.println("KV_DEBUG> (1) validBlockCount=" + validBlockCount);
        }
        /**************************************************************************************************************************************************
         R E A D  A N D  C O L L E C T  B L O C K S
         **************************************************************************************************************************************************/
        int intervals2Retrieve = (int) ((((to.getTime() - lastReading.getTime()) / 1000) / alphaA3.getProfileInterval()) + 2) + alphaA3.getRetrieveExtraIntervals();
        boolean leaveLoop = false;
        while (!leaveLoop) {


//            lpbd = alphaA3.getStandardTableFactory().getLoadProfileDataSetTableBlockHeader(0,block2read).getLoadProfileDataSet().getLoadProfileDataSets()[0];
//if (DEBUG>=2) System.out.println("KV_DEBUG> (2) header lpbd="+lpbd);
//            if ((validBlockCount >=nrOfValidBlocks) || (lpbd.getBlockEndTime().before(lastReading))) break;
//            validBlockCount++;

            if (validBlockCount >= nrOfValidBlocks) {
                break;
            }
            validBlockCount++;


            // Check if nr of intervals to request is less then nr of intervalsets in the block. If so, only request necessary nr of intervalsets...

            if (DEBUG >= 2) {
                System.out.println("KV_DEBUG> intervals2Retrieve=" + intervals2Retrieve);
            }

            if (lastBlock2read == block2read) {
                if (intervals2Retrieve < nrOfValidIntervals) {
                    lpbd = alphaA3.getStandardTableFactory().getLoadProfileDataSetTableIntervalsets(0, block2read, intervals2Retrieve).getLoadProfileDataSet().getLoadProfileDataSets()[0];
                    if (DEBUG >= 2) {
                        System.out.println("KV_DEBUG> 1.1 read " + intervals2Retrieve + " intervals and leave");
                    }
//                    intervals2Retrieve -= lpbd.nrOfValidIntervals(); //= 0;
                } else {
                    lpbd = alphaA3.getStandardTableFactory().getLoadProfileDataSetTableIntervalsets(0, block2read, nrOfValidIntervals).getLoadProfileDataSet().getLoadProfileDataSets()[0];
//                    intervals2Retrieve -= lpbd.nrOfValidIntervals(); //nrOfValidIntervals;
                    if (DEBUG >= 2) {
                        System.out.println("KV_DEBUG> 1.2 read " + nrOfValidIntervals + " intervals and continue");
                    }
                }
            } else {
                if (intervals2Retrieve < nrOfIntervalsPerBlock) { //lpbd.getNrOfIntervalsPerBlock()) {
                    lpbd = alphaA3.getStandardTableFactory().getLoadProfileDataSetTableIntervalsets(0, block2read, intervals2Retrieve).getLoadProfileDataSet().getLoadProfileDataSets()[0];
                    if (DEBUG >= 2) {
                        System.out.println("KV_DEBUG> 2.1 read " + intervals2Retrieve + " intervals and leave");
                    }
//                    intervals2Retrieve -= lpbd.nrOfValidIntervals(); //= 0;
                } else {
                    lpbd = alphaA3.getStandardTableFactory().getLoadProfileDataSetTable(0, block2read, 1).getLoadProfileDataSet().getLoadProfileDataSets()[0];
//                    intervals2Retrieve -= lpbd.nrOfValidIntervals(); //lpbd.getNrOfIntervalsPerBlock();
                    if (DEBUG >= 2) {
                        System.out.println("KV_DEBUG> 2.2 read " + nrOfIntervalsPerBlock + " intervals and continue");
                    }
                }
            }

            intervals2Retrieve -= lpbd.nrOfValidIntervals(); //lpbd.getNrOfIntervalsPerBlock();
            if (intervals2Retrieve == 0) {
                leaveLoop = true;
            }


            if (DEBUG >= 2) {
                System.out.println("KV_DEBUG> (2) data lpbd=" + lpbd);
            }

            if (lpbd.getBlockEndTime().before(lastReading)) {
                break;
            }

            loadProfileBlockDatas.add(lpbd);
            if (block2read++ >= (maxNrOfBlocks - 1)) {
                block2read = 0;
            }
        } // while(true)

        /**************************************************************************************************************************************************
         P A R S E  T H E  D A T A
         **************************************************************************************************************************************************/
        final int STATE_DST_TRANSITION_W_S = 1;
        final int STATE_DST_TRANSITION_S_W = 2;
        final int STATE_IDLE = 0;
        int state = STATE_IDLE;
        boolean powerOn = true;
        boolean skipping = false;

        IntervalSet previousIntervalSet = null;
        //currentDayBlock=true;
        List<IntervalData> intervalDatas = new ArrayList<>();
        Calendar cal = null;
        Iterator it = loadProfileBlockDatas.iterator();
        boolean firstInterval = true;
        while (it.hasNext()) {
            lpbd = (LoadProfileBlockData) it.next();
            if (cal == null) {
                cal = Calendar.getInstance(alphaA3.getTimeZone());
                cal.setTime(lpbd.getBlockEndTime());
            }

            IntervalSet[] intervalSets = lpbd.getLoadProfileInterval();
            //int nrOfIntervals = currentDayBlock?alphaA3.getStandardTableFactory().getLoadProfileStatusTable().getLoadProfileSet1Status().getNrOfValidIntervals():intervalSets.length;
            int nrOfIntervals = intervalSets.length;

            if (DEBUG == -1) {
                System.out.println("KV_DEBUG> ****** nrOfIntervals = " + nrOfIntervals + ", block end time at " + lpbd.getBlockEndTime() + ", intervalSets.length=" + intervalSets.length + ", currentDayBlock=" + currentDayBlock);
            }
            if (DEBUG == -1) {
                for (int i = 0; i < lpbd.getSimpleIntStatus().length; i++) {
                    System.out.println("KV_DEBUG> simpleStatus[" + i + "]=" + ParseUtils.buildBinaryRepresentation((long) lpbd.getSimpleIntStatus()[i], 8));
                }
            }

            boolean check2SkipInvalidIntervals = true;
            for (int i = (nrOfIntervals - 1); i >= 0; i--) {
                IntervalSet intervalSet = intervalSets[i];

                if (DEBUG == -1) {
                    System.out.println("KV_DEBUG> interval " + i + " at " + cal.getTime() + " intervalSet=" + intervalSet);
                }

                if (check2SkipInvalidIntervals) {
                    if (DEBUG == -1) {
                        System.out.println("KV_DEBUG> if (check2SkipInvalidIntervals)");
                    }
                    if (!intervalSet.isValid()) {
                        if (DEBUG == -1) {
                            System.out.println("KV_DEBUG> if (!intervalSet.isValid())");
                        }
                        if ((previousIntervalSet != null) &&
                                previousIntervalSet.isValid() &&
                                previousIntervalSet.isPowerFailWithintheInterval() &&
                                previousIntervalSet.isPartialDueToCommonState()) {
                            if (DEBUG == -1) {
                                System.out.println("KV_DEBUG> if ((previousIntervalSet != null)");
                            }
                            cal = Calendar.getInstance(alphaA3.getTimeZone());
                            cal.setTime(lpbd.getBlockEndTime());
                            ParseUtils.roundUp2nearestInterval(cal, alphaA3.getProfileInterval());
                            skipping = true;
                            powerOn = !powerOn;
                        } else {
                            if (DEBUG == -1) {
                                System.out.println("KV_DEBUG> ***************************** BUG TRAP!! if ((previousIntervalSet != null)");
                            }
                            nrOfIntervals--;
                            continue;
                        }
                    }
                    check2SkipInvalidIntervals = false;
                }

                if (currentDayBlock && (i == (nrOfIntervals - 1))) {
                    if (DEBUG == -1) {
                        System.out.println("KV_DEBUG> if (currentDayBlock && (i==(nrOfIntervals-1)))");
                    }

                    ParseUtils.roundDown2nearestInterval(cal, alphaA3.getProfileInterval());
                    continue;
                }

                // if first interval marked as DST AND time is NOT in DST, subtract ONE hour!
                if (firstInterval && intervalSet.isValid() && intervalSet.isDSTActive() && !alphaA3.getTimeZone().inDaylightTime(cal.getTime())) {
                    cal.add(Calendar.HOUR, -1);
                }
                firstInterval = false;

                // if interval is valid, add it to the profile data
                if (intervalSet.isValid()) {
                    skipping = false;
                    intervalDatas.add(createIntervalData(intervalSet, cal.getTime(), i, powerOn));
                    int common2EIstatus = intervalSet.getCommon2EIStatus(powerOn);
                    if ((((common2EIstatus & IntervalStateBits.POWERUP) == IntervalStateBits.POWERUP) &&
                            (i > 0 && (!intervalSets[i - 1].isValid() || intervalSets[i - 1].isPowerFailWithintheInterval()))) ||
                            (common2EIstatus & IntervalStateBits.POWERDOWN) == IntervalStateBits.POWERDOWN) {
                        powerOn = !powerOn;
                    }
                }
                // KV_TO_DO, i need the missing document profile.doc from Elster. After several calls to elster in March 2007, i still didn't get that document.
                // That document probably describes the extended status flags sequence and how to interprete them in several cases of the meter state (DST, clock set, powerfail, ...)
                // In the meantime, i added isPartialDueToCommonState() to check if we should go into DST transition state. all other cases should remain IDLE state as
                // they might be simple clock set states...
                // The DST transition S -> W on a DST enabled device is tested Well.
                // The DST transition W -> S on a DST enabled device is ?
                // The DST transition S -> W on a DST disnabled device is tested Well.
                // The DST transition W -> S on a DST disabled device is tested Well.

                // check if we have to do with DST transition from W to S
                // We get 4 disabled intervals surronded by commonflags clockresetforward AND DST active!
                if (previousIntervalSet != null) {
                    if (state == STATE_IDLE) {
                        if (previousIntervalSet.isValid() && previousIntervalSet.isClockResetForward() && !previousIntervalSet.isPartialDueToCommonState() && previousIntervalSet.isDSTActive() && !intervalSet.isValid()) {
                            state = STATE_DST_TRANSITION_W_S;
                            if (DEBUG == -1) {
                                System.out.println("STATE_DST_TRANSITION_W_S, time=" + cal.getTime());
                            }
                        } else if (previousIntervalSet.isValid() && previousIntervalSet.isClockResetBackwards() && !previousIntervalSet.isPartialDueToCommonState() && !previousIntervalSet.isDSTActive() && !intervalSet.isValid()) {
                            state = STATE_DST_TRANSITION_S_W;
                            if (DEBUG == -1) {
                                System.out.println("STATE_DST_TRANSITION_S_W, time=" + cal.getTime());
                            }
                        }
                    } else if (state == STATE_DST_TRANSITION_W_S) {
                        if (intervalSet.isValid() && intervalSet.isClockResetForward() && intervalSet.isPartialDueToCommonState() && intervalSet.isDSTActive() && !previousIntervalSet.isValid()) {
                            state = STATE_IDLE;
                            if (DEBUG == -1) {
                                System.out.println("STATE_IDLE, time=" + cal.getTime());
                            }
                        }
                    } else if (state == STATE_DST_TRANSITION_S_W) {
                        if (intervalSet.isValid() && intervalSet.isClockResetBackwards() && intervalSet.isPartialDueToCommonState() && !intervalSet.isDSTActive() && !previousIntervalSet.isValid()) {
                            state = STATE_IDLE;
                            if (DEBUG == -1) {
                                System.out.println("STATE_IDLE, time=" + cal.getTime());
                            }
                        }
                    }
                }

                if (state == STATE_IDLE && !skipping) {
                    cal.add(Calendar.SECOND, (-1) * alphaA3.getProfileInterval());
                }

                previousIntervalSet = intervalSet;

            } // for (int i=(nrOfIntervals-1);i>=0;i--)

            currentDayBlock = false;

        } // while(it.hasNext())

        profileData.setIntervalDatas(intervalDatas);

    } // private void buildIntervalData(ProfileData profileData, Date lastReading, Date to) throws IOException

    private IntervalData createIntervalData(IntervalSet intervalSet, Date endDate, int interval, boolean powerOn) throws IOException {

        IntervalFormat[] values = intervalSet.getIntervalData();
        int common2EIstatus = intervalSet.getCommon2EIStatus(powerOn);
        IntervalData intervalData = new IntervalData(endDate, common2EIstatus, intervalSet.getCommonStatus());

        for (int channel = 0; channel < alphaA3.getNumberOfChannels(); channel++) {
            BigDecimal bd = (BigDecimal) values[channel].getValue(); // raw value
            bd = bd.multiply(alphaA3.getAdjustChannelMultiplier()); // KV 28062007

            int protocolStatus = intervalSet.getChannelStatus(channel);
            int eiStatus = intervalSet.getchannel2EIStatus(channel);


            if (alphaA3.getProtocolChannelMap() != null) {
                // conversion to engineering units

                // KV_TO_DO
                // Depending on the UON for the profile data quantity, the engineering value calculation differs!
                // See KV2(c) document with all explanation about that. For the moment we only use
                // KVAh load profile calculation!

                if (alphaA3.getProtocolChannelMap().isProtocolChannel(channel)) {
                    if (alphaA3.getProtocolChannelMap().getProtocolChannel(channel).getValue() == 1) { // engineering values
                        SourceInfo si = new SourceInfo(alphaA3);
                        bd = si.basic2engineering(bd, channel, true, false);
                    } else if (alphaA3.getProtocolChannelMap().getProtocolChannel(channel).getValue() == 2) { // engineering values
                        SourceInfo si = new SourceInfo(alphaA3);
                        bd = si.basic2engineering(bd, channel, true, true);
                    } else if (alphaA3.getProtocolChannelMap().getProtocolChannel(channel).getValue() == 3) { //  // raw values following only pulseweight, scalar ans divisorset
                        SourceInfo si = new SourceInfo(alphaA3);
                        bd = si.applyDivisors(bd, channel);
                    }
                }
            }


            intervalData.addValue(bd, protocolStatus, eiStatus);

        } // for (int channel=0;channel<alphaA3.getNumberOfChannels();channel++)

        if (DEBUG == -1) {
            System.out.println("KV_DEBUG> interval=" + interval + " at endtime " + endDate + ", intervalData=" + intervalData + "\n*****************************************************************************************\n");
        }
        return intervalData;
    }

    private void buildChannelInfo(ProfileData profileData) throws IOException {
        // build channelunits
        for (int channel = 0; channel < alphaA3.getNumberOfChannels(); channel++) {
            int sourceIndex = alphaA3.getStandardTableFactory().getLoadProfileControlTable().getLoadProfileSelectionSet1()[channel].getLoadProfileSourceSelect();
            SourceInfo sourceUnits = new SourceInfo(alphaA3);
            Unit unit = Unit.get(""); //sourceUnits.getChannelUnit(sourceIndex).getVolumeUnit(); //Unit.get("");
            if (alphaA3.getProtocolChannelMap() != null) {
                // conversion to engineering units
                if (alphaA3.getProtocolChannelMap().isProtocolChannel(channel)) {
                    if (alphaA3.getProtocolChannelMap().getProtocolChannel(channel).getValue() == 1) { // engineering values
                        unit = sourceUnits.getChannelUnit(sourceIndex).getFlowUnit();
                    }
                    if (alphaA3.getProtocolChannelMap().getProtocolChannel(channel).getValue() == 2) { // engineering values
                        unit = sourceUnits.getChannelUnit(sourceIndex).getVolumeUnit();
                    }
                    if (alphaA3.getProtocolChannelMap().getProtocolChannel(channel).getValue() == 3) { // raw values following only pulseweight, scalar ans divisorset
                        unit = sourceUnits.getChannelUnit(sourceIndex).getVolumeUnit();
                    }


                }
            }
            com.energyict.protocol.ChannelInfo channelInfo = new com.energyict.protocol.ChannelInfo(channel, "AlphaA3_channel_" + channel, unit);
            profileData.addChannel(channelInfo);
        }
    }

    private void waitUntilTimeValid() throws IOException {
        Date date;
        long offset2IntervalBoundary = 0;
        while (true) {
            date = alphaA3.getTime();
            long profileInterval = alphaA3.getProfileInterval();
            long seconds = date.getTime() / 1000;
            offset2IntervalBoundary = seconds % profileInterval;
            if ((offset2IntervalBoundary < 5) || (offset2IntervalBoundary > (profileInterval - 10))) {
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }
            } else {
                break;
            }
        }
    }

}