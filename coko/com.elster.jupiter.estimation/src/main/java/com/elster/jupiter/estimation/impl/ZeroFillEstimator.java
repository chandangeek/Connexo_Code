package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Created by igh on 3/03/2015.
 */
public class ZeroFillEstimator extends AbstractEstimator {

    ZeroFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ZeroFillEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {

    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        return null;
    }

    @Override
    public List<String> getRequiredProperties() {
        return null;
    }

    @Override
    public void finish() {

    }

    @Override
    public EstimationResult estimate(EstimationBlock estimationBlock) {
        return null;
    }

    @Override
    public String getDefaultFormat() {
        return "Zero fill";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        //builder.add(getPropertySpecService().basicPropertySpec(FAIL_EQUAL_DATA, true, new BooleanFactory()));
        return builder.build();
    }
}
