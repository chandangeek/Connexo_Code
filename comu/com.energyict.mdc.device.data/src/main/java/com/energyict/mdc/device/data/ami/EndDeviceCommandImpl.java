package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.rest.FieldValidationException;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(name = "com.energyict.mdc.device.data.ami.EndDeviceCommand",
        service = {EndDeviceCommand.class, TranslationKeyProvider.class},
        property = "name=EndDeviceCommand", immediate = true)
public class EndDeviceCommandImpl implements EndDeviceCommand {
    private volatile String commandName;
    private volatile EndDeviceControlType endDeviceControlType;
    private volatile EndDevice endDevice;
    private volatile List<DeviceMessageId> deviceMessageIds;
    private volatile Map<String, Object> attributes;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;


    protected enum EndDeviceCommandType {
        ARM("arm"),
        CONNECT("connect"),
        DISCONNECT("disconnect"),
        ENABLE_LOAD_LIMIT("enableLoadLimit"),
        DISABLE_LOAD_LIMIT("disableLoadLimit");

        private final String typeName;


        EndDeviceCommandType (String typeName) {
            this.typeName = typeName;
        }

        public String getName() {
            return typeName;
        }

    }

    //For OSGI purposes
    public EndDeviceCommandImpl() {
    }

    public EndDeviceCommandImpl(String commandName, EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> deviceMessageIds, Map<String, Object> attributes, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.commandName = commandName;
        this.endDeviceControlType = endDeviceControlType;
        this.endDevice = endDevice;
        this.deviceMessageIds = deviceMessageIds;
        this.attributes = attributes;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Activate
    public void activate() {
        System.out.println("Activating EndDevice Command");
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public EndDeviceControlType getEndDeviceControlType() {
        return endDeviceControlType;
    }

    @Override
    public List<PropertySpec> getCommandArgumentSpecs() {
        List<PropertySpec> commandArgumentSpecs = new ArrayList<>();
        getDeviceMessageSpecs().stream()
                .forEach(messageSpec -> commandArgumentSpecs.addAll(messageSpec.getPropertySpecs()));
        return commandArgumentSpecs;
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public void setPropertyValue(PropertySpec propertySpec, Object value) {
        List<PropertySpec> argumentSpecs = getCommandArgumentSpecs();

        if (!argumentSpecs.contains(propertySpec)) {
            throw new FieldValidationException("Property spec not found", propertySpec.getName());
        } else {
            if (!propertySpec.getValueFactory().getValueType().isAssignableFrom(value.getClass())) {
                throw new FieldValidationException("Incorrect type", propertySpec.getName());
            }
            attributes.put(propertySpec.getName(),value);

        }
    }

    @Override
    public List<Long> getDeviceMessageIds(){
        List<Long> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.stream().forEach(id -> deviceMessageIds.add(id.longValue()));
        return deviceMessageIds;
    }

    private List<DeviceMessageSpec> getDeviceMessageSpecs() {
        List<DeviceMessageSpec> deviceMessageSpecs = new ArrayList<>();
        deviceMessageIds.stream()
                .forEach(msgId -> this.deviceMessageSpecificationService.findMessageSpecById(msgId
                        .dbValue()).ifPresent(foundMsgSpec -> deviceMessageSpecs.add(foundMsgSpec)));
        return deviceMessageSpecs;
    }

    public String getName(){
        return commandName;
    }



 }
