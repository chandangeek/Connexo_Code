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
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdw.UserFileService;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile UserFileService userFileService;
    private volatile DeviceDataService deviceDataService;

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
                LoadProfileConfigurationResource.class
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
    public void setUserFileService(UserFileService userFileService) {
        this.userFileService = userFileService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Override
    public void install() {
        new Installer(thesaurus).install();
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
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(engineModelService).to(EngineModelService.class);
            bind(userFileService).to(UserFileService.class);
            bind(deviceDataService).to(DeviceDataService.class);
        }
    }

}