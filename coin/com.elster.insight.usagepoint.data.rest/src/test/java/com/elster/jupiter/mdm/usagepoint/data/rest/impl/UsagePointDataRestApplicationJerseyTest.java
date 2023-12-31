/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.rest.AuditInfoFactory;
import com.elster.jupiter.audit.rest.impl.AuditInfoFactoryImpl;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
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
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointService;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoritesService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DeliverableType;
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
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.impl.ValidationServiceImpl;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    TimeService timeService;
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
    ValidationServiceImpl validationService;
    @Mock
    DataAggregationService dataAggregationService;
    @Mock
    static SecurityContext securityContext;
    @Mock
    UsagePointDataCompletionService usagePointDataCompletionService;
    @Mock
    FavoritesService favoritesService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    ServiceCallService serviceCallService;
    @Mock
    ServiceCallInfoFactory serviceCallInfoFactory;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    ReadingTypeDeliverableFactory readingTypeDeliverableFactory;
    @Mock
    LicenseService licenseService;
    @Mock
    IssueService issueService;
    @Mock
    BpmService bpmService;
    @Mock
    ThreadPrincipalService threadPrincipalService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory;
    @Mock
    CalendarService calendarService;
    @Mock
    CalendarInfoFactory calendarInfoFactory;
    @Mock
    JsonService jsonService;
    @Mock
    AppService appService;
    @Mock
    SearchService searchService;
    @Mock
    MessageService messageService;
    @Mock
    MeteringTranslationService meteringTranslationService;
    @Mock
    UsagePointLifeCycleService usagePointLifeCycleService;
    @Mock
    UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    @Mock
    UsagePointService usagePointService;
    @Mock
    TaskService taskService;
    @Mock
    UpgradeService upgradeService;
    @Mock
    OrmService ormService;
    ReadingType regularReadingType = mockReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    ReadingType irregularReadingType = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    @Mock
    UserService userService;
    @Mock
    AuditService auditService;

    AuditInfoFactory auditInfoFactory;

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
        application.setTimeService(timeService);
        application.setLocationService(locationService);
        application.setRestQueryService(restQueryService);
        application.setClockService(clock);
        application.setMeteringGroupService(meteringGroupsService);
        application.setUsagePointConfigurationService(usagePointConfigurationService);
        application.setEstimationService(estimationService);
        application.setDataAggregationService(dataAggregationService);
        application.setValidationService(validationService);
        application.setUsagePointDataCompletionService(usagePointDataCompletionService);
        application.setFavoritesService(favoritesService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setBpmService(bpmService);
        application.setIssueService(issueService);
        application.setServiceCallService(serviceCallService);
        application.setServiceCallInfoFactory(serviceCallInfoFactory);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        application.setReadingTypeDeliverableFactory(readingTypeDeliverableFactory);
        application.setThreadPrincipalService(threadPrincipalService);
        application.setLicenseService(licenseService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        application.setCalendarOnUsagePointInfoFactory(calendarOnUsagePointInfoFactory);
        application.setCalendarService(calendarService);
        application.setCalendarInfoFactory(calendarInfoFactory);
        application.setJsonService(jsonService);
        application.setAppService(appService);
        application.setSearchService(searchService);
        application.setMessageService(messageService);
        application.setMeteringTranslationService(meteringTranslationService);
        application.setUsagePointLifeCycleService(usagePointLifeCycleService);
        application.setUsagePointLifeCycleConfigurationService(usagePointLifeCycleConfigurationService);
        application.setPropertySpecService(propertySpecService);
        application.setUsagePointService(usagePointService);
        application.setUserService(userService);
        application.setAuditService(auditService);
        auditInfoFactory = new AuditInfoFactoryImpl();
        application.setAuditInfoFactory(auditInfoFactory);
        return application;
    }

    static ReadingType mockReadingType(String mrid) {
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
        when(readingType.getIntervalLength()).thenReturn(Optional.empty());
        when(readingType.getFullAliasName()).thenReturn("fullAliasName");
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

    UsagePointMetrologyConfiguration mockMetrologyConfigurationWithContract(long id, String name) {
        return mockMetrologyConfigurationWithContract(id, name, mockMetrologyPurpose(DefaultMetrologyPurpose.BILLING),
                mockMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION));
    }

    UsagePointMetrologyConfiguration mockMetrologyConfigurationWithContract(long id, String name, MetrologyPurpose mandatory, MetrologyPurpose optional) {
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(id);
        when(metrologyConfiguration.getName()).thenReturn(name);
        MeterRole meterRole = mockMeterRole(DefaultMeterRole.DEFAULT);
        when(metrologyConfiguration.getMeterRoles()).thenReturn(Collections.singletonList(meterRole));

        MetrologyContract contract = mockMetrologyContract(100L, mandatory, metrologyConfiguration);
        MetrologyContract contractOptional = mockMetrologyContract(101L, optional, metrologyConfiguration);

        when(contractOptional.isMandatory()).thenReturn(false);

        when(metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(contract, contractOptional));

        when(regularReadingType.isRegular()).thenReturn(true);
        ReadingTypeDeliverable channelDeliverable = mockReadingTypeDeliverable(1L, "1 regular RT", metrologyConfiguration, regularReadingType);

        when(irregularReadingType.isRegular()).thenReturn(false);
        ReadingTypeDeliverable registerTypeDeliverable = mockReadingTypeDeliverable(2L, "2 irregular RT", metrologyConfiguration, irregularReadingType);

        when(contract.getDeliverables()).thenReturn(Arrays.asList(channelDeliverable, registerTypeDeliverable));
        when(contractOptional.getDeliverables()).thenReturn(Arrays.asList(channelDeliverable, registerTypeDeliverable));

        return metrologyConfiguration;
    }

    private MeterRole mockMeterRole(DefaultMeterRole defaultReterRole) {
        MeterRole meterRole = mock(MeterRole.class);
        when(meterRole.getKey()).thenReturn(defaultReterRole.getKey());
        when(meterRole.getDisplayName()).thenReturn(defaultReterRole.getDefaultFormat());
        return meterRole;
    }

    private MetrologyContract mockMetrologyContract(long id, MetrologyPurpose metrologyPurpose, UsagePointMetrologyConfiguration metrologyConfiguration) {
        MetrologyContract contract = mock(MetrologyContract.class);
        when(contract.getId()).thenReturn(id);
        when(contract.getVersion()).thenReturn(1L);
        when(contract.isMandatory()).thenReturn(true);
        when(contract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(contract.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        return contract;
    }

    MetrologyPurpose mockMetrologyPurpose(DefaultMetrologyPurpose metrologyPurpose) {
        MetrologyPurpose purpose = mock(MetrologyPurpose.class);
        when(purpose.getId()).thenReturn(1L);
        when(purpose.getName()).thenReturn(metrologyPurpose.getName().getDefaultMessage());
        when(purpose.getDescription()).thenReturn(metrologyPurpose.getDescription().getDefaultMessage());
        return purpose;
    }

    private ReadingTypeDeliverable mockReadingTypeDeliverable(long id, String name, UsagePointMetrologyConfiguration metrologyConfiguration, ReadingType readingType) {
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getId()).thenReturn(id);
        when(deliverable.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(deliverable.getName()).thenReturn(name);
        when(deliverable.getReadingType()).thenReturn(readingType);
        Formula formula = mockFormula(readingType, metrologyConfiguration);
        when(deliverable.getFormula()).thenReturn(formula);
        when(deliverable.getType()).thenReturn(DeliverableType.NUMERICAL);
        return deliverable;
    }

    private Formula mockFormula(ReadingType readingType, UsagePointMetrologyConfiguration metrologyConfiguration) {
        Formula formula = mock(Formula.class);
        ReadingTypeRequirementNode requirementNode = mock(ReadingTypeRequirementNode.class);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(requirement.getReadingType()).thenReturn(readingType);
        when(requirementNode.getReadingTypeRequirement()).thenReturn(requirement);
        when(formula.getExpressionNode()).thenReturn(requirementNode);
        return formula;
    }

    void mockPropertyValueInfoService() {
        when(propertyValueInfoService.findPropertyValue(any(), any())).thenAnswer(invocationOnMock -> {
            Object[] arguments = invocationOnMock.getArguments();
            PropertySpec propertySpec = (PropertySpec) arguments[0];
            List<PropertyInfo> infos = (List<PropertyInfo>) arguments[1];
            return infos.stream()
                    .filter(info -> info.key.equals(propertySpec.getName()))
                    .map(info -> info.propertyValueInfo.value)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(null);
        });
        when(propertyValueInfoService.getPropertyInfos(any(), any(), any())).thenAnswer(invocationOnMock -> {
            Object[] arguments = invocationOnMock.getArguments();
            List<PropertySpec> propertySpecs = (List<PropertySpec>) arguments[0];
            Map<String, Object> actualProps = (Map<String, Object>) arguments[1];
            Map<String, Object> inheritedProps = (Map<String, Object>) arguments[2];
            return getPropertyInfo(propertySpecs, actualProps, inheritedProps);
        });
        when(propertyValueInfoService.getPropertyInfos(any(), any())).thenAnswer(invocationOnMock -> {
            Object[] arguments = invocationOnMock.getArguments();
            List<PropertySpec> propertySpecs = (List<PropertySpec>) arguments[0];
            Map<String, Object> actualProps = (Map<String, Object>) arguments[1];
            return getPropertyInfo(propertySpecs, actualProps, Collections.emptyMap());
        });
    }

    private Object getPropertyInfo(List<PropertySpec> propertySpecs, Map<String, Object> actualProps, Map<String, Object> inheritedProps) {
        return propertySpecs.stream()
                .map(propertySpec -> {
                    PropertyInfo info = new PropertyInfo();
                    info.key = propertySpec.getName();
                    info.propertyValueInfo = new PropertyValueInfo<>(actualProps.get(info.key), inheritedProps.get(info.key), null, null);
                    return info;
                })
                .collect(Collectors.toList());
    }

    PropertySpec mockPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        return propertySpec;
    }

    public Calendar mockCalendar(){
        return mock(Calendar.class);
    }

}