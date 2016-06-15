package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.rest.FieldValidationException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndDeviceCommandImpl implements EndDeviceCommand {

    private final EndDevice endDevice;
    private final EndDeviceControlType endDeviceControlType;
    private final List<DeviceMessageId> possibleDeviceMessageIds;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    private List<PropertySpec> commandArgumentSpecs = null;
    private Map<PropertySpec, Object> propertyValueMap = new HashMap<>();

    public EndDeviceCommandImpl(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        this.endDevice = endDevice;
        this.endDeviceControlType = endDeviceControlType;
        this.possibleDeviceMessageIds = possibleDeviceMessageIds;
        this.deviceService = deviceService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.thesaurus = thesaurus;
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice;
    }

    @Override
    public EndDeviceControlType getEndDeviceControlType() {
        return endDeviceControlType;
    }

    @Override
    public List<PropertySpec> getCommandArgumentSpecs() {
        if (this.commandArgumentSpecs == null) {
            Map<String, PropertySpec> uniquePropertySpecs = new HashMap<>();
            getDeviceMessageSpecs().stream().forEach(messageSpec -> messageSpec.getPropertySpecs().stream().forEach(spec -> uniquePropertySpecs.put(spec.getName(), spec)));
            this.commandArgumentSpecs = new ArrayList<>(uniquePropertySpecs.values());
        }
        return this.commandArgumentSpecs;
    }

    private List<DeviceMessageSpec> getDeviceMessageSpecs() {
        List<DeviceMessageSpec> deviceMessageSpecs = new ArrayList<>();
        possibleDeviceMessageIds.stream().forEach(msgId -> this.deviceMessageSpecificationService.findMessageSpecById(msgId.dbValue()).ifPresent(deviceMessageSpecs::add));
        return deviceMessageSpecs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setPropertyValue(PropertySpec propertySpec, Object value) {
        List<PropertySpec> argumentSpecs = getCommandArgumentSpecs();

        if (!argumentSpecs.contains(propertySpec)) {
            throw new FieldValidationException("Property spec not found", propertySpec.getName());
        } else if (!propertySpec.getValueFactory().getValueType().isAssignableFrom(value.getClass())) {
            throw new FieldValidationException("Incorrect type", propertySpec.getName());
        }
        getPropertyValueMap().put(propertySpec, value);
    }

    public List<DeviceMessage<Device>> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate) {
        List<DeviceMessageId> actualDeviceMessageIds = new ArrayList<>();
        boolean useReleaseDate = false;
        switch (EndDeviceControlTypeMapping.getMappingFor(getEndDeviceControlType())) {
            case ARM_REMOTE_SWITCH_FOR_CLOSURE:
            case ARM_REMOTE_SWITCH_FOR_OPEN:
                if (hasCommandArgumentValueFor(DeviceMessageConstants.contactorActivationDateAttributeName) && deviceHasSupportFor(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
                    actualDeviceMessageIds.addAll(Arrays.asList(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE));
                } else {
                    actualDeviceMessageIds.addAll(Arrays.asList(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.CONTACTOR_ARM));
                    useReleaseDate = true;
                }
                break;
            case CLOSE_REMOTE_SWITCH:
                if (hasCommandArgumentValueFor(DeviceMessageConstants.contactorActivationDateAttributeName) && deviceHasSupportFor(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
                    actualDeviceMessageIds.add(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
                } else {
                    actualDeviceMessageIds.add(DeviceMessageId.CONTACTOR_CLOSE);
                    useReleaseDate = true;
                }
                break;
            case OPEN_REMOTE_SWITCH:
                if (hasCommandArgumentValueFor(DeviceMessageConstants.contactorActivationDateAttributeName) && deviceHasSupportFor(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
                    actualDeviceMessageIds.add(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
                } else {
                    actualDeviceMessageIds.add(DeviceMessageId.CONTACTOR_OPEN);
                    useReleaseDate = true;
                }
                break;
            case LOAD_CONTROL_INITITATE:
                actualDeviceMessageIds.add(
                        hasCommandArgumentValueFor(DeviceMessageConstants.overThresholdDurationAttributeName)
                                ? DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION
                                : DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD);
                break;
            default:
                actualDeviceMessageIds.addAll(getPossibleDeviceMessageIds()); // Default case, all possible DeviceMessageIds should be used 1-on-1
                break;
        }

        return doCreateCorrespondingMultiSenseDeviceMessages(serviceCall, useReleaseDate ? releaseDate : Instant.now(), actualDeviceMessageIds);
    }

    private boolean hasCommandArgumentValueFor(String commandArgumentName) {
        return getPropertyValueMap().keySet().stream().anyMatch(propertySpec -> propertySpec.getName().equals(commandArgumentName));
    }

    private boolean deviceHasSupportFor(DeviceMessageId deviceMessageId) {
        return findDeviceForEndDevice(endDevice).getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages().contains(deviceMessageId);
    }

    private List<DeviceMessage<Device>> doCreateCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate, List<DeviceMessageId> deviceMessageIds) {
        Device multiSenseDevice = findDeviceForEndDevice(getEndDevice());
        List<DeviceMessage<Device>> deviceMessages = new ArrayList<>(deviceMessageIds.size());
        deviceMessageIds.stream().forEach(deviceMessageId -> {
            Device.DeviceMessageBuilder deviceMessageBuilder = multiSenseDevice.newDeviceMessage(deviceMessageId, TrackingCategory.serviceCall)
                    .setTrackingId(Long.toString(serviceCall.getId()))
                    .setReleaseDate(releaseDate);
            for (PropertySpec propertySpec : findDeviceMessageSpec(deviceMessageId).getPropertySpecs()) {
                deviceMessageBuilder.addProperty(propertySpec.getName(), getPropertyValueMap().get(propertySpec));
            }
            deviceMessages.add(deviceMessageBuilder.add());
        });
        return deviceMessages;
    }

    protected Map<PropertySpec, Object> getPropertyValueMap() {
        return propertyValueMap;
    }

    private List<DeviceMessageId> getPossibleDeviceMessageIds() {
        return possibleDeviceMessageIds;
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        return deviceService.findByUniqueMrid(endDevice.getMRID()).orElseThrow(NoSuchElementException.deviceWithMRIDNotFound(thesaurus, endDevice.getMRID()));
    }

    private DeviceMessageSpec findDeviceMessageSpec(DeviceMessageId deviceMessageId) {
        return this.deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue())
                .orElseThrow(NoSuchElementException.deviceMessageSpecWithIdNotFound(thesaurus, deviceMessageId.dbValue()));
    }
}