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
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignItem;
import com.energyict.mdc.tou.campaign.impl.MessageSeeds;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.util.Optional;

public class TimeOfUseItemDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, TimeOfUseCampaignItem {

    public enum FieldNames {
        DOMAIN("serviceCall", "service_call"),
        PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),
        DEVICE("device", "device"),
        DEVICE_MESSAGE("deviceMessage", "device_message_id"),
        ;

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
    private Reference<ServiceCall> serviceCall = Reference.empty();

    @IsPresent
    private Reference<Device> device = Reference.empty();
    private long parentServiceCallId;
    private Reference<DeviceMessage> deviceMessage = Reference.empty();


    @Inject
    public TimeOfUseItemDomainExtension(TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        super();
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
    public ServiceCall cancel() {
        ServiceCall serviceCall = getServiceCall();
        if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
            serviceCall.requestTransition(DefaultState.CANCELLED);
        }
        return serviceCallService.getServiceCall(serviceCall.getId()).get();
    }

    @Override
    public ServiceCall retry() {
        ServiceCall serviceCall = getServiceCall();
        if (serviceCall.canTransitionTo(DefaultState.PENDING)) {
            getDevice().getMessages().stream()
                    .filter(deviceMessage -> (deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING)
                            || deviceMessage.getStatus().equals(DeviceMessageStatus.WAITING)))
                    .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == 0)
                    .forEach(DeviceMessage::revoke);
            serviceCall.log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.RETRIED_BY_USER).format());
            dataModel.getInstance(TimeOfUseSendHelper.class)
                    .setCalendarOnDevice(getDevice(), serviceCall);
            return serviceCallService.getServiceCall(serviceCall.getId()).get();
        }
        return serviceCall;
    }

    @Override
    public long getParentServiceCallId() {
        return parentServiceCallId;
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


    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setDevice((Device) propertyValues.getProperty(FieldNames.DEVICE.javaName()));
        this.setParentServiceCallId((long) propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()));
        this.setDeviceMessage((DeviceMessage) propertyValues.getProperty(FieldNames.DEVICE_MESSAGE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE.javaName(), this.getDevice());
        propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), this.getParentServiceCallId());
        propertySetValues.setProperty(FieldNames.DEVICE_MESSAGE.javaName(), this.getDeviceMessage().orElse(null));
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}