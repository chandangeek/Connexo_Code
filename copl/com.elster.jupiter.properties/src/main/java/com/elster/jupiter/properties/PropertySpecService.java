/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * Provides services to build {@link PropertySpec}s.
 * The base method is {@link #specForValuesOf(ValueFactory)}
 * but convenience methods have been introduced for the commonly
 * used data types such as String, BigDecimal and Boolean.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:54)
 */
@ProviderType
public interface PropertySpecService {

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of values that are managed by the
     * specified {@link ValueFactory}.
     *
     * @param factory The ValueFactory
     * @return The PropertySpecBuilder
     */
    <T> PropertySpecBuilderWizard.NlsOptions<T> specForValuesOf(ValueFactory<T> factory);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of String values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<String> stringSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of Base64 encoded String values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<String> base64StringSpec();

    PropertySpecBuilderWizard.NlsOptions<String> textareaStringSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of Boolean values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Boolean> booleanSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of Long values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Long> longSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of BigDecimal values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<BigDecimal> bigDecimalSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of BigDecimal values that are bounded
     * by the specified upper and lower limit values.<br>
     * Note that the result of the building process
     * will actually be a {@link BoundedBigDecimalPropertySpec}.
     *
     * @param lowerLimit The lower limit
     * @param upperLimit upper limit
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<BigDecimal> boundedBigDecimalSpec(BigDecimal lowerLimit, BigDecimal upperLimit);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of positive BigDecimal values,
     * i.e. BigDecimal.ZERO <= value <= infinite.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<BigDecimal> positiveBigDecimalSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of TimeZone values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<TimeZone> timezoneSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of {@link TimeDuration} values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<TimeDuration> timeDurationSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of {@link TimeDuration} values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<TimeDuration> temporalAmountSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of {@link TimeDuration} values that will
     * use only time units smaller than a day.
     * For clarity's sake, this means hour, minute, second and millisecond.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<TimeDuration> durationSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of RelativePeriod values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<RelativePeriod> relativePeriodSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of persistent instances of the specified api class.
     *
     * @return The PropertySpecBuilder
     */
    <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass);

    /**
     * Creates a new {@link ValueFactory} for the specified api class references.
     *
     * @return The {@link ValueFactory}.
     */
    <T> ValueFactory<T> referenceValueFactory(Class<T> apiClass);
}
