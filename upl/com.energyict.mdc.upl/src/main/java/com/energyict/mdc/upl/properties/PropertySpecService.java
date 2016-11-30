package com.energyict.mdc.upl.properties;

import java.math.BigDecimal;
import java.time.Duration;
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
     * {@link PropertySpec} of TimeZone values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<TimeZone> timezoneSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of Duration values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<Duration> durationSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of persistent instances of the specified api class.
     *
     * @return The PropertySpecBuilder
     */
    <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass);

}