package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.kpi.rest.DataCollectionKpiInfoFactory;
import com.energyict.mdc.device.data.kpi.rest.KpiResource;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfoFactory;
import com.energyict.mdc.device.data.rest.DeviceInfoFactory;
import com.energyict.mdc.device.data.rest.SecurityPropertySetInfoFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateFactory;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.firmware.FirmwareService;
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
import java.util.*;
import java.util.logging.Logger;

@Component(name = "com.energyict.ddr.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/ddr", "app=MDC", "name=" + DeviceApplication.COMPONENT_NAME})
public class DeviceApplication extends Application implements TranslationKeyProvider {

    private final Logger logger = Logger.getLogger(DeviceApplication.class.getName());

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DDR";

    private volatile MasterDataService masterDataService;

    private volatile ConnectionTaskService connectionTaskService;
    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceImportService deviceImportService;
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
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile RestQueryService restQueryService;
    private volatile TaskService taskService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile Clock clock;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile FavoritesService favoritesService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile License license;
    private volatile FirmwareService firmwareService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                ExceptionLogger.class,
                DeviceResource.class,
                ProtocolDialectResource.class,
                RegisterResource.class,
                RegisterDataResource.class,
                DeviceValidationResource.class,
                LoadProfileResource.class,
                BulkScheduleResource.class,
                DeviceScheduleResource.class,
                DeviceComTaskResource.class,
                LogBookResource.class,
                DeviceFieldResource.class,
                ChannelResource.class,
                ChannelResource.class,
                DeviceGroupResource.class,
                SecurityPropertySetResource.class,
                ConnectionMethodResource.class,
                ComSessionResource.class,
                DeviceMessageResource.class,
                DeviceLabelResource.class,
                ConnectionResource.class,
                DeviceProtocolPropertyResource.class,
                KpiResource.class,
                AdhocGroupResource.class,
                DeviceEstimationResource.class,
                DeviceHistoryResource.class
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
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setDeviceImportService(DeviceImportService deviceImportService) {
        this.deviceImportService = deviceImportService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
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
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
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
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
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
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            if (uniqueIds.add(messageSeed.getKey())) {
                keys.add(messageSeed);
            }
        }

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
        for (EndDeviceEventorAction eventOrAction : EndDeviceEventorAction.values()) {
            if (uniqueIds.add(eventOrAction.toString())) {
                keys.add(new SimpleTranslationKey(eventOrAction.toString(), eventOrAction.getMnemonic()));
            }
        }
        return keys;
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
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
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

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
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
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ConnectionMethodInfoFactory.class).to(ConnectionMethodInfoFactory.class);
            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(deviceImportService).to(DeviceImportService.class);
            bind(engineConfigurationService).to(EngineConfigurationService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(schedulingService).to(SchedulingService.class);
            bind(validationService).to(ValidationService.class);
            bind(estimationService).to(EstimationService.class);
            bind(meteringService).to(MeteringService.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(yellowfinGroupsService).to(YellowfinGroupsService.class);
            bind(clock).to(Clock.class);
            bind(DeviceComTaskInfoFactory.class).to(DeviceComTaskInfoFactory.class);
            bind(SecurityPropertySetInfoFactory.class).to(SecurityPropertySetInfoFactory.class);
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
            bind(communicationTaskService).to(CommunicationTaskService.class);
            bind(favoritesService).to(FavoritesService.class);
            bind(topologyService).to(TopologyService.class);
            bind(DeviceConnectionTaskInfoFactory.class).to(DeviceConnectionTaskInfoFactory.class);
            bind(DeviceComTaskExecutionInfoFactory.class).to(DeviceComTaskExecutionInfoFactory.class);
            bind(DataCollectionKpiInfoFactory.class).to(DataCollectionKpiInfoFactory.class);
            bind(DeviceGroupInfoFactory.class).to(DeviceGroupInfoFactory.class);
            bind(dataCollectionKpiService).to(DataCollectionKpiService.class);
            bind(firmwareService).to(FirmwareService.class);
            bind(DeviceLifeCycleStateFactory.class).to(DeviceLifeCycleStateFactory.class);
            bind(DeviceInfoFactory.class).to(DeviceInfoFactory.class);
            bind(DeviceLifeCycleHistoryInfoFactory.class).to(DeviceLifeCycleHistoryInfoFactory.class);
            bind(ValidationInfoFactory.class).to(ValidationInfoFactory.class);
            bind(ValidationRuleInfoFactory.class).to(ValidationRuleInfoFactory.class);
            bind(DeviceDataInfoFactory.class).to(DeviceDataInfoFactory.class);
            bind(PropertyUtils.class).to(PropertyUtils.class);
        }
    }

}