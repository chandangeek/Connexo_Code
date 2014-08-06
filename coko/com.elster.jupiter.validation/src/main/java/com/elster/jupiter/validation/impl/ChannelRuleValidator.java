package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcesStatus;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

import java.util.Date;
import java.util.List;
import java.util.Map;

class ChannelRuleValidator {

    private final ValidationRuleImpl rule;

    ChannelRuleValidator(ValidationRuleImpl rule) {
        this.rule = rule;
    }

    Date validateReadings(Channel channel, Interval interval) {
        Date lastChecked = null;
        ListMultimap<Date, ReadingQuality> existingReadingQualities = getExistingReadingQualities(channel, interval);
        for (ReadingType channelReadingType : channel.getReadingTypes()) {
            if (rule.getReadingTypes().contains(channelReadingType)) {
                Validator validator = newValidator(channel, interval, channelReadingType);

                ReadingQualityType readingQualityType = validator.getReadingQualityTypeCode().or(defaultReadingQualityType());
                if (channel.isRegular()) {
                    Interval intervalToRequest = interval.withStart(new Date(interval.getStart().getTime() - 1));
                    for (IntervalReadingRecord intervalReading : channel.getIntervalReadings(channelReadingType, intervalToRequest)) {
                        ValidationResult result = validator.validate(intervalReading);
                        lastChecked = handleValidationResult(result, channel, lastChecked, existingReadingQualities, readingQualityType, intervalReading);
                    }
                } else {
                    for (ReadingRecord readingRecord : channel.getRegisterReadings(channelReadingType, interval)) {
                        ValidationResult result = validator.validate(readingRecord);
                        lastChecked = handleValidationResult(result, channel, lastChecked, existingReadingQualities, readingQualityType, readingRecord);
                    }
                }
                Map<Date, ValidationResult> finalValidationResults = validator.finish();
                for (Map.Entry<Date, ValidationResult> entry : finalValidationResults.entrySet()) {
                    lastChecked = handleValidationResult(entry.getValue(), channel, lastChecked, existingReadingQualities, readingQualityType, entry.getKey());
                }
            }
        }
        return lastChecked;
    }

    private ListMultimap<Date, ReadingQuality> getExistingReadingQualities(Channel channel, Interval interval) {
        List<ReadingQuality> readingQualities = channel.findReadingQuality(interval);
        return ArrayListMultimap.create(Multimaps.index(readingQualities, new Function<ReadingQuality, Date>() {
            @Override
            public Date apply(ReadingQuality input) {
                return input.getReadingTimestamp();
            }
        }));
    }

    private Validator newValidator(Channel channel, Interval interval, ReadingType channelReadingType) {
        Validator newValidator = rule.createNewValidator();
        newValidator.init(channel, channelReadingType, interval);
        return newValidator;
    }

    private ReadingQualityType defaultReadingQualityType() {
        return ReadingQualityType.defaultCodeForRuleId(rule.getId());
    }

    private Date handleValidationResult(ValidationResult result, Channel channel, Date lastChecked, ListMultimap<Date, ReadingQuality> existingReadingQualities,
                                        ReadingQualityType readingQualityType, BaseReadingRecord readingRecord) {
        Optional<ReadingQuality> existingQualityForType = getExistingReadingQualitiesForType(existingReadingQualities, readingQualityType, readingRecord.getTimeStamp());
        if (ValidationResult.SUSPECT.equals(result) && !existingQualityForType.isPresent()) {
            saveNewReadingQuality(channel, readingRecord, readingQualityType);
            readingRecord.setProcessingFlags(ProcesStatus.Flag.SUSPECT);
        }
        if (ValidationResult.PASS.equals(result) && existingQualityForType.isPresent()) {
            existingQualityForType.get().delete();
            existingReadingQualities.remove(readingRecord.getTimeStamp(), existingQualityForType);
        }
        return determineLastChecked(result, lastChecked, readingRecord.getTimeStamp());
    }

    private Date handleValidationResult(ValidationResult result, Channel channel, Date lastChecked, ListMultimap<Date, ReadingQuality> existingReadingQualities,
                                        ReadingQualityType readingQualityType, Date timestamp) {
        Optional<ReadingQuality> existingQualityForType = getExistingReadingQualitiesForType(existingReadingQualities, readingQualityType, timestamp);
        if (ValidationResult.SUSPECT.equals(result) && !existingQualityForType.isPresent()) {
            saveNewReadingQuality(channel, timestamp, readingQualityType);
        }
        if (ValidationResult.PASS.equals(result) && existingQualityForType.isPresent()) {
            existingQualityForType.get().delete();
            existingReadingQualities.remove(timestamp, existingQualityForType);
        }
        return determineLastChecked(result, lastChecked, timestamp);
    }

    private Optional<ReadingQuality> getExistingReadingQualitiesForType(ListMultimap<Date, ReadingQuality> existingReadingQualities, final ReadingQualityType readingQualityType, Date timeStamp) {
        List<ReadingQuality> iterable = existingReadingQualities.get(timeStamp);
        return iterable == null ? Optional.<ReadingQuality>absent() : Iterables.tryFind(iterable, new Predicate<ReadingQuality>() {
            @Override
            public boolean apply(ReadingQuality input) {
                return readingQualityType.equals(input.getType());
            }
        });
    }

    private Date determineLastChecked(ValidationResult result, Date lastChecked, Date timestamp) {
        Date newLastChecked = lastChecked;
        if (!ValidationResult.SKIPPED.equals(result)) {
            newLastChecked = lastChecked == null ? timestamp : Ordering.natural().max(lastChecked, timestamp);
        }
        return newLastChecked;
    }

    private void saveNewReadingQuality(Channel channel, BaseReadingRecord reading, ReadingQualityType readingQualityType) {
        ReadingQuality readingQuality = channel.createReadingQuality(readingQualityType, reading);
        readingQuality.save();
    }

    private void saveNewReadingQuality(Channel channel, Date timestamp, ReadingQualityType readingQualityType) {
        ReadingQuality readingQuality = channel.createReadingQuality(readingQualityType, timestamp);
        readingQuality.save();
    }


}
