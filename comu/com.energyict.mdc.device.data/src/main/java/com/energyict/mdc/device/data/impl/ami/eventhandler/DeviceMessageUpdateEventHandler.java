/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.text.MessageFormat;
import java.util.Optional;

import static com.elster.jupiter.metering.ami.CompletionMessageInfo.CompletionMessageStatus;
import static com.elster.jupiter.metering.ami.CompletionMessageInfo.FailureReason;

/**
 * Handles update events that are being sent when a {@link DeviceMessage} has been updated.
 * The events are only of interest if:
 * <ul>
 * <li>the {@link DeviceMessage} was linked to a {@link ServiceCall} used in the {@link MultiSenseHeadEndInterface}</li>
 * <li>the {@link DeviceMessage} has been executed by the Comserver (~ or in other words if the {@link DeviceMessageStatus}
 * has been updated to either CONFIRMED or FAILED), or the message has been revoked</li>
 * </ul>
 * If this is the case, then
 * <ul>
 * <li>the custom property set of the linked {@link ServiceCall} will be updated</li>
 * <li>the linked {@link ServiceCall} will then also be transited to either ONGOING or FAILED</li>
 * </ul>
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
@Component(name = "com.energyict.mdc.device.data.ami.devicemessage.update.eventhandler", service = TopicHandler.class, immediate = true)
public class DeviceMessageUpdateEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/data/deviceMessage/UPDATED";
    static final String OLD_OBIS_CODE_PROPERTY_NAME = "oldDeviceMessageStatus";

    private volatile ServiceCallService serviceCallService;
    private volatile CompletionOptionsCallBack completionOptionsCallBack;

    public DeviceMessageUpdateEventHandler() {
        super();
    }

    public DeviceMessageUpdateEventHandler(ServiceCallService serviceCallService, CompletionOptionsCallBack completionOptionsCallBack) {
        this();
        this.serviceCallService = serviceCallService;
        this.completionOptionsCallBack = completionOptionsCallBack;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCompletionOptionsCallBack(CompletionOptionsCallBack completionOptionsCallBack) {
        this.completionOptionsCallBack = completionOptionsCallBack;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        DeviceMessage deviceMessage = (DeviceMessage) event.getSource();
        ServiceCall serviceCall;
        if (trackingInfoContainsServiceCall(deviceMessage) && ((serviceCall = getServiceCallIfRelatedToHeadEndInterface(deviceMessage)) != null) && validateDeviceMessageStatusChanged(deviceMessage, event)) {
            switch (deviceMessage.getStatus()) {
                case CONFIRMED:
                    serviceCall.log(LogLevel.INFO, MessageFormat.format("Device command with ID {0} has been confirmed", deviceMessage.getId()));
                    deviceMessageConfirmed(deviceMessage, serviceCall);
                    break;
                case FAILED:
                    serviceCall.log(LogLevel.INFO, MessageFormat.format("Execution of device command with ID {0} failed", deviceMessage.getId()));
                    deviceMessageFailed(deviceMessage, serviceCall);
                    break;
                case REVOKED:
                    serviceCall.log(LogLevel.INFO, MessageFormat.format("Device command with ID {0} has been revoked", deviceMessage.getId()));
                    deviceMessageRevoked(deviceMessage, serviceCall);
                    break;
                case WAITING:
                    // Intentional fall-through
                case PENDING:
                    // Intentional fall-through
                case SENT:
                    // Intentional fall-through
                case INDOUBT:
                    // Intentional fall-through
                default:
                    // No action required
                    break;
            }
        }
    }

    private void deviceMessageConfirmed(DeviceMessage deviceMessage, ServiceCall serviceCall) {
        CommandServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CommandCustomPropertySet()).get();
        domainExtension.setNrOfUnconfirmedDeviceCommands(domainExtension.getNrOfUnconfirmedDeviceCommands() - 1);
        serviceCall.update(domainExtension);
        if (serviceCall.canTransitionTo(DefaultState.ONGOING)) {
            serviceCall.requestTransition(DefaultState.ONGOING);
        } // Else the ServiceCall is probably already in Ongoing state (or is already marked as Failed / Cancelled)
    }

    private void deviceMessageFailed(DeviceMessage deviceMessage, ServiceCall serviceCall) {
        logProtocolInfoOfDeviceMessage(deviceMessage, serviceCall);
        completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.FAILURE, FailureReason.ONE_OR_MORE_DEVICE_COMMANDS_FAILED);
        if (serviceCall.canTransitionTo(DefaultState.ONGOING)) {
            serviceCall.requestTransition(DefaultState.ONGOING);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
        // else the ServiceCall is probably already in Failed state
    }

    private void deviceMessageRevoked(DeviceMessage deviceMessage, ServiceCall serviceCall) {
        if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
            completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.CANCELLED, FailureReason.ONE_OR_MORE_DEVICE_COMMANDS_HAVE_BEEN_REVOKED);
            serviceCall.requestTransition(DefaultState.CANCELLED);
        }
        // else the ServiceCall is probably already in an end state
    }

    private void logProtocolInfoOfDeviceMessage(DeviceMessage deviceMessage, ServiceCall serviceCall) {
        if (deviceMessage.getProtocolInfo() != null && !deviceMessage.getProtocolInfo().isEmpty()) {
            serviceCall.log(LogLevel.INFO, MessageFormat.format("Device command {0} protocol information: {1}", deviceMessage.getId(), deviceMessage.getProtocolInfo()));
        }
    }

    private boolean trackingInfoContainsServiceCall(DeviceMessage deviceMessage) {
        return TrackingCategory.serviceCall.equals(deviceMessage.getTrackingCategory());
    }

    private ServiceCall getServiceCallIfRelatedToHeadEndInterface(DeviceMessage deviceMessage) {
        Optional<ServiceCall> serviceCallOptional = serviceCallService.getServiceCall(Long.parseLong(deviceMessage.getTrackingId()));
        if (serviceCallOptional.isPresent()) {
            for (ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping : ServiceCallCommands.ServiceCallTypeMapping.values()) {
                if (serviceCallTypeMapping.getTypeName().equals(serviceCallOptional.get().getType().getName())) {
                    return serviceCallOptional.get();
                }
            }
        }
        return null;
    }

    private boolean validateDeviceMessageStatusChanged(DeviceMessage deviceMessage, LocalEvent localEvent) {
        DeviceMessageStatus oldDeviceMessageStatus = DeviceMessageStatus.fromDb((Integer) localEvent.toOsgiEvent().getProperty(OLD_OBIS_CODE_PROPERTY_NAME));
        return !Checks.is(deviceMessage.getStatus()).equalTo(oldDeviceMessageStatus);
    }
}