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
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class EndDeviceCommandImpl implements EndDeviceCommand {

    private final EndDevice endDevice;
    private final EndDeviceControlType endDeviceControlType;
    private final List<DeviceMessageId> possibleDeviceMessageIds;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    private List<PropertySpec> commandArgumentSpecs = null;
    private Map<PropertySpec, Object> propertyValueMap = new HashMap<>();

    protected EndDeviceCommandImpl(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
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

    public abstract List<DeviceMessage> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate);

    protected boolean hasCommandArgumentValueFor(String commandArgumentName) {
        return getPropertyValueMap().keySet().stream().anyMatch(propertySpec -> propertySpec.getName().equals(commandArgumentName));
    }

    protected boolean deviceHasSupportFor(DeviceMessageId deviceMessageId) {
        return findDeviceForEndDevice(endDevice).getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages()
                        .stream()
                        .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getId)
                        .map(DeviceMessageId::havingId)
                        .collect(Collectors.toList())
                        .contains(deviceMessageId))
                .orElse(false);
    }

    protected List<DeviceMessage> doCreateCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate, List<DeviceMessageId> deviceMessageIds) {
        Device multiSenseDevice = findDeviceForEndDevice(getEndDevice());
        List<DeviceMessage> deviceMessages = new ArrayList<>(deviceMessageIds.size());
        int idx = 0;
        for (DeviceMessageId deviceMessageId : deviceMessageIds) {
            Device.DeviceMessageBuilder deviceMessageBuilder = multiSenseDevice.newDeviceMessage(deviceMessageId, TrackingCategory.serviceCall)
                    .setTrackingId(Long.toString(serviceCall.getId()))
                    .setReleaseDate(releaseDate.plusMillis(idx++)); // Add milliseconds to release date in order to ensure the order the device messages are executed is guaranteed
            for (PropertySpec propertySpec : findDeviceMessageSpec(deviceMessageId).getPropertySpecs()) {
                deviceMessageBuilder.addProperty(propertySpec.getName(), getPropertyValueMap().get(propertySpec));
            }
            deviceMessages.add(deviceMessageBuilder.add());
        }
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