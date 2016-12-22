package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleBuilder;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidateMetrologyConfigurationChannelsContainerTestIT {
    private static final String INPUT_RT_MRID = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final String OUTPUT_RT_MRID = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static ValidationInMemoryBootstrapModule inMemoryBootstrapModule = new ValidationInMemoryBootstrapModule(INPUT_RT_MRID, OUTPUT_RT_MRID);
    private static MeterRole meterRole;
    private static MetrologyPurpose metrologyPurpose;
    private static ServiceCategory serviceCategory;
    private static ReadingType inputReadingType;
    private static ReadingType outputReadingType;

    private Instant firstReadingTimestamp = ZonedDateTime.of(2015, 12, 1, 0, 0, 0, 0, inMemoryBootstrapModule.get(Clock.class).getZone()).toInstant();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        serviceCategory = inMemoryBootstrapModule.get(MeteringService.class).getServiceCategory(ServiceKind.ELECTRICITY).get();
        meterRole = inMemoryBootstrapModule.get(MetrologyConfigurationService.class).findDefaultMeterRole(DefaultMeterRole.MAIN);
        metrologyPurpose = inMemoryBootstrapModule.get(MetrologyConfigurationService.class).findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
        serviceCategory.addMeterRole(meterRole);
        inputReadingType = inMemoryBootstrapModule.get(MeteringService.class).getReadingType(INPUT_RT_MRID).get();
        outputReadingType = inMemoryBootstrapModule.get(MeteringService.class).getReadingType(OUTPUT_RT_MRID).get();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.get(TransactionService.class));

    private void createValidationConfiguration() {
        ValidationServiceImpl validationService = (ValidationServiceImpl) inMemoryBootstrapModule.get(ValidationService.class);
        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet("RS", QualityCodeSystem.MDM);
        ValidationRuleSetVersion ruleSetVersion = validationRuleSet.addRuleSetVersion("Always", Instant.ofEpochMilli(0));
        validationService.addResource(new ValidatorFactory() {
            @Override
            public List<String> available() {
                return Collections.singletonList("impl");
            }

            @Override
            public Validator create(String implementation, Map<String, Object> props) {
                return createTemplate(implementation);
            }

            @Override
            public Validator createTemplate(String implementation) {
                return new Validator() {
                    @Override
                    public Optional<QualityCodeIndex> getReadingQualityCodeIndex() {
                        return Optional.empty();
                    }

                    @Override
                    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
                    }

                    @Override
                    public Map<Instant, ValidationResult> finish() {
                        return Collections.emptyMap();
                    }

                    @Override
                    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
                        return ValidationResult.SUSPECT;
                    }

                    @Override
                    public ValidationResult validate(ReadingRecord readingRecord) {
                        return ValidationResult.VALID; // not used for now
                    }

                    @Override
                    public String getDisplayName() {
                        return "";
                    }

                    @Override
                    public String getDisplayName(String property) {
                        return "";
                    }

                    @Override
                    public String getDefaultFormat() {
                        return "";
                    }

                    @Override
                    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
                        return EnumSet.of(QualityCodeSystem.MDM);
                    }

                    @Override
                    public List<PropertySpec> getPropertySpecs() {
                        return Collections.emptyList();
                    }
                };
            }
        });
        ValidationRuleBuilder validationRuleBuilder = ruleSetVersion.addRule(ValidationAction.FAIL, "impl", "VR");
        validationRuleBuilder.withReadingType(outputReadingType).active(true).create();

        validationService.addValidationRuleSetResolver(new ValidationRuleSetResolver() {
            @Override
            public List<ValidationRuleSet> resolve(ValidationContext validationContext) {
                return Collections.singletonList(validationRuleSet);
            }

            @Override
            public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
                return ruleset == validationRuleSet;
            }
        });
    }

    @Test
    @Transactional
    public void testCanValidateEffectiveMetrologyConfigurationOnUsagePoint() {
        createValidationConfiguration();
        setupDefaultUsagePointLifeCycle();
        MetrologyConfigurationService metrologyConfigurationService = inMemoryBootstrapModule.get(MetrologyConfigurationService.class);
        UsagePointMetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.newUsagePointMetrologyConfiguration("MC", serviceCategory).create();
        metrologyConfiguration.addMeterRole(meterRole);
        FullySpecifiedReadingTypeRequirement readingTypeRequirement = metrologyConfiguration.newReadingTypeRequirement("RTR", meterRole).withReadingType(inputReadingType);
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("RTD", outputReadingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable readingTypeDeliverable = builder.build(builder.divide(builder.requirement(readingTypeRequirement), builder.constant(1000L)));
        MetrologyContract metrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose);
        metrologyContract.addDeliverable(readingTypeDeliverable);
        UsagePoint usagePoint = serviceCategory.newUsagePoint("UP", inMemoryBootstrapModule.get(Clock.class).instant()).create();
        usagePoint.apply(metrologyConfiguration, firstReadingTimestamp.minus(1, ChronoUnit.DAYS));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration().get();
        BaseReadingRecord baseReading = mock(BaseReadingRecord.class);
        when(baseReading.getTimeStamp()).thenReturn(firstReadingTimestamp);
        CalculatedMetrologyContractData calculatedMetrologyContractData = mock(CalculatedMetrologyContractData.class);
        doReturn(Collections.singletonList(baseReading)).when(calculatedMetrologyContractData).getCalculatedDataFor(readingTypeDeliverable);
        when(inMemoryBootstrapModule.getDataAggregationMock().calculate(eq(usagePoint), eq(metrologyContract), any(Range.class))).thenReturn(calculatedMetrologyContractData);

        ChannelsContainer channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).get();
        inMemoryBootstrapModule.get(ValidationService.class).validate(EnumSet.of(QualityCodeSystem.MDM), channelsContainer);

        List<ReadingQualityRecord> readingQualityRecords = channelsContainer.getChannel(outputReadingType)
                .get()
                .findReadingQualities()
                .ofQualitySystem(QualityCodeSystem.MDM)
                .atTimestamp(firstReadingTimestamp)
                .collect();
        assertThat(readingQualityRecords).hasSize(2);
        assertThat(readingQualityRecords.get(0).isSuspect()).isTrue();
    }

    private static void setupDefaultUsagePointLifeCycle() {
        UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = inMemoryBootstrapModule.get(UsagePointLifeCycleConfigurationService.class);
        usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle").markAsDefault();
    }
}
