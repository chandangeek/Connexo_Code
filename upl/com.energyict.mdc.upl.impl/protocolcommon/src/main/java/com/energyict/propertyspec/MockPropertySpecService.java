/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.propertyspec;

import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.TimeZone;

public class MockPropertySpecService implements PropertySpecService {

    private final PropertySpecBuilderWizard.NlsOptions propertySpecBuilder;

    public MockPropertySpecService() {
        propertySpecBuilder = new MockPropertySpecBuilderImpl(null);
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpecOfExactLength(int length) {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> stringSpecOfMaximumLength(int length) {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpecOfExactLength(int length) {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> textareaStringSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<String> encryptedStringSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Boolean> booleanSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Integer> integerSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Long> longSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> bigDecimalSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> boundedBigDecimalSpec(BigDecimal lowerLimit, BigDecimal upperLimit) {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<BigDecimal> positiveBigDecimalSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Date> dateTimeSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Date> dateSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<LocalTime> timeSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TimeZone> timeZoneSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<Duration> durationSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<TemporalAmount> temporalAmountSpec() {
        return propertySpecBuilder;
    }

    @Override
    public PropertySpecBuilderWizard.NlsOptions<ObisCode> obisCodeSpec() {
        return propertySpecBuilder;
    }

    @Override
    public <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(String apiClassName) {
        return propertySpecBuilder;
    }
}
