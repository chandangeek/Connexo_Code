package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
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
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Currency;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
public class UsagePointDataRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    static long firmwareComTaskId = 445632136865L;
    @Mock
    Clock clock;
    @Mock
    MeteringService meteringService;
    @Mock
    LocationService locationService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    UsagePointConfigurationService usagePointConfigurationService;
    @Mock
    EstimationService estimationService;
    @Mock
    ValidationService validationService;
    @Mock
    DataAggregationService dataAggregationService;
    @Mock
    static SecurityContext securityContext;
    @Mock
    UsagePointDataService usagePointDataService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    ServiceCallService serviceCallService;
    @Mock
    ServiceCallInfoFactory serviceCallInfoFactory;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;

    @Mock
    IssueService issueService;
    @Mock
    BpmService bpmService;
    @Mock
    ThreadPrincipalService threadPrincipalService;

    @Override
    protected Application getApplication() {
        UsagePointApplication application = new UsagePointApplication() {
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<>();
                classes.addAll(super.getClasses());
                classes.add(SecurityRequestFilter.class);
                return classes;
            }
        };
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setMeteringService(meteringService);
        application.setLocationService(locationService);
        application.setRestQueryService(restQueryService);
        application.setClockService(clock);
        application.setMeteringGroupService(meteringGroupsService);
        application.setUsagePointConfigurationService(usagePointConfigurationService);
        application.setEstimationService(estimationService);
        application.setDataAggregationService(dataAggregationService);
        application.setValidationService(validationService);
        application.setUsagePointDataService(usagePointDataService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setBpmService(bpmService);
        application.setIssueService(issueService);
        application.setServiceCallService(serviceCallService);
        application.setServiceCallInfoFactory(serviceCallInfoFactory);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        application.setThreadPrincipalService(threadPrincipalService);
        return application;
    }

    public ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        return readingType;
    }

    public UsagePointMetrologyConfiguration mockMetrologyConfiguration(long id, String name) {
        ReadingType readingType = this.mockReadingType("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        MetrologyPurpose purpose = mock(MetrologyPurpose.class);
        Channel channel = mock(Channel.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        MeterRole role = mock(MeterRole.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        Formula formula = mock(Formula.class);
        ReadingTypeRequirementNode requirementNode = mock(ReadingTypeRequirementNode.class);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);

        when(usagePointMetrologyConfiguration.getId()).thenReturn(id);
        when(usagePointMetrologyConfiguration.getName()).thenReturn(name);
        when(usagePointMetrologyConfiguration.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePointMetrologyConfiguration.getVersion()).thenReturn(1L);
        when(usagePointMetrologyConfiguration.getDescription()).thenReturn("some description");
        when(usagePointMetrologyConfiguration.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        when(usagePointMetrologyConfiguration.getMeterRoles()).thenReturn(Collections.singletonList(role));
        when(usagePointMetrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));

        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));

        when(role.getKey()).thenReturn(DefaultMeterRole.DEFAULT.getKey());
        when(role.getDisplayName()).thenReturn(DefaultMeterRole.DEFAULT.getDefaultFormat());

        when(purpose.getId()).thenReturn(1L);
        when(purpose.getDescription()).thenReturn(DefaultMetrologyPurpose.BILLING.getDescription().getDefaultMessage());
        when(purpose.getName()).thenReturn(DefaultMetrologyPurpose.BILLING.getName().getDefaultMessage());

        when(deliverable.getId()).thenReturn(1L);
        when(deliverable.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        when(deliverable.getName()).thenReturn("testDeliveralble");
        when(deliverable.getReadingType()).thenReturn(readingType);
        when(deliverable.getFormula()).thenReturn(formula);

        when(formula.getDescription()).thenReturn("testDescription");
        when(formula.getExpressionNode()).thenReturn(requirementNode);

        when(requirement.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        when(requirement.getReadingType()).thenReturn(readingType);
        when(requirementNode.getReadingTypeRequirement()).thenReturn(requirement);

        when(metrologyContract.getMetrologyPurpose()).thenReturn(purpose);
        when(metrologyContract.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        when(metrologyContract.isMandatory()).thenReturn(true);
        when(metrologyContract.getId()).thenReturn(1L);

        when(channel.getMainReadingType()).thenReturn(readingType);
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(channel));

        IntervalReadingRecord intervalReadingRecord = mock(IntervalReadingRecord.class);
        ReadingQualityRecord quality = mock(ReadingQualityRecord.class);
        ValidationEvaluator evaluator = mock(ValidationEvaluator.class);
        DataValidationStatus validationStatus = mock(DataValidationStatus.class);
        ValidationRule validationRule = mock(ValidationRule.class);
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        ReadingQualityType qualityType = new ReadingQualityType("3.5.258");

        when(intervalReadingRecord.getTimePeriod())
                .thenReturn(Optional.of(Range.openClosed(Instant.ofEpochMilli(1468875600000L), Instant.ofEpochMilli(1468962000000L))));
        when(intervalReadingRecord.getTimeStamp()).thenReturn(Instant.ofEpochMilli((1468962000000L)));
        when(intervalReadingRecord.getValue()).thenReturn(BigDecimal.valueOf(10L));
        when(quality.getType()).thenReturn(qualityType);
        doReturn(Collections.singletonList(quality)).when(validationStatus).getReadingQualities();
        List<IntervalReadingRecord> intervalReadings = Collections.singletonList(intervalReadingRecord);
        when(channel.getIntervalReadings(any(Range.class))).thenReturn(intervalReadings);
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(validationStatus.getReadingTimestamp()).thenReturn(Instant.ofEpochMilli((1468962000000L)));
        when(validationStatus.completelyValidated()).thenReturn(true);
        when(validationStatus.getValidationResult()).thenReturn(ValidationResult.SUSPECT);
        when(validationStatus.getOffendedRules()).thenReturn(Collections.singletonList(validationRule));
        when(validationRule.getId()).thenReturn(1L);
        when(validationRule.getDisplayName()).thenReturn("testRule");
        when(validationRule.getRuleSet()).thenReturn(validationRuleSet);
        when(validationRule.getRuleSetVersion()).thenReturn(ruleSetVersion);
        when(ruleSetVersion.getRuleSet()).thenReturn(validationRuleSet);
        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC), channel, intervalReadings,
                Range.openClosed(Instant.ofEpochMilli(1468846440000L), Instant.ofEpochMilli(1500382440000L))))
                .thenReturn(Collections.singletonList(validationStatus));

        return usagePointMetrologyConfiguration;
    }

    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }
}