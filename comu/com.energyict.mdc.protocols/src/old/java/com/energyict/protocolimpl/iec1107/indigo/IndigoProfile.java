/*
 * IndigoProfile.java
 *
 * Created on 8 juli 2004, 17:42
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

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
public class IndigoProfile {

    private static final int DEBUG=0;

    ProtocolLink protocolLink;
    LogicalAddressFactory logicalAddressFactory;
    MeterExceptionInfo meterExceptionInfo;

    /** Creates a new instance of IndigoProfile */
    public IndigoProfile(ProtocolLink protocolLink, MeterExceptionInfo meterExceptionInfo, LogicalAddressFactory logicalAddressFactory) {
        this.protocolLink=protocolLink;
        this.logicalAddressFactory=logicalAddressFactory;
        this.meterExceptionInfo=meterExceptionInfo;
    }

    public ProfileData getProfileData(Date from, Date to, boolean statusFlagChannel, boolean readCurrentDay) throws IOException {
        if (DEBUG>=1) System.out.println("KV_DEBUG> getProfileData("+from.toString()+","+to.toString()+")");
        if (to.getTime() < from.getTime()) throw new IOException("IndigoProfile, getProfileData, error ("+from.toString()+") > ("+to.toString()+")");
        long offset = to.getTime() - from.getTime();
        final long ONEDAY=24*60*60*1000;
        long tostd = to.getTime() + (long)protocolLink.getTimeZone().getOffset(to.getTime());
        long fromstd = from.getTime() + (long)protocolLink.getTimeZone().getOffset(from.getTime());
        long nrOfDaysToRetrieve = ((tostd/ONEDAY) - (fromstd/ONEDAY)) + 1;
        ProfileData profileData = doGetProfileData(nrOfDaysToRetrieve,statusFlagChannel,readCurrentDay);
        profileData.sort();
        if (!isProfileDataValid(from,to,profileData))
            throw new IOException("IndigoProfile, getProfileData(), No new interval read from meter. Meter is probably faulty!");
        return profileData;
    }

    private boolean isProfileDataValid(Date from, Date to, ProfileData profileData) {
        Iterator it = profileData.getIntervalDatas().iterator();
        while(it.hasNext()) {
            IntervalData itvd = (IntervalData)it.next();
            if (from.before(itvd.getEndTime()) && to.after(itvd.getEndTime()))
                return true;
        }
        return false;
    }

    public ProfileData doGetProfileData(long nrOfDaysToRetrieve, boolean statusFlagChannel, boolean readCurrentDay) throws IOException {
        ProfileData profileData = new ProfileData();
        boolean noProfileData;

        if (DEBUG>=1) System.out.println("KV_DEBUG> nrOfDaysToRetrieve="+nrOfDaysToRetrieve);

        // date to compare with...
        Date readDate = getLogicalAddressFactory().getDateTimeGMT().getDate();

        // The Indigo meter can have a maximum of 12 recording channels in its profile.
        // The meterdefinition has a bitmask that tells which of the 12 channels are used. Each channel has a
        // specific phenomenon.
        // Retrieve day per day for each channel, starting with the most recent one (today).

        int startDay=readCurrentDay?0:1;

        for (int day=startDay;day<nrOfDaysToRetrieve;day++) {
            if (DEBUG>=1) System.out.println("KV_DEBUG> day="+day);

            // array with one day profile data for each channel
            ProfileDay[] profileDays=new ProfileDay[getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels()];

            // *********************************************************************************************************************
            // get the profileDays
            noProfileData=false;
            for (int channelIndex=0;channelIndex<getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();channelIndex++) {
                if (DEBUG>=1) System.out.println("KV_DEBUG> channelIndex="+channelIndex);
                StringBuffer strbuff = new StringBuffer();
                // R3 AAAB(CCCC)
                strbuff.append(buildLength(day,3).toUpperCase()); // day to retrieve AAA
                strbuff.append(Integer.toHexString(getLogicalAddressFactory().getMeteringDefinition().getChannelId(channelIndex)).toUpperCase()); // channel B
                strbuff.append('(');
                strbuff.append(buildLength(1,4).toUpperCase()); // retrieve 1 day CCCC
                strbuff.append(')');
                getLogicalAddressFactory().getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ3,strbuff.toString().getBytes());
                byte[] ba = getLogicalAddressFactory().getProtocolLink().getFlagIEC1107Connection().receiveRawData();
                validateData(ba);

                profileDays[channelIndex]=new ProfileDay(ba,getLogicalAddressFactory());

                if (DEBUG>=2) System.out.println(profileDays[channelIndex]);

                // if a channel is empty, we suppose all channels are empty or no profiledata exist for that day!
                if (profileDays[channelIndex].getValues() == null) {
                    noProfileData = true;
                    break;
                }
            } // for (int channelIndex=0;channelIndex<getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();channelIndex++) {


            // *********************************************************************************************************************
            // build ChannelInfo only if first day
            if (day==startDay) {
                List channelInfos = new ArrayList();
                for (int channelIndex=0;channelIndex<getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();channelIndex++) {
                    Unit unit = profileDays[channelIndex].getChannelUnit();
                    if (!unit.equals(Unit.get("")))
                        // KV TO_DO following the doc about the profile, the value after calculation is the
                        // value in W, var or VA. This seems NOT to be so!
                        unit = unit.getVolumeUnit(); //getFlowUnit();
                    if ((statusFlagChannel) || ((!(statusFlagChannel)) && (!(profileDays[channelIndex].isStatusChannel()))))
                        channelInfos.add(new ChannelInfo(channelIndex,"Indigo+ channel "+channelIndex,unit));
                }
                profileData.setChannelInfos(channelInfos);
            }


            // *********************************************************************************************************************
            // build the intervaldata
            if (!noProfileData) {
                int intervalsPerDay = (3600*24)/getLogicalAddressFactory().getProtocolLink().getProfileInterval();
                List intervalDatas=new ArrayList();
                ProfileDay statusChannel=null;
                if (profileDays[profileDays.length-1].isStatusChannel())
                    statusChannel = profileDays[profileDays.length-1];

                // use profileday of channel 0 to init calendar
                Calendar calendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
                calendar.setTime(profileDays[0].getDate());

                for (int interval=0;interval<intervalsPerDay;interval++) {
                    IntervalData intervalData=null;
                    int protocolStatus=0;
                    calendar.add(Calendar.SECOND,getLogicalAddressFactory().getProtocolLink().getProfileInterval());
                    if (statusChannel != null)
                        protocolStatus = statusChannel.getIntValue(interval);

                    // verify if intervaldate < readDate else do not add future values to the profile...
                    // it seems that there is not other way to determine gaps or 0-consumption
                    if (calendar.getTime().before(readDate)) {
                        intervalData = new IntervalData(((Calendar)calendar.clone()).getTime(),mapIntervalStatus2EIStatus(protocolStatus),protocolStatus);
                        for (int channelIndex=0;channelIndex<getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();channelIndex++) {

                            // build meterevents from status and dailyflags...
                            int dailyStatus=0;
                            if (interval == 0)
                                dailyStatus = profileDays[channelIndex].getDailyFlags()<<8;

                            if (channelIndex == 0) {
                                List meterEvents = mapIntervalStatus2MeterEvents(protocolStatus|dailyStatus, intervalData.getEndTime());
                                if (meterEvents.size() > 0)
                                    profileData.getMeterEvents().addAll(meterEvents);
                            }

                            if ((statusFlagChannel) || ((!(statusFlagChannel)) && (!(profileDays[channelIndex].isStatusChannel()))))
                            intervalData.addValue(profileDays[channelIndex].getBigDecimalValue(interval));
                        } // for (int channelIndex=0;channelIndex<getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();channelIndex++)
                        profileData.addInterval(intervalData);
                    }
                } // for (int interval=0;interval<intervalsPerDay;interval++)
            } // if (profileDays[0].getValues() != null)
        } // for (int day=0;day < nrOfDaysToRetrieve, day++)

        return profileData;

    } // public List doGetProfileData(int nrOfDaysToRetrieve) throws IOException

    private void validateData(byte[] data) throws FlagIEC1107ConnectionException {
        String str = new String(data);
        // We know about ERRDAT and ERRADD as returned error codes from the Indigo+ meter.
        // Probably there are more...
        if (str.indexOf("ERR") != -1) {
            throw new FlagIEC1107ConnectionException("IndigoProfile, validateData, "+getLogicalAddressFactory().getMeterExceptionInfo().getExceptionInfo(str));
        }
    }

    // daily flags (these flags are shifted 1 byte so that we can use the same mapping methods)
    private static final int BATTERY_MAINTENANCE_FLAG=0x0800;
    private static final int CLOCK_FAILURE_FLAG=0x1000;
    private static final int MD_RESET_FLAG=0x2000;
    private static final int POWER_OFF_24_HOUR=0x4000;

    // interval status flags
    private static final int PHASE_C_DOWN=0x80;
    private static final int PHASE_B_DOWN=0x40;
    private static final int PHASE_A_DOWN=0x20;
    private static final int POWER_FAIL=0x10;
    private static final int REVERSE_ENERGY=0x08;
    private static final int LEVEL_2_ACCESS=0x04;
    private static final int CLOCK_SET=0x02;
    private static final int BATTERY_LOW=0x01;

    private int mapIntervalStatus2EIStatus(int intervalStatus) {
        int eiStatus=0;
        if ((intervalStatus & PHASE_C_DOWN) == PHASE_C_DOWN) {
            eiStatus |= IntervalStateBits.PHASEFAILURE;
        }
        if ((intervalStatus & PHASE_B_DOWN) == PHASE_B_DOWN) {
            eiStatus |= IntervalStateBits.PHASEFAILURE;
        }
        if ((intervalStatus & PHASE_A_DOWN) == PHASE_A_DOWN) {
            eiStatus |= IntervalStateBits.PHASEFAILURE;
        }
        if ((intervalStatus & POWER_FAIL) == POWER_FAIL) {
            eiStatus |= IntervalStateBits.POWERDOWN;
            //eiStatus |= IntervalStateBits.POWERUP;
        }
        if ((intervalStatus & REVERSE_ENERGY) == REVERSE_ENERGY) {
            eiStatus |= IntervalStateBits.REVERSERUN;
        }
//        if ((intervalStatus & LEVEL_2_ACCESS) == LEVEL_2_ACCESS) {
//            // absorbed...
//        }
        if ((intervalStatus & CLOCK_SET) == CLOCK_SET) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }
        if ((intervalStatus & BATTERY_LOW) == BATTERY_LOW) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        return eiStatus;
    } // private int mapIntervalStatus2EIStatus(int intervalStatus)

    private List mapIntervalStatus2MeterEvents(int status, Date date) {


        List meterEvents=new ArrayList();
//        if ((status & PHASE_C_DOWN) == PHASE_C_DOWN) {
//            // absorbed...
//        }
//        if ((status & PHASE_B_DOWN) == PHASE_B_DOWN) {
//            // absorbed...
//        }
//        if ((status & PHASE_A_DOWN) == PHASE_A_DOWN) {
//            // absorbed...
//        }
        if ((status & POWER_FAIL) == POWER_FAIL) {
            meterEvents.add(new MeterEvent(date,MeterEvent.POWERDOWN,status));
            //meterEvents.add(new MeterEvent(date,MeterEvent.POWERUP,status));
        }
//        if ((status & REVERSE_ENERGY) == REVERSE_ENERGY) {
//        }
//        if ((status & LEVEL_2_ACCESS) == LEVEL_2_ACCESS) {
//            // absorbed...
//        }
        if ((status & CLOCK_SET) == CLOCK_SET) {
            meterEvents.add(new MeterEvent(date,MeterEvent.SETCLOCK,status));
        }
        if ((status & BATTERY_LOW) == BATTERY_LOW) {
            meterEvents.add(new MeterEvent(date,MeterEvent.HARDWARE_ERROR,status));
        }
        if ((status & BATTERY_MAINTENANCE_FLAG) == BATTERY_MAINTENANCE_FLAG) {
            meterEvents.add(new MeterEvent(date,MeterEvent.HARDWARE_ERROR,status));
        }
        if ((status & CLOCK_FAILURE_FLAG) == CLOCK_FAILURE_FLAG) {
            meterEvents.add(new MeterEvent(date,MeterEvent.HARDWARE_ERROR,status));
        }
        if ((status & MD_RESET_FLAG) == MD_RESET_FLAG) {
            meterEvents.add(new MeterEvent(date,MeterEvent.MAXIMUM_DEMAND_RESET,status));
        }
//        if ((status & POWER_OFF_24_HOUR) == POWER_OFF_24_HOUR) {
//            // absorb
//        }
        return meterEvents;
    } // private int mapIntervalStatus2MeterEvents(int status,Date date)

    private String buildLength(int size,int length) {
        String str=Integer.toHexString(size);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length())
            for (int i=0;i<(length-str.length());i++)
                strbuff.append('0');
        strbuff.append(str);
        return strbuff.toString();
    }




    /**
     * Getter for property protocolLink.
     * @return Value of property protocolLink.
     */
    public com.energyict.protocolimpl.iec1107.ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    /**
     * Getter for property logicalAddressFactory.
     * @return Value of property logicalAddressFactory.
     */
    public com.energyict.protocolimpl.iec1107.indigo.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }

    /**
     * Getter for property meterExceptionInfo.
     * @return Value of property meterExceptionInfo.
     */
    public MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }

}
