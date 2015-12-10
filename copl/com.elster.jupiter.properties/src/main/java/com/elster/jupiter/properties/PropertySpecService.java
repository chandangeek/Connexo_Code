package com.elster.jupiter.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.RelativePeriod;

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
    <T> PropertySpecBuilderWizard.NlsOptions<TimeZone> timezoneSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of persistent instances of the specified api class.
     *
     * @return The PropertySpecBuilder
     */
    <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass);

    /**
     * Creates a {@link PropertySpec} for ListValue values that can have single or multiple values at the same time.
     * @param name The name of the PropertySpec
     * @param required The flag that indicates if the PropertySpec should be required or not
     * @param finder The finder values by key
     * @param values The list of possible values
     * @return The PropertySpec
     */
    <T extends HasIdAndName> PropertySpec listValuePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... values);

    PropertySpec relativePeriodPropertySpec(Thesaurus thesaurus, TranslationKey nameTranslationKey, TranslationKey descriptionTranslationKey, boolean required, RelativePeriod defaultPeriod);

    /**
     * @deprecated Use {@link #relativePeriodPropertySpec(Thesaurus, TranslationKey, TranslationKey, boolean, RelativePeriod)} instead
     */
    @Deprecated
    PropertySpec relativePeriodPropertySpec(String name, boolean required, RelativePeriod defaultPeriod);

    PropertySpec longPropertySpec(Thesaurus thesaurus, TranslationKey nameTranslationKey, TranslationKey descriptionTranslationKey, boolean required, Long defaultValue);

    /**
     * @deprecated Use {@link #longPropertySpec(Thesaurus, TranslationKey, TranslationKey, boolean, Long)} instead.
     */
    @Deprecated
    PropertySpec longPropertySpec(String name, boolean required, Long defaultValue);

    PropertySpec longPropertySpecWithValues(String name, boolean required, Long... values);

    PropertySpec positiveLongPropertySpec(String name, boolean required);

    PropertySpec boundedLongPropertySpec(String name, boolean required, Long lowerLimit, Long upperLimit);

}