package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
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

@Component(name = "com.energyict.mdc.device.data.impl.ami.EndDeviceCommand",
        service = {EndDeviceCommand.class},
        property = "name=EndDeviceCommand", immediate = true)
public class EndDeviceCommandImpl implements EndDeviceCommand {
    private volatile String commandName;
    private volatile EndDeviceControlType endDeviceControlType;
    private volatile EndDevice endDevice;
    private volatile List<DeviceMessageId> deviceMessageIds;
    private volatile PropertySpecService propertySpecService;
    private volatile List<PropertySpec> commandArgumentSpecs = new ArrayList<>();
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;


    protected enum EndDeviceCommandType {
        ARM("arm"),
        CONNECT("connect"),
        DISCONNECT("disconnect"),
        ENABLE_LOAD_LIMIT("enableLoadLimit"),
        DISABLE_LOAD_LIMIT("disableLoadLimit");

        private final String typeName;


        EndDeviceCommandType(String typeName) {
            this.typeName = typeName;
        }

        public String getName() {
            return typeName;
        }

    }

    //For OSGI purposes
    public EndDeviceCommandImpl() {
    }

    public EndDeviceCommandImpl(String commandName, EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> deviceMessageIds, PropertySpecService propertySpecService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.commandName = commandName;
        this.endDeviceControlType = endDeviceControlType;
        this.endDevice = endDevice;
        this.deviceMessageIds = deviceMessageIds;
        this.propertySpecService = propertySpecService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    EndDeviceCommandImpl init() {
        initCommandArgumentSpecs();
        return this;
    }


    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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
        return commandArgumentSpecs;
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice;
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
            PropertySpec newPropertySpec = propertySpecService.
                    specForValuesOf(propertySpec.getValueFactory())
                    .named(propertySpec.getName(), propertySpec.getDisplayName())
                    .describedAs(propertySpec.getDescription())
                    .setDefaultValue(value)
                    .finish();
            argumentSpecs.set(argumentSpecs.indexOf(propertySpec), newPropertySpec);



        }
    }

    @Override
    public List<Long> getDeviceMessageIds() {
        List<Long> longDeviceMessageIds = new ArrayList<>();
        deviceMessageIds.stream().forEach(id -> longDeviceMessageIds.add(id.dbValue()));
        return longDeviceMessageIds;
    }

    private List<DeviceMessageSpec> getDeviceMessageSpecs() {
        List<DeviceMessageSpec> deviceMessageSpecs = new ArrayList<>();
        deviceMessageIds.stream()
                .forEach(msgId -> this.deviceMessageSpecificationService.findMessageSpecById(msgId
                        .dbValue()).ifPresent(foundMsgSpec -> deviceMessageSpecs.add(foundMsgSpec)));
        return deviceMessageSpecs;
    }

    public String getName() {
        return commandName;
    }

    private void initCommandArgumentSpecs() {
        getDeviceMessageSpecs().stream()
                .forEach(messageSpec -> commandArgumentSpecs.addAll(messageSpec.getPropertySpecs()));
    }
}
