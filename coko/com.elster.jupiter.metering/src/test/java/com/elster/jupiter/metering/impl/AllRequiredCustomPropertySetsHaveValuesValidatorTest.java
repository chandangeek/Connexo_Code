/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verify {@link AllRequiredCustomPropertySetsHaveValuesValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class AllRequiredCustomPropertySetsHaveValuesValidatorTest {

    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private ConstraintValidatorContext context;
    @InjectMocks
    private AllRequiredCustomPropertySetsHaveValuesValidator valuesValidator;

    private Instant eachRangeStart = Instant.MIN;

    @Before
    public void initialize() {
        ConstraintValidatorContext.ConstraintViolationBuilder b1 = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext b2 = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(b1.addPropertyNode(anyString())).thenReturn(b2);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(b1);
    }

    @Test
    public void validatorTest() {
        validationConfigurations().forEach(upConfig ->
                assertThat(valuesValidator.isValid(upConfig.usagePoint, context)).withFailMessage("Validation result does not match expected one for configuration \"" + upConfig.description + "\"")
                        .isEqualTo(upConfig.expectValid));
    }

    private List<UPConfig> validationConfigurations() {
        return Arrays.asList(
                new UPConfig("Simple. both required and valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(true).mockAll(),
                new UPConfig("Simple. Service category has required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().notValid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(false).mockAll(),
                new UPConfig("Simple. MC has required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().notValid()))
                        .withValidationExpectation(false).mockAll(),
                new UPConfig("Simple. Both required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().notValid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().notValid()))
                        .withValidationExpectation(false).mockAll(),
                new UPConfig("Simple. both not required and valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.notRequired().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.notRequired().valid()))
                        .withValidationExpectation(true).mockAll(),
                new UPConfig("Simple. Service category has not required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.notRequired().notValid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.notRequired().valid()))
                        .withValidationExpectation(true).mockAll(),
                new UPConfig("Simple. MC has not required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.notRequired().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.notRequired().notValid()))
                        .withValidationExpectation(true).mockAll(),
                new UPConfig("Simple. Both not required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.notRequired().notValid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.notRequired().notValid()))
                        .withValidationExpectation(true).mockAll(),
                new UPConfig("Complex. All required all valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.required().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(true).mockAll(),
                new UPConfig("Complex. All required, service category has one not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.required().notValid())
                                        .with(CustomPropertySetConfig.required().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(false).mockAll(),
                new UPConfig("Complex. All required, mc has one not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.required().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().notValid())
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(false).mockAll(),
                new UPConfig("Complex. All required, mc and service category has not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().notValid())
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.required().notValid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().notValid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().notValid())
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(false).mockAll(),
                new UPConfig("Complex. Not all required, service category has one not required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.notRequired().notValid())
                                        .with(CustomPropertySetConfig.required().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(true).mockAll(),
                new UPConfig("Complex. Not all required, mc has one not required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.required().valid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.notRequired().notValid())
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(true).mockAll(),
                new UPConfig("Complex. Not all required, mc required not valid")
                        .withServiceCategory(
                                new CustomPropertySetsHolder()
                                        .with(CustomPropertySetConfig.notRequired().notValid())
                                        .with(CustomPropertySetConfig.required().valid())
                                        .with(CustomPropertySetConfig.notRequired().notValid()))
                        .addMC(new CustomPropertySetsHolder()
                                .with(CustomPropertySetConfig.notRequired().notValid())
                                .with(CustomPropertySetConfig.required().valid())
                                .with(CustomPropertySetConfig.required().notValid())
                                .with(CustomPropertySetConfig.required().valid()))
                        .withValidationExpectation(false).mockAll());
    }

    class UPConfig {

        CustomPropertySetsHolder serviceCategoryConfig;
        List<CustomPropertySetsHolder> mcConfigs;

        UsagePointImpl usagePoint;
        boolean expectValid;

        String description;

        UPConfig(String description) {
            this.description = description;
            mcConfigs = new ArrayList<>();
        }

        UPConfig withServiceCategory(CustomPropertySetsHolder serviceCategoryConfig) {
            this.serviceCategoryConfig = serviceCategoryConfig;
            return this;
        }

        UPConfig addMC(CustomPropertySetsHolder mcConfig) {
            mcConfigs.add(mcConfig);
            return this;
        }

        UPConfig withValidationExpectation(boolean expectValid) {
            this.expectValid = expectValid;
            return this;
        }

        UPConfig mockAll() {
            usagePoint = mock(UsagePointImpl.class);
            serviceCategoryConfig.customPropertySetConfigs.forEach(c -> c.setUsagePoint(usagePoint));
            mcConfigs.forEach(mc -> mc.customPropertySetConfigs.forEach(c -> c.setUsagePoint(usagePoint)));
            when(usagePoint.getInstallationTime()).thenReturn(eachRangeStart);
            ServiceCategory serviceCategory = mock(ServiceCategory.class);
            when(serviceCategory.getCustomPropertySets()).thenReturn(serviceCategoryConfig.customPropertySetConfigs.stream()
                    .map(RegisteredCustomPropertySetMyImpl::new)
                    .collect(Collectors.toList()));
            when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
            when(usagePoint.getAllEffectiveMetrologyConfigurations()).thenReturn(mcConfigs.stream()
                    .map(EffectiveMetrologyConfigurationOnUsagePointMyImpl::new)
                    .collect(Collectors.toList()));
            return this;
        }
    }

    class CustomPropertySetsHolder {

        List<CustomPropertySetConfig> customPropertySetConfigs;

        CustomPropertySetsHolder() {
            customPropertySetConfigs = new ArrayList<>();
        }

        CustomPropertySetsHolder with(CustomPropertySetConfig customPropertySetConfig) {
            customPropertySetConfigs.add(customPropertySetConfig);
            return this;
        }
    }

    static class CustomPropertySetConfig {

        boolean required;
        boolean valid;
        String name = "Test custom property set";

        UsagePoint usagePoint;

        void setUsagePoint(UsagePoint usagePoint) {
            this.usagePoint = usagePoint;
        }

        static CustomPropertySetConfig required() {
            CustomPropertySetConfig config = new CustomPropertySetConfig();
            config.required = true;
            return config;
        }

        static CustomPropertySetConfig notRequired() {
            CustomPropertySetConfig config = new CustomPropertySetConfig();
            config.required = false;
            return config;
        }

        CustomPropertySetConfig valid() {
            valid = true;
            return this;
        }

        CustomPropertySetConfig notValid() {
            valid = false;
            return this;
        }
    }

    class RegisteredCustomPropertySetMyImpl implements RegisteredCustomPropertySet {

        CustomPropertySetConfig config;

        RegisteredCustomPropertySetMyImpl(CustomPropertySetConfig config) {
            this.config = config;
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public CustomPropertySet getCustomPropertySet() {
            CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
            when(customPropertySet.getName()).thenReturn(config.name);
            when(customPropertySet.isRequired()).thenReturn(config.required);
            when(customPropertySetService.validateCustomPropertySetHasValues(customPropertySet, config.usagePoint, Range
                    .greaterThan(eachRangeStart))).thenReturn(config.valid);
            return customPropertySet;
        }

        @Override
        public Set<ViewPrivilege> getViewPrivileges() {
            return null;
        }

        @Override
        public Set<EditPrivilege> getEditPrivileges() {
            return null;
        }

        @Override
        public boolean isEditableByCurrentUser() {
            return false;
        }

        @Override
        public boolean isViewableByCurrentUser() {
            return false;
        }

        @Override
        public void updatePrivileges(Set<ViewPrivilege> viewPrivileges, Set<EditPrivilege> editPrivileges) {

        }
    }

    class EffectiveMetrologyConfigurationOnUsagePointMyImpl implements EffectiveMetrologyConfigurationOnUsagePoint {

        CustomPropertySetsHolder customPropertySetsHolder;

        EffectiveMetrologyConfigurationOnUsagePointMyImpl(CustomPropertySetsHolder customPropertySetsHolder) {
            this.customPropertySetsHolder = customPropertySetsHolder;
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public Interval getInterval() {
            return null;
        }

        @Override
        public Range<Instant> getRange() {
            return Range.greaterThan(eachRangeStart);
        }

        @Override
        public UsagePointMetrologyConfiguration getMetrologyConfiguration() {
            UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
            when(usagePointMetrologyConfiguration.getCustomPropertySets()).thenReturn(customPropertySetsHolder.customPropertySetConfigs
                    .stream()
                    .map(RegisteredCustomPropertySetMyImpl::new)
                    .collect(Collectors.toList()));
            return usagePointMetrologyConfiguration;
        }

        @Override
        public UsagePoint getUsagePoint() {
            return null;
        }

        @Override
        public void close(Instant closingDate) {

        }

        @Override
        public Instant getStart() {
            return null;
        }

        @Override
        public Instant getEnd() {
            return null;
        }

        @Override
        public Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract) {
            return null;
        }

        @Override
        public Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract, Instant when) {
            return null;
        }

        @Override
        public Optional<AggregatedChannel> getAggregatedChannel(MetrologyContract metrologyContract, ReadingType readingType) {
            return null;
        }

        @Override
        public void activateOptionalMetrologyContract(MetrologyContract metrologyContract, Instant when) {

        }

        @Override
        public void deactivateOptionalMetrologyContract(MetrologyContract metrologyContract, Instant when) {

        }

        @Override
        public List<ReadingTypeRequirement> getReadingTypeRequirements() {
            return null;
        }

        @Override
        public boolean isComplete(MetrologyContract metrologyContract) {
            return false;
        }
    }
}
