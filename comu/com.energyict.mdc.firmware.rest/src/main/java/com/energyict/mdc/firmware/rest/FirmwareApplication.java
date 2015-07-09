package com.energyict.mdc.firmware.rest;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.DeviceStateAccessFeature;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.rest.impl.*;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.firmware.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/fwc", "app=MDC", "name=" + FirmwareApplication.COMPONENT_NAME})
public class FirmwareApplication extends Application implements TranslationKeyProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "FWR";

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile RestQueryService restQueryService;
    private volatile TransactionService transactionService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile FirmwareService firmwareService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile TaskService taskService;
    private volatile Clock clock;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile CommunicationTaskService communicationTaskService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                FirmwareVersionResource.class,
                FirmwareFieldResource.class,
                FirmwareManagementOptionsResource.class,
                FirmwareCampaignResource.class,
                DeviceFirmwareVersionResource.class,
                DeviceFirmwareMessagesResource.class,
                FirmwareTypesResource.class,
                TransactionWrapper.class,
                MultiPartFeature.class,
                FirmwareComTaskResource.class,
                RestValidationExceptionMapper.class,
                DeviceStateAccessFeature.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(FirmwareMessageInfoFactory.class).to(FirmwareMessageInfoFactory.class);
            bind(DeviceFirmwareVersionInfoFactory.class).to(DeviceFirmwareVersionInfoFactory.class);
            bind(FirmwareManagementDeviceUtils.Factory.class).to(FirmwareManagementDeviceUtils.Factory.class);
            bind(FirmwareCampaignInfoFactory.class).to(FirmwareCampaignInfoFactory.class);
            bind(DeviceInFirmwareCampaignInfoFactory.class).to(DeviceInFirmwareCampaignInfoFactory.class);
            bind(FirmwareVersionInfoFactory.class).to(FirmwareVersionInfoFactory.class);
            bind(transactionService).to(TransactionService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(firmwareService).to(FirmwareService.class);
            bind(deviceService).to(DeviceService.class);
            bind(deviceMessageSpecificationService).to(DeviceMessageSpecificationService.class);
            bind(taskService).to(TaskService.class);
            bind(clock).to(Clock.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
        }
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
        List<TranslationKey> keys = new ArrayList<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
                keys.add(messageSeed);
        }
        return keys;
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
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }
}
