package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.*;

/**
* Created by tgr on 5/09/2014.
*/
class ValidationEvaluatorImpl implements ValidationEvaluator {

    private final ValidationServiceImpl validationService;

    ValidationEvaluatorImpl(ValidationServiceImpl validationService) {
        this.validationService = validationService;
    }

    private static final ReadingQuality OK_QUALITY = new ReadingQuality() {
        @Override
        public String getComment() {
            return "";
        }

        @Override
        public String getTypeCode() {
            return ReadingQualityType.MDM_VALIDATED_OK_CODE;
        }
    };


    @Override
    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
        if (qualities.isEmpty()) {
            return ValidationResult.NOT_VALIDATED;
        }
        if(qualities.size() == 1 && qualities.iterator().next().getTypeCode().equals(ReadingQualityType.MDM_VALIDATED_OK_CODE)) {
            return ValidationResult.VALID;
        }
        return ValidationResult.SUSPECT;
    }

    @Override
    public boolean isAllDataValidated(MeterActivation meterActivation) {
        for (IMeterActivationValidation meterActivationValidation : validationService.getIMeterActivationValidations(meterActivation)) {
            if (!meterActivationValidation.isAllDataValidated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings) {
        List<DataValidationStatus> result = new ArrayList<>(readings.size());
        if (!readings.isEmpty()) {
            List<ChannelValidation> channelValidations = validationService.getChannelValidations(channel);
            boolean configured = !channelValidations.isEmpty();
            boolean active = channelValidations.stream().anyMatch(ChannelValidation::hasActiveRules);
            Date lastChecked = configured ? getMinLastChecked(channelValidations.stream()
                    .filter(ChannelValidation::hasActiveRules)
                    .map(ChannelValidation::getLastChecked).collect(Collectors.toSet())) : null;

            ListMultimap<String, IValidationRule> validationRuleMap = getValidationRulesPerReadingQuality(channelValidations);

            ListMultimap<Date, ReadingQualityRecord> readingQualities = getReadingQualities(channel, getInterval(readings));
            ReadingQualityType validatedAndOk = new ReadingQualityType(ReadingQualityType.MDM_VALIDATED_OK_CODE);
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> qualities = (readingQualities.containsKey(reading.getTimeStamp()) ? readingQualities.get(reading.getTimeStamp()) : new ArrayList<ReadingQualityRecord>());
                if (qualities.isEmpty() && configured) {
                    if (wasValidated(lastChecked, reading.getTimeStamp())) {
                        qualities.add(channel.createReadingQuality(validatedAndOk, reading.getTimeStamp()));
                    }
                }
                boolean fullyValidated = false;
                if (configured && active) {
                    fullyValidated = (wasValidated(lastChecked, reading.getTimeStamp()));
                }
                result.add(createDataValidationStatusListFor(reading.getTimeStamp(), fullyValidated, qualities, validationRuleMap));

            }
        }

        return result;
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, Interval interval) {
        List<DataValidationStatus> result = new ArrayList<>();
        List<ChannelValidation> channelValidations = validationService.getChannelValidations(channel);
        boolean configured = !channelValidations.isEmpty();
        boolean active = channelValidations.stream().anyMatch(ChannelValidation::hasActiveRules);
        Date lastChecked = configured ? getMinLastChecked(channelValidations.stream()
                .filter(ChannelValidation::hasActiveRules)
                .map(ChannelValidation::getLastChecked).collect(Collectors.toSet())) : null;

        ListMultimap<String, IValidationRule> validationRuleMap = getValidationRulesPerReadingQuality(channelValidations);

        ListMultimap<Date, ReadingQualityRecord> readingQualities = getReadingQualities(channel, interval);

        for (Date readingTimestamp : readingQualities.keySet()) {
            List<ReadingQuality> qualities = new ArrayList<>(readingQualities.containsKey(readingTimestamp) ? readingQualities.get(readingTimestamp) : new ArrayList<ReadingQuality>());
            boolean wasValidated = wasValidated(lastChecked, readingTimestamp);
            if (qualities.isEmpty() && configured && wasValidated) {
                qualities.add(OK_QUALITY);
            }
            boolean fullyValidated = configured && active && wasValidated;
            result.add(createDataValidationStatusListFor(readingTimestamp, fullyValidated, qualities, validationRuleMap));

        }
        return result;
    }


    private Date getMinLastChecked(Iterable<Date> dates) {
        Comparator<Date> comparator = nullsFirst(naturalOrder());
        return dates.iterator().hasNext() ? Ordering.from(comparator).min(dates) : null;
    }

    private ListMultimap<String, IValidationRule> getValidationRulesPerReadingQuality(List<ChannelValidation> channelValidations) {
        Query<IValidationRule> ruleQuery = validationService.getAllValidationRuleQuery();
        Set<IValidationRule> rules = channelValidations.stream()
                .map(ChannelValidation::getMeterActivationValidation)
                .map(MeterActivationValidation::getRuleSet)
                .map(ValidationRuleSet::getId)
                .map(id -> ruleQuery.select(Operator.EQUAL.compare("ruleSetId", id)))
                .flatMap(l -> l.stream())
                .collect(Collectors.toSet());
        return Multimaps.index(rules, i -> i.getReadingQualityType().getCode());
    }

    private ListMultimap<Date, ReadingQualityRecord> getReadingQualities(Channel channel, Interval interval) {
        List<ReadingQualityRecord> readingQualities = channel.findReadingQuality(interval);
        return Multimaps.index(readingQualities, new Function<ReadingQualityRecord, Date>() {
            @Override
            public Date apply(ReadingQualityRecord input) {
                return input.getReadingTimestamp();
            }
        });
    }

    private Interval getInterval(List<? extends BaseReading> readings) {
        Date min = null;
        Date max = null;
        for (BaseReading reading : readings) {
            if (min == null || reading.getTimeStamp().before(min)) {
                min = reading.getTimeStamp();
            }
            if (max == null || reading.getTimeStamp().after(max)) {
                max = reading.getTimeStamp();
            }
        }
        return new Interval(min, max);
    }

    private boolean wasValidated(Date lastChecked, Date readingTimestamp) {
        return lastChecked != null && readingTimestamp.compareTo(lastChecked) <= 0;
    }

    private DataValidationStatus createDataValidationStatusListFor(Date timeStamp, boolean completelyValidated, List<? extends ReadingQuality> qualities, ListMultimap<String, IValidationRule> validationRuleMap) {
        DataValidationStatusImpl validationStatus = new DataValidationStatusImpl(timeStamp, completelyValidated);
        for (ReadingQuality quality : qualities) {
            validationStatus.addReadingQuality(quality, filterDuplicates(validationRuleMap.get(quality.getTypeCode())));
        }
        return validationStatus;
    }

    private List<IValidationRule> filterDuplicates(List<IValidationRule> iValidationRules) {
        Map<String, IValidationRule> filter = new HashMap<>();
        for (IValidationRule iValidationRule : iValidationRules) {
            if (filter.containsKey(iValidationRule.getImplementation())) {
                if (iValidationRule.getObsoleteDate() != null) {
                    filter.put(iValidationRule.getImplementation(), iValidationRule);
                }
            } else {
                filter.put(iValidationRule.getImplementation(), iValidationRule);
            }
        }
        return new ArrayList<>(filter.values());
    }


}
