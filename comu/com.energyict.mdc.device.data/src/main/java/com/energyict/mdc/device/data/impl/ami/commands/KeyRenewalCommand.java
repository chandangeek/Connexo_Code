package com.energyict.mdc.device.data.impl.ami.commands;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.TrackingCategory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ami.EndDeviceCommandImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class KeyRenewalCommand extends EndDeviceCommandImpl {

    private SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType;

    public KeyRenewalCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        super(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);
    }

    @Override
    public List<DeviceMessage> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate) {

        if (securityAccessorTypeOnDeviceType == null) {
            //should never end up here
            throw new IllegalStateException("Cannot continue! Security accessor object was not initialized.");
        }

        DeviceMessageId deviceMessageId = extractDeviceMessageId();
        List<PropertySpec> propertySpecs = findDeviceMessageSpec(deviceMessageId).getPropertySpecs();

        configureCommandAttributes(propertySpecs);

        return buildDeviceCommand(serviceCall, releaseDate, deviceMessageId, propertySpecs);
    }

    private List<DeviceMessage> buildDeviceCommand(ServiceCall serviceCall, Instant releaseDate, DeviceMessageId deviceMessageId, List<PropertySpec> propertySpecs) {
        Device multiSenseDevice = findLockedDeviceForEndDevice(getEndDevice());
        Device.DeviceMessageBuilder deviceMessageBuilder = multiSenseDevice.newDeviceMessage(deviceMessageId, TrackingCategory.serviceCall)
                .setTrackingId(Long.toString(serviceCall.getId()))
                .setReleaseDate(releaseDate);

        propertySpecs.forEach(propertySpec -> deviceMessageBuilder.addProperty(propertySpec.getName(), getPropertyValueMap().get(propertySpec)));
        List<DeviceMessage> deviceMessages = new ArrayList<>();
        deviceMessages.add(deviceMessageBuilder.add());
        return deviceMessages;
    }

    private void configureCommandAttributes(List<PropertySpec> propertySpecs) {
        PropertySpec accessorPropertySpec = extractSecurityAccessorPropertySpec(propertySpecs);
        setPropertyValue(accessorPropertySpec, securityAccessorTypeOnDeviceType.getSecurityAccessorType());

        //set all required commands attributes, but exclude the ones not filled in.
        //On security accessor all other properties are filled in except the ones referring to security accessor
        securityAccessorTypeOnDeviceType.getKeyRenewalAttributes()
                .stream()
                .filter(securityAccessorTypeOnDeviceType -> securityAccessorTypeOnDeviceType.getValue() != null)
                .forEach(securityAccessorTypeKeyRenewal -> setPropertyValue(securityAccessorTypeKeyRenewal.getSpecification(), securityAccessorTypeKeyRenewal.getValue()));
    }

    private PropertySpec extractSecurityAccessorPropertySpec(List<PropertySpec> propertySpecs) {
        List<PropertySpec> securityAccessorProperties = extractKeyTypePropertySpecs(propertySpecs);

        //We expect only one security accessor to be available
        if (securityAccessorProperties.size() > 1) {
            throw new IllegalStateException(thesaurus.getFormat(MessageSeeds.ONLY_ONE_KEY_TYPE_ATTRIBUTE_EXPECTED).format());
        }

        return securityAccessorProperties
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COMMAND_SHOULD_HAVE_A_KEY_TYPE_ATTRIBUTE)
                        .format(getEndDeviceControlType().getName())));
    }

    private List<PropertySpec> extractKeyTypePropertySpecs(List<PropertySpec> propertySpecs) {
        return propertySpecs
                .stream()
                .filter(propertySpec -> propertySpec.isReference() && SecurityAccessorType.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()))
                .collect(Collectors.toList());
    }

    private DeviceMessageId extractDeviceMessageId() {
        return securityAccessorTypeOnDeviceType.getKeyRenewalDeviceMessageId()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_KEY_RENEWAL_COMMAND_CONFIGURED)
                        .format(securityAccessorTypeOnDeviceType.getSecurityAccessorType().getName())));
    }

    public void setSecurityAccessorOnDeviceType(SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType) {
        this.securityAccessorTypeOnDeviceType = securityAccessorTypeOnDeviceType;
    }
}