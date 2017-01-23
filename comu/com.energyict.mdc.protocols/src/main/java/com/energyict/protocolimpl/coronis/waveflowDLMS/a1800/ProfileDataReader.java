package com.energyict.protocolimpl.coronis.waveflowDLMS.a1800;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.waveflowDLMS.A1800;
import com.energyict.protocolimpl.coronis.waveflowDLMS.AS1253;
import com.energyict.protocolimpl.coronis.waveflowDLMS.BatchObisCodeReader;
import com.energyict.protocolimpl.coronis.waveflowDLMS.TransparantObjectAccessFactory;
import com.energyict.protocolimpl.coronis.waveflowDLMS.WaveFlowDLMSException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProfileDataReader {

    A1800 a1800;
    private int step_nr_of_entries = 36; //Max number of profile data entries we can read out at once, over RF.

    public ProfileDataReader(A1800 a1800) {
        this.a1800 = a1800;
    }

    public static final int CAPTURED_OBJECTS_DATE_FIELD_INDEX = 0;
    public static final int CAPTURED_OBJECTS_STATUSBITS_FIELD_INDEX = 1;
    public static final int CAPTURED_OBJECTS_EXTENDED_STATUSBITS_FIELD_INDEX = 2;
    public static final int CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX = 3;

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        int profileInterval = a1800.getProfileInterval();
        BatchObisCodeReader batchObisCodeReader = new BatchObisCodeReader(a1800);

        if (a1800.isApplyMultiplier()) {
            batchObisCodeReader.add(2, A1800.SCALE_FACTOR);
            batchObisCodeReader.add(2, A1800.MULTIPLIER);
        }

        int nrOfLogEntriesInUse = 0;
        if (includeEvents) {
            batchObisCodeReader.add(7, A1800.LOG_PROFILE);
        }
        if (a1800.isVerifyProfileInterval()) {
            batchObisCodeReader.add(4, a1800.getLoadProfileObisCode());
        }

        batchObisCodeReader.invoke();

        if (includeEvents) {
            nrOfLogEntriesInUse = batchObisCodeReader.intValue(A1800.LOG_PROFILE);
        }
        if (a1800.isVerifyProfileInterval()) {
            profileInterval = batchObisCodeReader.intValue(a1800.getLoadProfileObisCode());
        }
        int scaleFactor = 0;
        int multiplier = 1;
        if (a1800.isApplyMultiplier()) {
            scaleFactor = batchObisCodeReader.intValue(A1800.SCALE_FACTOR);
            multiplier = batchObisCodeReader.intValue(A1800.MULTIPLIER);
        }

        if (profileInterval != a1800.getProfileInterval()) {
            throw new WaveFlowDLMSException("Invalid profile interval. Configured is [" + a1800.getProfileInterval() + "] s, configured in meter is [" + profileInterval + "]!");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(a1800.getTimeZone());
        Date lastReceivedRecord = cal.getTime();
        int total_nr_of_entries = (int) (((lastReceivedRecord.getTime() - lastReading.getTime()) / 1000) / profileInterval);
        int max = a1800.getMaxNumberOfIntervals();
        if (max != 0) {
            total_nr_of_entries = max;   //Limit the number of intervals that is requested in total
        }

        int offset = 0;

        if (total_nr_of_entries < step_nr_of_entries) {
            step_nr_of_entries = total_nr_of_entries + 2;
        }

        //Fetch the data in steps, until the lastReading is reached
        while (lastReceivedRecord.after(lastReading)) {
            int from = step_nr_of_entries + offset;
            from = (from > total_nr_of_entries) ? total_nr_of_entries + 2 : from;
            int to = offset + (offset > 0 ? 1 : 0);
            if (to > from) {
                break;
            }
            AbstractDataType adt = a1800.getTransparantObjectAccessFactory().readObjectAttributeEntry(a1800.getLoadProfileObisCode(), TransparantObjectAccessFactory.ATTRIBUTE_VALUE, from, to);
            offset += step_nr_of_entries;

            // parse the AXD-R returned data...
            Calendar calendar = Calendar.getInstance(a1800.getTimeZone());
            Array array = adt.getArray();
            for (AbstractDataType arrayElement : array.getAllDataTypes()) {

                Structure structure = arrayElement.getStructure();
                int nrOfchannels = structure.nrOfDataTypes() - CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX; // nr of channels = nr of elements in structure - date field - startus bits field
                if (nrOfchannels == 0) {
                    throw new WaveFlowDLMSException("No channels in the load profile. Might be a configuration error!");
                }

                if (channelInfos.size() == 0) {
                    // because we have to take care not to do too many round trips, we leave the unit type responsibility to EIServer to configure.
                    for (int i = 0; i < nrOfchannels; i++) {
                        channelInfos.add(new ChannelInfo(i, "A1800_" + (i + 1), Unit.get("")));
                    }
                    profileData.setChannelInfos(channelInfos);
                }

                AbstractDataType structureElement = structure.getDataType(CAPTURED_OBJECTS_DATE_FIELD_INDEX);
                DateTime dateTime = new DateTime(structureElement.getOctetString(), a1800.getTimeZone());
                calendar.setTime(dateTime.getValue().getTime());
                lastReceivedRecord = calendar.getTime();
                if (lastReceivedRecord.before(lastReading)) {
                    break;
                }

                if (ParseUtils.isOnIntervalBoundary(calendar, a1800.getProfileInterval())) {
                    int protocolStatus = structure.getDataType(CAPTURED_OBJECTS_STATUSBITS_FIELD_INDEX).intValue();
                    long extendedProtocolStatus = structure.getDataType(CAPTURED_OBJECTS_EXTENDED_STATUSBITS_FIELD_INDEX).intValue();

                    IntervalData intervalData = new IntervalData(calendar.getTime(), protocolStatus2EICode(protocolStatus), protocolStatus);
                    for (int index = 0; index < nrOfchannels; index++) {
                        BigDecimal bd = BigDecimal.valueOf(structure.getDataType(CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX + index).longValue());
                        if (a1800.isApplyMultiplier()) {
                            bd = bd.multiply(BigDecimal.valueOf(multiplier)).multiply(BigDecimal.valueOf(Math.pow(10, scaleFactor)));
                        }

                        intervalData.addValue(bd, protocolStatus, extendedProtocolStatus2EICode(extendedProtocolStatus, index));
                    }
                    profileData.addInterval(intervalData);
                }
            } // for (AbstractDataType arrayElement : array.getAllDataTypes())
        }

        if (includeEvents) {
            profileData.setMeterEvents(readMeterLogbook(lastReading, nrOfLogEntriesInUse));
        }

        profileData.sort();

        return profileData;
    }

    private List<MeterEvent> readMeterLogbook(Date lastReading, int nrOfLogEntriesInUse) throws IOException {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        int count = 0;
        while (!lastReadingIsReached(meterEvents, lastReading)) {
            count++;
            int fromEntry = 3 * count;
            int offset = 3 * (count - 1);
            AbstractDataType adt = a1800.getTransparantObjectAccessFactory().readObjectAttributeEntry(AS1253.LOG_PROFILE, TransparantObjectAccessFactory.ATTRIBUTE_VALUE, fromEntry > nrOfLogEntriesInUse ? nrOfLogEntriesInUse : fromEntry, offset);
            Array array = adt.getArray();
            for (AbstractDataType arrayElement : array.getAllDataTypes()) {
                DateTime dateTime = new DateTime(arrayElement.getStructure().getDataType(0).getOctetString(), a1800.getTimeZone());
                Date date = dateTime.getValue().getTime();
/*
            Example:
			OctetString=$07$DB$01$03$00$10$0E$39$00$00$00$00
			  Unsigned16=10    sequence nr
			  Unsigned16=0     user id
			  Unsigned16=2060  event nr
*/
                int meterEventCode2MeterEvents = arrayElement.getStructure().getDataType(3).intValue();
                meterEvents.add(meterEventCode2MeterEvent(date, meterEventCode2MeterEvents));
            }
            if (fromEntry > nrOfLogEntriesInUse) {
                break;
            }
        }

        int applicationstatus = a1800.getParameterFactory().readApplicationStatus();

        if ((applicationstatus & 0x01) == 0x01) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, 10, "Link fault with energymeter"));
        }
        if ((applicationstatus & 0x02) == 0x02) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.POWERUP, 9, "Power Back notification (RF module)"));
        }
        if ((applicationstatus & 0x04) == 0x04) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.POWERDOWN, 12, "Power down notification (RF module)"));
        }

        if ((applicationstatus & 0x7) != 0) {
            a1800.getParameterFactory().writeApplicationStatus(0);
        }

        meterEvents.addAll(ErrorRegisterParser.readMeterEvents(a1800));
        meterEvents.addAll(WarningRegisterParser.readMeterEvents(a1800));

        return meterEvents;
    }

    /**
     * Check if there's an event with a timestamp that is earlier than the last reading
     */
    private boolean lastReadingIsReached(List<MeterEvent> meterEvents, Date lastReading) {
        for (MeterEvent meterEvent : meterEvents) {
            if (meterEvent.getTime().before(lastReading)) {
                return true;
            }
        }
        return false;
    }


    private MeterEvent meterEventCode2MeterEvent(Date date, int meterEventCode) {

        if (meterEventCode == 1) {
            return new MeterEvent(date, MeterEvent.POWERDOWN, meterEventCode, "Primary Power Down");
        }
        if (meterEventCode == 2) {
            return new MeterEvent(date, MeterEvent.POWERUP, meterEventCode, "Primary Power Up");
        }
        if (meterEventCode == 3) {
            return new MeterEvent(date, MeterEvent.SETCLOCK_BEFORE, meterEventCode, "Time Changed (old time)");
        }
        if (meterEventCode == 4) {
            return new MeterEvent(date, MeterEvent.SETCLOCK_AFTER, meterEventCode, "Time Changed (new time)");
        }
        if (meterEventCode == 11) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "End Device Programmed");
        }
        if (meterEventCode == 18) {
            return new MeterEvent(date, MeterEvent.CLEAR_DATA, meterEventCode, "Event Log Cleared");
        }
        if (meterEventCode == 20) {
            return new MeterEvent(date, MeterEvent.BILLING_ACTION, meterEventCode, "Demand Reset Occurred");
        }
        if (meterEventCode == 21) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Self Read Occurred");
        }
        if (meterEventCode == 32) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Test mode started");
        }
        if (meterEventCode == 33) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Test mode stopped");
        }
        if (meterEventCode == 2048) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG Enter Tier Override");
        }
        if (meterEventCode == 2049) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG Exit Tier Override");
        }
        if (meterEventCode == 2050) {
            return new MeterEvent(date, MeterEvent.TERMINAL_OPENED, meterEventCode, "MFG Terminal cover tamper");
        }
        if (meterEventCode == 2051) {
            return new MeterEvent(date, MeterEvent.COVER_OPENED, meterEventCode, "MFG Main cover tamper");
        }
        if (meterEventCode == 2052) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG External Event 0");
        }
        if (meterEventCode == 2053) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG External Event 1");
        }
        if (meterEventCode == 2054) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG External Event 2");
        }
        if (meterEventCode == 2055) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG External Event 3");
        }
        if (meterEventCode == 2056) {
            return new MeterEvent(date, MeterEvent.PHASE_FAILURE, meterEventCode, "MFG Phase A OFF");
        }
        if (meterEventCode == 2057) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG Phase A ON");
        }
        if (meterEventCode == 2058) {
            return new MeterEvent(date, MeterEvent.PHASE_FAILURE, meterEventCode, "MFG Phase B OFF");
        }
        if (meterEventCode == 2059) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG Phase B ON");
        }
        if (meterEventCode == 2060) {
            return new MeterEvent(date, MeterEvent.PHASE_FAILURE, meterEventCode, "MFG Phase C OFF");
        }
        if (meterEventCode == 2061) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "MFG Phase C ON");
        }
        if (meterEventCode == 2062) {
            return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Remote Flash failed");
        }
        return new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Unknown meterevent [" + meterEventCode + "]");

    }

    private int extendedProtocolStatus2EICode(long extendedProtocolStatus, int channelIndex) {

        int eiCode = 0;
        int extendedStatus = (int) ((extendedProtocolStatus >> (channelIndex * 4)) & 0x0FL);


		/*
         *
		 * bit 3
         * bit 2
         * bit 1 Overflow. The channel data has a value that exceeds the format used internally in the meter.
         *                 The value displayed is the maximum that can be represented by that format (2/5/6bytes, signed/unsigned).
         * bit 0 TestMode. Data recorded in test mode
		 *
		 */

        if ((extendedStatus & 0x01) == 0x01) {
            eiCode |= IntervalStateBits.TEST;
        }
        if ((extendedStatus & 0x02) == 0x02) {
            eiCode |= IntervalStateBits.OVERFLOW;
        }

        return eiCode;
    }

    private int protocolStatus2EICode(int protocolStatus) {

        int eiCode = 0;
        if ((protocolStatus & 0x01) == 0x00) {
            eiCode |= IntervalStateBits.CORRUPTED;  // bit 0  IF 1 interval is valid, if 0 invalid!!!
        }
        //bit 1 reserved
        if ((protocolStatus & 0x0C) == 0x04) {
            eiCode |= IntervalStateBits.SHORTLONG; // bit 2/3 partial interval
        }
        if ((protocolStatus & 0x0C) == 0x08) {
            eiCode |= IntervalStateBits.SHORTLONG; // bit 2/3 long interval
        }
        if ((protocolStatus & 0x0C) == 0x0C) {
            eiCode |= IntervalStateBits.CORRUPTED; // bit 2/3 skipped interval
        }

        if ((protocolStatus & 0x10) == 0x10) {
            eiCode |= IntervalStateBits.OTHER; // bit 4  DST
        }
        if ((protocolStatus & 0x20) == 0x20) {
            eiCode |= IntervalStateBits.POWERDOWN; // bit 5  powerfail
        }
        if ((protocolStatus & 0x40) == 0x40) {
            eiCode |= IntervalStateBits.SHORTLONG; // bit 6  clock set forward
        }
        if ((protocolStatus & 0x80) == 0x80) {
            eiCode |= IntervalStateBits.SHORTLONG; // bit 7  clock set backward
        }

        return eiCode;
    }

}
