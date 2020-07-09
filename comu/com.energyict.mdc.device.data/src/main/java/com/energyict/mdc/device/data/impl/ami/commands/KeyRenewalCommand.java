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

    private boolean forServiceKey;
    private List<SecurityAccessorTypeOnDeviceType> securityAccessorTypeOnDeviceTypes;

    public KeyRenewalCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        super(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);
    }

    public void setServiceKey(boolean serviceKey) {
        forServiceKey = serviceKey;
    }

    @Override
    public List<DeviceMessage> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate) {
        List<DeviceMessage> deviceMessages = new ArrayList<>();

        if (securityAccessorTypeOnDeviceTypes == null || securityAccessorTypeOnDeviceTypes.isEmpty()) {
            //should never end up here
            throw new IllegalStateException("Cannot continue! Security accessors are not initialized.");
        }

        for (SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType: securityAccessorTypeOnDeviceTypes) {
            DeviceMessageId deviceMessageId = extractDeviceMessageId(securityAccessorTypeOnDeviceType);
            List<PropertySpec> propertySpecs = findDeviceMessageSpec(deviceMessageId).getPropertySpecs();

            configureCommandAttributes(securityAccessorTypeOnDeviceType, propertySpecs);

            deviceMessages.add(buildDeviceCommand(serviceCall, releaseDate, deviceMessageId, propertySpecs));
        }

        return deviceMessages;
    }

    private DeviceMessage buildDeviceCommand(ServiceCall serviceCall, Instant releaseDate, DeviceMessageId deviceMessageId, List<PropertySpec> propertySpecs) {
        Device multiSenseDevice = findDeviceForEndDevice(getEndDevice());
        Device.DeviceMessageBuilder deviceMessageBuilder = multiSenseDevice.newDeviceMessage(deviceMessageId, TrackingCategory.serviceCall)
                .setTrackingId(Long.toString(serviceCall.getId()))
                .setReleaseDate(releaseDate);

        propertySpecs.forEach(propertySpec -> deviceMessageBuilder.addProperty(propertySpec.getName(), getPropertyValueMap().get(propertySpec)));
        return deviceMessageBuilder.add();
    }

    private void configureCommandAttributes(SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType, List<PropertySpec> propertySpecs) {
        if (!propertySpecs.isEmpty()) {
            PropertySpec accessorPropertySpec = extractSecurityAccessorPropertySpec(propertySpecs);
            setPropertyValue(accessorPropertySpec, securityAccessorTypeOnDeviceType.getSecurityAccessorType());
        }

        //set all required commands attributes, but exclude the ones not filled in.
        //On security accessor all other properties are filled in except the ones referring to security accessor
        (forServiceKey ? securityAccessorTypeOnDeviceType.getServiceKeyRenewalAttributes() : securityAccessorTypeOnDeviceType.getKeyRenewalAttributes())
                .stream()
                .filter(securityAccessorTypeKeyRenewal -> securityAccessorTypeKeyRenewal.getValue() != null)
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

    private DeviceMessageId extractDeviceMessageId(SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType) {
        if (forServiceKey) {
            return securityAccessorTypeOnDeviceType.getServiceKeyRenewalDeviceMessageId()
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_SERVICE_KEY_RENEWAL_COMMAND_CONFIGURED)
                            .format(securityAccessorTypeOnDeviceType.getSecurityAccessorType().getName())));
        }
        return securityAccessorTypeOnDeviceType.getKeyRenewalDeviceMessageId()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_KEY_RENEWAL_COMMAND_CONFIGURED)
                        .format(securityAccessorTypeOnDeviceType.getSecurityAccessorType().getName())));
    }

    public void setSecurityAccessorOnDeviceTypes(List<SecurityAccessorTypeOnDeviceType> securityAccessorTypeOnDeviceTypes) {
        this.securityAccessorTypeOnDeviceTypes = securityAccessorTypeOnDeviceTypes;
    }
}