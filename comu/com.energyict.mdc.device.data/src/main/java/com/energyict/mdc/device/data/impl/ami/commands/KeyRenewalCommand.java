package com.energyict.mdc.device.data.impl.ami.commands;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.TrackingCategory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.EndDeviceCommandImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class KeyRenewalCommand extends EndDeviceCommandImpl {

    public KeyRenewalCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        super(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);
    }

    @Override
    public List<DeviceMessage> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate) {
        //TODO: see how we get here, the device configuration security accesor object
        //TODO: get from security accessor all required info, including the command to execute
        DeviceMessageId deviceMessageId = DeviceMessageId.SECURITY_KEY_RENEWAL;//securityAccessorType.getKeyRenewalMessageId();
        //TODO: get all device message properties and add them to endDeviceCommand
        //setPropertyValue();
        Device multiSenseDevice = findDeviceForEndDevice(getEndDevice());

        Device.DeviceMessageBuilder deviceMessageBuilder = multiSenseDevice.newDeviceMessage(deviceMessageId, TrackingCategory.serviceCall)
                .setTrackingId(Long.toString(serviceCall.getId()))
                .setReleaseDate(releaseDate);
        for (PropertySpec propertySpec : findDeviceMessageSpec(deviceMessageId).getPropertySpecs()) {
            deviceMessageBuilder.addProperty(propertySpec.getName(), getPropertyValueMap().get(propertySpec));
        }
        List<DeviceMessage> deviceMessages = new ArrayList<>();
        deviceMessages.add(deviceMessageBuilder.add());
        return deviceMessages;
//        return doCreateCorrespondingMultiSenseDeviceMessages(serviceCall, Instant.now(), Collections.singletonList(DeviceMessageId.SECURITY_KEY_RENEWAL));
    }
}