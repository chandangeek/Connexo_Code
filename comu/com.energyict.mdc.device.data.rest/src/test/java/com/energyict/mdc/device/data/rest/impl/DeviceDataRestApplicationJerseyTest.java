/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.rest.AuditInfoFactory;
import com.elster.jupiter.audit.rest.impl.AuditInfoFactoryImpl;
import com.elster.jupiter.bpm.BpmService;
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
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.rest.AliasSearchFilterFactory;
import com.elster.jupiter.pki.rest.SecurityAccessorResourceHelper;
import com.elster.jupiter.pki.rest.impl.AliasSearchFilterFactoryImpl;
import com.elster.jupiter.pki.rest.impl.SecurityAccessorResourceHelperImpl;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.impl.PropertyValueInfoServiceImpl;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.services.ObisCodeDescriptor;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskUserAction;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.configuration.rest.TrustStoreValuesProvider;
import com.energyict.mdc.device.configuration.rest.impl.SecurityAccessorInfoFactoryImpl;
import com.energyict.mdc.device.configuration.rest.impl.TrustStoreValuesProviderImpl;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CrlRequestService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.rest.DeviceStateAccessFeature;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.impl.MdcPropertyUtilsImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.SystemComTask;

import com.energyict.obis.ObisCode;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceDataRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    static long firmwareComTaskId = 445632136865L;
    static long firmwareComTaskExecutionId = 446532136865L;

    @Mock(extraInterfaces = ComTask.class)
    SystemComTask firmwareComTask;
    @Mock
    ConnectionTaskService connectionTaskService;
    @Mock
    DeviceService deviceService;
    @Mock
    TopologyService topologyService;
    @Mock
    MultiElementDeviceService multiElementDeviceService;
    @Mock
    BatchService batchService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    IssueService issueService;
    @Mock
    IssueDataValidationService issueDataValidationService;
    @Mock
    SchedulingService schedulingService;
    @Mock
    ValidationService validationService;
    @Mock
    EstimationService estimationService;
    @Mock
    Clock clock;
    @Mock
    MasterDataService masterDataService;
    @Mock
    JsonService jsonService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    MeteringGroupsService meteringGroupService;
    @Mock
    MeteringService meteringService;
    @Mock
    MeteringTranslationService meteringTranslationService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    TaskService taskService;
    @Mock
    CommunicationTaskService communicationTaskService;
    @Mock
    PriorityComTaskService priorityComTaskService;
    @Mock
    CommunicationTaskReportService communicationTaskReportService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    FavoritesService favoritesService;
    @Mock
    DataCollectionKpiService dataCollectionKpiService;
    @Mock
    YellowfinGroupsService yellowfinGroupsService;
    @Mock
    FirmwareService firmwareService;
    @Mock
    DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    DeviceLifeCycleService deviceLifeCycleService;
    @Mock
    AppService appService;
    @Mock
    MessageService messageService;
    @Mock
    LoadProfileService loadProfileService;
    @Mock
    SearchService searchService;
    @Mock
    DeviceMessageService deviceMessageService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    MdcReadingTypeUtilService mdcReadingTypeUtilService;
    @Mock
    ServiceCallService serviceCallService;
    @Mock
    BpmService bpmService;
    @Mock
    ServiceCallInfoFactory serviceCallInfoFactory;
    @Mock
    CalendarInfoFactory calendarInfoFactory;
    @Mock
    CalendarService calendarService;
    @Mock
    DeviceAlarmService deviceAlarmService;
    @Mock
    UserService userService;
    @Mock
    ObisCodeDescriptor obisCodeDescriptor;
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    protected RegisteredDevicesKpiService registeredDevicesKpiService;
    @Mock
    private volatile ThreadPrincipalService threadPrincipalService;
    @Mock
    LocationService locationService;
    @Mock
    static SecurityContext securityContext;
    @Mock
    IssueInfoFactoryService issueInfoFactoryService;
    @Mock
    OrmService ormService;
    @Mock
    DataModel dataModel;
    @Mock
    IssueActionService issueActionService;
    @Mock
    com.elster.jupiter.tasks.TaskService tskService;
    @Mock
    CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    @Mock
    CrlRequestService crlRequestService;
    @Mock
    HsmEnergyService hsmEnergyService;
    @Mock
    private CommandRuleService cmdRuleService;
    @Mock
    MeteringZoneService meteringZoneService;
    @Mock
    AuditService auditService;
    PropertyValueInfoService propertyValueInfoService;
    MdcPropertyUtils mdcPropertyUtils;
    SecurityAccessorResourceHelper securityAccessorResourceHelper;
    SecurityAccessorInfoFactory securityAccessorInfoFactory;
    TrustStoreValuesProvider trustStoreValuesProvider;
    AliasSearchFilterFactory aliasSearchFilterFactory;
    ChannelInfoFactory channelInfoFactory;
    ReadingTypeInfoFactory readingTypeInfoFactory;
    AuditInfoFactory auditInfoFactory;

    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

    @Before
    public void setup() {
        when(ormService.getDataModel(anyString())).thenReturn(Optional.empty());
        when(obisCodeDescriptor.describe(any(ObisCode.class))).thenReturn("obisCodeDescription");
        readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        channelInfoFactory = new ChannelInfoFactory(clock, topologyService, readingTypeInfoFactory);
        this.setupTranslations();
        when(taskService.findComTask(anyLong())).thenReturn(Optional.empty());
        when(taskService.findComTask(firmwareComTaskId)).thenReturn(Optional.of(firmwareComTask));
        when(firmwareComTask.isSystemComTask()).thenReturn(true);
        when(firmwareComTask.isUserComTask()).thenReturn(false);
        when(topologyService.availabilityDate(any(Channel.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(any(Register.class))).thenReturn(Optional.empty());
        when(topologyService.findDataloggerReference(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.findLastDataloggerReference(any(Device.class))).thenReturn(Optional.empty());
        when(multiElementDeviceService.findMultiElementDeviceReference(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        Finder<EndDeviceZone> endDeviceZoneFinder = mock(Finder.class);
        doReturn(Stream.of(new EndDeviceZone[]{})).when(endDeviceZoneFinder).stream();
        when(meteringZoneService.getByEndDevice(any(EndDevice.class))).thenReturn(endDeviceZoneFinder);
        EndDevice endDevice = mock(EndDevice.class);
        when(meteringService.findEndDeviceByMRID(any(String.class))).thenReturn(Optional.of(endDevice));
        when(firmwareService.findFirmwareManagementOptions(any())).thenReturn(Optional.empty());
    }

    protected void setupTranslations() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        doReturn(messageFormat).when(thesaurus).getFormat(any(MessageSeed.class));
        doReturn(messageFormat).when(thesaurus).getFormat(any(TranslationKey.class));
        doReturn(messageFormat).when(thesaurus).getSimpleFormat(any(MessageSeed.class));
    }

    protected boolean disableDeviceConstraintsBasedOnDeviceState() {
        return true;
    }

    @Override
    protected Application getApplication() {
        DeviceApplication application = new DeviceApplication() {
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<>(super.getClasses());
                if (disableDeviceConstraintsBasedOnDeviceState()) {
                    classes.remove(DeviceStateAccessFeature.class);
                }
                classes.add(SecurityRequestFilter.class);
                return classes;
            }
        };
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setMasterDataService(masterDataService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setJsonService(jsonService);
        application.setProtocolPluggableService(protocolPluggableService);
        application.setClockService(clock);
        application.setConnectionTaskService(connectionTaskService);
        application.setDeviceService(deviceService);
        application.setTopologyService(topologyService);
        application.setMultiElementDeviceService(multiElementDeviceService);
        application.setBatchService(batchService);
        application.setEngineConfigurationService(engineConfigurationService);
        application.setIssueService(issueService);
        application.setIssueDataValidationService(issueDataValidationService);
        application.setMeteringGroupsService(meteringGroupService);
        application.setMeteringService(meteringService);
        application.setSchedulingService(schedulingService);
        application.setValidationService(validationService);
        application.setEstimationService(estimationService);
        application.setRestQueryService(restQueryService);
        application.setTaskService(taskService);
        application.setCommunicationTaskService(communicationTaskService);
        application.setPriorityComTaskService(priorityComTaskService);
        application.setCommunicationTaskReportService(communicationTaskReportService);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setFavoritesService(favoritesService);
        application.setDataCollectionKpiService(dataCollectionKpiService);
        application.setYellowfinGroupsService(yellowfinGroupsService);
        application.setFirmwareService(firmwareService);
        application.setDeviceLifeCycleService(deviceLifeCycleService);
        application.setAppService(appService);
        application.setMessageService(messageService);
        application.setSearchService(searchService);
        application.setLoadProfileService(loadProfileService);
        application.setDeviceMessageService(deviceMessageService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setServiceCallService(serviceCallService);
        application.setServiceCallInfoFactory(serviceCallInfoFactory);
        application.setBpmService(bpmService);
        application.setThreadPrincipalService(threadPrincipalService);
        application.setCalendarInfoFactory(calendarInfoFactory);
        application.setCalendarService(calendarService);
        application.setLocationService(locationService);
        application.setMeteringTranslationService(meteringTranslationService);
        application.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        propertyValueInfoService = createPropertyValueInfoService();
        application.setPropertyValueInfoService(propertyValueInfoService);
        mdcPropertyUtils = new MdcPropertyUtilsImpl(propertyValueInfoService, meteringGroupService);
        application.setMdcPropertyUtils(mdcPropertyUtils);
        application.setDeviceAlarmService(deviceAlarmService);
        application.setUserService(userService);
        application.setObisCodeDescriptor(obisCodeDescriptor);
        application.setSecurityManagementService(securityManagementService);
        application.setIssueInfoFactoryService(issueInfoFactoryService);
        application.setOrmService(ormService);
        application.setRegisteredDevicesKpiService(registeredDevicesKpiService);
        application.setTskService(tskService);
        application.setCrlRequestTaskPropertiesService(crlRequestTaskPropertiesService);
        application.setCrlRequestService(crlRequestService);
        securityAccessorResourceHelper = new SecurityAccessorResourceHelperImpl(securityManagementService, propertyValueInfoService);;
        application.setSecurityAccessorResourceHelper(securityAccessorResourceHelper);
        securityAccessorInfoFactory = new SecurityAccessorInfoFactoryImpl(mdcPropertyUtils, securityManagementService);
        application.setSecurityAccessorInfoFactory(securityAccessorInfoFactory);
        trustStoreValuesProvider = new TrustStoreValuesProviderImpl(securityManagementService);
        application.setTrustStoreValuesProvider(trustStoreValuesProvider);
        aliasSearchFilterFactory = new AliasSearchFilterFactoryImpl(securityManagementService);
        application.setAliasSearchFilterFactory(aliasSearchFilterFactory);
        application.setCommandRuleService(cmdRuleService);
        application.setMeteringZoneService(meteringZoneService);
        application.setAuditService(auditService);
        auditInfoFactory = new AuditInfoFactoryImpl();
        application.setAuditInfoFactory(auditInfoFactory);
        return application;
    }

    public ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
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
        when(readingType.isCumulative()).thenReturn(true);
        return readingType;
    }

    AppServer mockAppServers(String... name) {
        AppServer appServer = mock(AppServer.class);
        List<SubscriberExecutionSpec> execSpecs = new ArrayList<>();
        for (String specName : name) {
            SubscriberExecutionSpec subscriberExecutionSpec = mock(SubscriberExecutionSpec.class);
            SubscriberSpec spec = mock(SubscriberSpec.class);
            when(subscriberExecutionSpec.getSubscriberSpec()).thenReturn(spec);
            when(subscriberExecutionSpec.isActive()).thenReturn(true);
            DestinationSpec destinationSpec = mock(DestinationSpec.class);
            when(spec.getDestination()).thenReturn(destinationSpec);
            when(destinationSpec.getName()).thenReturn(specName);
            when(destinationSpec.isActive()).thenReturn(true);
            List<SubscriberSpec> list = mock(List.class);
            when(list.isEmpty()).thenReturn(false);
            when(destinationSpec.getSubscribers()).thenReturn(list);
            execSpecs.add(subscriberExecutionSpec);
        }
        doReturn(execSpecs).when(appServer).getSubscriberExecutionSpecs();
        when(appServer.isActive()).thenReturn(true);
        when(appService.findAppServers()).thenReturn(Arrays.asList(appServer));
        return appServer;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenAnswer(invocation -> list.stream()); // Make sure to answer with a new stream each time
        return finder;
    }

    static PropertyValueInfoService createPropertyValueInfoService() {
        PropertyValueInfoServiceImpl propertyValueInfoService = new PropertyValueInfoServiceImpl();
        propertyValueInfoService.activate();
        return propertyValueInfoService;
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

    PropertySpec mockPropertySpec(String name, ValueFactory valueFactory) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        when(propertySpec.getDisplayName()).thenReturn(name);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        return propertySpec;
    }
    
    protected void preparePrivileges(ComTask comTask, User user) {
        Set<ComTaskUserAction> userActions = new HashSet<>();
        userActions.add(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_1);
        when(comTask.getUserActions()).thenReturn(userActions);
        Set<Privilege> privileges = new HashSet<>();
        Privilege privilege = mock(Privilege.class);
        when(privilege.getName()).thenReturn(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_1.getPrivilege());
        privileges.add(privilege);
        when(user.getPrivileges()).thenReturn(privileges);
    }


}
