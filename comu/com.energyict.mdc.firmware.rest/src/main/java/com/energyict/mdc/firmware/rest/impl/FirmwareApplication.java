/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.DeviceStateAccessFeature;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.firmware.rest.impl.campaign.DeviceInFirmwareCampaignInfoFactory;
import com.energyict.mdc.firmware.rest.impl.campaign.FirmwareCampaignInfoFactory;
import com.energyict.mdc.firmware.rest.impl.campaign.FirmwareCampaignResource;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.firmware.rest", service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/fwc", "app=MDC", "name=" + FirmwareApplication.COMPONENT_NAME})
public class FirmwareApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {
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
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile MdcPropertyUtils mdcPropertyUtils;
    private volatile SecurityManagementService securityManagementService;
    private volatile FirmwareCampaignService firmwareCampaignService;
    private volatile DeviceMessageService deviceMessageService;
    private volatile ServiceCallService serviceCallService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile ConnectionTaskService connectionTaskService;

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
                FirmwareComTaskResource.class,
                MultiPartFeature.class,
                RestValidationExceptionMapper.class,
                DeviceStateAccessFeature.class,
                SecurityAccessorResource.class
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
            bind(mdcPropertyUtils).to(MdcPropertyUtils.class);
            bind(FirmwareMessageInfoFactory.class).to(FirmwareMessageInfoFactory.class);
            bind(DeviceFirmwareVersionInfoFactory.class).to(DeviceFirmwareVersionInfoFactory.class);
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
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(DeviceFirmwareLifecycleHistoryInfoFactory.class).to(DeviceFirmwareLifecycleHistoryInfoFactory.class);
            bind(securityManagementService).to(SecurityManagementService.class);
            bind(SecurityAccessorInfoFactory.class).to(SecurityAccessorInfoFactory.class);
            bind(firmwareCampaignService).to(FirmwareCampaignService.class);
            bind(deviceMessageService).to(DeviceMessageService.class);
            bind(ConcurrentModificationExceptionFactory.class).to(ConcurrentModificationExceptionFactory.class);
            bind(serviceCallService).to(ServiceCallService.class);
            bind(communicationTaskService).to(CommunicationTaskService.class);
            bind(connectionTaskService).to(ConnectionTaskService.class);
        }
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        return keys;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(FirmwareService.COMPONENTNAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(TimeService.COMPONENT_NAME, Layer.DOMAIN));
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
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
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

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setMdcPropertyUtils(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }
}
