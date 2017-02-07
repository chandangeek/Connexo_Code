/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.List;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultValidatorFactoryTest {
    public static final Quantity MINIMUM = Quantity.create(BigDecimal.valueOf(1000L), 1, "Wh");
    public static final Quantity MAXIMUM = Quantity.create(BigDecimal.valueOf(5000L), 1, "Wh");
    private DefaultValidatorFactory defaultValidatorFactory;

    @Before
    public void setUp() throws Exception {
        defaultValidatorFactory = new DefaultValidatorFactory();
        defaultValidatorFactory.setPropertySpecService(new PropertySpecServiceImpl());
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
            expectedTranslations += ((IValidator)validator).getExtraTranslations().size();
        }

        assertThat(validatorFactory.getKeys()).hasSize(expectedTranslations);
    }

}