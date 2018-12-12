/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimation.impl.EstimationServiceImpl;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.ChannelEstimationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChannelEstimationRuleOverriddenPropertiesIT extends PersistenceIntegrationTest {

    private static final String BULK_A_PLUS_KWH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String DELTA_A_PLUS_KWH = "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0";

    private static final String DEVICE_NAME = "SPE001";
    private static final String ESTIMATION_RULE = "VR01";
    private static final String ESTIMATOR = "com...estimator";

    private static final String PROPERTY_1 = "prop1";
    private static final String PROPERTY_2 = "prop2";
    private static final String PROPERTY_3 = "prop3";

    private static EstimatorFactory estimatorFactory;

    private PropertySpec propertySpec_1, propertySpec_2, propertySpec_3;
    private Estimator estimator;
    private ReadingType readingType;
    private EstimationRule estimationRule;
    private Device device;
    private DeviceEstimation deviceEstimation;

    @BeforeClass
    public static void beforeClass() {
        estimatorFactory = mock(EstimatorFactory.class);
        ((EstimationServiceImpl) inMemoryPersistence.getEstimationService()).addEstimatorFactory(estimatorFactory);
    }

    @Before
    public void before() {
        this.propertySpec_1 = createLongPropertySpec(PROPERTY_1);
        this.propertySpec_2 = createLongPropertySpec(PROPERTY_2);
        this.propertySpec_3 = createLongPropertySpec(PROPERTY_3);

        this.readingType = findReadingType(BULK_A_PLUS_KWH);
        this.estimator = mockEstimator(ESTIMATOR, EstimationPropertyDefinitionLevel.TARGET_OBJECT, propertySpec_1, propertySpec_2, propertySpec_3);
        this.estimationRule = createEstimationRule(ESTIMATION_RULE, estimator, readingType);
        this.device = createDevice(DEVICE_NAME);
        this.deviceEstimation = device.forEstimation();
    }

    @After
    public void after() {
        Mockito.reset(estimatorFactory);
    }

    private EstimationRule createEstimationRule(String ruleSetName, String ruleName, Estimator estimator, ReadingType... readingTypes) {
        EstimationService estimationService = inMemoryPersistence.getEstimationService();
        EstimationRuleSet estimationRuleSet = estimationService.createEstimationRuleSet(ruleSetName, QualityCodeSystem.MDC);
        return estimationRuleSet.addRule(estimator.getDisplayName(), ruleName).withReadingType(readingTypes).create();
    }

    private EstimationRule createEstimationRule(String ruleName, Estimator estimator, ReadingType... readingTypes) {
        return createEstimationRule("RuleSet: " + ruleName, ruleName, estimator, readingTypes);
    }

    private static Estimator mockEstimator(String estimatorImpl, EstimationPropertyDefinitionLevel level, PropertySpec... properties) {
        Estimator estimator = mock(Estimator.class);
        when(estimator.getDisplayName()).thenReturn(estimatorImpl);
        when(estimator.getPropertySpecs(level)).thenReturn(Arrays.asList(properties));
        when(estimatorFactory.available()).thenReturn(Collections.singletonList(estimatorImpl));
        when(estimatorFactory.createTemplate(estimatorImpl)).thenReturn(estimator);
        return estimator;
    }

    @Test
    @Transactional
    public void noOverriddenProperties() {
        // Business method
        List<? extends ChannelEstimationRuleOverriddenProperties> allOverriddenProperties = deviceEstimation.findAllOverriddenProperties();

        // Asserts
        assertThat(allOverriddenProperties).isEmpty();
    }

    @Test
    @Transactional
    public void overrideEstimationRulePropertiesOnChannel() {
        // Business method
        ChannelEstimationRuleOverriddenProperties persistedProperties = deviceEstimation.overridePropertiesFor(estimationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> properties = deviceEstimation.findOverriddenProperties(estimationRule, readingType);

        assertThat(properties).isPresent();
        ChannelEstimationRuleOverriddenProperties overriddenProperties = properties.get();
        assertThat(overriddenProperties.getDevice()).isEqualTo(device);
        assertThat(overriddenProperties.getReadingType()).isEqualTo(readingType);
        assertThat(overriddenProperties.getEstimationRuleName()).isEqualTo(ESTIMATION_RULE);
        assertThat(overriddenProperties.getEstimatorImpl()).isEqualTo(ESTIMATOR);
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
    public void updateEstimationRuleOverriddenPropertiesOnChannel() {
        ChannelEstimationRuleOverriddenProperties persistedProperties = deviceEstimation.overridePropertiesFor(estimationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.setProperties(ImmutableMap.of(propertySpec_2.getName(), 2L));
        persistedProperties.update();

        // Asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> properties = deviceEstimation.findOverriddenProperties(estimationRule, readingType);

        assertThat(properties).isPresent();
        ChannelEstimationRuleOverriddenProperties overriddenProperties = properties.get();
        assertThat(overriddenProperties.getDevice()).isEqualTo(device);
        assertThat(overriddenProperties.getReadingType()).isEqualTo(readingType);
        assertThat(overriddenProperties.getEstimationRuleName()).isEqualTo(ESTIMATION_RULE);
        assertThat(overriddenProperties.getEstimatorImpl()).isEqualTo(ESTIMATOR);
        assertThat(overriddenProperties.getProperties()).isEqualTo(ImmutableMap.of(propertySpec_2.getName(), 2L));
        assertThat(overriddenProperties.getId()).isEqualTo(persistedProperties.getId());
        assertThat(overriddenProperties.getVersion()).isEqualTo(persistedProperties.getVersion());
    }

    @Test
    @Transactional
    public void deleteEstimationRuleOverriddenPropertiesOnChannel() {
        ChannelEstimationRuleOverriddenProperties persistedProperties = deviceEstimation.overridePropertiesFor(estimationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.delete();

        // Asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> properties = deviceEstimation.findOverriddenProperties(estimationRule, readingType);

        assertThat(properties).isEmpty();
    }

    @Test
    @Transactional
    public void updateEstimationRuleOverriddenPropertiesThatLeadsToDelete() {
        ChannelEstimationRuleOverriddenProperties persistedProperties = deviceEstimation.overridePropertiesFor(estimationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.setProperties(Collections.emptyMap());
        persistedProperties.update();

        // Asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> properties = deviceEstimation.findOverriddenProperties(estimationRule, readingType);

        assertThat(properties).isEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}", property = "readingType")
    public void overridePropertiesIfReadingTypeIsNull() {
        EstimationRule estimationRule = mock(EstimationRule.class);
        when(estimationRule.getName()).thenReturn(ESTIMATION_RULE);
        when(estimationRule.getImplementation()).thenReturn(ESTIMATOR);

        // Business method
        deviceEstimation.overridePropertiesFor(estimationRule, null).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}", property = "ruleName")
    public void overridePropertiesIfEstimationRuleHasEmptyName() {
        EstimationRule estimationRule = mock(EstimationRule.class);
        when(estimationRule.getName()).thenReturn("");
        when(estimationRule.getImplementation()).thenReturn(ESTIMATOR);
        Device device = createDevice("up");

        // Business method
        deviceEstimation.overridePropertiesFor(estimationRule, readingType).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    public void overridePropertiesIfEstimatorImplIsNotSpecified() {
        EstimationRule estimationRule = mock(EstimationRule.class);
        when(estimationRule.getName()).thenReturn(ESTIMATION_RULE);
        when(estimationRule.getImplementation()).thenReturn(null);

        expectedException.expect(PropertyCannotBeOverriddenException.class);

        // Business method
        deviceEstimation.overridePropertiesFor(estimationRule, readingType).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    public void findOverriddenPropertiesPerEstimationRule() {
        ReadingType anotherReadingType = findReadingType(DELTA_A_PLUS_KWH);

        EstimationRule estimationRule_1 = createEstimationRule("rs1", "r1", estimator, readingType);
        EstimationRule estimationRule_2 = createEstimationRule("rs2", "r1", estimator, readingType);
        EstimationRule estimationRule_3 = createEstimationRule("rs3", "r3", estimator, readingType);
        EstimationRule estimationRule_4 = createEstimationRule("rs4", "r1", estimator, anotherReadingType);

        deviceEstimation.overridePropertiesFor(estimationRule_1, readingType)
                .override(propertySpec_1.getName(), 10L)
                .complete();

        // Business methods & asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> overriddenProperties;

        overriddenProperties = deviceEstimation.findOverriddenProperties(estimationRule_1, readingType);
        assertThat(overriddenProperties).isPresent();
        assertThat(overriddenProperties.get().getProperties()).containsExactly(MapEntry.entry(propertySpec_1.getName(), 10L));

        // estimationRule_2 inherits all overridden properties since it has the same name and estimator
        overriddenProperties = deviceEstimation.findOverriddenProperties(estimationRule_2, readingType);
        assertThat(overriddenProperties).isPresent();
        assertThat(overriddenProperties.get().getProperties()).containsExactly(MapEntry.entry(propertySpec_1.getName(), 10L));

        // estimationRule_3 has no properties set because of another estimation rule name
        overriddenProperties = deviceEstimation.findOverriddenProperties(estimationRule_3, readingType);
        assertThat(overriddenProperties).isEmpty();

        // estimationRule_4 has no properties set because of another reading type
        overriddenProperties = deviceEstimation.findOverriddenProperties(estimationRule_4, anotherReadingType);
        assertThat(overriddenProperties).isEmpty();
    }

    @Test
    @Transactional
    public void overridePropertyWhichCantBeOverridden() {
        expectedException.expect(PropertyCannotBeOverriddenException.class);

        // Business method
        ChannelEstimationRuleOverriddenProperties persistedProperties = deviceEstimation.overridePropertiesFor(estimationRule, readingType)
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
