package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.Installer;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

@Component(name = "com.energyict.dtc.rest", service = { Application.class, InstallService.class }, immediate = true, property = {"alias=/dtc", "name=" + DeviceConfigurationApplication.COMPONENT_NAME})
public class DeviceConfigurationApplication extends Application implements InstallService {

    public static final String COMPONENT_NAME = "DCR";

    private volatile MasterDataService masterDataService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile TransactionService transactionService;
    private volatile MeteringService meteringService;
    private volatile EngineModelService engineModelService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private volatile TaskService taskService;
    private volatile NlsService nlsService;
    private volatile UserService userService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile ValidationService validationService;
    private volatile DeviceService deviceService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                ExceptionLogger.class,
                DeviceTypeResource.class,
                DeviceConfigFieldResource.class,
                DeviceConfigurationResource.class,
                RegisterConfigurationResource.class,
                RegisterTypeResource.class,
                ReadingTypeResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                JsonMappingExceptionMapper.class,
                LocalizedExceptionMapper.class,
                ProtocolDialectResource.class,
                ConnectionMethodResource.class,
                RegisterTypeResource.class,
                RegisterGroupResource.class,
                SecurityPropertySetResource.class,
                ComTaskEnablementResource.class,
                LoadProfileTypeResource.class,
                ExecutionLevelResource.class,
                LoadProfileConfigurationResource.class,
                DeviceConfigsValidationRuleSetResource.class,
                ValidationRuleSetResource.class
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
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void install() {
        Installer installer = new Installer();
        Set<MessageSeed> messageSeeds = new HashSet<>();
        messageSeeds.addAll(Arrays.asList(MessageSeeds.values()));
        messageSeeds.addAll(camouflagePhenomenaAsMessageSeeds());
        installer.createTranslations(COMPONENT_NAME, thesaurus, Layer.REST, messageSeeds.toArray(new MessageSeed[messageSeeds.size()]));
    }

    private Set<MessageSeed> camouflagePhenomenaAsMessageSeeds() {
        Set<MessageSeed> messageSeedSet = new HashSet<>();
        for (final Phenomenon phenomenon : masterDataService.findAllPhenomena()) {
            messageSeedSet.add(
                new MessageSeed() {
                    @Override
                    public String getModule() {
                        return DeviceConfigurationApplication.COMPONENT_NAME;
                    }

                    @Override
                    public int getNumber() {
                        return (int) phenomenon.getId();
                    }

                    @Override
                    public String getKey() {
                        return phenomenon.getName();
                    }

                    @Override
                    public String getDefaultFormat() {
                        return phenomenon.getName();
                    }

                    @Override
                    public Level getLevel() {
                        return Level.SEVERE;
                    }
                });
        }
        return messageSeedSet;
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
            bind(engineModelService).to(EngineModelService.class);
            bind(validationService).to(ValidationService.class);
            bind(deviceService).to(DeviceService.class);
            bind(userService).to(UserService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(PropertyUtils.class).to(PropertyUtils.class);
        }
    }

}