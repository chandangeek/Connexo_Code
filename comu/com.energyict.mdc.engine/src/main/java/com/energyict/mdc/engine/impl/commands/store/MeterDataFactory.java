package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link Reading Readings} and {@link com.elster.jupiter.metering.readings.IntervalReading}s
 * based on a specific <i>CollectedData</i>.
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/11/13
 * Time: 09:49
 */
public final class MeterDataFactory {

    /**
     * Creates a {@link Reading} based on the given CollectedRegister and the ObisCode.
     *
     * @param deviceRegister The given collectedRegister
     * @param obisCode The obisCode of the collectedRegister
     * @param readingTypeUtilService The MdcReadingTypeUtilService
     * @return the newly created Reading
     */
    public static Reading createReadingForDeviceRegisterAndObisCode(final CollectedRegister deviceRegister, final ObisCode obisCode, MdcReadingTypeUtilService readingTypeUtilService) {
        ReadingImpl reading = getRegisterReading(deviceRegister, obisCode, readingTypeUtilService);
        if (deviceRegister.getFromTime() != null && deviceRegister.getToTime() != null) {
            reading.setInterval(deviceRegister.getFromTime(), deviceRegister.getToTime());
        }
        return reading;
    }

    private static ReadingImpl getRegisterReading(final CollectedRegister collectedRegister, final ObisCode obisCode, MdcReadingTypeUtilService readingTypeUtilService) {
        return new ReadingImpl(
                readingTypeUtilService.getReadingTypeFrom(obisCode, collectedRegister.getCollectedQuantity().getUnit()),
                collectedRegister.getCollectedQuantity().getAmount(),
                collectedRegister.getEventTime() != null ? collectedRegister.getEventTime() : collectedRegister.getReadTime());
    }

    /**
     * Creates a list of {@link EndDeviceEvent EndDeviceEvents} based on the given DeviceLogBook
     *
     * @param deviceLogBook the collected LogBook which will serve as an input for the EndDeviceEvents
     * @return the newly created EndDeviceEvent list
     */
    public static List<EndDeviceEvent> createEndDeviceEventsFor(DeviceLogBook deviceLogBook) {
        List<EndDeviceEvent> endDeviceEvents = new ArrayList<>();
        for (MeterProtocolEvent meterProtocolEvent : deviceLogBook.getCollectedMeterEvents()) {
            EndDeviceEventImpl endDeviceEvent = new EndDeviceEventImpl(meterProtocolEvent.getEventType().getName(), meterProtocolEvent.getTime());
            endDeviceEvent.setLogBookId(meterProtocolEvent.getEventLogId());
            endDeviceEvent.setLogBookPosition(meterProtocolEvent.getDeviceEventId());
            endDeviceEvents.add(endDeviceEvent);
        }
        return endDeviceEvents;
    }

    /**
     * Creates a list of {@link IntervalBlock IntervalBlocks} based on the given CollectedLoadProfile and interval
     *
     * @param collectedLoadProfile The collectedLoadProfile which will server as input for the IntervalBlocks
     * @param interval The intervalPeriod which solely is used create the ReadingTypeCode
     * @param readingTypeUtilService The MdcReadingTypeUtilService
     * @return the newly created IntervalBlocks list
     */
    public static List<IntervalBlock> createIntervalBlocksFor(CollectedLoadProfile collectedLoadProfile, TimeDuration interval, MdcReadingTypeUtilService readingTypeUtilService) {
        List<IntervalBlockImpl> intervalBlock = createIntervalBlocks(collectedLoadProfile, interval, readingTypeUtilService);
        for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
            for (Pair<IntervalBlockImpl, IntervalValue> pair : DualIterable.endWithLongest(intervalBlock, intervalData.getIntervalValues())) {
                // safest way to convert from Number to BigDecimal -> using the Number#toString()
                pair.getFirst().addIntervalReading(new IntervalReadingImpl(intervalData.getEndTime(), new BigDecimal(pair.getLast().getNumber().toString())));
            }
        }
        return new ArrayList<IntervalBlock>(intervalBlock);
    }

    private static List<IntervalBlockImpl> createIntervalBlocks(CollectedLoadProfile collectedLoadProfile, TimeDuration interval, MdcReadingTypeUtilService readingTypeUtilService) {
        List<IntervalBlockImpl> intervalBlocks = new ArrayList<>();
        for (ChannelInfo channelInfo : collectedLoadProfile.getChannelInfo()) {
            ObisCode channelObisCode;
            channelObisCode = channelInfo.getChannelObisCode();
            String readingTypeMRID = getReadingTypeFrom(interval, readingTypeUtilService, channelInfo, channelObisCode);
            intervalBlocks.add(new IntervalBlockImpl(readingTypeMRID));
        }
        return intervalBlocks;

    }

    private static String getReadingTypeFrom(TimeDuration interval, MdcReadingTypeUtilService readingTypeUtilService, ChannelInfo channelInfo, ObisCode channelObisCode) {
        String readingTypeMRID = channelInfo.getReadingTypeMRID();
        if(Checks.is(readingTypeMRID).empty()){
            readingTypeMRID = readingTypeUtilService.getReadingTypeFrom(channelObisCode, channelInfo.getUnit(), interval);
        }
        return readingTypeMRID;
    }

}