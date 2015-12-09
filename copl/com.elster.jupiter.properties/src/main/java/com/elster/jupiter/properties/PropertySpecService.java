package com.elster.jupiter.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.RelativePeriod;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;

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
     * {@link PropertySpec} of BigDecimal values.
     *
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilderWizard.NlsOptions<BigDecimal> bigDecimalSpec();

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of BigDecimal values that are bounded
     * by the specified upper and lower limit values.
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
     * {@link PropertySpec} of persistent instances of the specified api class.
     *
     * @return The PropertySpecBuilder
     */
    <T> PropertySpecBuilderWizard.NlsOptions<T> referenceSpec(Class<T> apiClass);

    /**
     * Creates a {@link PropertySpec} for a String value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    PropertySpec stringPropertySpec(String name, boolean required, String defaultValue);

    /**
     * @deprecated Use {@link #stringPropertySpecWithValuesAndDefaultValue(Thesaurus, TranslationKey, TranslationKey, boolean, String, String...)} instead
     */
    @Deprecated
    PropertySpec stringPropertySpecWithValuesAndDefaultValue(String name, boolean required, String defaultValue, String... values);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param values The allowed values for the PropertySpec
     * @return the PropertySpec
     */
    PropertySpec bigDecimalPropertySpecWithValues(String name, boolean required, BigDecimal... values);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value with a default value.
     *
     * @param thesaurus The Thesaurus that contains the translations for the name and description keys
     * @param nameTranslationKey The TranslationKey for the name of the PropertySpec
     * @param descriptionTranslationKey The TranslationKey for the description of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    PropertySpec bigDecimalPropertySpec(Thesaurus thesaurus, TranslationKey nameTranslationKey, TranslationKey descriptionTranslationKey, boolean required, BigDecimal defaultValue);

    /**
     * @deprecated Use {@link #bigDecimalPropertySpec(Thesaurus, TranslationKey, TranslationKey, boolean, BigDecimal)} instead
     */
    @Deprecated
    PropertySpec bigDecimalPropertySpec(String name, boolean required, BigDecimal defaultValue);

    /**
     * Creates a {@link PropertySpec} for positive BigDecimal values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @return The PropertySpec
     */
    PropertySpec positiveDecimalPropertySpec(String name, boolean required);

    /**
     * Creates a {@link PropertySpec} for BigDecimal values that are limited between the lowerLimit and the upperLimit (inclusive).
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param lowerLimit The lowest value allowed
     * @param upperLimit The largest value allowed
     * @return The PropertySpec
     */
    PropertySpec boundedDecimalPropertySpec(String name, boolean required, BigDecimal lowerLimit, BigDecimal upperLimit);

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

    <T extends HasIdAndName> PropertySpec stringReferencePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... values);

}