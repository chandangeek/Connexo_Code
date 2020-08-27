/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.rest.AppServerHelper;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.rest.AuditI18N;
import com.elster.jupiter.audit.rest.AuditInfoFactory;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cbo.I18N;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.rest.AliasSearchFilterFactory;
import com.elster.jupiter.pki.rest.SecurityAccessorResourceHelper;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.common.device.data.KeyAccessorStatus;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.services.ObisCodeDescriptor;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfoFactory;
import com.energyict.mdc.device.configuration.rest.TrustStoreValuesProvider;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CrlRequestService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskPropertyInfo;
import com.energyict.mdc.device.data.crlrequest.rest.impl.CrlRequestTaskInfoFactory;
import com.energyict.mdc.device.data.crlrequest.rest.impl.CrlRequestTaskResource;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.kpi.rest.DataCollectionKpiInfoFactory;
import com.energyict.mdc.device.data.kpi.rest.KpiResource;
import com.energyict.mdc.device.data.kpi.rest.RegisteredDevicesKpiInfoFactory;
import com.energyict.mdc.device.data.kpi.rest.RegisteredDevicesKpiResource;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.device.data.rest.DeviceStateAccessFeature;
import com.energyict.mdc.device.data.rest.ReadingQualitiesTranslationKeys;
import com.energyict.mdc.device.data.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.data.rest.SecurityPropertySetInfoFactory;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateFactory;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.ddr.rest", service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true, property = {"alias=/ddr", "app=MDC", "name=" + DeviceApplication.COMPONENT_NAME})
public class DeviceApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DDR";

    private volatile MasterDataService masterDataService;

    private volatile ObisCodeDescriptor obisCodeDescriptor;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile MultiElementDeviceService multiElementDeviceService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile BatchService batchService;
    private volatile IssueService issueService;
    private volatile IssueDataValidationService issueDataValidationService;
    private volatile TransactionService transactionService;
    private volatile YellowfinGroupsService yellowfinGroupsService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile SchedulingService schedulingService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile MeteringService meteringService;
    private volatile MeteringZoneService meteringZoneService;
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile LocationService locationService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile RestQueryService restQueryService;
    private volatile TaskService taskService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile Clock clock;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile PriorityComTaskService priorityComTaskService;
    private volatile CommunicationTaskReportService communicationTaskReportService;
    private volatile FavoritesService favoritesService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile License license;
    private volatile FirmwareService firmwareService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile AppService appService;
    private volatile AppServerHelper appServerHelper;
    private volatile MessageService messageService;
    private volatile SearchService searchService;
    private volatile LoadProfileService loadProfileService;
    private volatile DeviceMessageService deviceMessageService;
    private volatile DevicesForConfigChangeSearchFactory devicesForConfigChangeSearchFactory;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile ServiceCallService serviceCallService;
    private volatile BpmService bpmService;
    private volatile ServiceCallInfoFactory serviceCallInfoFactory;
    private volatile CalendarInfoFactory calendarInfoFactory;
    private volatile CalendarService calendarService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile UserService userService;
    private volatile SecurityManagementService securityManagementService;
    private volatile MdcPropertyUtils mdcPropertyUtils;
    private volatile IssueActionService issueActionService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile IssueInfoFactoryService issueInfoFactoryService;
    private volatile OrmService ormService;
    private volatile com.elster.jupiter.tasks.TaskService tskService;
    private volatile CommandRuleService commandRuleService;
    private volatile AuditService auditService;


    private volatile SecurityAccessorResourceHelper securityAccessorResourceHelper;
    private volatile com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory securityAccessorInfoFactory;
    private volatile com.elster.jupiter.audit.rest.AuditInfoFactory auditInfoFactory;
    private volatile TrustStoreValuesProvider trustStoreValuesProvider;
    private volatile AliasSearchFilterFactory aliasSearchFilterFactory;
    private volatile RegisteredDevicesKpiService registeredDevicesKpiService;

    private volatile CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    private volatile CrlRequestService crlRequestService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                RestValidationExceptionMapper.class,
                ExceptionLogger.class,
                DeviceResource.class,
                ProtocolDialectResource.class,
                RegisterResource.class,
                RegisterDataResource.class,
                RegisterHistoryDataResource.class,
                DeviceValidationResource.class,
                LoadProfileResource.class,
                BulkLoadProfileResource.class,
                BulkScheduleResource.class,
                BulkZoneResource.class,
                DeviceScheduleResource.class,
                DeviceSharedScheduleResource.class,
                DeviceComTaskResource.class,
                LogBookResource.class,
                DeviceFieldResource.class,
                ChannelResource.class,
                DeviceGroupResource.class,
                SecurityPropertySetResource.class,
                SecurityAccessorResource.class,
                ConnectionMethodResource.class,
                ComSessionResource.class,
                DeviceMessageResource.class,
                DeviceLabelResource.class,
                ConnectionResource.class,
                DeviceProtocolPropertyResource.class,
                KpiResource.class,
                RegisteredDevicesKpiResource.class,
                AdhocGroupResource.class,
                DeviceEstimationResource.class,
                DeviceHistoryResource.class,
                DeviceLifeCycleActionResource.class,
                DeviceStateAccessFeature.class,
                EstimationErrorExceptionMapper.class,
                EstimatorPropertiesExceptionMapper.class,
                ChannelValidationResource.class,
                ChannelEstimationResource.class,
                DeviceMessageSearchResource.class,
                CrlRequestTaskResource.class,
                DeviceZoneResource.class,
                ProcessResource.class,
                AppServerHelper.class,
                AuditService.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setCommandRuleService(CommandRuleService commandRuleService){
        this.commandRuleService = commandRuleService;
    }

    @Reference
    public void setRegisteredDevicesKpiService(RegisteredDevicesKpiService registeredDevicesKpiService) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
    }

    @Reference
    public void setObisCodeDescriptor(ObisCodeDescriptor obisCodeDescriptor) {
        this.obisCodeDescriptor = obisCodeDescriptor;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setMultiElementDeviceService(MultiElementDeviceService multiElementDeviceService) {
        this.multiElementDeviceService = multiElementDeviceService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
        this.issueCreationService = issueService.getIssueCreationService();
        this.issueAssignmentService = issueService.getIssueAssignmentService();
    }

    @Reference
    public void setIssueDataValidationService(IssueDataValidationService issueDataValidationService) {
        this.issueDataValidationService = issueDataValidationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setYellowfinGroupsService(YellowfinGroupsService yellowfinGroupsService) {
        this.yellowfinGroupsService = yellowfinGroupsService;
    }

    @Reference
    public void setIssueInfoFactoryService(IssueInfoFactoryService issueInfoFactoryService) {
        this.issueInfoFactoryService = issueInfoFactoryService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(AuditI18N.COMPONENT_NAME, Layer.REST))
                .join(nlsService.getThesaurus(I18N.COMPONENT_NAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(DeviceMessageSpecificationService.COMPONENT_NAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(TopologyService.COMPONENT_NAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(SecurityManagementService.COMPONENTNAME, Layer.DOMAIN));
    }

    @Reference
    public void setLoadProfileService(LoadProfileService loadProfileService) {
        this.loadProfileService = loadProfileService;
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setServiceCallInfoFactory(ServiceCallInfoFactory serviceCallInfoFactory) {
        this.serviceCallInfoFactory = serviceCallInfoFactory;
    }

    @Reference
    public void setCalendarInfoFactory(CalendarInfoFactory calendarInfoFactory) {
        this.calendarInfoFactory = calendarInfoFactory;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setMdcPropertyUtils(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @Reference
    public void setSecurityAccessorResourceHelper(SecurityAccessorResourceHelper securityAccessorResourceHelper) {
        this.securityAccessorResourceHelper = securityAccessorResourceHelper;
    }

    @Reference
    public void setSecurityAccessorInfoFactory(com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory securityAccessorInfoFactory) {
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
    }

    @Reference
    public void setAuditInfoFactory(AuditInfoFactory auditInfoFactory) {
        this.auditInfoFactory = auditInfoFactory;
    }

    @Reference
    public void setTrustStoreValuesProvider(TrustStoreValuesProvider trustStoreValuesProvider) {
        this.trustStoreValuesProvider = trustStoreValuesProvider;
    }

    @Reference
    public void setAliasSearchFilterFactory(AliasSearchFilterFactory aliasSearchFilterFactory) {
        this.aliasSearchFilterFactory = aliasSearchFilterFactory;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        Set<String> uniqueIds = new HashSet<>();
        List<TranslationKey> keys = new ArrayList<>();
        for (EndDeviceType type : EndDeviceType.values()) {
            if (uniqueIds.add(type.toString())) {
                keys.add(new SimpleTranslationKey(type.toString(), type.getMnemonic()));
            }
        }
        for (EndDeviceDomain domain : EndDeviceDomain.values()) {
            if (uniqueIds.add(domain.toString())) {
                keys.add(new SimpleTranslationKey(domain.toString(), domain.getMnemonic()));
            }
        }
        for (EndDeviceSubDomain subDomain : EndDeviceSubDomain.values()) {
            if (uniqueIds.add(subDomain.toString())) {
                keys.add(new SimpleTranslationKey(subDomain.toString(), subDomain.getMnemonic()));
            }
        }
        for (EndDeviceEventOrAction eventOrAction : EndDeviceEventOrAction.values()) {
            if (uniqueIds.add(eventOrAction.toString())) {
                keys.add(new SimpleTranslationKey(eventOrAction.toString(), eventOrAction.getMnemonic()));
            }
        }
        for (FirmwareType firmwareType : FirmwareType.values()) {
            if (uniqueIds.add(firmwareType.getType())) {
                keys.add(new SimpleTranslationKey(firmwareType.getType(), firmwareType.getDescription()));
            }
        }
        keys.addAll(Arrays.asList(DefaultTranslationKey.values()));
        keys.addAll(Arrays.asList(TaskStatusTranslationKeys.values()));
        keys.addAll(Arrays.asList(ConnectionTaskSuccessIndicatorTranslationKeys.values()));
        keys.addAll(Arrays.asList(ComSessionSuccessIndicatorTranslationKeys.values()));
        keys.addAll(Arrays.asList(CompletionCodeTranslationKeys.values()));
        keys.addAll(Arrays.asList(DeviceMessageStatusTranslationKeys.values()));
        keys.addAll(Arrays.asList(ReadingQualitiesTranslationKeys.values()));
        keys.addAll(Arrays.asList(ConnectionStrategyTranslationKeys.values()));
        keys.addAll(Arrays.asList(DeviceSearchModelTranslationKeys.values()));
        keys.addAll(Arrays.asList(LocationTranslationKeys.values()));
        keys.addAll(Arrays.asList(KeyAccessorStatus.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringZoneService(MeteringZoneService meteringZoneService) {
        this.meteringZoneService = meteringZoneService;
    }

    @Reference
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Reference
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setClockService(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTskService(com.elster.jupiter.tasks.TaskService tskService) {
        this.tskService = tskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setPriorityComTaskService(PriorityComTaskService priorityComTaskService) {
        this.priorityComTaskService= priorityComTaskService;
    }

    @Reference
    public void setCommunicationTaskReportService(CommunicationTaskReportService communicationTaskReportService) {
        this.communicationTaskReportService = communicationTaskReportService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setFavoritesService(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Reference
    public void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setCrlRequestTaskPropertiesService(CrlRequestTaskPropertiesService crlRequestTaskPropertiesService) {
        this.crlRequestTaskPropertiesService = crlRequestTaskPropertiesService;
    }

    @Reference
    public void setCrlRequestService(CrlRequestService crlRequestService) {
        this.crlRequestService = crlRequestService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(obisCodeDescriptor).to(ObisCodeDescriptor.class);
            bind(masterDataService).to(MasterDataService.class);
            bind(connectionTaskService).to(ConnectionTaskService.class);
            bind(deviceService).to(DeviceService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(transactionService).to(TransactionService.class);
            bind(issueService).to(IssueService.class);
            bind(issueDataValidationService).to(IssueDataValidationService.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ChannelResourceHelper.class).to(ChannelResourceHelper.class);
            bind(EstimationHelper.class).to(EstimationHelper.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ConnectionMethodInfoFactory.class).to(ConnectionMethodInfoFactory.class);
            bind(mdcPropertyUtils).to(MdcPropertyUtils.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(batchService).to(BatchService.class);
            bind(engineConfigurationService).to(EngineConfigurationService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(schedulingService).to(SchedulingService.class);
            bind(validationService).to(ValidationService.class);
            bind(estimationService).to(EstimationService.class);
            bind(meteringService).to(MeteringService.class);
            bind(meteringZoneService).to(MeteringZoneService.class);
            bind(meteringTranslationService).to(MeteringTranslationService.class);
            bind(locationService).to(LocationService.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(yellowfinGroupsService).to(YellowfinGroupsService.class);
            bind(clock).to(Clock.class);
            bind(DeviceComTaskInfoFactory.class).to(DeviceComTaskInfoFactory.class);
            bind(SecurityPropertySetInfoFactory.class).to(SecurityPropertySetInfoFactory.class);
            bind(SecurityAccessorInfoFactory.class).to(SecurityAccessorInfoFactory.class);
            bind(ChannelResource.class).to(ChannelResource.class);
            bind(ValidationInfoHelper.class).to(ValidationInfoHelper.class);
            bind(ComSessionInfoFactory.class).to(ComSessionInfoFactory.class);
            bind(ComTaskExecutionSessionInfoFactory.class).to(ComTaskExecutionSessionInfoFactory.class);
            bind(JournalEntryInfoFactory.class).to(JournalEntryInfoFactory.class);
            bind(DeviceMessageInfoFactory.class).to(DeviceMessageInfoFactory.class);
            bind(deviceMessageSpecificationService).to(DeviceMessageSpecificationService.class);
            bind(DeviceMessageCategoryInfoFactory.class).to(DeviceMessageCategoryInfoFactory.class);
            bind(DeviceMessageSpecInfoFactory.class).to(DeviceMessageSpecInfoFactory.class);
            bind(taskService).to(TaskService.class);
            bind(tskService).to(com.elster.jupiter.tasks.TaskService.class);
            bind(communicationTaskService).to(CommunicationTaskService.class);
            bind(priorityComTaskService).to(PriorityComTaskService.class);
            bind(favoritesService).to(FavoritesService.class);
            bind(topologyService).to(TopologyService.class);
            bind(multiElementDeviceService).to(MultiElementDeviceService.class);
            bind(DeviceConnectionTaskInfoFactory.class).to(DeviceConnectionTaskInfoFactory.class);
            bind(DeviceComTaskExecutionInfoFactory.class).to(DeviceComTaskExecutionInfoFactory.class);
            bind(DataCollectionKpiInfoFactory.class).to(DataCollectionKpiInfoFactory.class);
            bind(RegisteredDevicesKpiInfoFactory.class).to(RegisteredDevicesKpiInfoFactory.class);
            bind(DeviceGroupInfoFactory.class).to(DeviceGroupInfoFactory.class);
            bind(dataCollectionKpiService).to(DataCollectionKpiService.class);
            bind(firmwareService).to(FirmwareService.class);
            bind(DeviceLifeCycleStateFactory.class).to(DeviceLifeCycleStateFactory.class);
            bind(DeviceInfoFactory.class).to(DeviceInfoFactory.class);
            bind(DeviceLifeCycleHistoryInfoFactory.class).to(DeviceLifeCycleHistoryInfoFactory.class);
            bind(DeviceFirmwareHistoryInfoFactory.class).to(DeviceFirmwareHistoryInfoFactory.class);
            bind(ValidationInfoFactory.class).to(ValidationInfoFactory.class);
            bind(ValidationRuleInfoFactory.class).to(ValidationRuleInfoFactory.class);
            bind(DeviceDataInfoFactory.class).to(DeviceDataInfoFactory.class);
            bind(deviceLifeCycleService).to(DeviceLifeCycleService.class);
            bind(DeviceLifeCycleActionInfoFactory.class).to(DeviceLifeCycleActionInfoFactory.class);
            bind(EstimationRuleInfoFactory.class).to(EstimationRuleInfoFactory.class);
            bind(DeviceAttributesInfoFactory.class).to(DeviceAttributesInfoFactory.class);
            bind(LocationInfoFactory.class).to(LocationInfoFactory.class);
            bind(AppServerHelper.class).to(AppServerHelper.class);
            bind(appService).to(AppService.class);
            bind(messageService).to(MessageService.class);
            bind(loadProfileService).to(LoadProfileService.class);
            bind(searchService).to(SearchService.class);
            bind(deviceMessageService).to(DeviceMessageService.class);
            bind(DevicesForConfigChangeSearchFactory.class).to(DevicesForConfigChangeSearchFactory.class);
            bind(customPropertySetService).to(CustomPropertySetService.class);
            bind(serviceCallService).to(ServiceCallService.class);
            bind(DataLoggerSlaveDeviceInfoFactory.class).to(DataLoggerSlaveDeviceInfoFactory.class);
            bind(bpmService).to(BpmService.class);
            bind(GoingOnResource.class).to(GoingOnResource.class);
            bind(serviceCallInfoFactory).to(ServiceCallInfoFactory.class);
            bind(threadPrincipalService).to(ThreadPrincipalService.class);
            bind(calendarInfoFactory).to(CalendarInfoFactory.class);
            bind(calendarService).to(CalendarService.class);
            bind(deviceAlarmService).to(DeviceAlarmService.class);
            bind(userService).to(UserService.class);
            bind(securityManagementService).to(SecurityManagementService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(TimeOfUseInfoFactory.class).to(TimeOfUseInfoFactory.class);
            bind(MeterActivationInfoFactory.class).to(MeterActivationInfoFactory.class);
            bind(deviceLifeCycleConfigurationService).to(DeviceLifeCycleConfigurationService.class);
            bind(ormService).to(OrmService.class);
            bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
            bind(ChannelInfoFactory.class).to(ChannelInfoFactory.class);
            bind(ReadingQualityInfoFactory.class).to(ReadingQualityInfoFactory.class);
            bind(ChannelValidationRuleInfoFactory.class).to(ChannelValidationRuleInfoFactory.class);
            bind(ChannelEstimationRuleInfoFactory.class).to(ChannelEstimationRuleInfoFactory.class);
            bind(SecurityAccessorPlaceHolder.class).to(SecurityAccessorPlaceHolder.class);
            bind(ExecutionLevelInfoFactory.class).to(ExecutionLevelInfoFactory.class);
            bind(ChannelReferenceDataCopier.class).to(ChannelReferenceDataCopier.class);
            bind(CommandInfoFactory.class).to(CommandInfoFactory.class);
            bind(registeredDevicesKpiService).to(RegisteredDevicesKpiService.class);
            bind(IssueResourceHelper.class).to(IssueResourceHelper.class);
            bind(issueActionService).to(IssueActionService.class);
            bind(issueInfoFactoryService).to(IssueInfoFactoryService.class);
            bind(IssueActionInfoFactory.class).to(IssueActionInfoFactory.class);
            bind(securityAccessorResourceHelper).to(SecurityAccessorResourceHelper.class);
            bind(securityAccessorInfoFactory).to(com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory.class);
            bind(trustStoreValuesProvider).to(TrustStoreValuesProvider.class);
            bind(aliasSearchFilterFactory).to(AliasSearchFilterFactory.class);
            bind(commandRuleService).to(CommandRuleService.class);
            bind(CrlRequestTaskInfoFactory.class).to(CrlRequestTaskInfoFactory.class);
            bind(crlRequestTaskPropertiesService).to(CrlRequestTaskPropertiesService.class);
            bind(CrlRequestTaskPropertyInfo.class).to(CrlRequestTaskPropertyInfo.class);
            bind(crlRequestService).to(CrlRequestService.class);
            bind(EndDeviceZoneInfoFactory.class).to(EndDeviceZoneInfoFactory.class);
            bind(DeviceZoneResource.class).to(DeviceZoneResource.class);
            bind(auditService).to(AuditService.class);
            bind(AuditInfoFactory.class).to(AuditInfoFactory.class);
            bind(auditInfoFactory).to(AuditInfoFactory.class);
            bind(ComTaskExecutionPrivilegeCheck.class).to(ComTaskExecutionPrivilegeCheck.class);
            bind(CustomPropertySetInfoFactory.class).to(CustomPropertySetInfoFactory.class);
            bind(DataLoggerInfoFactory.class).to(DataLoggerInfoFactory.class);
            bind(MultiElementInfoFactory.class).to(MultiElementInfoFactory.class);

        }
    }
}