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
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
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
import com.elster.jupiter.validation.ValidationService;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointDataRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {
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

    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

    public UsagePointMetrologyConfiguration mockMetrologyConfigurationWithContract(long id, String name) {
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(id);
        when(metrologyConfiguration.getName()).thenReturn(name);
        MeterRole meterRole = mockMeterRole(DefaultMeterRole.DEFAULT);
        when(metrologyConfiguration.getMeterRoles()).thenReturn(Collections.singletonList(meterRole));

        MetrologyContract contract = mockMetrologyContract(1L, DefaultMetrologyPurpose.BILLING, metrologyConfiguration);

        ReadingType regularReadingType = this.mockReadingType("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        when(regularReadingType.isRegular()).thenReturn(true);
        ReadingTypeDeliverable channelDeliverable = mockReadingTypeDeliverable(1L, "regular RT", metrologyConfiguration, regularReadingType);

        ReadingType irregularReadingType = this.mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        when(irregularReadingType.isRegular()).thenReturn(false);
        ReadingTypeDeliverable registerTypeDeliverable = mockReadingTypeDeliverable(2L, "irregular RT", metrologyConfiguration, irregularReadingType);

        when(contract.getDeliverables()).thenReturn(Arrays.asList(channelDeliverable, registerTypeDeliverable));
        return metrologyConfiguration;
    }

    private MeterRole mockMeterRole(DefaultMeterRole defaultReterRole) {
        MeterRole meterRole = mock(MeterRole.class);
        when(meterRole.getKey()).thenReturn(defaultReterRole.getKey());
        when(meterRole.getDisplayName()).thenReturn(defaultReterRole.getDefaultFormat());
        return meterRole;
    }

    private MetrologyContract mockMetrologyContract(long id, DefaultMetrologyPurpose metrologyPurpose, UsagePointMetrologyConfiguration metrologyConfiguration) {
        MetrologyContract contract = mock(MetrologyContract.class);
        when(contract.getId()).thenReturn(id);
        when(contract.isMandatory()).thenReturn(true);
        MetrologyPurpose purpose = mockMetrologyPurpose(metrologyPurpose);
        when(contract.getMetrologyPurpose()).thenReturn(purpose);
        when(contract.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        MetrologyContract.Status status = mockMetrologyContractStatus();
        when(contract.getStatus(any())).thenReturn(status);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(contract));
        return contract;
    }

    private MetrologyPurpose mockMetrologyPurpose(DefaultMetrologyPurpose metrologyPurpose) {
        MetrologyPurpose purpose = mock(MetrologyPurpose.class);
        when(purpose.getId()).thenReturn(1L);
        when(purpose.getName()).thenReturn(metrologyPurpose.getName().getDefaultMessage());
        when(purpose.getDescription()).thenReturn(metrologyPurpose.getDescription().getDefaultMessage());
        return purpose;
    }

    private MetrologyContract.Status mockMetrologyContractStatus() {
        MetrologyContract.Status status = mock(MetrologyContract.Status.class);
        when(status.getKey()).thenReturn("INCOMPLETE");
        when(status.getName()).thenReturn("incomplete");
        return status;
    }

    private ReadingTypeDeliverable mockReadingTypeDeliverable(long id, String name, UsagePointMetrologyConfiguration metrologyConfiguration, ReadingType readingType) {
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getId()).thenReturn(id);
        when(deliverable.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(deliverable.getName()).thenReturn(name);
        when(deliverable.getReadingType()).thenReturn(readingType);
        Formula formula = mockFormula(readingType, metrologyConfiguration);
        when(deliverable.getFormula()).thenReturn(formula);
        return deliverable;
    }

    private Formula mockFormula(ReadingType readingType, UsagePointMetrologyConfiguration metrologyConfiguration) {
        Formula formula = mock(Formula.class);
        when(formula.getDescription()).thenReturn("Formula Description");
        ReadingTypeRequirementNode requirementNode = mock(ReadingTypeRequirementNode.class);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(requirement.getReadingType()).thenReturn(readingType);
        when(requirementNode.getReadingTypeRequirement()).thenReturn(requirement);
        when(formula.getExpressionNode()).thenReturn(requirementNode);
        return formula;
    }
}