package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.issues.IssueCollector;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Warning;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.use;

class OverflowCheck {

    private final IssueCollector issueCollector;
    private DeviceImpl device;

    @Inject
    OverflowCheck(IssueService issueService) {
        issueCollector = issueService.newIssueCollector();
    }

    static OverflowCheck from(DataModel dataModel, DeviceImpl device) {
        return dataModel.getInstance(OverflowCheck.class).init(device);
    }

    private OverflowCheck init(DeviceImpl device) {
        this.device = device;
        return this;
    }

    private static <T> Optional<T> optionally(boolean condition, Supplier<T> valueIfTrue) {
        return Optional.of(condition)
                .filter(b -> b)
                .map(b -> valueIfTrue.get());
    }

    public MeterReading toCheckedMeterReading(MeterReading meterReading) {
        // make a map of readings to their overflow values
        Map<BaseReading, BigDecimal> overflowCheckedMap = Stream.of(
                meterReading.getReadings()
                        .stream()
                        .map(this::readingPairedWithOverflow),
                meterReading.getIntervalBlocks()
                        .stream()
                        .flatMap(intervalBlock -> intervalBlock.getIntervals()
                                .stream()
                                .map(use(this::pairedWithOverflow).with(intervalBlock.getReadingTypeCode()))
                        )
        )
                .flatMap(Function.identity())
                .flatMap(Functions.asStream())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        if (overflowCheckedMap.isEmpty()) {
            return meterReading;
        }
        MeterReadingImpl copy = MeterReadingImpl.newInstance();
        copy.addAllEndDeviceEvents(meterReading.getEvents());
        meterReading.getReadings()
                .stream()
                .map(use(this::toCheckedReading).with(overflowCheckedMap))
                .forEach(copy::addReading);
        meterReading.getIntervalBlocks()
                .stream()
                .map(use(this::toCheckedIntervalBlock).with(overflowCheckedMap))
                .forEach(copy::addIntervalBlock);
        return copy;
    }

    public List<Warning> getWarnings() {
        return issueCollector.getWarnings()
                .stream()
                .collect(Collectors.toList());
    }

    private Optional<Pair<BaseReading, BigDecimal>> readingPairedWithOverflow(Reading reading) {
        return pairedWithOverflow(reading, reading.getReadingTypeCode());
    }

    private Optional<Pair<BaseReading, BigDecimal>> pairedWithOverflow(BaseReading reading, String readingTypeCode) {
        return getOverflowValue(readingTypeCode)
                .flatMap(use(this::getOverflowCheckedValue).on(reading));
    }

    private Reading toCheckedReading(Reading reading, Map<BaseReading, BigDecimal> overflowCheckedMap) {
        BigDecimal checked = overflowCheckedMap.get(reading);
        if (checked != null) {
            OverflowReading overflowReading = new OverflowReading(reading, checked);
            issueCollector.addWarning(device, MessageSeeds.Keys.READING_OVERFLOW_DETECTED, device.getmRID(), reading.getReadingTypeCode(), DateTimeFormatter.ISO_INSTANT.format(reading.getTimeStamp()), reading.getValue(), overflowReading.getValue());
            return overflowReading;
        }
        return reading;
    }

    private IntervalBlock toCheckedIntervalBlock(IntervalBlock intervalBlock, Map<BaseReading, BigDecimal> overflowCheckedMap) {
        if (intervalBlock.getIntervals().stream().noneMatch(overflowCheckedMap::containsKey)) {
            return intervalBlock;
        }
        IntervalBlockImpl copy = IntervalBlockImpl.of(intervalBlock.getReadingTypeCode());
        intervalBlock.getIntervals()
                .stream()
                .map(intervalReading -> this.toCheckedIntervalReading(intervalBlock, intervalReading, overflowCheckedMap))
                .forEach(copy::addIntervalReading);
        return copy;
    }

    private Optional<BigDecimal> getOverflowValue(String readingTypeCode) {
        return device.getChannels()
                .stream()
                .filter(channel -> channel.getReadingType().getMRID().equals(readingTypeCode))
                .findAny()
                .map(Channel::getOverflow);
    }

    private IntervalReading toCheckedIntervalReading(IntervalBlock intervalBlock, IntervalReading reading, Map<BaseReading, BigDecimal> overflowCheckedMap) {
        BigDecimal checked = overflowCheckedMap.get(reading);
        if (checked != null) {
            OverflowIntervalReading overflowIntervalReading = new OverflowIntervalReading(reading, checked);
            issueCollector.addWarning(device, MessageSeeds.Keys.READING_OVERFLOW_DETECTED, device.getmRID(), intervalBlock.getReadingTypeCode(), DateTimeFormatter.ISO_INSTANT.format(reading.getTimeStamp()), reading.getValue(), overflowIntervalReading.getValue());
            return overflowIntervalReading;
        }
        return reading;
    }

    private Optional<Pair<BaseReading, BigDecimal>> getOverflowCheckedValue(BaseReading reading, BigDecimal overFlow) {
        return optionally(overflows(reading, overFlow), () -> Pair.of(reading, reading.getValue().subtract(overFlow)));
    }

    private boolean overflows(BaseReading reading, BigDecimal overFlow) {
        BigDecimal value = reading.getValue();
        return value != null && value.compareTo(overFlow) > 0;
    }
}
