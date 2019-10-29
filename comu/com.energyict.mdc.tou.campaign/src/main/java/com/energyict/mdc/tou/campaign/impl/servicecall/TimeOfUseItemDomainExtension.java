/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignException;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignItem;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class TimeOfUseItemDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, TimeOfUseCampaignItem {

    public enum FieldNames {
        DOMAIN("serviceCall", "service_call"),
        PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),
        DEVICE("device", "device"),
        DEVICE_MESSAGE("deviceMessage", "device_message_id"),
        STEP_OF_UPDATE("stepOfUpdate", "step_of_update");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;
    private final TimeOfUseCampaignServiceImpl timeOfUseCampaignService;
    private Reference<ServiceCall> serviceCall = Reference.empty();

    @IsPresent
    private Reference<Device> device = Reference.empty();
    private long parentServiceCallId;
    private Reference<DeviceMessage> deviceMessage = Reference.empty();
    private long stepOfUpdate;


    @Inject
    public TimeOfUseItemDomainExtension(TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        super();
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.dataModel = timeOfUseCampaignService.getDataModel();
        thesaurus = dataModel.getInstance(Thesaurus.class);
        serviceCallService = dataModel.getInstance(ServiceCallService.class);
    }

    @Override
    public Device getDevice() {
        return device.get();
    }

    @Override
    public Optional<DeviceMessage> getDeviceMessage() {
        return deviceMessage.getOptional();
    }

    @Override
    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public ServiceCall cancel(boolean initFromCampaign) {
        ServiceCall serviceCall = getServiceCall();
        if (serviceCall.getState().equals(DefaultState.ONGOING)) {
            if (!initFromCampaign) {
                throw new TimeOfUseCampaignException(thesaurus, MessageSeeds.DEVICE_IS_NOT_PENDING_STATE);
            }
        }
        if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
            serviceCall.requestTransition(DefaultState.CANCELLED);
        }
        return serviceCallService.getServiceCall(serviceCall.getId()).get();
    }

    @Override
    public ServiceCall retry() {
        ServiceCall serviceCall = getServiceCall();
        if (serviceCall.getParent().get().getExtension(TimeOfUseCampaignDomainExtension.class).get().isManuallyCancelled()) {
            throw new TimeOfUseCampaignException(thesaurus, MessageSeeds.CAMPAIGN_WITH_DEVICE_CANCELLED);
        }
        if (serviceCall.canTransitionTo(DefaultState.PENDING)) {
            serviceCall.log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.RETRIED_BY_USER).format());
            serviceCall.requestTransition(DefaultState.PENDING);
        }
        return serviceCallService.getServiceCall(serviceCall.getId()).get();
    }

    @Override
    public long getParentServiceCallId() {
        return parentServiceCallId;
    }

    @Override
    public long getStepOfUpdate() {
        return stepOfUpdate;
    }

    public void setParentServiceCallId(long parentServiceCallId) {
        this.parentServiceCallId = parentServiceCallId;
    }

    public void setDevice(Device device) {
        this.device.set(device);
    }

    public void setDeviceMessage(DeviceMessage deviceMessage) {
        this.deviceMessage.set(deviceMessage);
    }

    public void setStepOfUpdate(long stepOfUpdate) {
        this.stepOfUpdate = stepOfUpdate;
    }

    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setDevice((Device) propertyValues.getProperty(FieldNames.DEVICE.javaName()));
        this.setParentServiceCallId((long) propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()));
        this.setDeviceMessage((DeviceMessage) propertyValues.getProperty(FieldNames.DEVICE_MESSAGE.javaName()));
        this.setStepOfUpdate((long) propertyValues.getProperty(FieldNames.STEP_OF_UPDATE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE.javaName(), this.getDevice());
        propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), this.getParentServiceCallId());
        propertySetValues.setProperty(FieldNames.DEVICE_MESSAGE.javaName(), this.getDeviceMessage().orElse(null));
        propertySetValues.setProperty(FieldNames.STEP_OF_UPDATE.javaName(), this.getStepOfUpdate());
    }

    public void update() {
        getServiceCall().update(this);
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }

    private TimeOfUseCampaignDomainExtension getCampaign() {
        return getServiceCall().getParent().get().getExtension(TimeOfUseCampaignDomainExtension.class).get();
    }

    @Override
    public Optional<ComTaskExecution> findOrCreateVerificationComTaskExecution() {
        return getDevice().getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getId() == getCampaign().getValidationComTaskId())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .anyMatch(task -> task instanceof StatusInformationTask))
                .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                .filter(comTaskEnablement -> (timeOfUseCampaignService.findComTaskExecution(getDevice(), comTaskEnablement) == null)
                        || (!timeOfUseCampaignService.findComTaskExecution(getDevice(), comTaskEnablement).isOnHold()))
                .findAny()
                .map(comTaskEnablement -> getDevice().getComTaskExecutions().stream()
                        .filter(comTaskExecution -> comTaskExecution.getComTask().equals(comTaskEnablement.getComTask()))
                        .findAny().orElseGet(() -> getDevice().newAdHocComTaskExecution(comTaskEnablement).add()));
    }

    @Override
    public Optional<ComTaskExecution> findOrCreateUploadComTaskExecution() {
        return getDevice().getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getId() == getCampaign().getCalendarUploadComTaskId())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().isManualSystemTask())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .filter(task -> task instanceof MessagesTask)
                        .map(task -> ((MessagesTask) task))
                        .map(MessagesTask::getDeviceMessageCategories)
                        .flatMap(List::stream)
                        .anyMatch(deviceMessageCategory -> deviceMessageCategory.getId() == 0))
                .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                .filter(comTaskEnablement -> (timeOfUseCampaignService.findComTaskExecution(getDevice(), comTaskEnablement) == null)
                        || (!timeOfUseCampaignService.findComTaskExecution(getDevice(), comTaskEnablement).isOnHold()))
                .findAny()
                .map(comTaskEnablement -> getDevice().getComTaskExecutions().stream()
                        .filter(comTaskExecution -> comTaskExecution.getComTask().equals(comTaskEnablement.getComTask()))
                        .findAny().orElseGet(() -> getDevice().newAdHocComTaskExecution(comTaskEnablement).add()));
    }
}