/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
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
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import java.util.stream.Stream;

import static com.elster.jupiter.estimators.impl.AbstractMainCheckEstimator.ReferenceReadingQuality.NO_CHECK_CHANNEL;
import static com.elster.jupiter.estimators.impl.AbstractMainCheckEstimator.ReferenceReadingQuality.NO_MC;
import static com.elster.jupiter.estimators.impl.AbstractMainCheckEstimator.ReferenceReadingQuality.NO_PURPOSE_ON_UP;
import static com.elster.jupiter.estimators.impl.AbstractMainCheckEstimator.ReferenceReadingQuality.REFERENCE_DATA_MISSING;
import static com.elster.jupiter.estimators.impl.AbstractMainCheckEstimator.ReferenceReadingQuality.REFERENCE_DATA_OK;
import static com.elster.jupiter.estimators.impl.AbstractMainCheckEstimator.ReferenceReadingQuality.REFERENCE_DATA_SUSPECT;

public abstract class AbstractMainCheckEstimator extends AbstractEstimator {

    protected static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDM);
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DefaultDateTimeFormatters.mediumDate()
            .withShortTime()
            .build()
            .withZone(ZoneId
                    .systemDefault())
            .withLocale(Locale.ENGLISH);


    static final String CHECK_PURPOSE = TranslationKeys.CHECK_PURPOSE.getKey();

    protected MetrologyPurpose checkPurpose;

    ReadingType checkReadingType;

    protected UsagePoint validatingUsagePoint;

    protected ValidationService validationService;
    protected MetrologyConfigurationService metrologyConfigurationService;

    AbstractMainCheckEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
    }

    AbstractMainCheckEstimator(Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
    }

    @Override
    protected void init() {
        checkPurpose = (MetrologyPurpose) getProperty(CHECK_PURPOSE);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(buildCheckPurposePropertySpec());
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.singletonList(CHECK_PURPOSE);
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    void touchCheckReadingType(ReadingType readingType) {
        if (checkReadingType == null) {
            checkReadingType = readingType;
        }
    }

    PropertySpec buildCheckPurposePropertySpec() {
        List<MetrologyPurpose> metrologyPurposes = metrologyConfigurationService.getMetrologyPurposes();
        return getPropertySpecService()
                .referenceSpec(MetrologyPurpose.class)
                .named(TranslationKeys.CHECK_PURPOSE)
                .describedAs(TranslationKeys.CHECK_PURPOSE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(metrologyPurposes)
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
    }

    protected String blockToString(EstimationBlock block) {
        return DATE_TIME_FORMATTER.format(block.estimatables()
                .get(0)
                .getTimestamp()) + " until " + DATE_TIME_FORMATTER.format(block
                .estimatables()
                .get(block.estimatables().size() - 1)
                .getTimestamp());
    }


    abstract TranslationKey getEstimatorNameKey();

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {

        // it is possible estimationBlocks list is empty
        if (estimationBlocks.isEmpty()) {
            return SimpleEstimationResult.builder().build();
        }

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
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.ESTIMATOR_FAIL_NO_UP)
                            .format(getThesaurus().getFormat(getEstimatorNameKey())
                                    .format()));
            return SimpleEstimationResult.of(estimationBlocks, Collections.emptyList());
        } else {
            this.validatingUsagePoint = usagePoint.get();
        }

        touchCheckReadingType(estimationBlocks.get(0).getReadingType());

        List<EstimationBlock> remain = new ArrayList<>();
        List<EstimationBlock> estimated = new ArrayList<>();

        for (EstimationBlock block : estimationBlocks) {
            if (estimate(block)) {
                estimated.add(block);
            } else {
                remain.add(block);
            }
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    abstract String getMessage(ReferenceReadingQuality referenceReadingQuality, EstimationBlock estimationBlock);

    private boolean estimate(EstimationBlock estimationBlock) {
        // find reference values for each estimatable in block
        Map<Estimatable, ReferenceReading> referenceReadingMap = estimateBlock(estimationBlock);
        // we can estimate block only if we have reference values for each estimatable in this block
        if (referenceReadingMap.values().stream().filter(Predicates.not(ReferenceReading::isOk)).count() != 0) {
            // there are 'not ok' reference values
            // lets capture reason and log appropriate failure message
            String message = null;
            for (ReferenceReadingQuality readingQuality : ReferenceReadingQuality.values()) {
                if (referenceReadingMap.values()
                        .stream()
                        .map(ReferenceReading::getQuality)
                        .anyMatch(e -> e.equals(readingQuality))) {
                    message = getMessage(readingQuality, estimationBlock);
                    break;
                }
            }
            if (message == null) {
                // should not happens
                message = getThesaurus().getFormat(MessageSeeds.ESTIMATOR_FAIL_INTERNAL_ERROR)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(getEstimatorNameKey())
                                .format(), validatingUsagePoint.getName(), checkPurpose.getName(), estimationBlock.getReadingType()
                                .getFullAliasName());
            }
            LoggingContext.get().warning(getLogger(), message);
            return false;
        } else {
            // set estimation values to each estimatable
            referenceReadingMap.forEach((e, r) -> e.setEstimation(r.getReferenceValue()));
            return true;
        }
    }


    // assumption: block always belongs to one effective metrology configuration
    private Map<Estimatable, ReferenceReading> estimateBlock(EstimationBlock estimationBlock) {

        Instant startInterval = estimationBlock.estimatables().get(0).getTimestamp();
        List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMCList = new ArrayList<>();
        if (estimationBlock.estimatables().size() == 1) {
            validatingUsagePoint.getEffectiveMetrologyConfiguration(startInterval).ifPresent(effectiveMCList::add);
        } else {
            Instant endInterval = estimationBlock.estimatables()
                    .get(estimationBlock.estimatables().size() - 1)
                    .getTimestamp();

            Range<Instant> blockRange = Range.closedOpen(startInterval, endInterval);
            effectiveMCList.addAll(validatingUsagePoint.getEffectiveMetrologyConfigurations(blockRange));
        }
        return handleEffectiveMC(effectiveMCList, estimationBlock);
    }

    private Map<Estimatable, ReferenceReading> handleEffectiveMC(List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMCList, EstimationBlock estimationBlock) {

        if (effectiveMCList.size() == 0) {
            return Stream.of(new ReferenceReading(NO_MC))
                    .collect(Collectors.toMap(c -> estimationBlock.estimatables().get(0), Function.identity()));
        }

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = effectiveMCList.get(0);
        Optional<MetrologyContract> metrologyContract = effectiveMC
                .getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(checkPurpose))
                .findAny();
        return handleMetrologyContract(effectiveMC, metrologyContract, estimationBlock);
    }

    private Map<Estimatable, ReferenceReading> handleMetrologyContract(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, Optional<MetrologyContract> metrologyContract, EstimationBlock estimationBlock) {
        if (!metrologyContract.isPresent()) {
            return Stream.of(new ReferenceReading(NO_PURPOSE_ON_UP))
                    .collect(Collectors.toMap(c -> estimationBlock.estimatables().get(0), Function.identity()));
        }

        Optional<ChannelsContainer> channelsContainerWithCheckChannel = effectiveMC
                .getChannelsContainer(metrologyContract.get());

        return handleChannelContainer(channelsContainerWithCheckChannel, estimationBlock);
    }

    private Map<Estimatable, ReferenceReading> handleChannelContainer(Optional<ChannelsContainer> channelsContainerWithCheckChannel, EstimationBlock estimationBlock) {
        if (!channelsContainerWithCheckChannel.isPresent()) {
            return Stream.of(new ReferenceReading(NO_CHECK_CHANNEL))
                    .collect(Collectors.toMap(c -> estimationBlock.estimatables().get(0), Function.identity()));
        }

        Optional<Channel> checkChannel = channelsContainerWithCheckChannel.get()
                .getChannel(checkReadingType);
        if (!checkChannel.isPresent()) {
            return Stream.of(new ReferenceReading(NO_CHECK_CHANNEL))
                    .collect(Collectors.toMap(c -> estimationBlock.estimatables().get(0), Function.identity()));
        }
        Instant startInterval = estimationBlock.estimatables().get(0).getTimestamp();
        Instant searchIntervalStart = ZonedDateTime.ofInstant(startInterval, checkChannel.get().getZoneId())
                .minus(checkChannel.get().getIntervalLength().get())
                .toInstant();
        Instant searchIntervalEnd = estimationBlock.estimatables()
                .get(estimationBlock.estimatables().size() - 1)
                .getTimestamp();
        Map<Instant, IntervalReadingRecord> checkChannelBaseReadings = checkChannel.get()
                .getIntervalReadings(Range.openClosed(searchIntervalStart, searchIntervalEnd))
                .stream()
                .collect(Collectors.toMap(BaseReading::getTimeStamp, Function.identity()));

        ValidationEvaluator evaluator = validationService.getEvaluator();
        Map<Instant, DataValidationStatus> validationStatusMap = evaluator.getValidationStatus(QUALITY_CODE_SYSTEMS, checkChannel
                .get(), checkChannelBaseReadings.values().stream().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(DataValidationStatus::getReadingTimestamp, Function.identity()));

        return estimationBlock.estimatables().stream().collect(Collectors.toMap(Function.identity(), e ->
                handleCheckReading(checkChannelBaseReadings.get(e.getTimestamp()), validationStatusMap.get(e.getTimestamp()))
        ));
    }

    private ReferenceReading handleCheckReading(IntervalReadingRecord checkReading, DataValidationStatus dataValidationStatus) {

        if (checkReading == null) {
            return new ReferenceReading(REFERENCE_DATA_MISSING);
        }

        if (dataValidationStatus != null && !dataValidationStatus.getValidationResult()
                .equals(ValidationResult.SUSPECT)) {
            return new ReferenceReading(REFERENCE_DATA_OK).withReferenceReading(checkReading);
        }

        return new ReferenceReading(REFERENCE_DATA_SUSPECT);
    }

    protected class InitCancelException extends Exception {
    }

    public enum TranslationKeys implements TranslationKey {

        CHECK_PURPOSE("validator.purpose", "Check purpose"),
        CHECK_PURPOSE_DESCRIPTION("validator.purpose.description", "Check purpose");

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

    enum ReferenceReadingQuality {
        NO_MC, NO_PURPOSE_ON_UP, NO_CHECK_CHANNEL, REFERENCE_DATA_MISSING, REFERENCE_DATA_SUSPECT, REFERENCE_DATA_OK
    }
}
