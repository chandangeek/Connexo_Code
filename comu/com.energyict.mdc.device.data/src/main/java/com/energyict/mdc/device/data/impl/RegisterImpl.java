/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterDataUpdater;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class RegisterImpl<R extends Reading, RS extends RegisterSpec> implements Register<R, RS> {

    /**
     * The {@link RegisterSpec} for which this Register is serving.
     */
    private final RS registerSpec;
    /**
     * The Device which <i>owns</i> this Register.
     */
    protected final DeviceImpl device;


    private final List<Aggregate> aggregatesWithEventDate = Arrays.asList(Aggregate.MAXIMUM, Aggregate.FIFTHMAXIMIMUM,
            Aggregate.FOURTHMAXIMUM, Aggregate.MINIMUM, Aggregate.SECONDMAXIMUM, Aggregate.SECONDMINIMUM, Aggregate.THIRDMAXIMUM);

    public RegisterImpl(DeviceImpl device, RS registerSpec) {
        this.registerSpec = registerSpec;
        this.device = device;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public RS getRegisterSpec() {
        return registerSpec;
    }

    @Override
    public Optional<ReadingType> getCalculatedReadingType(Instant timeStamp) {
        return Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getMultiplier(Instant timeStamp) {
        return Optional.empty();
    }

    @Override
    public Optional<R> getReading(Instant timestamp) {
        List<R> atMostOne = this.getReadings(Range.singleton(timestamp));
        if (atMostOne.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(atMostOne.get(0));
        }
    }

    @Override
    public List<R> getReadings(Interval interval) {
        return this.getReadings(interval.toOpenClosedRange());
    }

    public List<R> getHistoryReadings(Interval interval, boolean changedDataOnly) {
        return this.getHistoryReadings(interval.toOpenClosedRange(), changedDataOnly);
    }

    private List<R> getReadings(Range<Instant> interval) {
        List<ReadingRecord> koreReadings = this.device.getReadingsFor(this, interval);
        return this.toReadings(koreReadings);
    }

    private List<R> getHistoryReadings(Range<Instant> interval, boolean changedDataOnly) {
        List<ReadingRecord> koreReadings = this.device.getHistoryReadingsFor(this, interval);
        List<Optional<DataValidationStatus>> validationStatuses = this.getHistoryValidationStatuses(this, interval, koreReadings);
        List<R> readings = this.toReadings(koreReadings, validationStatuses);
        return changedDataOnly ? this.filterChangedData(readings) : readings;
    }

    private List<Optional<DataValidationStatus>> getValidationStatuses(List<ReadingRecord> readings) {
        return readings
                .stream()
                .map(this::getValidationStatus)
                .collect(Collectors.toList());
    }

    private Optional<DataValidationStatus> getValidationStatus(ReadingRecord reading) {
        List<DataValidationStatus> validationStatuses = this.getValidationStatus(Collections.singletonList(reading), Range.closed(reading.getTimeStamp(), reading.getTimeStamp()));
        if (validationStatuses.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(validationStatuses.get(0));
        }
    }

    private List<DataValidationStatus> getValidationStatus(List<? extends BaseReading> readings, Range<Instant> interval) {
        return this.device.forValidation().getValidationStatus(this, readings, interval);
    }

    private List<R> toReadings(List<ReadingRecord> koreReadings) {
    private List<Optional<DataValidationStatus>> getHistoryValidationStatuses(Register<?, ?> register, Range<Instant> interval, List<ReadingRecord> readings) {
        Map<ReadingRecord, List<ReadingQualityRecord>> historyReadingQualities = this.getHistoryReadingQualities(interval, readings, register);
        return readings
                .stream()
                .map(reading -> getHistoryValidationStatus(reading, historyReadingQualities.get(reading)))
                .collect(Collectors.toList());
    }

    private Map<ReadingRecord, List<ReadingQualityRecord>> getHistoryReadingQualities(Range<Instant> interval, List<ReadingRecord> readings, Register<?, ?> register) {
        Map<ReadingRecord, List<ReadingQualityRecord>> mapReadingQualityRecord = new HashMap<>();
        readings.stream().forEach(readingRecord -> mapReadingQualityRecord.put(readingRecord, new ArrayList<>()));

        List<? extends ReadingQualityRecord> readingQualities = this.device.getMeter().get().getReadingQualities(interval);
        List<JournalEntry<? extends ReadingQualityRecord>> readingQualitiesJournal = this.device.getMeter().get().getReadingQualitiesJournal(interval,
                Collections.singletonList(register.getRegisterSpec().getRegisterType().getReadingType()),
                readings.stream().map(r -> r.getChannel().getId()).distinct().collect(Collectors.toList()));
        List<ReadingQualityRecord> allReadingQuality = readingQualities.stream()
                .filter(r -> r.getReadingType() == register.getRegisterSpec().getRegisterType().getReadingType())
                .collect(Collectors.toList());
        allReadingQuality.addAll(readingQualitiesJournal.stream().map(j -> j.get()).collect(Collectors.toList()));

        allReadingQuality.stream().forEach(rqj -> {
            Optional<ReadingRecord> journalReadingOptional = Optional.empty();
            journalReadingOptional = ((rqj.getTypeCode().compareTo("2.5.258") == 0) || ((rqj.getTypeCode().compareTo("2.5.259") == 0))) ?
                    readings.stream().sorted((a, b) -> b.getReportedDateTime().compareTo(a.getReportedDateTime())).filter(x -> x.getReportedDateTime().compareTo(rqj.getTimestamp()) <= 0).findFirst() :
                    readings.stream().sorted((a, b) -> a.getReportedDateTime().compareTo(b.getReportedDateTime())).filter(x -> x.getReportedDateTime().compareTo(rqj.getTimestamp()) >= 0).findFirst();


            journalReadingOptional.ifPresent(journalReading -> {
                mapReadingQualityRecord.get(journalReading).add(rqj);
            });
        });
        return mapReadingQualityRecord;
    }

    private Optional<DataValidationStatus> getHistoryValidationStatus(ReadingRecord reading, List<ReadingQualityRecord> readingQualityRecords) {
        List<DataValidationStatus> validationStatuses = this.getHistoryValidationStatus(Arrays.asList(reading), readingQualityRecords, Range.closed(reading.getTimeStamp(), reading.getTimeStamp()));
        if (validationStatuses.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(validationStatuses.get(0));
        }
    }

    private List<DataValidationStatus> getHistoryValidationStatus(List<? extends BaseReading> readings, List<ReadingQualityRecord> readingQualityRecords, Range<Instant> interval) {
        return this.device.forValidation().getHistoryValidationStatus(this, readings, readingQualityRecords, interval);
    }

    private List<R> toReadings(List<ReadingRecord> koreReadings) {
        List<R> readings = new ArrayList<>(koreReadings.size());
        ReadingRecord previous = null;
        for (ReadingRecord current : koreReadings) {
            List<DataValidationStatus> validationStatus = this.getValidationStatus(Collections.singletonList(current), Range.closed(current.getTimeStamp(), current.getTimeStamp()));
            if (validationStatus.isEmpty()) {
                readings.add(this.newUnvalidatedReading(current, previous));
            } else {
                readings.add(this.newValidatedReading(current, validationStatus.get(0), previous));
            }
            previous = current;
        }
        return readings;
    }
    private List<R> toReadings(List<ReadingRecord> koreReadings, List<Optional<DataValidationStatus>> validationStatuses) {
        List<R> readings = new ArrayList<>(koreReadings.size());
        for (Pair<ReadingRecord, Optional<DataValidationStatus>> koreReadingAndStatus : DualIterable.endWithShortest(koreReadings, validationStatuses)) {
            readings.add(this.toReading(koreReadingAndStatus));
        }
        return readings;
    }

    private R toReading (Pair<ReadingRecord, Optional<DataValidationStatus>> koreReadingAndStatus) {
        if (koreReadingAndStatus.getLast().isPresent()) {
            return this.newValidatedReading(koreReadingAndStatus.getFirst(), koreReadingAndStatus.getLast().get(), null);
        }
        else {
            return this.newUnvalidatedReading(koreReadingAndStatus.getFirst(), null);
        }
    }

    private List<R> filterChangedData(List<R> readings) {
        return readings.stream()
                .filter(r1 -> readings
                        .stream()
                        .filter(r2 -> r2.getTimeStamp().equals(r1.getTimeStamp()))
                        .count() > 1)
                .collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    protected abstract R newUnvalidatedReading(ReadingRecord actualReading, ReadingRecord previousReading);

    protected abstract R newValidatedReading(ReadingRecord actualReading, DataValidationStatus validationStatus, ReadingRecord previous);

    @Override
    public Optional<R> getLastReading() {
        return this.device.getLastReadingFor(this).map(this::toReading);
    }

    private R toReading(ReadingRecord readingRecord) {
        return this.toReading(Pair.of(readingRecord, this.getValidationStatus(readingRecord)));
    }

    @Override
    public Optional<Instant> getLastReadingDate() {
        return this.getLastReading().map(Reading::getTimeStamp);
    }

    @Override
    public ObisCode getRegisterTypeObisCode() {
        return getRegisterSpec().getRegisterType().getObisCode();
    }

    @Override
    public ObisCode getRegisterSpecObisCode() {
        return getRegisterSpec().getObisCode();
    }

    @Override
    public ObisCode getDeviceObisCode() {
        Optional<ReadingTypeObisCodeUsage> readingTypeObisCodeUsageOptional = getDevice().getReadingTypeObisCodeUsage(getReadingType());
        if (readingTypeObisCodeUsageOptional.isPresent()) {
            return readingTypeObisCodeUsageOptional.get().getObisCode();
        }
        return getRegisterSpec().getDeviceObisCode();
    }

    @Override
    public long getRegisterSpecId() {
        return getRegisterSpec().getId();
    }

    @Override
    public ReadingType getReadingType() {
        return getRegisterSpec().getReadingType();
    }

    @Override
    public boolean hasData() {
        return this.device.hasData(this);
    }

    @Override
    public boolean hasEventDate() {
        return aggregatesWithEventDate.contains(getReadingType().getAggregate());
    }

    @Override
    public boolean isCumulative() {
        return getRegisterSpec().getReadingType().isCumulative();
    }

    @Override
    public boolean isBilling() {
        return getReadingType().getMacroPeriod().equals(MacroPeriod.BILLINGPERIOD);
    }

    @Override
    public RegisterDataUpdater startEditingData() {
        return new RegisterDataUpdaterImpl(this);
    }

    private class RegisterDataUpdaterImpl implements RegisterDataUpdater {
        private final RegisterImpl<R, RS> register;
        private final QualityCodeSystem handlingSystem = QualityCodeSystem.MDC;
        private final List<BaseReading> edited = new ArrayList<>();
        private final List<BaseReading> confirmed = new ArrayList<>();
        private final Map<Channel, List<BaseReadingRecord>> obsolete = new HashMap<>();
        private Optional<Instant> activationDate = Optional.empty();

        private RegisterDataUpdaterImpl(RegisterImpl<R, RS> register) {
            this.register = register;
        }

        @Override
        public RegisterDataUpdater editReading(BaseReading modified, Instant editTimeStamp) {
            updateBillingTimeStampChange(modified, editTimeStamp);
            this.activationDate.ifPresent(previousTimestamp -> this.setActivationDateIfBefore(modified.getTimeStamp()));
            this.edited.add(modified);
            return this;
        }

        private void updateBillingTimeStampChange(BaseReading modified, Instant editTimeStamp) {
            if(isBilling() && !editTimeStamp.equals(modified.getTimeStamp())){
                this.removeReading(editTimeStamp);
            }
        }

        @Override
        public RegisterDataUpdater confirmReading(BaseReading modified, Instant editTimeStamp) {
            updateBillingTimeStampChange(modified, editTimeStamp);
            this.activationDate.ifPresent(previousTimestamp -> this.setActivationDateIfBefore(modified.getTimeStamp()));
            this.confirmed.add(modified);
            return this;
        }

        private void setActivationDateIfBefore(Instant modified) {
            if (modified.isBefore(this.activationDate.get())) {
                this.activationDate = Optional.of(modified);
            }
        }

        @Override
        public RegisterDataUpdater removeReading(Instant timestamp) {
            Channel channel = this.register.device.findOrCreateKoreChannel(timestamp, this.register);
            BaseReadingRecord reading =
                    channel
                        .getReading(timestamp)
                        .orElseThrow(() -> new IllegalArgumentException("No reading for register " + this.register.getRegisterSpec().getReadingType().getAliasName() + " @ " + timestamp));
            this.obsolete.computeIfAbsent(channel, c -> new ArrayList<>()).add(reading);
            return this;
        }

        @Override
        public void complete() {
            this.activationDate.ifPresent(this.register.device::ensureActiveOn);
            this.edited.forEach(this::addOrEdit);
            this.confirmed.forEach(this::confirm);
            this.obsolete.forEach((channel, readings) -> channel.removeReadings(handlingSystem, readings));
        }

        private void addOrEdit(BaseReading reading) {
            this.register.device
                .findOrCreateKoreChannel(reading.getTimeStamp(), this.register)
                    .editReadings(handlingSystem, Collections.singletonList(reading));
        }

        private void confirm(BaseReading reading) {
            this.register.device
                    .findOrCreateKoreChannel(reading.getTimeStamp(), this.register)
                    .confirmReadings(handlingSystem, Collections.singletonList(reading));
        }
    }

}
