/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl.properties;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimation.impl.EstimationServiceImpl;
import com.elster.jupiter.mdm.usagepoint.data.ChannelEstimationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointEstimation;
import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.mdm.usagepoint.data.impl.UsagePointDataInMemoryBootstrapModule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChannelEstimationRuleOverriddenPropertiesIT {

    private static final String USAGEPOINT_NAME = "UP001";
    private static final String ESTIMATION_RULE = "VR01";
    private static final String ESTIMATOR = "com...estimator";

    private static final String PROPERTY_1 = "prop1";
    private static final String PROPERTY_2 = "prop2";
    private static final String PROPERTY_3 = "prop3";

    private static UsagePointDataInMemoryBootstrapModule inMemoryBootstrapModule = new UsagePointDataInMemoryBootstrapModule();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static EstimatorFactory estimatorFactory;

    private PropertySpec propertySpec_1, propertySpec_2, propertySpec_3;
    private Estimator estimator;
    private ReadingType readingType;
    private EstimationRule estimationRule;
    private UsagePoint usagePoint;
    private UsagePointEstimation usagePointEstimation;

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();

        estimatorFactory = mock(EstimatorFactory.class);
        ((EstimationServiceImpl) inMemoryBootstrapModule.getEstimationService()).addEstimatorFactory(estimatorFactory);
    }

    @Before
    public void before() {
        this.propertySpec_1 = createLongPropertySpec(PROPERTY_1);
        this.propertySpec_2 = createLongPropertySpec(PROPERTY_2);
        this.propertySpec_3 = createLongPropertySpec(PROPERTY_3);

        this.readingType = findReadingType(UsagePointDataInMemoryBootstrapModule.BULK_A_PLUS_KWH);
        this.estimator = mockEstimator(ESTIMATOR, EstimationPropertyDefinitionLevel.TARGET_OBJECT, propertySpec_1, propertySpec_2, propertySpec_3);
        this.estimationRule = createEstimationRule(ESTIMATION_RULE, estimator, readingType);
        this.usagePoint = createUsagePoint(USAGEPOINT_NAME);
        this.usagePointEstimation = forEstimation(usagePoint);
    }

    @After
    public void after() {
        Mockito.reset(estimatorFactory);
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    private EstimationRule createEstimationRule(String ruleSetName, String ruleName, Estimator estimator, ReadingType... readingTypes) {
        EstimationService estimationService = inMemoryBootstrapModule.getEstimationService();
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
        List<? extends ChannelEstimationRuleOverriddenProperties> allOverriddenProperties = usagePointEstimation.findAllOverriddenProperties();

        // Asserts
        assertThat(allOverriddenProperties).isEmpty();
    }

    @Test
    @Transactional
    public void overrideEstimationRulePropertiesOnChannel() {
        // Business method
        ChannelEstimationRuleOverriddenProperties persistedProperties = usagePointEstimation.overridePropertiesFor(estimationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> properties = usagePointEstimation.findOverriddenProperties(estimationRule, readingType);

        assertThat(properties).isPresent();
        ChannelEstimationRuleOverriddenProperties overriddenProperties = properties.get();
        assertThat(overriddenProperties.getUsagePoint()).isEqualTo(usagePoint);
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
        ChannelEstimationRuleOverriddenProperties persistedProperties = usagePointEstimation.overridePropertiesFor(estimationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.setProperties(ImmutableMap.of(propertySpec_2.getName(), 2L));
        persistedProperties.update();

        // Asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> properties = usagePointEstimation.findOverriddenProperties(estimationRule, readingType);

        assertThat(properties).isPresent();
        ChannelEstimationRuleOverriddenProperties overriddenProperties = properties.get();
        assertThat(overriddenProperties.getUsagePoint()).isEqualTo(usagePoint);
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
        ChannelEstimationRuleOverriddenProperties persistedProperties = usagePointEstimation.overridePropertiesFor(estimationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.delete();

        // Asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> properties = usagePointEstimation.findOverriddenProperties(estimationRule, readingType);

        assertThat(properties).isEmpty();
    }

    @Test
    @Transactional
    public void updateEstimationRuleOverriddenPropertiesThatLeadsToDelete() {
        ChannelEstimationRuleOverriddenProperties persistedProperties = usagePointEstimation.overridePropertiesFor(estimationRule, readingType)
                .override(propertySpec_1.getName(), 1L)
                .override(propertySpec_3.getName(), 3L)
                .complete();

        // Business method
        persistedProperties.setProperties(Collections.emptyMap());
        persistedProperties.update();

        // Asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> properties = usagePointEstimation.findOverriddenProperties(estimationRule, readingType);

        assertThat(properties).isEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "usagePoint")
    public void overridePropertiesIfUsagePointIsNull() {
        EstimationRule estimationRule = mock(EstimationRule.class);
        when(estimationRule.getName()).thenReturn(ESTIMATION_RULE);
        when(estimationRule.getImplementation()).thenReturn(ESTIMATOR);

        ReadingType readingType = findReadingType(UsagePointDataInMemoryBootstrapModule.BULK_A_PLUS_KWH);
        UsagePointEstimation usagePointEstimation = forEstimation(null);

        // Business method
        usagePointEstimation.overridePropertiesFor(estimationRule, readingType).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "readingType")
    public void overridePropertiesIfReadingTypeIsNull() {
        EstimationRule estimationRule = mock(EstimationRule.class);
        when(estimationRule.getName()).thenReturn(ESTIMATION_RULE);
        when(estimationRule.getImplementation()).thenReturn(ESTIMATOR);

        UsagePointEstimation usagePointEstimation = forEstimation(usagePoint);

        // Business method
        usagePointEstimation.overridePropertiesFor(estimationRule, null).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "ruleName")
    public void overridePropertiesIfEstimationRuleHasEmptyName() {
        EstimationRule estimationRule = mock(EstimationRule.class);
        when(estimationRule.getName()).thenReturn("");
        when(estimationRule.getImplementation()).thenReturn(ESTIMATOR);
        UsagePoint usagePoint = createUsagePoint("up");

        ReadingType readingType = findReadingType(UsagePointDataInMemoryBootstrapModule.BULK_A_PLUS_KWH);
        UsagePointEstimation usagePointEstimation = forEstimation(usagePoint);

        // Business method
        usagePointEstimation.overridePropertiesFor(estimationRule, readingType).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    public void overridePropertiesIfEstimatorImplIsNotSpecified() {
        EstimationRule estimationRule = mock(EstimationRule.class);
        when(estimationRule.getName()).thenReturn(ESTIMATION_RULE);
        when(estimationRule.getImplementation()).thenReturn(null);

        ReadingType readingType = findReadingType(UsagePointDataInMemoryBootstrapModule.BULK_A_PLUS_KWH);
        UsagePointEstimation usagePointEstimation = forEstimation(usagePoint);

        expectedException.expect(PropertyCannotBeOverriddenException.class);

        // Business method
        usagePointEstimation.overridePropertiesFor(estimationRule, readingType).override(propertySpec_1.getName(), 1L).complete();

        // Asserts
        // exception is thrown
    }

    @Test
    @Transactional
    public void findOverriddenPropertiesPerEstimationRule() {
        ReadingType anotherReadingType = findReadingType(UsagePointDataInMemoryBootstrapModule.BULK_A_PLUS_WH);

        EstimationRule estimationRule_1 = createEstimationRule("rs1", "r1", estimator, readingType);
        EstimationRule estimationRule_2 = createEstimationRule("rs2", "r1", estimator, readingType);
        EstimationRule estimationRule_3 = createEstimationRule("rs3", "r3", estimator, readingType);
        EstimationRule estimationRule_4 = createEstimationRule("rs4", "r1", estimator, anotherReadingType);

        UsagePointEstimation usagePointEstimation = forEstimation(usagePoint);

        usagePointEstimation.overridePropertiesFor(estimationRule_1, readingType)
                .override(propertySpec_1.getName(), 10L)
                .complete();

        // Business methods & asserts
        Optional<? extends ChannelEstimationRuleOverriddenProperties> overriddenProperties;

        overriddenProperties = usagePointEstimation.findOverriddenProperties(estimationRule_1, readingType);
        assertThat(overriddenProperties).isPresent();
        assertThat(overriddenProperties.get().getProperties()).containsExactly(MapEntry.entry(propertySpec_1.getName(), 10L));

        // estimationRule_2 inherits all overridden properties since it has the same name and estimator
        overriddenProperties = usagePointEstimation.findOverriddenProperties(estimationRule_2, readingType);
        assertThat(overriddenProperties).isPresent();
        assertThat(overriddenProperties.get().getProperties()).containsExactly(MapEntry.entry(propertySpec_1.getName(), 10L));

        // estimationRule_3 has no properties set because of another estimation rule name
        overriddenProperties = usagePointEstimation.findOverriddenProperties(estimationRule_3, readingType);
        assertThat(overriddenProperties).isEmpty();

        // estimationRule_4 has no properties set because of another reading type
        overriddenProperties = usagePointEstimation.findOverriddenProperties(estimationRule_4, anotherReadingType);
        assertThat(overriddenProperties).isEmpty();
    }

    @Test
    @Transactional
    public void overridePropertyWhichCantBeOverridden() {
        expectedException.expect(PropertyCannotBeOverriddenException.class);

        // Business method
        ChannelEstimationRuleOverriddenProperties persistedProperties = usagePointEstimation.overridePropertiesFor(estimationRule, readingType)
                .override("xxx", 1L)
                .complete();

        // Asserts
        // exception is thrown
    }

    private PropertySpec createLongPropertySpec(String name) {
        PropertySpecService propertySpecService = inMemoryBootstrapModule.getPropertySpecService();
        return propertySpecService.longSpec().named(name, name).describedAs(name).finish();
    }

    private UsagePointEstimation forEstimation(UsagePoint usagePoint) {
        return inMemoryBootstrapModule.getUsagePointDataModelService().forEstimation(usagePoint);
    }

    private UsagePoint createUsagePoint(String name) {
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        return serviceCategory.newUsagePoint(name, Instant.EPOCH).create();
    }

    private ReadingType findReadingType(String readingTypeMrid) {
        return inMemoryBootstrapModule.getMeteringService().getReadingType(readingTypeMrid).get();
    }
}
