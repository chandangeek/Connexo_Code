package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.streams.Accumulator;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
 
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
    
    Instant validateRule(IValidationRule rule) {
    	this.rule = rule;
        return channel.getReadingTypes().stream()
                .filter(r -> rule.getReadingTypes().contains(r))
                .map(r -> validateReadings(r))                
                .max(Comparator.naturalOrder())
                .orElse(range.upperEndpoint());
    }
    
    private Instant validateReadings(ReadingType readingType) {
        Accumulator<Instant, ValidatedResult> lastChecked = new Accumulator<>(range.lowerEndpoint(), (d, v) -> determineLastChecked(v, d));

        Consumer<ValidatedResult> validatedResultHandler = validationTarget -> {
            handleValidationResult(validationTarget);
            lastChecked.accept(validationTarget);
        };

        Validator validator = rule.createNewValidator();
        validator.init(channel, readingType, range);

        validatedResults(validator, readingType)
                .forEach(validatedResultHandler);

        validator.finish().entrySet().stream()
                .map(entry -> new MissingTarget(entry.getKey(), entry.getValue()))
                .forEach(validatedResultHandler);

        return lastChecked.getAccumulated();
    }

    private Stream<ValidatedResult> validatedResults(Validator validator, ReadingType channelReadingType) {
        if (channel.isRegular()) {
            return intervalReadings.stream()
    			.map(intervalReading -> new ReadingTarget(intervalReading, validator.validate(intervalReading)));
        }
        return registerReadings.stream()
                .map(readingRecord -> new ReadingTarget(readingRecord, validator.validate(readingRecord)));
    }

    private final ListMultimap<Instant, ReadingQualityRecord> getExistingReadingQualities() {
        List<ReadingQualityRecord> readingQualities = channel.findReadingQuality(range);
        return ArrayListMultimap.create(Multimaps.index(readingQualities, ReadingQualityRecord::getReadingTimestamp));
    }

    private void handleValidationResult(ValidatedResult target) {
        if (target.ruleFailed()) {
            handleRuleFailed(target);
            return;
        }
        handleRulePassed(target);
    }

    private void handleRulePassed(ValidatedResult target) {
        Optional<ReadingQualityRecord> existingQualityForType = getExistingReadingQualityForType(target.getTimestamp());
        if (existingQualityForType.isPresent() && existingQualityForType.get().isActual()) {
            existingQualityForType.get().makePast();
        }
    }

    private void handleRuleFailed(ValidatedResult target) {
        setValidationQuality(target);
        if (ValidationAction.FAIL.equals(rule.getAction())) {
            setSuspectQuality(target);
        }
    }

    private void setSuspectQuality(ValidatedResult target) {
        Optional<ReadingQualityRecord> suspectQuality = existingReadingQualities.get(target.getTimestamp()).stream()
                .filter(ReadingQualityRecord::isSuspect)
                .findFirst();
        ReadingQualityRecord suspectQualityRecord = suspectQuality.orElseGet(() -> {
            ReadingQualityRecord record = saveNewReadingQuality(channel, target, ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT));
            existingReadingQualities.put(target.getTimestamp(), record);
            return record;
        });
        if (!suspectQualityRecord.isActual()) {
            suspectQualityRecord.makeActual();
        }
    }

    private void setValidationQuality(ValidatedResult target) {
        Optional<ReadingQualityRecord> existingQualityForType = getExistingReadingQualityForType(target.getTimestamp());
        if (existingQualityForType.isPresent()) {
            if (!existingQualityForType.get().isActual()) {
                existingQualityForType.get().makeActual();
            }
            return;
        }
        ReadingQualityRecord readingQualityRecord = saveNewReadingQuality(channel, target, rule.getReadingQualityType());
        existingReadingQualities.put(target.getTimestamp(), readingQualityRecord);
        target.getReadingRecord().ifPresent(r -> r.setProcessingFlags(ProcessStatus.Flag.SUSPECT));
    }


    private Optional<ReadingQualityRecord> getExistingReadingQualityForType(Instant timeStamp) {
        return existingReadingQualities.get(timeStamp).stream()
                .filter(input -> rule.getReadingQualityType().equals(input.getType()))
                .findFirst();
    }

    private Instant determineLastChecked(ValidatedResult target, Instant lastChecked) {
        if (!ValidationResult.NOT_VALIDATED.equals(target.getResult())) {
            return Ordering.natural().max(lastChecked, target.getTimestamp());
        }
        return lastChecked;
    }

    private ReadingQualityRecord saveNewReadingQuality(Channel channel, ValidatedResult target, ReadingQualityType readingQualityType) {
        ReadingQualityRecord readingQuality = target.getReadingRecord().map(r -> channel.createReadingQuality(readingQualityType, r))
                .orElseGet(() -> channel.createReadingQuality(readingQualityType, target.getTimestamp()));
        readingQuality.save();
        existingReadingQualities.put(readingQuality.getReadingTimestamp(),  readingQuality);
        return readingQuality;
    }

    private interface ValidatedResult {

        Optional<BaseReadingRecord> getReadingRecord();

        Instant getTimestamp();

        ValidationResult getResult();

        default boolean ruleFailed() {
            return ValidationResult.SUSPECT.equals(getResult());
        }
    }

    private static class ReadingTarget implements ValidatedResult {
        private final BaseReadingRecord readingRecord;
        private final ValidationResult validationResult;

        private ReadingTarget(BaseReadingRecord readingRecord, ValidationResult validationResult) {
            this.readingRecord = readingRecord;
            this.validationResult = validationResult;
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
    }

    private static class MissingTarget implements ValidatedResult {
        private final Instant timestamp;
        private final ValidationResult validationResult;

        private MissingTarget(Instant timestamp, ValidationResult validationResult) {
            this.timestamp = timestamp;
            this.validationResult = validationResult;
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
    
}
