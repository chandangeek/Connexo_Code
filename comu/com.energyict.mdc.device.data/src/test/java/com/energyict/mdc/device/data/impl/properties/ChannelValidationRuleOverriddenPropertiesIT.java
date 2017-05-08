/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.energyict.mdc.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChannelValidationRuleOverriddenPropertiesIT extends PersistenceIntegrationTest {

    private static final String BULK_A_PLUS_KWH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String DELTA_A_PLUS_KWH = "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0";

    private static final String DEVICE_NAME = "SPE001";
    private static final String VALIDATION_RULE = "VR01";
    private static final String VALIDATOR = "com...validator";
    private static final ValidationAction VALIDATION_ACTION = ValidationAction.FAIL;

    private static final String PROPERTY_1 = "prop1";
    private static final String PROPERTY_2 = "prop2";
    private static final String PROPERTY_3 = "prop3";

    private static ValidatorFactory validatorFactory;

    private PropertySpec propertySpec_1, propertySpec_2, propertySpec_3;
    private Validator validator;
    private ReadingType readingType;
    private ValidationRule validationRule;
    private Device device;
    private DeviceValidation deviceValidation;

    @BeforeClass
    public static void beforeClass() {
        validatorFactory = mock(ValidatorFactory.class);
        inMemoryPersistence.getValidationService().addValidatorFactory(validatorFactory);
    }

    @Before
    public void before() {
        this.propertySpec_1 = createLongPropertySpec(PROPERTY_1);
        this.propertySpec_2 = createLongPropertySpec(PROPERTY_2);
        this.propertySpec_3 = createLongPropertySpec(PROPERTY_3);

        this.readingType = findReadingType(BULK_A_PLUS_KWH);
        this.validator = mockValidator(VALIDATOR, ValidationPropertyDefinitionLevel.TARGET_OBJECT, propertySpec_1, propertySpec_2, propertySpec_3);
        this.validationRule = createValidationRule(VALIDATION_RULE, validator, VALIDATION_ACTION, readingType);
        this.device = createDevice(DEVICE_NAME);
        this.deviceValidation = device.forValidation();
    }

    @After
    public void after() {
        Mockito.reset(validatorFactory);
    }

    private ValidationRule createValidationRule(String ruleSetName, String ruleName, Validator validator, ValidationAction validationAction, ReadingType... readingTypes) {
        ValidationService validationService = inMemoryPersistence.getValidationService();
        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(ruleSetName, QualityCodeSystem.MDC);
        ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion(null, null);
        return validationRuleSetVersion.addRule(validationAction, validator.getDisplayName(), ruleName).withReadingType(readingTypes).create();
    }

    private ValidationRule createValidationRule(String ruleName, Validator validator, ValidationAction validationAction, ReadingType... readingTypes) {
        return createValidationRule("RuleSet: " + ruleName, ruleName, validator, validationAction, readingTypes);
    }

    private static Validator mockValidator(String validatorImpl, ValidationPropertyDefinitionLevel level, PropertySpec... properties) {
        Validator validator = mock(Validator.class);
        when(validator.getDisplayName()).thenReturn(validatorImpl);
        when(validator.getPropertySpecs(level)).thenReturn(Arrays.asList(properties));
        when(validatorFactory.available()).thenReturn(Collections.singletonList(validatorImpl));
        when(validatorFactory.createTemplate(validatorImpl)).thenReturn(validator);
        return validator;
    }

    @Test
    @Transactional
    public void noOverriddenProperties() {
        // Business method
        List<? extends ChannelValidationRuleOverriddenProperties> allOverriddenProperties = deviceValidation.findAllOverriddenProperties();

        // Asserts
        assertThat(allOverriddenProperties).isEmpty();
    }

    @Test
    @Transactional
    public void overrideValidationRulePropertiesOnChannel() {
        // Business method
        ChannelValidationRuleOverriddenProperties persistedProperties = deviceValidation.overridePropertiesFor(validationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Asserts
        Optional<? extends ChannelValidationRuleOverriddenProperties> properties = deviceValidation.findOverriddenProperties(validationRule, readingType);

        assertThat(properties).isPresent();
        ChannelValidationRuleOverriddenProperties overriddenProperties = properties.get();
        assertThat(overriddenProperties.getDevice()).isEqualTo(device);
        assertThat(overriddenProperties.getReadingType()).isEqualTo(readingType);
        assertThat(overriddenProperties.getValidationRuleName()).isEqualTo(VALIDATION_RULE);
        assertThat(overriddenProperties.getValidatorImpl()).isEqualTo(VALIDATOR);
        assertThat(overriddenProperties.getValidationAction()).isEqualTo(VALIDATION_ACTION);
        assertThat(overriddenProperties.getProperties()).isEqualTo(
                ImmutableMap.of(
                        propertySpec_1.getName(), 1L,
                        propertySpec_3.getName(), 3L
                ));
        assertThat(overriddenProperties.getId()).isEqualTo(persistedProperties.getId());
        assertThat(overriddenProperties.getVersion()).isEqualTo(persistedProperties.getVersion());
    }

    @Test
    @Transactional
    public void updateValidationRuleOverriddenPropertiesOnChannel() {
        ChannelValidationRuleOverriddenProperties persistedProperties = deviceValidation.overridePropertiesFor(validationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.setProperties(ImmutableMap.of(propertySpec_2.getName(), 2L));
        persistedProperties.update();

        // Asserts
        Optional<? extends ChannelValidationRuleOverriddenProperties> properties = deviceValidation.findOverriddenProperties(validationRule, readingType);

        assertThat(properties).isPresent();
        ChannelValidationRuleOverriddenProperties overriddenProperties = properties.get();
        assertThat(overriddenProperties.getDevice()).isEqualTo(device);
        assertThat(overriddenProperties.getReadingType()).isEqualTo(readingType);
        assertThat(overriddenProperties.getValidationRuleName()).isEqualTo(VALIDATION_RULE);
        assertThat(overriddenProperties.getValidatorImpl()).isEqualTo(VALIDATOR);
        assertThat(overriddenProperties.getValidationAction()).isEqualTo(VALIDATION_ACTION);
        assertThat(overriddenProperties.getProperties()).isEqualTo(ImmutableMap.of(propertySpec_2.getName(), 2L));
        assertThat(overriddenProperties.getId()).isEqualTo(persistedProperties.getId());
        assertThat(overriddenProperties.getVersion()).isEqualTo(persistedProperties.getVersion());
    }

    @Test
    @Transactional
    public void deleteValidationRuleOverriddenPropertiesOnChannel() {
        ChannelValidationRuleOverriddenProperties persistedProperties = deviceValidation.overridePropertiesFor(validationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.delete();

        // Asserts
        Optional<? extends ChannelValidationRuleOverriddenProperties> properties = deviceValidation.findOverriddenProperties(validationRule, readingType);

        assertThat(properties).isEmpty();
    }

    @Test
    @Transactional
    public void updateValidationRuleOverriddenPropertiesThatLeadsToDelete() {
        ChannelValidationRuleOverriddenProperties persistedProperties = deviceValidation.overridePropertiesFor(validationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.setProperties(Collections.emptyMap());
        persistedProperties.update();

        // Asserts
        Optional<? extends ChannelValidationRuleOverriddenProperties> properties = deviceValidation.findOverriddenProperties(validationRule, readingType);

        assertThat(properties).isEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}", property = "readingType")
    public void overridePropertiesIfReadingTypeIsNull() {
        ValidationRule validationRule = mock(ValidationRule.class);
        when(validationRule.getName()).thenReturn(VALIDATION_RULE);
        when(validationRule.getImplementation()).thenReturn(VALIDATOR);
        when(validationRule.getAction()).thenReturn(VALIDATION_ACTION);

        // Business method
        deviceValidation.overridePropertiesFor(validationRule, null).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}", property = "ruleName")
    public void overridePropertiesIfValidationRuleHasEmptyName() {
        ValidationRule validationRule = mock(ValidationRule.class);
        when(validationRule.getName()).thenReturn("");
        when(validationRule.getImplementation()).thenReturn(VALIDATOR);
        when(validationRule.getAction()).thenReturn(VALIDATION_ACTION);
        Device device = createDevice("dev");

        // Business method
        deviceValidation.overridePropertiesFor(validationRule, readingType).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    public void overridePropertiesIfValidatorImplIsNotSpecified() {
        ValidationRule validationRule = mock(ValidationRule.class);
        when(validationRule.getName()).thenReturn(VALIDATION_RULE);
        when(validationRule.getImplementation()).thenReturn(null);
        when(validationRule.getAction()).thenReturn(VALIDATION_ACTION);

        expectedException.expect(ValidatorNotFoundException.class);

        // Business method
        deviceValidation.overridePropertiesFor(validationRule, readingType).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}", property = "validationAction")
    public void overridePropertiesIfValidationActionIsNull() {
        ValidationRule validationRule = mock(ValidationRule.class);
        when(validationRule.getName()).thenReturn(VALIDATION_RULE);
        when(validationRule.getImplementation()).thenReturn(VALIDATOR);
        when(validationRule.getAction()).thenReturn(null);

        // Business method
        deviceValidation.overridePropertiesFor(validationRule, readingType).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    public void findOverriddenPropertiesPerValidationRule() {
        ReadingType anotherReadingType = findReadingType(DELTA_A_PLUS_KWH);

        ValidationRule validationRule_1 = createValidationRule("rs1", "r1", validator, ValidationAction.FAIL, readingType);
        ValidationRule validationRule_2 = createValidationRule("rs2", "r1", validator, ValidationAction.WARN_ONLY, readingType);
        ValidationRule validationRule_3 = createValidationRule("rs3", "r1", validator, ValidationAction.FAIL, readingType);
        ValidationRule validationRule_4 = createValidationRule("rs4", "r4", validator, ValidationAction.FAIL, readingType);
        ValidationRule validationRule_5 = createValidationRule("rs5", "r1", validator, ValidationAction.FAIL, anotherReadingType);

        deviceValidation.overridePropertiesFor(validationRule_1, readingType)
                .override(propertySpec_1.getName(), 10L)
                .complete();

        // Business methods & asserts
        Optional<? extends ChannelValidationRuleOverriddenProperties> overriddenProperties;

        overriddenProperties = deviceValidation.findOverriddenProperties(validationRule_1, readingType);
        assertThat(overriddenProperties).isPresent();
        assertThat(overriddenProperties.get().getProperties()).containsExactly(MapEntry.entry(propertySpec_1.getName(), 10L));

        // validationRule_2 has no properties set because of another validation action
        overriddenProperties = deviceValidation.findOverriddenProperties(validationRule_2, readingType);
        assertThat(overriddenProperties).isEmpty();

        // validationRule_3 inherits all overridden properties since it has the same name, validator and validation action
        overriddenProperties = deviceValidation.findOverriddenProperties(validationRule_3, readingType);
        assertThat(overriddenProperties).isPresent();
        assertThat(overriddenProperties.get().getProperties()).containsExactly(MapEntry.entry(propertySpec_1.getName(), 10L));

        // validationRule_4 has no properties set because of another validation rule name
        overriddenProperties = deviceValidation.findOverriddenProperties(validationRule_4, readingType);
        assertThat(overriddenProperties).isEmpty();

        // validationRule_5 has no properties set because of another reading type
        overriddenProperties = deviceValidation.findOverriddenProperties(validationRule_5, anotherReadingType);
        assertThat(overriddenProperties).isEmpty();
    }

    @Test
    @Transactional
    public void overridePropertyWhichCantBeOverridden() {
        expectedException.expect(PropertyCannotBeOverriddenException.class);

        // Business method
        deviceValidation.overridePropertiesFor(validationRule, readingType)
                .override("xxx", 1L)
                .complete();

        // Asserts
        // exception is thrown
    }

    private PropertySpec createLongPropertySpec(String name) {
        PropertySpecService propertySpecService = inMemoryPersistence.getPropertySpecService();
        return propertySpecService.longSpec().named(name, name).describedAs(name).finish();
    }

    private Device createDevice(String name) {
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, Instant.now());
    }

    private ReadingType findReadingType(String readingTypeMrid) {
        return inMemoryPersistence.getMeteringService().getReadingType(readingTypeMrid).get();
    }
}
