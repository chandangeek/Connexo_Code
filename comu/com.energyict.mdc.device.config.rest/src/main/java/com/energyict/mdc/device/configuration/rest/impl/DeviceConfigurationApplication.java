package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.configuration.rest.SecurityPropertySetPrivilegeTranslationKeys;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.dtc.rest",
        service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/dtc", "app=MDC", "name=" + DeviceConfigurationApplication.COMPONENT_NAME})
public class DeviceConfigurationApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DCR";

    private volatile MasterDataService masterDataService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile TransactionService transactionService;
    private volatile MeteringService meteringService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private volatile TaskService taskService;
    private volatile NlsService nlsService;
    private volatile UserService userService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile DeviceService deviceService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile License license;
    private volatile FirmwareService firmwareService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile CustomPropertySetService customPropertySetService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                ExceptionLogger.class,
                RestValidationExceptionMapper.class,
                DeviceTypeResource.class,
                DeviceConfigFieldResource.class,
                DeviceConfigurationResource.class,
                DeviceConfigConflictMappingResource.class,
                RegisterConfigurationResource.class,
                ReadingTypeResource.class,
                ProtocolDialectResource.class,
                ConnectionMethodResource.class,
                RegisterGroupResource.class,
                SecurityPropertySetResource.class,
                ComTaskEnablementResource.class,
                LoadProfileTypeResource.class,
                ExecutionLevelResource.class,
                LoadProfileConfigurationResource.class,
                DeviceConfigValidationRuleSetResource.class,
                ValidationRuleSetResource.class,
                EstimationRuleSetResource.class,
                DeviceMessagesResource.class,
                DeviceMessagePrivilegesResource.class,
                ProtocolPropertiesResource.class,
                DeviceConfigurationEstimationRuleSetResource.class
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
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
        this.thesaurus = this.thesaurus.join(nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        keys.addAll(Arrays.asList(ConnectionStrategyTranslationKeys.values()));
        keys.addAll(Arrays.asList(SecurityPropertySetPrivilegeTranslationKeys.values()));
        keys.addAll(Arrays.asList(DeviceMessageExecutionLevelTranslationKeys.values()));
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
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(masterDataService).to(MasterDataService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(transactionService).to(TransactionService.class);
            bind(meteringService).to(MeteringService.class);
            bind(mdcReadingTypeUtilService).to(MdcReadingTypeUtilService.class);
            bind(taskService).to(TaskService.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(ConnectionMethodInfoFactory.class).to(ConnectionMethodInfoFactory.class);
            bind(SecurityPropertySetInfoFactory.class).to(SecurityPropertySetInfoFactory.class);
            bind(ExecutionLevelInfoFactory.class).to(ExecutionLevelInfoFactory.class);
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(engineConfigurationService).to(EngineConfigurationService.class);
            bind(validationService).to(ValidationService.class);
            bind(estimationService).to(EstimationService.class);
            bind(deviceService).to(DeviceService.class);
            bind(userService).to(UserService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(PropertyUtils.class).to(PropertyUtils.class);
            bind(deviceMessageSpecificationService).to(DeviceMessageSpecificationService.class);
            bind(firmwareService).to(FirmwareService.class);
            bind(deviceLifeCycleConfigurationService).to(DeviceLifeCycleConfigurationService.class);
            bind(ValidationRuleInfoFactory.class).to(ValidationRuleInfoFactory.class);
            bind(customPropertySetService).to(CustomPropertySetService.class);
        }
    }
}