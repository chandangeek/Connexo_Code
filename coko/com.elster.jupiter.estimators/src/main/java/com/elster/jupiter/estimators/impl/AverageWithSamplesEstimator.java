package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igh on 25/03/2015.
 */
public class AverageWithSamplesEstimator extends AbstractEstimator {

    private final ValidationService validationService;
    private final MeteringService meteringService = null;

    static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "maxNumberOfConsecutiveSuspects";
    static final BigDecimal MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = new BigDecimal(10);

    static final String MIN_NUMBER_OF_SAMPLES = "minNumberOfSamples";
    static final BigDecimal MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE = new BigDecimal(1);
    static final String MAX_NUMBER_OF_SAMPLES = "maxNumberOfSamples";
    static final BigDecimal MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE = new BigDecimal(10);

    static final String ALLOW_NEGATIVE_VALUES = "allowNegativeValues";
    static final String RELATIVE_PERIOD = "relativePeriod";
    static final String ADVANCE_READINGS_SETTINGS = "relativePeriod";

    private BigDecimal numberOfConsecutiveSuspects;
    private BigDecimal minNumberOfSamples;
    private BigDecimal maxNumberOfSamples;
    private boolean allowNegativeValues = false;
    private RelativePeriod relativePeriod;


    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService) {
        super(thesaurus, propertySpecService);
        this.validationService = validationService;
    }

    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.validationService = validationService;
    }

    @Override
    public void init() {
        numberOfConsecutiveSuspects = (BigDecimal) properties.get(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
        if (numberOfConsecutiveSuspects == null) {
            this.numberOfConsecutiveSuspects = MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE;
        }

        maxNumberOfSamples = (BigDecimal) properties.get(MIN_NUMBER_OF_SAMPLES);
        if (maxNumberOfSamples == null) {
            this.maxNumberOfSamples = MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE;
        }

        minNumberOfSamples = (BigDecimal) properties.get(MAX_NUMBER_OF_SAMPLES);
        if (minNumberOfSamples == null) {
            this.minNumberOfSamples = MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE;
        }

        Object allowNegativeValuesPropertyValue = properties.get(ALLOW_NEGATIVE_VALUES);
        if (allowNegativeValuesPropertyValue != null) {
            this.allowNegativeValues = ((Boolean) allowNegativeValuesPropertyValue).booleanValue();
        }

        relativePeriod = (RelativePeriod) properties.get(RELATIVE_PERIOD);
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        return null;
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks) {
        estimationBlocks.forEach(this::estimate);
        return SimpleEstimationResult.of(Collections.<EstimationBlock>emptyList(), estimationBlocks);
    }

    public void estimate(EstimationBlock estimationBlock) {
        estimationBlock.estimatables().forEach(estimatable -> estimate(estimatable));
    }

    private void estimate(Estimatable estimatable) {
        estimatable.setEstimation(null);
        Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
    }

    private Range<Instant> getPeriod(Channel channel, Instant referenceTime) {
        if (relativePeriod != null) {
            Range<ZonedDateTime> range = relativePeriod.getInterval(ZonedDateTime.ofInstant(referenceTime, channel.getZoneId()));
            Instant start = range.lowerEndpoint().toInstant();
            Instant end = range.upperEndpoint().toInstant();
            return Range.open(start, end);
        } else {
            return Range.all();
        }
    }

    @Override
    public String getDefaultFormat() {
        return "Average with samples";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, false, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE));

        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MIN_NUMBER_OF_SAMPLES, false, MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE));

        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MAX_NUMBER_OF_SAMPLES, false, MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE));

        builder.add(new BasicPropertySpec(ALLOW_NEGATIVE_VALUES, false, new BooleanFactory()));

        builder.add(getPropertySpecService().relativePeriodPropertySpec(
                RELATIVE_PERIOD, false, null));



        PropertySpecBuilder propertySpecBuilder = getPropertySpecService().newPropertySpecBuilder(new AdvanceReadingsSettingsFactory(meteringService));
        propertySpecBuilder.markRequired();
        PropertySpec spec =
                propertySpecBuilder.name(ADVANCE_READINGS_SETTINGS).setDefaultValue(new NoneAdvanceReadingsSettings()).finish();
        builder.add(spec);


        return builder.build();
    }

}

