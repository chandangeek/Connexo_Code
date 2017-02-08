/*
 * QuantumProfile.java
 *
 * Created on 18 september 2006, 13:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.quantum.basepages.MassMemoryRecordBasePage;
import com.energyict.protocolimpl.itron.quantum.basepages.UnitTable;

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
public class QuantumProfile {

    final int DEBUG=0;

    Quantum quantum;



    /** Creates a new instance of QuantumProfile */
    public QuantumProfile(Quantum quantum) {
        this.quantum=quantum;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {

if (DEBUG>=1) System.out.println("read from "+lastReading);

        ProfileData profileData = new ProfileData();

        profileData.setChannelInfos(buildChannelInfos());

        profileData.setIntervalDatas(buildIntervalData(lastReading,includeEvents));
        profileData.sort();

        if (includeEvents)
            profileData.generateEvents(); // because we do not have a logbook!


        return profileData;
    } // public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException

    private List buildChannelInfos() throws IOException {
        List channelInfos = new ArrayList();
        int nrOfChannels = quantum.getBasePagesFactory().getMassMemoryBasePages().getNumberOfChannels();
        for (int channel=0;channel<nrOfChannels;channel++) {
            UnitTable ut = UnitTable.findUnitTable(quantum.getBasePagesFactory().getMassMemoryBasePages().getChannelPrograms()[channel].getRegisterNumber());
            ChannelInfo chi = new ChannelInfo(channel,"Quantum_channel_"+(channel+1),Unit.get(ut.getUnit().getDlmsCode(),quantum.getLoadProfileUnitScale()));
            BigDecimal bd = BigDecimal.valueOf((long)quantum.getBasePagesFactory().getMassMemoryBasePages().getChannelPrograms()[channel].getMultiplier());

            if (ut.isVoltSquare())
                chi.setMultiplier(bd.multiply(quantum.getBasePagesFactory().getVoltageAndCurrentBasePage().getIntegrationConstant().getVoltSquare()));
            else if (ut.isAmpSquare())
                chi.setMultiplier(bd.multiply(quantum.getBasePagesFactory().getVoltageAndCurrentBasePage().getIntegrationConstant().getAmpSquare()));
            else
                chi.setMultiplier(bd.multiply(quantum.getBasePagesFactory().getVoltageAndCurrentBasePage().getIntegrationConstant().getDemandAndEnergy()));
            channelInfos.add(chi);
        }
        return channelInfos;
    }

    private int mapStatus2EIStatus(int status) {
        int eiStatus=0;
        if ((status & 0x8) == 0x8) // RAM ERROR
            eiStatus|=IntervalStateBits.DEVICE_ERROR;
        if ((status & 0x4) == 0x4) // OVERFLOW
            eiStatus|=IntervalStateBits.OVERFLOW;
        return eiStatus;
    }

    private List buildIntervalData(Date lastReading, boolean includeEvents) throws IOException {

        // wait to request profile data until 20 seconds before or 10 seconds after crossboundary to avoid unsynchronized table read!
        waitUntilTimeValid(10,20);
        // Get current meter calendar time to calculate last interval close timestamp
        Calendar now = quantum.getBasePagesFactory().getRealTimeBasePage().getCalendar();

        List intervalDatas = new ArrayList();
        List massMemoryRecords = readMassMemoryRecords(lastReading);

        int profileInterval = quantum.getBasePagesFactory().getMassMemoryBasePages().getRecordingIntervalLength();
        int nrOfChannels = quantum.getBasePagesFactory().getMassMemoryBasePages().getNumberOfChannels();
        int currentIntervalNr = quantum.getBasePagesFactory().getMassMemoryBasePages().getCurrentMassMemoryIntervalNumber();
        int recordLengthInMinutes = profileInterval*60;

        // loop through all records starting with the oldest...
        for(int recordNr=massMemoryRecords.size()-1;recordNr>=0;recordNr--) {
            MassMemoryRecordBasePage massMemoryRecord = (MassMemoryRecordBasePage)massMemoryRecords.get(recordNr);


            Calendar cal=null;
            if (recordNr==0) {
                if (massMemoryRecords.size() == 1) {

                    // KV_TO_DO
                    // In this case, the meter seems to have a bug in the firmware. Waiting 20 seconds after intervalclose seems not to be enough to have the
                    // currentIntervalNr updated correct! So, the current time will point to the previous interval...
                    // This is a difficult to simulate issue and this is also very rarely. The phenomenon seems to happen
                    // when reading the meter within the first 60 intervals after a mass memory (profile) clear and 20 sec after the intervalboundary.

                    cal = (Calendar)now.clone();
                    ParseUtils.roundDown2nearestInterval(cal, quantum.getProfileInterval());
                } else {
                    MassMemoryRecordBasePage temp = (MassMemoryRecordBasePage)massMemoryRecords.get(1);
                    cal = (Calendar)temp.getCalendar().clone();
                    cal.add(Calendar.MINUTE, currentIntervalNr*profileInterval);
                }
            } else {
                cal = (Calendar)massMemoryRecord.getCalendar().clone();
            }



            for (int interval=59;interval>=0;interval--) {


                if ((recordNr>0) || ((recordNr==0) && (interval < currentIntervalNr))) {
                    int eiStatus=0;
                    int protocolStatus=0;
                    boolean outage = (massMemoryRecord.getStatus() & (0x80 << Math.abs(interval -59))) != 0;
                    if (outage)
                        eiStatus = (IntervalStateBits.POWERDOWN | IntervalStateBits.POWERUP);
                    eiStatus |= mapStatus2EIStatus((int)massMemoryRecord.getStatus()&0xF);
                    protocolStatus = (int)massMemoryRecord.getStatus()&0xF;

                    IntervalData intervalData = new IntervalData(new Date(cal.getTime().getTime()), eiStatus, protocolStatus );

                    for (int channel=0;channel<nrOfChannels;channel++) {

                        BigDecimal bd = BigDecimal.valueOf((long)massMemoryRecord.getIntervalRecords()[interval].getValues()[channel]);
                        bd = bd.multiply(quantum.getAdjustChannelMultiplier());
                        intervalData.addValue(bd);

                        if (DEBUG>=1) {
                            System.out.println(""+cal.getTime()+", recordNr="+recordNr+", interval="+interval+", channel="+channel+", val="+massMemoryRecord.getIntervalRecords()[interval].getValues()[channel]);
                        }
                    } // for (int channel=0;channel<nrOfChannels;channel++)

                    intervalDatas.add(intervalData);
                    cal.add(Calendar.MINUTE,(-1)*profileInterval);

                } // if ((recordNr>0) || ((recordNr==0) && (interval < currentIntervalNr)))


            } // for (int interval=59;interval>=0;interval--)


        } // for(int i=massMemoryRecords.size()-1;i>0;i--)


        return intervalDatas;

    } // private List buildIntervalData(Date lastReading, boolean includeEvents) throws IOException

    private List readMassMemoryRecords(Date lastReading) throws IOException {

        /*
         *  Mass memory (load profile) is organized in records of recordSize length. The area is circular and we have to calculate the max nr of records possible.
         */

        List massMemoryRecords = new ArrayList();
        int recordSize = quantum.getBasePagesFactory().getMassMemoryBasePages(true).getMassMemoryRecordLength();


        int massMemoryStartOffset = quantum.getBasePagesFactory().getMassMemoryBasePages().getLogicalMassMemoryStartOffset();

        int maxNrOfRecords = (quantum.getBasePagesFactory().getMassMemoryBasePages().getLogicalMassMemoryEndOffset()-massMemoryStartOffset)/recordSize;
//        boolean firstRoundTrip = false; //quantum.getBasePagesFactory().getMassMemoryBasePages().getCurrentMassMemoryRecordOffset() ;
        int currentMassMemoryRecordNr = (quantum.getBasePagesFactory().getMassMemoryBasePages().getCurrentMassMemoryRecordOffset()-massMemoryStartOffset) / recordSize;

        if (DEBUG>=1) System.out.println("recordSize="+recordSize+", massMemoryStartOffset=0x"+Integer.toHexString(massMemoryStartOffset)+", maxNrOfRecords="+maxNrOfRecords+", currentMassMemoryRecordNr="+currentMassMemoryRecordNr);

        // read current record. However, current record does not have a timestamp attached, so we will read that record
        // always and use the current-1 to start time reference calculation
        //massMemoryRecords.add(quantum.getBasePagesFactory().getMassMemoryRecordBasePageByAddress(currentMassMemoryRecordAddress));
        massMemoryRecords.add(quantum.getBasePagesFactory().getMassMemoryRecordBasePageByRecordNr(currentMassMemoryRecordNr));

        int profileInterval = quantum.getProfileInterval()/60;
        int recordLengthInMinutes = profileInterval*60;

        //int massMemoryRecordAddress=currentMassMemoryRecordAddress-recordSize;
        int massMemoryRecordNr=currentMassMemoryRecordNr;
        massMemoryRecordNr = (maxNrOfRecords + (massMemoryRecordNr-1)) % maxNrOfRecords;

        // start loop to determine how deep we have to read to get all required intervals
        //while(massMemoryRecordAddress >= massMemoryStartOffset) {
        while(true) {

            if (massMemoryRecordNr == currentMassMemoryRecordNr)  // if roundtrip, quit loop
                break;
//            if ((firstRoundTrip) &&(massMemoryRecordNr == (maxNrOfRecords-1))) // if first roundtrip
//                break;

            //MassMemoryRecordBasePage massMemoryRecord = quantum.getBasePagesFactory().getMassMemoryRecordBasePageByAddress(massMemoryRecordAddress);
            MassMemoryRecordBasePage massMemoryRecord = quantum.getBasePagesFactory().getMassMemoryRecordBasePageByRecordNr(massMemoryRecordNr);

            Calendar cal = (Calendar)massMemoryRecord.getCalendar().clone();
            //cal.add(Calendar.MINUTE, (-1)*recordLengthInMinutes);  // return to first interval of record
            //cal.add(Calendar.MINUTE, profileInterval);  // endtime of interval

            if (DEBUG>=1) System.out.println("massMemoryRecordNr="+massMemoryRecordNr+", massMemoryRecord.getCalendar().gettime()="+massMemoryRecord.getCalendar().getTime()+", cal.getTime()="+cal.getTime()+", lastReading="+lastReading);


            if (cal.getTime().before(lastReading)) {
                // if first interval timestamp is before lastReading, quit loop
                break;
            } else {
                // else, decrement mass memory record pointer and read another record
                massMemoryRecords.add(massMemoryRecord);
                //massMemoryRecordAddress-=recordSize;
                massMemoryRecordNr = (maxNrOfRecords + (massMemoryRecordNr-1)) % maxNrOfRecords;
            }

        } // while(true)


        if (DEBUG>=1) {
            Iterator it = massMemoryRecords.iterator();
            while(it.hasNext()) {
                MassMemoryRecordBasePage massMemoryRecord = (MassMemoryRecordBasePage)it.next();
                System.out.println(massMemoryRecord);
            }
        }

        return massMemoryRecords;

    } // private List readMassMemoryRecords(Date lastReding)

    private void waitUntilTimeValid(int secondsAfterIntervalClose,int secondsBeforeIntervalClose) throws IOException {
        Date date=null;
        long offset2IntervalBoundary=0;
        while(true) {
            date = quantum.getTime();
            long profileInterval = quantum.getProfileInterval();
            long seconds = date.getTime()/1000;
            offset2IntervalBoundary = seconds%profileInterval;
            if ((offset2IntervalBoundary<secondsAfterIntervalClose) || (offset2IntervalBoundary>(profileInterval-secondsBeforeIntervalClose))) {
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }
            } else {

                break;
            }
        } // while(true)
    } // private void waitUntilTimeValid()

}
