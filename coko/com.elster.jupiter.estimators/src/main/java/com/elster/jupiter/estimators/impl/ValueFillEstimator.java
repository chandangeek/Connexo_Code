/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.logging.LoggingContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ValueFillEstimator extends AbstractEstimator {
    static final String MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS = TranslationKeys.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS.getKey();
    private static final TimeDuration MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = TimeDuration.days(1);

    static final String FILL_VALUE = TranslationKeys.FILL_VALUE.getKey();
    private static final BigDecimal DEFAULT_FILL_VALUE = BigDecimal.ZERO;

    /**
     * Contains {@link TranslationKey}s for all the
     * {@link PropertySpec}s of this estimator.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2015-12-07 (14:03)
     */
    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(ValueFillEstimator.class.getName(), "Value fill [STD]"),
        MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS("valuefill.maxPeriodOfConsecutiveSuspects", "Maximum period of consecutive suspects"),
        MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DESCRIPTION("valuefill.maxPeriodOfConsecutiveSuspects.description",
                "The maximum period of consecutive suspects that is allowed. If this period is exceeded, data is not estimated, but can be manually edited or estimated."),
        FILL_VALUE("valuefill.fillValue", "Fill value"),
        FILL_VALUE_DESCRIPTION("valuefill.fillValue.description", "The value fill rule is the most simple rule and estimates suspect readings by replacing all the readings by a user defined value.");

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

    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    private TimeDuration maxPeriodOfConsecutiveSuspects;
    private BigDecimal fillValue;

    ValueFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ValueFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        checkRequiredProperties();
    }

    @Override
    public void init() {
        maxPeriodOfConsecutiveSuspects = getProperty(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.class)
                .orElse(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);
        fillValue = getProperty(FILL_VALUE, BigDecimal.class)
                .orElse(DEFAULT_FILL_VALUE);
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {
        List<EstimationBlock> remain = new ArrayList<>();
        List<EstimationBlock> estimated = new ArrayList<>();
        for (EstimationBlock block : estimationBlocks) {
            estimate(block, remain, estimated);
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    public void estimate(EstimationBlock block, List<EstimationBlock> remain, List<EstimationBlock> estimated) {
        try (LoggingContext contexts = initLoggingContext(block)) {
            if (canEstimate(block)) {
                block.estimatables().forEach(this::estimate);
                estimated.add(block);
            } else {
                remain.add(block);
            }
        }
    }

    private void estimate(Estimatable estimatable) {
        estimatable.setEstimation(fillValue);
//        Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
    }

    private boolean canEstimate(EstimationBlock block) {
        return isBlockSizeOk(block);
    }

    private boolean isBlockSizeOk(EstimationBlock block) {
        Range<Instant> actualPeriodOfSuspects = Range.encloseAll(block.estimatables().stream()
                .map(Estimatable::getTimestamp)
                .collect(Collectors.toSet()));
        boolean blockSizeOk = actualPeriodOfSuspects.upperEndpoint().toEpochMilli() - actualPeriodOfSuspects.lowerEndpoint().toEpochMilli()
                < maxPeriodOfConsecutiveSuspects.getMilliSeconds(); // max period of consecutive suspects is assumed open-closed, thus strict inequality
        if (!blockSizeOk) {
            String message = "Failed estimation with {rule}: Block {block} since its size exceeds the maximum of {0}";
            LoggingContext.get().info(getLogger(), message, maxPeriodOfConsecutiveSuspects);
        }
        return blockSizeOk;
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService()
                .timeDurationSpec()
                .named(TranslationKeys.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS)
                .describedAs(TranslationKeys.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE)
                .finish());
        builder.add(getPropertySpecService()
                .bigDecimalSpec()
                .named(TranslationKeys.FILL_VALUE)
                .describedAs(TranslationKeys.FILL_VALUE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(DEFAULT_FILL_VALUE)
                .finish());
        return builder.build();
    }

    @Override
    public void validateProperties(Map<String, Object> estimatorProperties) {
        ImmutableMap.Builder<String, Consumer<Map.Entry<String, Object>>> builder = ImmutableMap.builder();
        builder.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, property -> {
            TimeDuration value = (TimeDuration) property.getValue();
            if (value.getMilliSeconds() < 1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_PERIOD_OF_ZERO_OR_NEGATIVE_LENGTH,
                        "properties." + MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS);
            }
        });

        ImmutableMap<String, Consumer<Map.Entry<String, Object>>> propertyValidations = builder.build();

        estimatorProperties.entrySet().forEach(property ->
                Optional.ofNullable(propertyValidations.get(property.getKey()))
                        .ifPresent(validator -> validator.accept(property)));
    }
}
