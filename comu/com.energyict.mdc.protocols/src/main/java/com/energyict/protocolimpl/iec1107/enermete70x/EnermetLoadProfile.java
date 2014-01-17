/*
 * EnermetLoadProfile.java
 *
 * Created on 28 oktober 2004, 14:09
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class EnermetLoadProfile {

    private static final int DEBUG = 0;
    private static final boolean ALL_EVENTS = true;
    private static final int MAX_ENERMET_PROFILES_CHANNELS=8;

    EnermetBase enermet=null;

    // cached
    int nrOfChannels=-1;

    /** Creates a new instance of EnermetLoadProfile */
    public EnermetLoadProfile(EnermetBase enermet) {
        this.enermet=enermet;
    }

    //    public int getProfileInterval() {
    //        enermet.getDataReadingCommandFactory().getHistorySeriesRead().getProfileDataBlock(new Date((new Date()).getTime()-3600),1,channel);
    //    }

    public int getNrOfChannels() throws IOException {
        if (nrOfChannels == -1) {
            if (enermet.getProtocolChannelMap() == null) {
                nrOfChannels=0;
                for (int channel=1;channel<=MAX_ENERMET_PROFILES_CHANNELS;channel++) {
                    try {
                        enermet.getDataReadingCommandFactory().getHistorySeriesRead().getLoadProfileDataBlock(new Date((new Date()).getTime()-3600),1,channel);
                        nrOfChannels++;
                    }
                    catch(ProtocolConnectionException e) {
                        //absorb ([4])
                        if ((e.getProtocolErrorCode() == null) || (e.getProtocolErrorCode().compareTo(enermet.COMMAND_CANNOT_BE_EXECUTED) != 0))
                            throw e;
                        else
                            break;
                    }
                }
            }
            else nrOfChannels = enermet.getProtocolChannelMap().getNrOfProtocolChannels();
        } // if (nrOfChannels == -1)

        return nrOfChannels;

    } // public int getNrOfChannels()


    public ProfileData getProfileData(Date fromDate, boolean includeEvents) throws IOException {

        ProfileData profileData = new ProfileData();
        List intervalDatas = new ArrayList();
        Calendar from = ProtocolUtils.getCleanGMTCalendar();
        from.setTime(fromDate);
        from.set(Calendar.HOUR_OF_DAY,0);
        from.set(Calendar.MINUTE,0);
        from.set(Calendar.SECOND,0);


        int nrOfDays = (int)(((new Date()).getTime() - from.getTime().getTime()) / (24*3600000L))+1;
        for (int day=0; day<nrOfDays ; day++) {
            // starting oldest day, get a LoadProfileDataBlock for each channel and add to an array


            LoadProfileDataBlock[] loadProfileDataBlocks = new LoadProfileDataBlock[getNrOfChannels()];
            for (int channel=1;channel<=getNrOfChannels();channel++) {
                loadProfileDataBlocks[channel-1] = enermet.getDataReadingCommandFactory().getHistorySeriesRead().getLoadProfileDataBlock(from.getTime(),channel);
                if (DEBUG >=1)
                    System.out.println("KV_DEBUG> 2 DAY="+day+", "+loadProfileDataBlocks[channel-1]+", "+from.getTime());
            }

            // construct channelinfos using first day first interval
            if (day == 0) {
                List channelInfos = new ArrayList();
                for (int channel=0; channel<loadProfileDataBlocks.length; channel++) {
                    LoadProfileEntry loadProfileEntry =
                    (LoadProfileEntry)loadProfileDataBlocks[channel].getLoadProfileEntries().get(0);
                    channelInfos.add(new ChannelInfo(channel,"Enermet IEC1107 "+(channel+1),loadProfileEntry.getQuantity().getUnit()));
                }
                profileData.setChannelInfos(channelInfos);
            } // if (day == 0)

            // build the intervaldata
            intervalDatas.addAll(buildIntervalDatas(loadProfileDataBlocks,fromDate));

            from.add(Calendar.DATE, 1);

        } // for (int day=0; day<nrOfDays ; day++)

        profileData.setIntervalDatas(intervalDatas);

        // KV 28072005
        if (includeEvents)
            profileData.setMeterEvents(retrieveEventLog(fromDate));


        profileData.sort();
        profileData.applyEvents(enermet.getProfileInterval()/60);

        return profileData;

    } // public ProfileData getProfileData(Date fromDate, boolean includeEvents) throws IOException

    private List buildIntervalDatas(LoadProfileDataBlock[] loadProfileDataBlocks, Date fromDate) {
        int profileInterval = loadProfileDataBlocks[0].getProfileInterval();

        Calendar calendar = ProtocolUtils.getCleanGMTCalendar();
        calendar.setTime(loadProfileDataBlocks[0].getFirstStartingDate());
        calendar.add(Calendar.SECOND, profileInterval);

        int nrOfIntervals = loadProfileDataBlocks[0].getLoadProfileEntries().size();
        List intervalDatas=new ArrayList();

        for (int interval=0; interval<nrOfIntervals; interval ++) {
            int status=0,eiStatus=0;
            List intervalValues=new ArrayList();

            for (int channel=0; channel<loadProfileDataBlocks.length; channel++) {
                LoadProfileEntry loadProfileEntry =
                (LoadProfileEntry)loadProfileDataBlocks[channel].getLoadProfileEntries().get(interval);
                if (!loadProfileEntry.isMissing()) {
                    intervalValues.add(new IntervalValue(loadProfileEntry.getQuantity().getAmount(),loadProfileEntry.getStatus(),loadProfileEntry.getEiStatus()));
                    status |= loadProfileEntry.getStatus();
                    eiStatus |= loadProfileEntry.getEiStatus();
                }
                else break;
            } // for (int channel=0; channel<loadProfileDataBlocks.size(); channel++)

            if ((intervalValues.size() > 0) && (!(fromDate.after(calendar.getTime()))))
                intervalDatas.add(new IntervalData(((Calendar)calendar.clone()).getTime(),eiStatus,status,0,intervalValues));

            // add interval to timestamp
            calendar.add(Calendar.SECOND, profileInterval);

        } // for (int interval=0; interval<nrOfIntervals; interval ++)

        return intervalDatas;

    } // private List buildIntervalDatas(LoadProfileDataBlock[] loadProfileDataBlocks)


    /*
     *  We use a local seqNr to keep track of the circular logbuffer because we do not have any idea
     *  about the size of the buffer. We must however avoid duplicate log entries...
     */
    private List retrieveEventLog(Date fromDate) throws IOException {
        List eventLogEntries=new ArrayList();
        EventLogRead eventLogRead = enermet.getDataReadingCommandFactory().getEventLog();
        //int count=0;

        EventLogEntry lastEventLogEntry = eventLogRead.getEventLogLatestEntry();
        if ((lastEventLogEntry == null) || (fromDate.after(lastEventLogEntry.getDate())))
            return new ArrayList();

        int latestSequenceNr = lastEventLogEntry.getId();

        if (DEBUG>=1) System.out.println("KV_DEBUG> latestSequenceNr="+latestSequenceNr);

        int seqNr = latestSequenceNr;
        boolean found=false;
        while(!found) {

            //int seqNr = latestSequenceNr-count*eventLogRead.NR_OF_LOG_ENTRIES;

            List events = eventLogRead.getEventLogFrom(seqNr);
            if (events.size()==0)
                found=true;
            else {
                Iterator it = events.iterator();
                while(it.hasNext()) {
                    EventLogEntry ele = (EventLogEntry)it.next();
                    if (fromDate.after(ele.getDate())) {
                        found=true;
                        break;
                    }
                    seqNr = ele.getId();
                    //eventLogEntries.add(ele);
                    addEventLogEntry(eventLogEntries,ele);
                } // while(it.hasNext())
            }
            //count++;
        } // while(!found)


        if (DEBUG>=1) {
            System.out.println("Event Log Entries from "+fromDate+" ("+eventLogEntries.size()+"):");
            Iterator it = eventLogEntries.iterator();
            while(it.hasNext()) {
                EventLogEntry ele = (EventLogEntry)it.next();
                System.out.println(ele);
            } // while(it.hasNext())
        }

        return buildMeterEvents(eventLogEntries);

    } // private List retrieveEventLog(Date fromDate) throws IOException

    /*
     *  Find in eventlist if event already exist
     */
    private void addEventLogEntry(List eventLogEntries,EventLogEntry ele2add) {
        boolean add=true;
        Iterator it = eventLogEntries.iterator();
        while(it.hasNext()) {
            EventLogEntry ele = (EventLogEntry)it.next();
            if (ele.getId() == ele2add.getId())
                add=false;
        } // while(it.hasNext())
        if (add)
            eventLogEntries.add(ele2add);
    } // private void addEventLogEntry(List eventLogEntries,EventLogEntry ele2add)

    private static final int EVENT_TYPE_POWERFAIL=1;
    private static final int EVENT_TYPE_FAULT_SITUATION=2;
    private static final int EVENT_TYPE_STATE_INPUT_CHANGE=3;
    private static final int EVENT_TYPE_REPROGRAMMED=4;
    private static final int EVENT_TYPE_COMMSTART=5;
    private static final int EVENT_TYPE_RTCSET=6;
    private static final int EVENT_TYPE_DIALOUT_SESSION=7;
    private static final int EVENT_TYPE_STATE_OUTPUT_CHANGE=8;
    private static final int EVENT_TYPE_COMMSTOP=9;
    private static final int EVENT_TYPE_VERSION_NR_CHANGED=11;
    private static final int EVENT_TYPE_CORE_FAULT=20;
    private static final int EVENT_TYPE_MEASURE_FAULT=21;
    private static final int EVENT_TYPE_UNEXPECTED_BRANCH=32;

    private List buildMeterEvents(List eventLogEntries) {
        List meterEvents = new ArrayList();
        Iterator it = eventLogEntries.iterator();
        while(it.hasNext()) {
            EventLogEntry ele = (EventLogEntry)it.next();

            switch(ele.getType()) {

                case EVENT_TYPE_POWERFAIL: {
                    meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.POWERDOWN,ele.getType()));
                    Calendar calendar = ProtocolUtils.getCleanGMTCalendar();
                    calendar.setTime(ele.getDate());
                    calendar.add(Calendar.DATE,ele.getInfo1());
                    calendar.add(Calendar.HOUR_OF_DAY,ele.getInfo2());
                    calendar.add(Calendar.MINUTE,ele.getInfo3());
                    calendar.add(Calendar.SECOND,ele.getInfo4());
                    meterEvents.add(new MeterEvent(calendar.getTime(),MeterEvent.POWERUP,ele.getType()));
                } break; // case EVENT_TYPE_POWERFAIL:

                case EVENT_TYPE_FAULT_SITUATION: {
                    String description=null;
                    int eiCode = MeterEvent.OTHER;
                    switch(ele.getInfo1()) {
                        case 3:
                            description = "option module type error";
                            break;
                        case 4:
                            description = "watchdog has caused a unit reset";
                            eiCode = MeterEvent.WATCHDOGRESET;
                            break;
                        case 6:
                            description = "error detected in real time, e.g. duration of a power break has exceeded the guaranteed battery backup time of the real time clock. In this situation the time information may be errorneous";
                            break;
                        case 9:
                            description = "checksum error detected in code memory (EPROM)";
                            eiCode = MeterEvent.ROM_MEMORY_ERROR;
                            break;
                        case 11:
                            description="checksum error detected in non-volatile memory (EEPROM)";
                            eiCode = MeterEvent.ROM_MEMORY_ERROR;
                            break;
                        case 12:
                            description="register overflow or underflow has occurred";
                            eiCode = MeterEvent.REGISTER_OVERFLOW;
                            break;
                        case 13:
                            description="modem fault has been detected";
                            eiCode = MeterEvent.HARDWARE_ERROR;
                            break;
                        case 14:
                            description="hardware fault has been detected";
                            eiCode = MeterEvent.HARDWARE_ERROR;
                            break;
                        case 15:
                            description="fault input is active (indicating a fault from a meter etc...)";
                            eiCode = MeterEvent.HARDWARE_ERROR;
                            break;
                        case 16:
                            description="pulse output overflow or underflow has occurred";
                            break;

                        case 1:
                        case 2:
                        case 5:
                        case 7:
                        case 8:
                        case 10:
                            eiCode = MeterEvent.OTHER;
                            description = "reserved for future use (decription taken from Enermet V5.17 IEC1107 protocol document";
                            break;

                        default:
                            eiCode = MeterEvent.OTHER;
                            description = "unknown fault type code "+ele.getInfo1();
                            break;
                    } // switch(ele.getInfo1())

                    description += getAppearing(ele.getInfo2());

                    meterEvents.add(new MeterEvent(ele.getDate(),eiCode,ele.getType(),description));

                } break; // case EVENT_TYPE_FAULT_SITUATION:

                case EVENT_TYPE_STATE_INPUT_CHANGE: {
                    String description = "state input "+ele.getInfo1()+" changed state to "+ele.getInfo2();
                    meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.OTHER,ele.getType(),description));
                } break; // case EVENT_TYPE_STATE_INPUT_CHANGE:

                case EVENT_TYPE_REPROGRAMMED: {

                    StringBuffer description=new StringBuffer();
                    description.append(getCommunicationchannelDescription(ele.getInfo1()));
                    switch(ele.getInfo2()) {
                        case 0:
                            description.append(", reprogramming");
                            break;
                        case 1:
                            description.append(", partial reprogramming");

                            switch(ele.getInfo3()) {
                                case 1:
                                    description.append(", partial programming starts");
                                    break;
                                case 2:
                                    description.append(", modem partial programming done");
                                    break;
                                case 3:
                                    description.append(", tariff control partial programming done");
                                    break;
                                case 4:
                                    description.append(", display programming done");
                                    break;
                                case 5:
                                    description.append(", alarms partial programming done");
                                    break;
                                case 6:
                                    description.append(", general partial programming done");
                                    break;
                                case 7:
                                    description.append(", preliminary values set");
                                    break;
                                default:
                                    description.append(", unknown partial programmed section code "+ele.getInfo3());
                                    break;
                            } // switch(ele.getInfo3())

                            break; // case 1:

                        default:
                            description.append(", unknown programming mode code "+ele.getInfo2());
                            break;
                    } // switch(ele.getInfo2())

                    meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.CONFIGURATIONCHANGE,ele.getType(),description.toString()));

                } break; // case EVENT_TYPE_REPROGRAMMED:

                case EVENT_TYPE_COMMSTART: {
                    StringBuffer description=new StringBuffer();
                    description.append(getCommunicationchannelDescription(ele.getInfo1()));

                    switch(ele.getInfo2()) {
                        case 0:
                            description.append(", successful connection was established");
                            break;
                        default:
                            description.append(", unknown reason code "+ele.getInfo2());
                            break;
                    } // switch(ele.getInfo2())

                    switch(ele.getInfo3()) {
                        case 0:
                            description.append(", no rights, wrong password");
                            break;
                        case 1:
                            description.append(", read rights");
                            break;
                        case 3:
                            description.append(", read and low control rights");
                            break;
                        case 7:
                            description.append(", read and high control rights, always with SCTM");
                            break;
                        case 8:
                            description.append(", configuration rights");
                            break;
                        case 17:
                            description.append(", factory and read rights");
                            break;
                        case 19:
                            description.append(", factory read and low control rights");
                            break;
                        case 23:
                            description.append(", factory read and high control rights");
                            break;
                        case 24:
                            description.append(", factory and configuration rights");
                            break;
                        default:
                            description.append(", unknown access rights code "+ele.getInfo3());
                            break;
                    } // switch(ele.getInfo3())

                    switch(ele.getInfo4()) {
                        case 0:
                            description.append(", IEC1107");
                            break;
                        case 1:
                            description.append(", SCTM");
                            break;
                        case 2:
                            description.append(", COSEM");
                            break;
                        default:
                            description.append(", unknown protocol code "+ele.getInfo4());
                            break;
                    } // switch(ele.getInfo4())

                    if (ALL_EVENTS)
                        meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.OTHER,ele.getType(),description.toString()));

                } break; // case EVENT_TYPE_COMMSTART:

                case EVENT_TYPE_RTCSET: {
                    StringBuffer description=new StringBuffer();

                    switch(ele.getInfo1()) {
                        case 0:
                            description.append("time set command is given via push buttons");
                            break;
                        case 1:
                        case 2:
                        case 3:
                            description.append(getCommunicationchannelDescription(ele.getInfo1()));
                            break;
                        case 4:
                            description.append("time synchronized with external input");
                            break;
                        default:
                            description.append(", unknown communicationchannel code "+ele.getInfo1());
                            break;
                    } // switch(ele.getInfo1())

                    switch(ele.getInfo2()) {
                        case 0:
                            description.append(", time adjustment command was received and performed. No additional information id available");
                            break;
                        case 1:
                            description.append(", a small positive (forward) adjustment was performed");
                            break;
                        case 2:
                            description.append(", a small negative (backward) adjustment was performed");
                            break;
                        case 3:
                            description.append(", a medium size positive (forward) adjustment was performed");
                            break;
                        case 4:
                            description.append(", a medium size negative (backward) adjustment was performed");
                            break;
                        case 5:
                            description.append(", master time set command was executed (the real time was moved forwards)");
                            break;
                        case 6:
                            description.append(", master time set command was executed (the real time was moved backwards)");
                            break;

                        default:
                            description.append(", unknown type of time adjustment code "+ele.getInfo2());
                            break;
                    } // switch(ele.getInfo2())

                    description.append(", time difference "+ele.getInfo3()+" sec.");

                    meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.SETCLOCK,ele.getType(),description.toString()));

                } break; // case EVENT_TYPE_RTCSET:

                case EVENT_TYPE_DIALOUT_SESSION: {
                    StringBuffer description=new StringBuffer();
                    switch(ele.getInfo1()) {
                        case 2:
                        case 3:
                            description.append(getCommunicationchannelDescription(ele.getInfo1()));
                            break;
                        default:
                            description.append("unknown communicationchannel code "+ele.getInfo1());
                            break;
                    } // switch(ele.getInfo1())

                    switch(ele.getInfo2()) {
                        case 1:
                            description.append(", time event was the reason for dialing");
                            break;
                        case 2:
                            description.append(", external event (status input) was the reason for dialing");
                            break;
                        case 3:
                            description.append(", internal event (error register) was the reason for dialing");
                            break;
                        default:
                            description.append(", unknown reason code "+ele.getInfo2());
                            break;
                    } // switch(ele.getInfo2())

                    switch(ele.getInfo3()) {
                        case 0:
                            description.append(", successfull connection was established");
                            break;
                        case 1:
                            description.append(", connection establishment failed");
                            break;
                        default:
                            description.append(", unknown success code "+ele.getInfo3());
                            break;
                    } // switch(ele.getInfo3())

                    meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.OTHER,ele.getType(),description.toString()));

                } break; // case EVENT_TYPE_DIALOUT_SESSION:

                case EVENT_TYPE_STATE_OUTPUT_CHANGE: {
                    String description = "state output "+ele.getInfo1()+" changed state to "+ele.getInfo2();
                    meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.OTHER,ele.getType(),description));
                } break; // case EVENT_TYPE_STATE_OUTPUT_CHANGE:

                case EVENT_TYPE_COMMSTOP: {
                    StringBuffer description=new StringBuffer();
                    description.append(getCommunicationchannelDescription(ele.getInfo1()));

                    switch(ele.getInfo2()) {
                        case 0:
                            description.append(", normal logout (break command with IEC1107), normal logout in COSEM using DISC command");
                            break;
                        case 1:
                            description.append(", timeout (SCTM), inactivity timeout (COSEM)");
                            break;
                        default:
                            description.append(", unknown reason code "+ele.getInfo2());
                            break;
                    } // switch(ele.getInfo2())

                    switch(ele.getInfo3()) {
                        case 0:
                            break;
                        default:
                            description.append(", unknown info3 code "+ele.getInfo3());
                            break;
                    } // switch(ele.getInfo3())

                    if (ALL_EVENTS)
                        meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.OTHER,ele.getType(),description.toString()));

                } break; // case EVENT_TYPE_COMMSTOP:

                case EVENT_TYPE_VERSION_NR_CHANGED: {
                    StringBuffer description=new StringBuffer();
                    switch(ele.getInfo1()) {
                        case 0:
                            description.append("reserved for future use (decription taken from Enermet V5.17 IEC1107 protocol document");
                            break;
                        case 1:
                            description.append("TRAP command has been received");
                            break;
                        case 2:
                            description.append("SW version has been changed");
                            break;
                        case 3:
                            description.append("location of background buffer has been changed");
                            break;
                        default:
                            description.append("unknown mode code "+ele.getInfo1());
                            break;
                    } // switch(ele.getInfo1())

                    description.append(", "+ele.getInfo2()+", "+ele.getInfo3());
                    meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.CONFIGURATIONCHANGE,ele.getType(),description.toString()));
                } break; // case EVENT_TYPE_VERSION_NR_CHANGED:

                case EVENT_TYPE_CORE_FAULT: {
                    StringBuffer description=new StringBuffer();
                    int eiCode = MeterEvent.OTHER;
                    switch(ele.getInfo1()) {
                        case 1:
                            description.append("non volatile memory (EEPROM) was initialised. Normally, this should never happen.");
                            eiCode = MeterEvent.ROM_MEMORY_ERROR;
                            break;
                        case 2:
                            description.append("checksum error detected in non volatile memory (EEPROM)");
                            eiCode = MeterEvent.ROM_MEMORY_ERROR;
                            break;
                        case 3:
                            description.append("checksum error detected in volatile memory (RAM)");
                            eiCode = MeterEvent.RAM_MEMORY_ERROR;
                            break;
                        case 4:
                            description.append("checksum error detected in code memory (EPROM)");
                            eiCode = MeterEvent.ROM_MEMORY_ERROR;
                            break;
                        case 5:
                            description.append("fault on core AD convertor");
                            eiCode = MeterEvent.HARDWARE_ERROR;
                            break;

                        default:
                            description.append("unknown fault type code "+ele.getInfo1());
                            break;
                    } // switch(ele.getInfo1())

                    description.append(getAppearing(ele.getInfo2()));

                    meterEvents.add(new MeterEvent(ele.getDate(),eiCode,ele.getType(),description.toString()));

                } break; // case EVENT_TYPE_CORE_FAULT:

                case EVENT_TYPE_MEASURE_FAULT: {
                    StringBuffer description=new StringBuffer();
                    int eiCode = MeterEvent.METER_ALARM;
                    switch(ele.getInfo1()) {
                        case 1:
                            description.append("no voltage on L1");
                            break;
                        case 2:
                            description.append("no voltage on L2");
                            break;
                        case 3:
                            description.append("no voltage on L3");
                            break;
                        case 4:
                            description.append("low voltage on L1");
                            break;
                        case 5:
                            description.append("low voltage on L2");
                            break;
                        case 6:
                            description.append("low voltage on L3");
                            break;
                        case 7:
                            description.append("high voltage on L1");
                            break;
                        case 8:
                            description.append("high voltage on L2");
                            break;
                        case 9:
                            description.append("high voltage on L3");
                            break;
                        case 10:
                            description.append("wrong phase order");
                            break;
                        case 11:
                            description.append("voltage symmetry");
                            break;
                        case 12:
                            description.append("high current on L1");
                            break;
                        case 13:
                            description.append("high current on L2");
                            break;
                        case 14:
                            description.append("high current on L3");
                            break;

                        default:
                            description.append("unknown fault type code "+ele.getInfo1());
                            break;

                    } // switch(ele.getInfo1())

                    description.append(getAppearing(ele.getInfo2()));

                    meterEvents.add(new MeterEvent(ele.getDate(),eiCode,ele.getType(),description.toString()));

                } break; // case EVENT_TYPE_MEASURE_FAULT:

                case EVENT_TYPE_UNEXPECTED_BRANCH: {
                    StringBuffer description=new StringBuffer();
                    int eiCode = MeterEvent.PROGRAM_FLOW_ERROR;

                    switch(ele.getInfo1()) {
                        case 1:
                            description.append("invalid parameter");
                            break;
                        case 2:
                            description.append("invalid time");
                            break;
                        case 3:
                            description.append("invalid date");
                            break;
                        case 4:
                            description.append("invalid use of protected code");
                            break;
                        case 5:
                            description.append("read or write using an invalid address");
                            break;
                        case 6:
                            description.append("compiler has produced an invalid piece of executable code");
                            break;
                        case 7:
                            description.append("unknown state in protocol state machine");
                            break;
                        case 8:
                            description.append("a serial io buffer overloaded");
                            break;
                        case 9:
                            description.append("divide by zero");
                            break;
                        case 10:
                            description.append("unexpected case in switch (forgotten initialisation)");
                            break;
                        case 11:
                            description.append("error on write of eeprom");
                            break;
                        case 12:
                            description.append("invalid configuration (in eeprom)");
                            break;
                        case 13:
                            description.append("value of variable is out of allowed range");
                            break;
                        case 14:
                            description.append("storing of a register background value failed");
                            break;
                        case 15:
                            description.append("invalid command mode for a message handler");
                            break;
                        case 16:
                            description.append("metering core didn't answer");
                            break;
                        case 17:
                            description.append("core communications ran out of buffers");
                            break;
                        case 18:
                            description.append("result is too big or too little");
                            break;
                        default:
                            description.append("unknown fault type code "+ele.getInfo1());
                            break;
                    } // switch(ele.getInfo1())

                    description.append(", "+ele.getInfo2());

                    meterEvents.add(new MeterEvent(ele.getDate(),eiCode,ele.getType(),description.toString()));

                } break; // case EVENT_TYPE_UNEXPECTED_BRANCH:

                default: {
                    meterEvents.add(new MeterEvent(ele.getDate(),MeterEvent.OTHER,ele.getType()));
                } break;

            } // switch(ele.getType())

        } // while(it.hasNext())

        return meterEvents;

    } // private List buildMeterEvents(List eventLogEntries)

    private String getAppearing(int info) {
        String description;
        switch(info) {
            case 0:
               description = ", the fault disappeared";
               break;
            case 1:
               description = ", the fault was detected";
               break;
            default:
               description = ", unknown info2 code "+info;
               break;

        } // switch(info)
        return description;

    } // private String getAppearing(int info)

    private String getCommunicationchannelDescription(int info) {
        StringBuffer description = new StringBuffer();
        switch(info) {
            case 0:
                description.append("unknown communicationchannel");
                break;
            case 1:
                description.append("opto communicationchannel");
                break;
            case 2:
                description.append("first communicationchannel channel found in option modules");
                break;
            case 3:
                description.append("second communicationchannel channel found in option modules");
                break;
            default:
                description.append("unknown communicationchannel code "+info);
                break;
        } // switch(info)
        return description.toString();
    } // private String getCommunicationchannelDescription(int info)



} // public class EnermetLoadProfile




