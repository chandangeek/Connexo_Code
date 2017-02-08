/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MeterDataFactory {

    /**
     * Creates a {@link Reading} based on the given CollectedRegister and the ObisCode.
     *
     * @param deviceRegister The given collectedRegister
     * @return the newly created Reading
     */
    public static Reading createReadingForDeviceRegisterAndObisCode(final CollectedRegister deviceRegister) {
        ReadingImpl reading = getRegisterReading(deviceRegister);
        if (deviceRegister.getFromTime() != null && deviceRegister.getToTime() != null) {
            reading.setTimePeriod(deviceRegister.getFromTime(), deviceRegister.getToTime());
        }
        return reading;
    }

    private static ReadingImpl getRegisterReading(final CollectedRegister collectedRegister) {
        if (!collectedRegister.isTextRegister()) {
            return ReadingImpl.of(
                    collectedRegister.getReadingType().getMRID(),
                    collectedRegister.getCollectedQuantity() != null ? collectedRegister.getCollectedQuantity().getAmount() : BigDecimal.ZERO,
                    (collectedRegister.getEventTime() != null ? collectedRegister.getEventTime() : collectedRegister.getReadTime()));
        } else {
            return ReadingImpl.of(
                    collectedRegister.getReadingType().getMRID(),
                    collectedRegister.getText(),
                    (collectedRegister.getEventTime() != null ? collectedRegister.getEventTime() : collectedRegister.getReadTime()));
        }
    }

    /**
     * Creates a list of {@link EndDeviceEvent EndDeviceEvents} based on the given DeviceLogBook
     *
     * @param deviceLogBook the collected LogBook which will serve as an input for the EndDeviceEvents
     * @param logBookId     the (MDC) database id of the LogBook
     * @return the newly created EndDeviceEvent list
     */
    public static List<EndDeviceEvent> createEndDeviceEventsFor(CollectedLogBook deviceLogBook, long logBookId) {
        List<EndDeviceEvent> endDeviceEvents = new ArrayList<>();
        for (MeterProtocolEvent meterProtocolEvent : deviceLogBook.getCollectedMeterEvents()) {
            EndDeviceEventImpl endDeviceEvent = EndDeviceEventImpl.of(meterProtocolEvent.getEventType().getMRID(), meterProtocolEvent.getTime().toInstant());
            endDeviceEvent.setLogBookId(logBookId);
            endDeviceEvent.setLogBookPosition(meterProtocolEvent.getDeviceEventId());
            endDeviceEvent.setType(String.valueOf(meterProtocolEvent.getProtocolCode()));
            endDeviceEvents.add(endDeviceEvent);
        }
        return endDeviceEvents;
    }

    /**
     * Creates a list of {@link IntervalBlock IntervalBlocks} based on the given CollectedLoadProfile and interval
     *
     * @param collectedLoadProfile The collectedLoadProfile which will server as input for the IntervalBlocks
     * @return the newly created IntervalBlocks list
     */
    public static List<IntervalBlock> createIntervalBlocksFor(CollectedLoadProfile collectedLoadProfile) {
        List<IntervalBlockImpl> intervalBlock = createIntervalBlocks(collectedLoadProfile);
        for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
            for (Pair<IntervalBlockImpl, IntervalValue> pair : DualIterable.endWithShortest(intervalBlock, intervalData.getIntervalValues())) {

                Set<ReadingQualityType> readingQualityTypes = new HashSet<>();
                readingQualityTypes.addAll(pair.getLast().getReadingQualityTypes());
                readingQualityTypes.addAll(intervalData.getReadingQualityTypes());

                IntervalReadingImpl intervalReading = IntervalReadingImpl.of(
                        intervalData.getEndTime().toInstant(),
                        new BigDecimal(pair.getLast().getNumber().toString()),  // safest way to convert from Number to BigDecimal -> using the Number#toString()
                        readingQualityTypes
                );

                pair.getFirst().addIntervalReading(intervalReading);
            }
        }
        return new ArrayList<>(intervalBlock);
    }

    private static List<IntervalBlockImpl> createIntervalBlocks(CollectedLoadProfile collectedLoadProfile) {
        return collectedLoadProfile.getChannelInfo().stream().map(channelInfo -> IntervalBlockImpl.of(channelInfo.getReadingTypeMRID())).collect(Collectors.toList());

    }
}