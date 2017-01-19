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
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterProtocolEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates {@link Reading Readings} and {@link com.elster.jupiter.metering.readings.IntervalReading}s
 * based on a specific <i>CollectedData</i>.
 * <p>
 * Copyrights EnergyICT
 * Date: 26/11/13
 * Time: 09:49
 */
public final class MeterDataFactory {

    /**
     * Creates a {@link Reading} based on the given CollectedRegister and the ObisCode.
     *
     * @param deviceRegister The given collectedRegister
     * @return the newly created Reading
     */
    public static Reading createReadingForDeviceRegisterAndObisCode(final CollectedRegister deviceRegister, final String readingTypeMRID) {
        ReadingImpl reading = getRegisterReading(deviceRegister, readingTypeMRID);
        if (deviceRegister.getFromTime() != null && deviceRegister.getToTime() != null) {
            reading.setTimePeriod(deviceRegister.getFromTime().toInstant(), deviceRegister.getToTime().toInstant());
        }
        return reading;
    }

    private static ReadingImpl getRegisterReading(final CollectedRegister collectedRegister, final String readingTypeMRID) {
        if (!collectedRegister.isTextRegister()) {
            return ReadingImpl.of(
                    readingTypeMRID,
                    collectedRegister.getCollectedQuantity() != null ? collectedRegister.getCollectedQuantity().getAmount() : BigDecimal.ZERO,
                    (collectedRegister.getEventTime() != null ? collectedRegister.getEventTime().toInstant() : collectedRegister.getReadTime().toInstant()));
        } else {
            return ReadingImpl.of(
                    readingTypeMRID,
                    collectedRegister.getText(),
                    (collectedRegister.getEventTime() != null ? collectedRegister.getEventTime().toInstant() : collectedRegister.getReadTime().toInstant()));
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
            EndDeviceEventImpl endDeviceEvent = EndDeviceEventImpl.of(meterProtocolEvent.getEventType().getCode(), meterProtocolEvent.getTime().toInstant());
            endDeviceEvent.setLogBookId(logBookId);
            endDeviceEvent.setLogBookPosition(meterProtocolEvent.getDeviceEventId());
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
                readingQualityTypes.addAll(pair.getLast().getReadingQualityTypes().stream().map(ReadingQualityType::new).collect(Collectors.toSet()));
                readingQualityTypes.addAll(intervalData.getReadingQualityTypes().stream().map(ReadingQualityType::new).collect(Collectors.toList()));

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