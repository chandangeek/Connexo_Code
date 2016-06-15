package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.impl.ami.MultiSenseHeadEndInterface;
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

    private ServiceCall serviceCall;

    public DeviceMessageUpdateEventHandler() {
        super();
    }

    public DeviceMessageUpdateEventHandler(ServiceCallService serviceCallService) {
        this();
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        DeviceMessage deviceMessage = (DeviceMessage) event.getSource();
        if (trackingInfoContainsServiceCall(deviceMessage) && serviceCallRelatedToHeadEndInterface(deviceMessage) && validateDeviceMessageStatusChanged(deviceMessage, event)) {
            switch (deviceMessage.getStatus()) {
                case CONFIRMED:
                    getServiceCall().log(LogLevel.INFO, MessageFormat.format("Device command {0} has been confirmed", deviceMessage.getId()));
                    deviceMessageConfirmed(deviceMessage);
                    break;
                case FAILED:
                    getServiceCall().log(LogLevel.INFO, MessageFormat.format("Execution of device command {0} failed", deviceMessage.getId()));
                    deviceMessageFailed(deviceMessage);
                    break;
                case REVOKED:
                    getServiceCall().log(LogLevel.INFO, MessageFormat.format("Device command {0} has been revoked", deviceMessage.getId()));
                    deviceMessageFailed(deviceMessage);
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

    private void deviceMessageConfirmed(DeviceMessage deviceMessage) {
        CommandServiceCallDomainExtension domainExtension = getServiceCall().getExtensionFor(new CommandCustomPropertySet()).get();
        domainExtension.setNrOfUnconfirmedDeviceCommands(domainExtension.getNrOfUnconfirmedDeviceCommands() - 1);
        getServiceCall().update(domainExtension);
        if (getServiceCall().canTransitionTo(DefaultState.ONGOING)) {
            getServiceCall().requestTransition(DefaultState.ONGOING);
        } // Else the ServiceCall is probably already in Ongoing state (or is already marked as Failed / Cancelled)
    }

    private void deviceMessageFailed(DeviceMessage deviceMessage) {
        logProtocolInfoOfDeviceMessage(deviceMessage);
        if (getServiceCall().canTransitionTo(DefaultState.ONGOING)) {
            getServiceCall().requestTransition(DefaultState.ONGOING);
            getServiceCall().requestTransition(DefaultState.FAILED);
        }
        // else the ServiceCall is probably already in Failed state
    }

    private void logProtocolInfoOfDeviceMessage(DeviceMessage deviceMessage) {
        if (deviceMessage.getProtocolInfo() != null && !deviceMessage.getProtocolInfo().isEmpty()) {
            getServiceCall().log(LogLevel.INFO, MessageFormat.format("Device command {0} protocol information: {1}", deviceMessage.getId(), deviceMessage.getProtocolInfo()));
        }
    }

    private boolean trackingInfoContainsServiceCall(DeviceMessage deviceMessage) {
        return TrackingCategory.serviceCall.equals(deviceMessage.getTrackingCategory());
    }

    private boolean serviceCallRelatedToHeadEndInterface(DeviceMessage deviceMessage) {
        Optional<ServiceCall> serviceCallOptional = serviceCallService.getServiceCall(Long.parseLong(deviceMessage.getTrackingId()));
        if (serviceCallOptional.isPresent()) {
            setServiceCall(serviceCallOptional.get());
            for (ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping : ServiceCallCommands.ServiceCallTypeMapping.values()) {
                if (serviceCallTypeMapping.getTypeName().equals(getServiceCall().getType().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validateDeviceMessageStatusChanged(DeviceMessage deviceMessage, LocalEvent localEvent) {
        DeviceMessageStatus oldDeviceMessageStatus = DeviceMessageStatus.fromDb((Integer) localEvent.toOsgiEvent().getProperty(OLD_OBIS_CODE_PROPERTY_NAME));
        return !Checks.is(deviceMessage.getStatus()).equalTo(oldDeviceMessageStatus);
    }

    public ServiceCall getServiceCall() {
        return serviceCall;
    }

    public void setServiceCall(ServiceCall serviceCall) {
        this.serviceCall = serviceCall;
    }
}