/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.streams.Accumulator;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.either;
import static com.elster.jupiter.util.streams.Predicates.not;

class ChannelValidator {
    private final Channel channel;
    private final Range<Instant> range;
    private final ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities;
    private IValidationRule rule;
    private final List<? extends ReadingRecord> registerReadings;
    private final List<? extends IntervalReadingRecord> intervalReadings;


    ChannelValidator(Channel channel, Range<Instant> range) {
        this.channel = channel;
        this.range = range;
        this.existingReadingQualities = getExistingReadingQualities();
        if (channel.isRegular()) {
            intervalReadings = channel.getIntervalReadings(range);
            registerReadings = null;
        } else {
            intervalReadings = null;
            registerReadings = channel.getRegisterReadings(range);
        }
    }

    Instant validateRule(IValidationRule rule, Logger logger) {
        this.rule = rule;
        return channel.getReadingTypes().stream()
                .filter(r -> rule.getReadingTypes().contains(r))
                .map(readingType -> validateReadings(readingType, logger))
                .max(Comparator.naturalOrder())
                .orElse(range.upperEndpoint());
    }

    private Instant validateReadings(ReadingType readingType, Logger logger) {
        Accumulator<Instant, ValidatedResult> lastChecked = new Accumulator<>(range.lowerEndpoint(), (d, v) -> determineLastChecked(v, d));

        Consumer<ValidatedResult> validatedResultHandler = validationTarget -> {
            handleValidationResult(validationTarget);
            lastChecked.accept(validationTarget);
        };

        Validator validator = rule.createNewValidator(channel.getChannelsContainer(), readingType);
        validator.init(channel, readingType, range, logger);

        validatedResults(validator, readingType)
                .forEach(validatedResultHandler);

        validator.finish().entrySet().stream()
                .map(entry -> new MissingTarget(entry.getKey(), entry.getValue(), readingType))
                .forEach(validatedResultHandler);

        Map<ReadingQualityType, List<TransientReadingQualityRecord>> collect = existingReadingQualities.values()
                .stream()
                .filter(qr -> qr instanceof TransientReadingQualityRecord)
                .map(TransientReadingQualityRecord.class::cast)
                .filter(not(TransientReadingQualityRecord::wasSaved))
                .collect(Collectors.groupingBy(TransientReadingQualityRecord::getReadingQualityType));

        collect.entrySet().forEach(entry -> {
            List<BaseReadingRecord> records = entry.getValue().stream().map(qr -> qr.validatedResult.getReadingRecord()).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            List<Instant> timestamps = entry.getValue()
                    .stream()
                    .filter(not(qr -> qr.validatedResult.getReadingRecord().isPresent()))
                    .map(qr -> qr.validatedResult.getTimestamp())
                    .collect(Collectors.toList());

            channel.createReadingQualityForRecords(entry.getKey(), readingType, records);
            channel.createReadingQualityForTimestamps(entry.getKey(), readingType, timestamps);
        });

        existingReadingQualities.values().stream().filter(qr -> qr instanceof TransientReadingQualityRecord)
                .map(TransientReadingQualityRecord.class::cast).forEach(TransientReadingQualityRecord::update);

        return lastChecked.getAccumulated();
    }

    private Stream<ValidatedResult> validatedResults(Validator validator, ReadingType channelReadingType) {
        return channel.isRegular() ?
                intervalReadings.stream()
                        .map(intervalReading -> new ReadingTarget(intervalReading, validator.validate(intervalReading), channelReadingType)) :
                registerReadings.stream()
                        .map(readingRecord -> new ReadingTarget(readingRecord, validator.validate(readingRecord), channelReadingType));
    }

    private ListMultimap<Instant, ReadingQualityRecord> getExistingReadingQualities() {
        List<ReadingQualityRecord> readingQualities = channel.findReadingQualities().inTimeInterval(range).collect();
        return ArrayListMultimap.create(Multimaps.index(readingQualities, ReadingQualityRecord::getReadingTimestamp));
    }

    private void handleValidationResult(ValidatedResult target) {
        if (target.ruleFailed()) {
            handleRuleFailed(target);
        } else {
            handleRulePassed(target);
        }
    }

    private void handleRulePassed(ValidatedResult target) {
        Optional<ReadingQualityRecord> existingQualityForType = getExistingReadingQualityForType(target.getTimestamp(), target.getReadingType());
        if (existingQualityForType.isPresent() && existingQualityForType.get().isActual()) {
            existingQualityForType.get().makePast();
        }
    }

    private void handleRuleFailed(ValidatedResult target) {
        if (!isConfirmed(target.getTimestamp())) {
            setValidationQuality(target);
            if (ValidationAction.FAIL.equals(rule.getAction())) {
                setSuspectQuality(target, rule.getRuleSet().getQualityCodeSystem());
            }
        }
    }

    private void setSuspectQuality(ValidatedResult target, QualityCodeSystem qualityCodeSystem) {
        Optional<ReadingQualityRecord> suspectQuality = existingReadingQualities.get(target.getTimestamp()).stream()
                .filter(ReadingQualityRecord::isSuspect)
                .filter(readingQualityRecord -> readingQualityRecord.getReadingType().equals(target.getReadingType()))
                .filter(readingQualityRecord -> readingQualityRecord.getType().getSystemCode() == qualityCodeSystem.ordinal())
                .findFirst();
        ReadingQualityRecord suspectQualityRecord = suspectQuality.orElseGet(() ->
                saveNewReadingQuality(channel, target, ReadingQualityType.of(qualityCodeSystem, QualityCodeIndex.SUSPECT)));
        if (!suspectQualityRecord.isActual()) {
            suspectQualityRecord.makeActual();
        }
    }

