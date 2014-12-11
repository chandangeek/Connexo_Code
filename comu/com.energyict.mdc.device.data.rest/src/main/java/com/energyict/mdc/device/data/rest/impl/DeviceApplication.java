package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.Installer;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.rest.SecurityPropertySetInfoFactory;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.collect.ImmutableSet;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.ddr.rest", service = {Application.class, InstallService.class}, immediate = true, property = {"alias=/ddr", "app=MDC", "name=" + DeviceApplication.COMPONENT_NAME})
public class DeviceApplication extends Application implements InstallService {

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
    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile EngineModelService engineModelService;
    private volatile SchedulingService schedulingService;
    private volatile ValidationService validationService;
    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile RestQueryService restQueryService;
    private volatile TaskService taskService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile Clock clock;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile FavoritesService favoritesService;
    private volatile License license;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                ExceptionLogger.class,
                DeviceResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                JsonMappingExceptionMapper.class,
                LocalizedExceptionMapper.class,
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
                DeviceGroupResource.class,
                SecurityPropertySetResource.class,
                ConnectionMethodResource.class,
                ComSessionResource.class,
                DeviceMessageResource.class,
                DeviceLabelResource.class
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
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
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

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public void install() {
        Installer installer = new Installer();
        installer.createTranslations(COMPONENT_NAME, thesaurus, Layer.REST, MessageSeeds.values());
        createTranslations();
        createLabelCategories();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(NlsService.COMPONENTNAME, FavoritesService.COMPONENTNAME);
    }

    private void createTranslations() {
        try {
            Map<String, Translation> translations = new HashMap<>();

            for (EndDeviceType type : EndDeviceType.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, type.toString()).defaultMessage(type.getMnemonic());
                translations.put(type.toString(), SimpleTranslation.translation(nlsKey, Locale.ENGLISH, type.getMnemonic()));
            }
            for (EndDeviceDomain domain : EndDeviceDomain.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, domain.toString()).defaultMessage(domain.getMnemonic());
                translations.put(domain.toString(), SimpleTranslation.translation(nlsKey, Locale.ENGLISH, domain.getMnemonic()));
            }
            for (EndDeviceSubDomain subDomain : EndDeviceSubDomain.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, subDomain.toString()).defaultMessage(subDomain.getMnemonic());
                translations.put(subDomain.toString(), SimpleTranslation.translation(nlsKey, Locale.ENGLISH, subDomain.getMnemonic()));
            }
            for (EndDeviceEventorAction eventOrAction : EndDeviceEventorAction.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, eventOrAction.toString()).defaultMessage(eventOrAction.getMnemonic());
                translations.put(eventOrAction.toString(), SimpleTranslation.translation(nlsKey, Locale.ENGLISH, eventOrAction.getMnemonic()));
            }

            thesaurus.addTranslations(translations.values());
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private void createLabelCategories() {
        try {
            favoritesService.createLabelCategory(MessageSeeds.MDC_LABEL_CATEGORY_FAVORITES.getKey());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
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
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ConnectionMethodInfoFactory.class).to(ConnectionMethodInfoFactory.class);
            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(deviceImportService).to(DeviceImportService.class);
            bind(engineModelService).to(EngineModelService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(schedulingService).to(SchedulingService.class);
            bind(validationService).to(ValidationService.class);
            bind(meteringService).to(MeteringService.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(restQueryService).to(RestQueryService.class);
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
        }
    }

}