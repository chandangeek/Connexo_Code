package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
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

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.copy;
import static com.elster.jupiter.util.streams.Predicates.notNull;

class ChannelRuleValidator {

    private final ValidationRuleImpl rule;

    ChannelRuleValidator(ValidationRuleImpl rule) {
        this.rule = rule;
    }

    Instant validateReadings(Channel channel, Range<Instant> range) {
        ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities = getExistingReadingQualities(channel, range);
        return channel.getReadingTypes().stream()
                .filter(r -> rule.getReadingTypes().contains(r))
                .map(r -> validateReadings(channel, range, r, existingReadingQualities))
                .filter(notNull())
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private Instant validateReadings(Channel channel, Range<Instant> interval, ReadingType channelReadingType, ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities) {
        Accumulator<Instant, ValidatedResult> lastChecked = new Accumulator<>((d, v) -> determineLastChecked(v, d));

        Consumer<ValidatedResult> validatedResultHandler = validationTarget -> {
            handleValidationResult(channel, existingReadingQualities, validationTarget);
            lastChecked.accept(validationTarget);
        };

        Validator validator = rule.createNewValidator();
        validator.init(channel, channelReadingType, interval);

        validatedResults(validator, channel, channelReadingType, interval)
                .forEach(validatedResultHandler);

        validator.finish().entrySet().stream()
                .map(entry -> new MissingTarget(entry.getKey(), entry.getValue()))
                .forEach(validatedResultHandler);

        return lastChecked.getAccumulated();
    }

    private Stream<ValidatedResult> validatedResults(Validator validator, Channel channel, ReadingType channelReadingType, Range<Instant> interval) {
        if (channel.isRegular()) {
            Range<Instant> toRequest = copy(interval).withClosedLowerBound(interval.lowerEndpoint().minusMillis(1));
            return channel.getIntervalReadings(channelReadingType, toRequest).stream()
                    .map(intervalReading -> new ReadingTarget(intervalReading, validator.validate(intervalReading)));
        }
        return channel.getRegisterReadings(channelReadingType, interval).stream()
                .map(readingRecord -> new ReadingTarget(readingRecord, validator.validate(readingRecord)));
    }

    private ListMultimap<Instant, ReadingQualityRecord> getExistingReadingQualities(Channel channel, Range<Instant> interval) {
        List<ReadingQualityRecord> readingQualities = channel.findReadingQuality(interval);
        return ArrayListMultimap.create(Multimaps.index(readingQualities, ReadingQualityRecord::getReadingTimestamp));
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

    private void handleValidationResult(Channel channel, ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities, ValidatedResult target) {
        if (target.ruleFailed()) {
            handleRuleFailed(channel, existingReadingQualities, target);
            return;
        }
        handleRulePassed(existingReadingQualities, target);
    }

    private void handleRulePassed(ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities, ValidatedResult target) {
        Optional<ReadingQualityRecord> existingQualityForType = getExistingReadingQualityForType(existingReadingQualities, target.getTimestamp());
        if (existingQualityForType.isPresent() && existingQualityForType.get().isActual()) {
            existingQualityForType.get().makePast();
        }
    }

    private void handleRuleFailed(Channel channel, ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities, ValidatedResult target) {
        setValidationQuality(channel, existingReadingQualities, target);
        if (ValidationAction.FAIL.equals(rule.getAction())) {
            setSuspectQuality(channel, existingReadingQualities, target);
        }
    }

    private void setSuspectQuality(Channel channel, ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities, ValidatedResult target) {
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

    private void setValidationQuality(Channel channel, ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities, ValidatedResult target) {
        Optional<ReadingQualityRecord> existingQualityForType = getExistingReadingQualityForType(existingReadingQualities, target.getTimestamp());
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


    private Optional<ReadingQualityRecord> getExistingReadingQualityForType(ListMultimap<Instant, ReadingQualityRecord> existingReadingQualities, Instant timeStamp) {
        return existingReadingQualities.get(timeStamp).stream()
                .filter(input -> rule.getReadingQualityType().equals(input.getType()))
                .findFirst();
    }

    private Instant determineLastChecked(ValidatedResult target, Instant lastChecked) {
        if (!ValidationResult.NOT_VALIDATED.equals(target.getResult())) {
            return lastChecked == null ? target.getTimestamp() : Ordering.natural().max(lastChecked, target.getTimestamp());
        }
        return lastChecked;
    }

    private ReadingQualityRecord saveNewReadingQuality(Channel channel, ValidatedResult target, ReadingQualityType readingQualityType) {
        ReadingQualityRecord readingQuality = target.getReadingRecord().map(r -> channel.createReadingQuality(readingQualityType, r))
                .orElseGet(() -> channel.createReadingQuality(readingQualityType, target.getTimestamp()));
        readingQuality.save();
        return readingQuality;
    }

}
