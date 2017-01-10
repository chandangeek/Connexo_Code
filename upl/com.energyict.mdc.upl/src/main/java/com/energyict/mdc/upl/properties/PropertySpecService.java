package com.energyict.mdc.upl.properties;

import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.TimeZone;

/**
 * Provides services to build {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (08:36)
 */
public interface PropertySpecService {

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of String values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<String> stringSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of String values that need to be
     * an exact number of characters in length.
     *
     * @param length The number of characters
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<String> stringSpecOfExactLength(int length);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of String values whose length should not
     * exceed the specified number of characters.
     *
     * @param length The maximumn number of characters
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<String> stringSpecOfMaximumLength(int length);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of {@link HexString} values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of {@link HexString} values that need to be
     * an exact number of characters in length.
     *
     * @param length The number of characters
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<HexString> hexStringSpecOfExactLength(int length);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of encrypted String values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<String> encryptedStringSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of {@link Password} values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Password> passwordSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of Boolean values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Boolean> booleanSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of Integer values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Integer> integerSpec();

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
     * {@link PropertySpec} of Date values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Date> dateTimeSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of Date values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Date> dateSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of LocalTime values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<LocalTime> timeSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of TimeZone values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<TimeZone> timeZoneSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of Duration values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Duration> durationSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of TemporalAmount values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<TemporalAmount> temporalAmountSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of {@link com.energyict.obis.ObisCode} values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<ObisCode> obisCodeSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of persistent instances of the specified api class.
     * We specifically use the name of the class to avoid that we need
     * to add an interface class each time a protocol needs a reference
     * to a business object for the first time (i.e. that business object
     * has not been used by any other protocol before) as that would
     * require an API change of the universal protocol layer
     * and that should be as stable as possible.
     *
     * @return The PropertySpecBuilder
     */
    <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(String apiClassName);

}