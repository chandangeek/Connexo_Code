/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.estimators.impl.MainCheckEstimator.ReferenceReadingQuality.NO_CHECK_CHANNEL;
import static com.elster.jupiter.estimators.impl.MainCheckEstimator.ReferenceReadingQuality.NO_PURPOSE_ON_UP;
import static com.elster.jupiter.estimators.impl.MainCheckEstimator.ReferenceReadingQuality.REFERENCE_DATA_MISSING;
import static com.elster.jupiter.estimators.impl.MainCheckEstimator.ReferenceReadingQuality.REFERENCE_DATA_OK;
import static com.elster.jupiter.estimators.impl.MainCheckEstimator.ReferenceReadingQuality.REFERENCE_DATA_SUSPECT;

/**
 * This estimator will replace suspect values for a channel with time-corresponding readings
 * of the channel with the same reading type of the check channel on the configured purpose of the same usage point.
 */
public class MainCheckEstimator extends AbstractEstimator implements Estimator {

    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDM);

    static final String CHECK_PURPOSE = TranslationKeys.CHECK_PURPOSE.getKey();
    static final String COMPLETE_PERIOD = TranslationKeys.COMPLETE_PERIOD.getKey();

    private ValidationService validationService;
    private MetrologyConfigurationService metrologyConfigurationService;

    private String checkPurpose;
    private boolean completePeriod;

    private UsagePoint usagePoint;

    MainCheckEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
    }

    MainCheckEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(CHECK_PURPOSE, COMPLETE_PERIOD);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {

        List<String> metrologyPurposes = metrologyConfigurationService.getMetrologyPurposes()
                .stream()
                .map(MetrologyPurpose::getName)
                .collect(Collectors.toList());

        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder
                .add(getPropertySpecService()
                        .stringSpec()
                        .named(TranslationKeys.CHECK_PURPOSE)
                        .describedAs(TranslationKeys.CHECK_PURPOSE_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(metrologyPurposes.size() != 0 ? metrologyPurposes.get(0) : "")
                        .addValues(metrologyPurposes)
                        .markExhaustive(PropertySelectionMode.COMBOBOX)
                        .finish());
        builder
                .add(getPropertySpecService()
                        .booleanSpec()
                        .named(TranslationKeys.COMPLETE_PERIOD)
                        .describedAs(TranslationKeys.COMPLETE_PERIOD_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(false)
                        .finish());

        return builder.build();
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {

        Optional<UsagePoint> usagePoint = estimationBlocks.stream()
                .map(EstimationBlock::getChannel)
                .map(Channel::getChannelsContainer)
                .map(ChannelsContainer::getUsagePoint)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());

        if (!usagePoint.isPresent()) {
            // no usage point found
            LoggingContext.get()
                    .warning(getLogger(), "Failed to perform estimation using method " + TranslationKeys.ESTIMATOR_NAME.getDefaultFormat() + " since usage point had not been found.");
            return SimpleEstimationResult.of(estimationBlocks, Collections.emptyList());
        } else {
            this.usagePoint = usagePoint.get();
        }

        List<EstimationBlock> remain = new ArrayList<>();
        List<EstimationBlock> estimated = new ArrayList<>();

        for (EstimationBlock block : estimationBlocks) {
            if (estimate(block)) {
                estimated.add(block);
            } else {
                // check complete period flag
                if (completePeriod) {
                    // so, if any block is not estimated - whole period is not estimated
                    return SimpleEstimationResult.of(estimationBlocks, Collections.emptyList());
                } else {
                    remain.add(block);
                }
            }
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    private boolean estimate(EstimationBlock estimationBlock) {
        // find reference values for each estimatable in block
        Map<Estimatable, ReferenceReading> referenceReadingMap = getCheckReadings(estimationBlock);
        // we can estimate block only if we have reference values for each estimatable in this block
        if (referenceReadingMap.values().stream().filter(Predicates.not(ReferenceReading::isOk)).count() != 0) {
            // there are 'not ok' reference values
            // lets capture reason and log appropriate failure message
            String message;
            if (referenceReadingMap.values()
                    .stream()
                    .map(ReferenceReading::getQuality)
                    .anyMatch(e -> e.equals(ReferenceReadingQuality.NO_PURPOSE_ON_UP))) {
                message = FailMessages.PURPOSE_DOES_NOT_EXIST_ON_UP.getMessage(estimationBlock, usagePoint.getName(), checkPurpose);
            } else if (referenceReadingMap.values()
                    .stream()
                    .map(ReferenceReading::getQuality)
                    .anyMatch(e -> e.equals(ReferenceReadingQuality.NO_CHECK_CHANNEL))) {
                message = FailMessages.NO_OUTPUTS_ON_PURPOSE_WITH_READING_TYPE.getMessage(estimationBlock, usagePoint.getName(), checkPurpose);
            } else if (referenceReadingMap.values()
                    .stream()
                    .map(ReferenceReading::getQuality)
                    .anyMatch(e -> e.equals(ReferenceReadingQuality.REFERENCE_DATA_MISSING) || e.equals(ReferenceReadingQuality.REFERENCE_DATA_SUSPECT))) {
                message = FailMessages.DATA_SUSPECT_OR_MISSING.getMessage(estimationBlock, usagePoint.getName(), checkPurpose);
            } else {
                // should not happens
                message = FailMessages.INTERNAL_ERROR.getMessage(estimationBlock, usagePoint.getName(), checkPurpose);
            }
            LoggingContext.get().warning(getLogger(), message);
            return false;
        } else {
            // set estimation values to each estimatable
            referenceReadingMap.forEach((e, r) -> e.setEstimation(r.getReferenceValue()));
            return true;
        }
    }


    private Map<Estimatable, ReferenceReading> getCheckReadings(EstimationBlock estimationBlock) {
        // find reference value for each estimatable
        return estimationBlock.estimatables()
                .stream()
                .collect(Collectors.toMap(Function.identity(), (estimatable -> {
                    Instant readingTimeStamp = estimatable.getTimestamp();
                    Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurationOnUsagePoint = usagePoint
                            .getEffectiveMetrologyConfiguration(readingTimeStamp);
                    if (effectiveMetrologyConfigurationOnUsagePoint.isPresent()) {
                        Optional<MetrologyContract> metrologyContract = effectiveMetrologyConfigurationOnUsagePoint.get()
                                .getMetrologyConfiguration()
                                .getContracts()
                                .stream()
                                .filter(contract -> contract.getMetrologyPurpose().getName().equals(checkPurpose))
                                .findAny();
                        return handleMetrologyContract(metrologyContract.orElse(null), effectiveMetrologyConfigurationOnUsagePoint
                                .get(), estimationBlock
                                .getReadingType(), readingTimeStamp);
                    } else {
                        return new ReferenceReading(NO_PURPOSE_ON_UP);
                    }
                })));
    }

    // get reference data from metrology contract
    private ReferenceReading handleMetrologyContract(MetrologyContract metrologyContract, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint, ReadingType readingType, Instant timeStamp) {
        if (metrologyContract != null) {
            Optional<ChannelsContainer> channelsContainerWithCheckChannel = effectiveMetrologyConfigurationOnUsagePoint
                    .getChannelsContainer(metrologyContract);
            if (channelsContainerWithCheckChannel.isPresent()) {
                Optional<Channel> checkChannel = channelsContainerWithCheckChannel.get()
                        .getChannel(readingType);
                if (checkChannel.isPresent()) {
                    List<IntervalReadingRecord> checkChannelBaseReadings = checkChannel.get()
                            .getIntervalReadings(Range.closed(timeStamp.minusMillis(1), timeStamp.plusMillis(1)));
                    if (checkChannelBaseReadings.size() == 1) {
                        return handleIntervalReading(checkChannelBaseReadings.get(0), checkChannel.get());
                    } else {
                        return new ReferenceReading(REFERENCE_DATA_MISSING);
                    }
                }
            }
            return new ReferenceReading(NO_CHECK_CHANNEL);
        } else {
            return new ReferenceReading(NO_PURPOSE_ON_UP);
        }
    }

    // get reference data from interval reading of check channel
    private ReferenceReading handleIntervalReading(IntervalReadingRecord checkChannelBaseReading, Channel channel) {
        if (checkChannelBaseReading != null) {
            ValidationEvaluator evaluator = validationService.getEvaluator();
            Optional<ValidationResult> checkReadingValidationResult = evaluator.getValidationStatus(QUALITY_CODE_SYSTEMS, channel,
                    ImmutableList.of(checkChannelBaseReading))
                    .stream()
                    .map(DataValidationStatus::getValidationResult)
                    .findFirst();
            if (checkReadingValidationResult.isPresent() && !checkReadingValidationResult
                    .get()
                    .equals(ValidationResult.SUSPECT)) {
                return new ReferenceReading(REFERENCE_DATA_OK).withReferenceReading(checkChannelBaseReading);
            }
            return new ReferenceReading(REFERENCE_DATA_SUSPECT);
        } else {
            return new ReferenceReading(REFERENCE_DATA_MISSING);
        }
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    @Override
    protected void init() {
        checkPurpose = (String) getProperty(CHECK_PURPOSE);
        completePeriod = (boolean) getProperty(COMPLETE_PERIOD);
    }

    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(MainCheckEstimator.class.getName(), "Main/Check substitution"),

        CHECK_PURPOSE("maincheck.purpose", "Check purpose"),
        CHECK_PURPOSE_DESCRIPTION("maincheck.purpose.description", "Check purpose"),

        COMPLETE_PERIOD("maincheck.complete.period", "Complete period"),
        COMPLETE_PERIOD_DESCRIPTION("maincheck.complete.period.description", "Complete period");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }

    }

    private enum FailMessages {
        PURPOSE_DOES_NOT_EXIST_ON_UP {
            @Override
            String getMessage(EstimationBlock block, String usagePointName, String purpose) {
                return "Failed to estimate period \"" + blockToString(block) + "\" using method " + TranslationKeys.ESTIMATOR_NAME
                        .getDefaultFormat() + " on " + block.getReadingType()
                        .getFullAliasName() + " since the specified purpose doesn't exist on the " + usagePointName;
            }
        }, NO_OUTPUTS_ON_PURPOSE_WITH_READING_TYPE {
            @Override
            String getMessage(EstimationBlock block, String usagePointName, String purpose) {
                return "Failed to estimate period \"" + blockToString(block) + "\" using method " + TranslationKeys.ESTIMATOR_NAME
                        .getDefaultFormat() + " on " + block.getReadingType()
                        .getFullAliasName() + " since 'check' output with matching reading type on the specified purpose doesn't exist on " + usagePointName;
            }
        }, DATA_SUSPECT_OR_MISSING {
            @Override
            String getMessage(EstimationBlock block, String usagePointName, String purpose) {
                return "Failed to estimate period \"" + blockToString(block) + "\" using method " + TranslationKeys.ESTIMATOR_NAME
                        .getDefaultFormat() + " on " + usagePointName + "/" + purpose + "/" + block.getReadingType()
                        .getFullAliasName() + " since data from 'check' output is suspect or missing";
            }
        }, INTERNAL_ERROR {
            @Override
            String getMessage(EstimationBlock block, String usagePointName, String purpose) {
                return "Failed to estimate period \"" + blockToString(block) + "\" using method " + TranslationKeys.ESTIMATOR_NAME
                        .getDefaultFormat() + " on " + usagePointName + "/" + purpose + "/" + block.getReadingType()
                        .getFullAliasName() + " due to internal error";
            }
        };

        abstract String getMessage(EstimationBlock block, String usagePointName, String purpose);

        static String blockToString(EstimationBlock block) {
            return DATA_FORMAT.format(block.estimatables()
                    .get(0)
                    .getTimestamp()
                    .toEpochMilli()) + " until " + DATA_FORMAT.format(block
                    .estimatables()
                    .get(block.estimatables().size() - 1)
                    .getTimestamp().toEpochMilli());
        }

        static DateFormat DATA_FORMAT = new SimpleDateFormat("E, FF MMM yyyy hh:mm", Locale.US);

    }

    private class ReferenceReading {

        ReferenceReading(ReferenceReadingQuality quality) {
            this.quality = quality;
        }

        ReferenceReadingQuality quality;
        IntervalReadingRecord referenceReading;

        ReferenceReading withReferenceReading(IntervalReadingRecord referenceReading) {
            this.referenceReading = referenceReading;
            return this;
        }

        boolean isOk() {
            return quality.equals(REFERENCE_DATA_OK);
        }

        ReferenceReadingQuality getQuality() {
            return quality;
        }

        BigDecimal getReferenceValue() {
            return Optional.of(referenceReading).map(IntervalReadingRecord::getValue).orElse(null);
        }

    }

    protected enum ReferenceReadingQuality {
        NO_PURPOSE_ON_UP, NO_CHECK_CHANNEL, REFERENCE_DATA_MISSING, REFERENCE_DATA_SUSPECT, REFERENCE_DATA_OK
    }
}
