/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.BoundedBigDecimalPropertySpec;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PropertySpecServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-07 (19:27)
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertySpecServiceImplTest {

    private static final String TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS = "Translation not supported in unit tests";

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private TranslationKey nameTranslationKey;
    @Mock
    private TranslationKey descriptionTranslationKey;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
    }

    @Test
    public void minimalStringSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
            this.getTestInstance()
                .stringSpec()
                .named(this.nameTranslationKey)
                .describedAs(this.descriptionTranslationKey)
                .fromThesaurus(this.thesaurus)
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec.isRequired()).isFalse();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNull();
    }

    @Test
    public void multiValuedStringSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
                this.getTestInstance()
                        .stringSpec()
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .markMultiValued()
                        .addValues("One", "Two", "Three")
                        .markExhaustive()
                        .setDefaultValue("One")
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.supportsMultiValues()).isTrue();
        ValueFactory valueFactory = propertySpec.getValueFactory();
        assertThat(valueFactory).isInstanceOf(ListValueFactory.class);
        ListValueFactory listValueFactory = (ListValueFactory) valueFactory;
        assertThat(listValueFactory.getActualFactory()).isInstanceOf(StringFactory.class);
        assertThat(propertySpec.isRequired()).isTrue();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo("One");
        assertThat(possibleValues.getAllValues()).containsOnly("One", "Two", "Three");
    }

    @Test
    public void stringSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
            this.getTestInstance()
                .stringSpec()
                .named(this.nameTranslationKey)
                .describedAs(this.descriptionTranslationKey)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues("One", "Two", "Three")
                .markExhaustive()
                .setDefaultValue("One")
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo("One");
        assertThat(possibleValues.getAllValues()).containsOnly("One", "Two", "Three");
    }

    @Test
    public void minimalStringSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .stringSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec.isRequired()).isFalse();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNull();
    }

    @Test
    public void multiValuedStringSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .stringSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .markRequired()
                .markMultiValued()
                .addValues("One", "Two", "Three")
                .markExhaustive()
                .setDefaultValue("One")
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.supportsMultiValues()).isTrue();
        ValueFactory valueFactory = propertySpec.getValueFactory();
        assertThat(valueFactory).isInstanceOf(ListValueFactory.class);
        ListValueFactory listValueFactory = (ListValueFactory) valueFactory;
        assertThat(listValueFactory.getActualFactory()).isInstanceOf(StringFactory.class);
        assertThat(propertySpec.isRequired()).isTrue();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo("One");
        assertThat(possibleValues.getAllValues()).containsOnly("One", "Two", "Three");
    }

    @Test
    public void stringSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .stringSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .markRequired()
                .addValues("One", "Two", "Three")
                .markExhaustive()
                .setDefaultValue("One")
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo("One");
        assertThat(possibleValues.getAllValues()).containsOnly("One", "Two", "Three");
    }

    @Test
    public void booleanSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
                this.getTestInstance()
                        .booleanSpec()
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .addValues(true, false)
                        .markExhaustive()
                        .setDefaultValue(true)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo(true);
        assertThat(possibleValues.getAllValues()).containsOnly(true, false);
    }

    @Test
    public void booleanSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .booleanSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .markRequired()
                .addValues(true, false)
                .markExhaustive()
                .setDefaultValue(true)
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo(true);
        assertThat(possibleValues.getAllValues()).containsOnly(true, false);
    }

    @Test
    public void longSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
                this.getTestInstance()
                        .longSpec()
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .addValues(1L, 5L, 10L)
                        .markExhaustive()
                        .setDefaultValue(100L)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo(100L);
        assertThat(possibleValues.getAllValues()).containsOnly(1L, 5L, 10L);
    }

    @Test
    public void longSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .longSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .markRequired()
                .addValues(1L, 5L, 10L)
                .markExhaustive()
                .setDefaultValue(100L)
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo(100L);
        assertThat(possibleValues.getAllValues()).containsOnly(1L, 5L, 10L);
    }

    @Test
    public void bigDecimalSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
                this.getTestInstance()
                        .bigDecimalSpec()
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .addValues(BigDecimal.ONE, new BigDecimal(5), BigDecimal.TEN)
                        .markExhaustive()
                        .setDefaultValue(new BigDecimal(100))
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo(new BigDecimal(100));
        assertThat(possibleValues.getAllValues()).containsOnly(BigDecimal.ONE, new BigDecimal(5), BigDecimal.TEN);
    }

    @Test
    public void bigDecimalSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .bigDecimalSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .markRequired()
                .addValues(BigDecimal.ONE, new BigDecimal(5), BigDecimal.TEN)
                .markExhaustive()
                .setDefaultValue(new BigDecimal(100))
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo(new BigDecimal(100));
        assertThat(possibleValues.getAllValues()).containsOnly(BigDecimal.ONE, new BigDecimal(5), BigDecimal.TEN);
    }

    @Test
    public void boundedBigDecimalSpecForThesaurusBasedApproach() {
        BigDecimal lowerLimit = new BigDecimal(123);
        BigDecimal upperLimit = new BigDecimal(456);
        PropertySpec propertySpec =
                this.getTestInstance()
                        .boundedBigDecimalSpec(lowerLimit, upperLimit)
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec).isInstanceOf(BoundedBigDecimalPropertySpec.class);
        BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec = (BoundedBigDecimalPropertySpec) propertySpec;
        assertThat(boundedBigDecimalPropertySpec.getLowerLimit()).isEqualTo(lowerLimit);
        assertThat(boundedBigDecimalPropertySpec.getUpperLimit()).isEqualTo(upperLimit);
    }

    @Test
    public void boundedBigDecimalSpecForHardCodedApproach() {
        BigDecimal lowerLimit = new BigDecimal(123);
        BigDecimal upperLimit = new BigDecimal(456);
        PropertySpec propertySpec = this.getTestInstance()
                .boundedBigDecimalSpec(lowerLimit, upperLimit)
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec).isInstanceOf(BoundedBigDecimalPropertySpec.class);
        BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec = (BoundedBigDecimalPropertySpec) propertySpec;
        assertThat(boundedBigDecimalPropertySpec.getLowerLimit()).isEqualTo(lowerLimit);
        assertThat(boundedBigDecimalPropertySpec.getUpperLimit()).isEqualTo(upperLimit);
    }

    @Test
    public void positiveBigDecimalSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
                this.getTestInstance()
                        .positiveBigDecimalSpec()
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec).isInstanceOf(BoundedBigDecimalPropertySpec.class);
        BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec = (BoundedBigDecimalPropertySpec) propertySpec;
        assertThat(boundedBigDecimalPropertySpec.getLowerLimit()).isEqualTo(BigDecimal.ZERO);
        assertThat(boundedBigDecimalPropertySpec.getUpperLimit()).isNull();
    }

    @Test
    public void positiveBigDecimalSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .positiveBigDecimalSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec).isInstanceOf(BoundedBigDecimalPropertySpec.class);
        BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec = (BoundedBigDecimalPropertySpec) propertySpec;
        assertThat(boundedBigDecimalPropertySpec.getLowerLimit()).isEqualTo(BigDecimal.ZERO);
        assertThat(boundedBigDecimalPropertySpec.getUpperLimit()).isNull();
    }

    @Test
    public void referenceSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
                this.getTestInstance()
                        .referenceSpec(ExampleEntity.class)
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .addValues(new ExampleEntity(1L), new ExampleEntity(5L), new ExampleEntity(10L))
                        .markExhaustive()
                        .setDefaultValue(new ExampleEntity(100L))
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        ValueFactory valueFactory = propertySpec.getValueFactory();
        assertThat(valueFactory).isNotNull();
        assertThat(valueFactory.getValueType()).isEqualTo(ExampleEntity.class);
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo(new ExampleEntity(100L));
        assertThat(possibleValues.getAllValues()).containsOnly(new ExampleEntity(1L), new ExampleEntity(5L), new ExampleEntity(10L));
    }

    @Test
    public void referenceSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .referenceSpec(ExampleEntity.class)
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .markRequired()
                .addValues(new ExampleEntity(1L), new ExampleEntity(5L), new ExampleEntity(10L))
                .markExhaustive()
                .setDefaultValue(new ExampleEntity(100L))
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        ValueFactory valueFactory = propertySpec.getValueFactory();
        assertThat(valueFactory).isNotNull();
        assertThat(valueFactory.getValueType()).isEqualTo(ExampleEntity.class);
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getDefault()).isEqualTo(new ExampleEntity(100L));
        assertThat(possibleValues.getAllValues()).containsOnly(new ExampleEntity(1L), new ExampleEntity(5L), new ExampleEntity(10L));
    }

    @Test
    public void timeZoneSpecForThesaurusBasedApproach() {
        PropertySpec propertySpec =
                this.getTestInstance()
                        .timezoneSpec()
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .setDefaultValue(TimeZone.getDefault())
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(TimeZone.getDefault());
    }

    @Test
    public void timeZoneSpecForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .timezoneSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .markRequired()
                .setDefaultValue(TimeZone.getDefault())
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(TimeZone.getDefault());
    }

    @Test
    public void relativePeriodSpecForThesaurusBasedApproach() {
        RelativePeriod relativePeriod = mock(RelativePeriod.class);
        PropertySpec propertySpec =
                this.getTestInstance()
                        .relativePeriodSpec()
                        .named(this.nameTranslationKey)
                        .describedAs(this.descriptionTranslationKey)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .setDefaultValue(relativePeriod)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(this.nameTranslationKey.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.nameTranslationKey);
        assertThat(propertySpec.getDescription()).isEqualTo(TRANSLATION_NOT_SUPPORTED_IN_UNIT_TESTS);
        verify(this.thesaurus).getFormat(this.descriptionTranslationKey);
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(relativePeriod);
    }

    @Test
    public void relativePeriodSpecForHardCodedApproach() {
        RelativePeriod relativePeriod = mock(RelativePeriod.class);
        PropertySpec propertySpec = this.getTestInstance()
                .relativePeriodSpec()
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .markRequired()
                .setDefaultValue(relativePeriod)
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo("hardcoded");
        assertThat(propertySpec.getDisplayName()).isEqualTo("display name");
        assertThat(propertySpec.isRequired()).isTrue();
        assertThat(propertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(relativePeriod);
    }

    private PropertySpecService getTestInstance() {
        return new PropertySpecServiceImpl(this.timeService, this.ormService, new DefaultBeanService());
    }

    private class ExampleEntity {
        private final long id;

        private ExampleEntity(long id) {
            super();
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ExampleEntity that = (ExampleEntity) o;

            return id == that.id;

        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

}