    private TransientReadingQualityRecord setValidationQuality(ValidatedResult target) {
        Optional<ReadingQualityRecord> existingQualityForType = getExistingReadingQualityForType(target.getTimestamp(), target.getReadingType());
        if (existingQualityForType.isPresent()) {
            if (!existingQualityForType.get().isActual()) {
                existingQualityForType.get().makeActual();
            }
        } else {
            TransientReadingQualityRecord transientReadingQualityRecord = saveNewReadingQuality(channel, target, rule.getReadingQualityType());
            target.getReadingRecord().ifPresent(r -> r.setProcessingFlags(ProcessStatus.Flag.SUSPECT));
            return transientReadingQualityRecord;
        }
        return null;
    }

    private Optional<ReadingQualityRecord> getExistingReadingQualityForType(Instant timeStamp, ReadingType readingType) {
        return existingReadingQualities.get(timeStamp).stream()
                .filter(readingQualityRecord -> readingQualityRecord.getReadingType().equals(readingType))
                .filter(input -> rule.getReadingQualityType().equals(input.getType()))
                .findFirst();
    }

    private boolean isConfirmed(Instant timeStamp) {
        return existingReadingQualities.get(timeStamp).stream()
                .filter(ReadingQualityRecord::isActual)
                .anyMatch(ReadingQualityRecord::isConfirmed);
    }

    private Instant determineLastChecked(ValidatedResult target, Instant lastChecked) {
        if (!ValidationResult.NOT_VALIDATED.equals(target.getResult())) {
            return Ordering.natural().max(lastChecked, target.getTimestamp());
        }
        return lastChecked;
    }

    private TransientReadingQualityRecord saveNewReadingQuality(Channel channel, ValidatedResult target, ReadingQualityType readingQualityType) {
        TransientReadingQualityRecord readingQuality = new TransientReadingQualityRecord(readingQualityType, target);
        existingReadingQualities.put(readingQuality.getReadingTimestamp(), readingQuality);
        return readingQuality;
    }

    private interface ValidatedResult {

        Optional<BaseReadingRecord> getReadingRecord();

        ReadingType getReadingType();

        Instant getTimestamp();

        ValidationResult getResult();

        default boolean ruleFailed() {
            return ValidationResult.SUSPECT.equals(getResult());
        }
    }

    private static class ReadingTarget implements ValidatedResult {
        private final BaseReadingRecord readingRecord;
        private final ValidationResult validationResult;
        private final ReadingType readingType;

        private ReadingTarget(BaseReadingRecord readingRecord, ValidationResult validationResult, ReadingType readingType) {
            this.readingRecord = readingRecord;
            this.validationResult = validationResult;
            this.readingType = readingType;
        }

        @Override
        public Optional<BaseReadingRecord> getReadingRecord() {
            return Optional.of(readingRecord);
        }

        @Override
        public Instant getTimestamp() {
            return readingRecord.getTimeStamp();
        }

        @Override
        public ValidationResult getResult() {
            return validationResult;
        }

        @Override
        public ReadingType getReadingType() {
            return readingType;
        }
    }

    private static class MissingTarget implements ValidatedResult {
        private final Instant timestamp;
        private final ValidationResult validationResult;
        private final ReadingType readingType;

        private MissingTarget(Instant timestamp, ValidationResult validationResult, ReadingType readingType) {
            this.timestamp = timestamp;
            this.validationResult = validationResult;
            this.readingType = readingType;
        }

        @Override
        public ReadingType getReadingType() {
            return readingType;
        }

        @Override
        public Optional<BaseReadingRecord> getReadingRecord() {
            return Optional.empty();
        }

        @Override
        public Instant getTimestamp() {
            return timestamp;
        }

        @Override
        public ValidationResult getResult() {
            return validationResult;
        }
    }

    private static class TransientReadingQualityRecord implements ReadingQualityRecord {

        private final ReadingQualityType readingQualityType;
        private final ValidatedResult validatedResult;
        private boolean actual = true;
        private boolean saved = false;

        public TransientReadingQualityRecord(ReadingQualityType readingQualityType, ValidatedResult validatedResult) {
            this.readingQualityType = readingQualityType;
            this.validatedResult = validatedResult;
        }

        public ReadingQualityType getReadingQualityType() {
            return readingQualityType;
        }

        @Override
        public Instant getTimestamp() {
            return Instant.now();
        }

        @Override
        public Channel getChannel() {
            return null;
        }

        @Override
        public CimChannel getCimChannel() {
            return null;
        }

        @Override
        public ReadingType getReadingType() {
            return validatedResult.getReadingType();
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public void setComment(String comment) {

        }

        @Override
        public Optional<BaseReadingRecord> getBaseReadingRecord() {
            return validatedResult.getReadingRecord();
        }

        @Override
        public void update() {
            saved = true;
        }

        @Override
        public Instant getReadingTimestamp() {
            return validatedResult.getReadingRecord().map(BaseReading::getTimeStamp).orElse(validatedResult.getTimestamp());
        }

        @Override
        public void delete() {

        }

        @Override
        public long getVersion() {
            return 0;
        }

        @Override
        public boolean isActual() {
            return actual;
        }

        @Override
        public void makePast() {
            actual = false;
        }

        @Override
        public void makeActual() {
            actual = true;
        }

        @Override
        public String getComment() {
            return null;
        }

        @Override
        public String getTypeCode() {
            return readingQualityType.getCode();
        }

        public boolean wasSaved() {
            return saved;
        }
    }
}
