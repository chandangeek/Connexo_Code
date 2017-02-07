package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterDataUpdater;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation of a Register of a {@link com.energyict.mdc.device.data.Device},
 * which is actually a wrapping around a {@link com.energyict.mdc.device.config.RegisterSpec}
 * of the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/14
 * Time: 11:19
 */
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

    private List<R> getReadings(Range<Instant> interval) {
        List<ReadingRecord> koreReadings = this.device.getReadingsFor(this, interval);
        List<Optional<DataValidationStatus>> validationStatuses = this.getValidationStatuses(koreReadings);
        return this.toReadings(koreReadings, validationStatuses);
    }

    private List<Optional<DataValidationStatus>> getValidationStatuses(List<ReadingRecord> readings) {
        return readings
                .stream()
                .map(this::getValidationStatus)
                .collect(Collectors.toList());
    }

    private Optional<DataValidationStatus> getValidationStatus(ReadingRecord reading) {
        List<DataValidationStatus> validationStatuses = this.getValidationStatus(Arrays.asList(reading), Range.closed(reading.getTimeStamp(), reading.getTimeStamp()));
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

    private List<R> toReadings(List<ReadingRecord> koreReadings, List<Optional<DataValidationStatus>> validationStatuses) {
        List<R> readings = new ArrayList<>(koreReadings.size());
        for (Pair<ReadingRecord, Optional<DataValidationStatus>> koreReadingAndStatus : DualIterable.endWithShortest(koreReadings, validationStatuses)) {
            readings.add(this.toReading(koreReadingAndStatus));
        }
        return readings;
    }

    private R toReading (Pair<ReadingRecord, Optional<DataValidationStatus>> koreReadingAndStatus) {
        if (koreReadingAndStatus.getLast().isPresent()) {
            return this.newValidatedReading(koreReadingAndStatus.getFirst(), koreReadingAndStatus.getLast().get());
        }
        else {
            return this.newUnvalidatedReading(koreReadingAndStatus.getFirst());
        }
    }

    protected abstract R newUnvalidatedReading(ReadingRecord actualReading);

    protected abstract R newValidatedReading(ReadingRecord actualReading, DataValidationStatus validationStatus);

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
        public RegisterDataUpdater editReading(BaseReading modified) {
            this.activationDate.ifPresent(previousTimestamp -> this.setActivationDateIfBefore(modified.getTimeStamp()));
            this.edited.add(modified);
            return this;
        }

        @Override
        public RegisterDataUpdater confirmReading(BaseReading modified) {
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
