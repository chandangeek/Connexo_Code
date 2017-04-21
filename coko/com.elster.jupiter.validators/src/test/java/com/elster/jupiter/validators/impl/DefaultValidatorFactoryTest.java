/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.config.MetrologyPurposeImpl;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultValidatorFactoryTest {

    public static final Quantity MINIMUM = Quantity.create(BigDecimal.valueOf(1000L), 1, "Wh");
    public static final Quantity MAXIMUM = Quantity.create(BigDecimal.valueOf(5000L), 1, "Wh");
    private DefaultValidatorFactory defaultValidatorFactory;

    @Mock
    private NlsService nlsService;
    @Mock
    private ValidationService validationService;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;

    @Before
    public void setUp() throws Exception {
        when(nlsService.getThesaurus(any(), any())).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
        defaultValidatorFactory = new DefaultValidatorFactory(nlsService, new PropertySpecServiceImpl() ,metrologyConfigurationService, validationService);

        MetrologyPurposeImpl metrologyPurpose = mock(MetrologyPurposeImpl.class);
        when(metrologyPurpose.getName()).thenReturn("purpose");
        when(metrologyConfigurationService.getMetrologyPurposes()).thenReturn(Arrays.asList(metrologyPurpose));
        defaultValidatorFactory.setMetrologyConfigurationService(metrologyConfigurationService);
    }

    @Test
    public void testAvailable() {
        List<String> available = defaultValidatorFactory.available();
        assertThat(available).contains(ThresholdValidator.class.getName());
    }

    @Test
    public void testCreateThresholdValidator() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(ThresholdValidator.MIN, (Object) MINIMUM, ThresholdValidator.MAX, MAXIMUM);

        Validator validator = defaultValidatorFactory.create(ThresholdValidator.class.getName(), properties);

        assertThat(validator).isNotNull().isInstanceOf(ThresholdValidator.class);
    }

    @Test
    public void testCreateThresholdValidatorTemplate() {
        Validator validator = defaultValidatorFactory.createTemplate(ThresholdValidator.class.getName());

        assertThat(validator).isNotNull().isInstanceOf(ThresholdValidator.class);
    }

    @Test
    public void testMainCheckValidator() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(MainCheckValidator.CHECK_PURPOSE, "purpose",
                MainCheckValidator.MAX_ABSOLUTE_DIFF, new TwoValuesAbsoluteDifference(),
                MainCheckValidator.MIN_THRESHOLD, new NonOrBigDecimalValueProperty(),
                MainCheckValidator.PASS_IF_NO_REF_DATA, false,
                MainCheckValidator.USE_VALIDATED_DATA, false);

        Validator validator = defaultValidatorFactory.create(MainCheckValidator.class.getName(), properties);

        assertThat(validator).isNotNull().isInstanceOf(MainCheckValidator.class);
    }

    @Test
    public void testMainCheckValidatorTemplate() {

        Validator validator = defaultValidatorFactory.createTemplate(MainCheckValidator.class.getName());

        assertThat(validator).isNotNull().isInstanceOf(MainCheckValidator.class);
    }

    @Test
    public void testCreateRegisterIncreaseValidator() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(RegisterIncreaseValidator.FAIL_EQUAL_DATA, (Object) true);

        Validator validator = defaultValidatorFactory.create(RegisterIncreaseValidator.class.getName(), properties);

        assertThat(validator).isNotNull().isInstanceOf(RegisterIncreaseValidator.class);
    }

    @Test
    public void testCreateRegisterIncreaseValidatorTemplate() {
        Validator validator = defaultValidatorFactory.createTemplate(RegisterIncreaseValidator.class.getName());

        assertThat(validator).isNotNull().isInstanceOf(RegisterIncreaseValidator.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUnavailableValidator() {
        ImmutableMap<String, Object> properties = ImmutableMap.of("minimum", (Object) MINIMUM, "maximum", MAXIMUM);

        Validator validator = defaultValidatorFactory.create("unsupported", properties);

        assertThat(validator).isNotNull().isInstanceOf(ThresholdValidator.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUnavailableValidatorTemplate() {
        Validator validator = defaultValidatorFactory.createTemplate("unavailable");

        assertThat(validator).isNotNull().isInstanceOf(ThresholdValidator.class);
    }

    @Test
    public void testInstall() {
        NlsService nlsService = mock(NlsService.class);
        Thesaurus thesaurus = mock(Thesaurus.class);
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(nlsMessageFormat.format()).thenReturn("This unit test does not care about translations");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);
        when(nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesaurus);
        DefaultValidatorFactory validatorFactory = defaultValidatorFactory;
        validatorFactory.setNlsService(nlsService);

        int expectedTranslations = TranslationKeys.values().length;
        for (String implementation : validatorFactory.available()) {
            expectedTranslations++;
            Validator validator = validatorFactory.createTemplate(implementation);
            expectedTranslations += validator.getPropertySpecs().size();
            expectedTranslations += ((IValidator) validator).getExtraTranslations().size();
        }

        assertThat(validatorFactory.getKeys()).hasSize(expectedTranslations);
    }

}
