package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.EndDeviceControlTypeMapping;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.UnsupportedCommandException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.device.data.impl.ami.EndDeviceCommandFactory",
        service = {CommandFactory.class, TranslationKeyProvider.class},
        property = "name=EndDeviceCommandFactory", immediate = true)
public class EndDeviceCommandFactoryImpl implements CommandFactory, TranslationKeyProvider {
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;


    public EndDeviceCommandFactoryImpl() {
    }

    public EndDeviceCommandFactoryImpl(MeteringService meteringService, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, NlsService nlsService, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.nlsService = nlsService;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }


    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
        System.out.println("Activatiing Head End Command Factory");
    }


    @Override
    public EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, Instant activationDate, Quantity limit) {
        EndDeviceCommand endDeviceCommand = null;

        //beautify using switch with enum
        if (endDeviceControlType.equals(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN
                .getControlType(meteringService)
                .get())) {
            endDeviceCommand = createArmCommand(endDevice, true, activationDate);
        }
        if (endDeviceControlType.equals(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_CLOSURE
                .getControlType(meteringService)
                .get())) {
            endDeviceCommand = createArmCommand(endDevice, false, activationDate);
        }
        if (endDeviceControlType.equals(EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH
                .getControlType(meteringService)
                .get())) {
            endDeviceCommand = createConnectCommand(endDevice, activationDate);
        }
        if (endDeviceControlType.equals(EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH
                .getControlType(meteringService)
                .get())) {
            endDeviceCommand = createDisconnectCommand(endDevice, activationDate);
        }
        if (endDeviceControlType.equals(EndDeviceControlTypeMapping.LOAD_CONTROL_INITITATE
                .getControlType(meteringService)
                .get())) {
            endDeviceCommand = createEnableLoadLimitCommand(endDevice, limit);
        }
        return endDeviceCommand;
    }


    @Override
    public EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen, Instant activationDate) {
        EndDeviceCommand endDeviceCommand;
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.ARM.getName();
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_ARM);
        if (armForOpen) {
            deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_OPEN);
            endDeviceCommand = new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN
                    .getControlType(meteringService)
                    .get()), deviceMessageIds, propertySpecService, deviceMessageSpecificationService).init();
        } else {
            deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_CLOSE);
            endDeviceCommand = new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_CLOSURE
                    .getControlType(meteringService)
                    .get()), deviceMessageIds, propertySpecService, deviceMessageSpecificationService).init();
        }
        if (activationDate != null) {
            updateCommandPropertySpec(endDeviceCommand, DeviceMessageConstants.contactorActivationDateAttributeName, Date
                    .from(activationDate));
        }
        return endDeviceCommand;
    }


    @Override
    public EndDeviceCommand createConnectCommand(EndDevice endDevice, Instant activationDate) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.CONNECT.getName();
        Map<String, Object> attributes = new HashMap<>();
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();

        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_CLOSE);

        EndDeviceCommand endDeviceCommand = new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH
                .getControlType(meteringService)
                .get()), deviceMessageIds, propertySpecService, deviceMessageSpecificationService).init();
        if (activationDate != null) {
            updateCommandPropertySpec(endDeviceCommand, DeviceMessageConstants.contactorActivationDateAttributeName, Date
                    .from(activationDate));
        }
        return endDeviceCommand;
    }

    @Override
    public EndDeviceCommand createDisconnectCommand(EndDevice endDevice, Instant activationDate) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.DISCONNECT.getName();
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_OPEN);
        EndDeviceCommand endDeviceCommand = new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH
                .getControlType(meteringService)
                .get()), deviceMessageIds, propertySpecService, deviceMessageSpecificationService).init();
        if (activationDate != null) {
            updateCommandPropertySpec(endDeviceCommand, DeviceMessageConstants.contactorActivationDateAttributeName, Date
                    .from(activationDate));
        }
        return endDeviceCommand;
    }

    @Override
    public EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice, Quantity limit) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.ENABLE_LOAD_LIMIT.getName();
        //TODO: improve by handling load balancing
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        //deviceMessageIds.add(tariffs != null ? DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD_WITH_TARIFFS : DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD);
        deviceMessageIds.add(DeviceMessageId.LOAD_BALANCING_ENABLE_LOAD_LIMITING);
        EndDeviceCommand endDeviceCommand = new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.LOAD_CONTROL_INITITATE
                .getControlType(meteringService)
                .get()), deviceMessageIds, propertySpecService, deviceMessageSpecificationService).init();
        if (limit != null) {
            updateCommandPropertySpec(endDeviceCommand, DeviceMessageConstants.normalThresholdAttributeName, limit);
        }
        return endDeviceCommand;
    }

    @Override
    public EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.DISABLE_LOAD_LIMIT.getName();
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING);
        return new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.LOAD_CONTROL_TERMINATE
                .getControlType(meteringService)
                .get()), deviceMessageIds, propertySpecService, deviceMessageSpecificationService).init();
    }

    private EndDeviceControlType supportsCommand(EndDevice endDevice, EndDeviceControlType type) {
        Device device = findDeviceForEndDevice(endDevice);
        int endDeviceType = EndDeviceType.get(Math.toIntExact(device.getDeviceConfiguration().getDeviceType().getId()))
                .getValue();
        List<EndDeviceControlType> controlTypes = new ArrayList<>();
        Stream.of(EndDeviceControlTypeMapping.values()).forEach(typ -> {
            if (typ.getEndDeviceControlTypeMRID().startsWith(String.valueOf(endDeviceType) + ".")
                    || typ.getEndDeviceControlTypeMRID().startsWith("0.")) {
                meteringService.getEndDeviceControlType(typ.getEndDeviceControlTypeMRID())
                        .ifPresent(found -> controlTypes.add(found));
            }
        });
        return controlTypes.stream()
                .filter(controlType ->
                        controlType.equals(type))
                .findFirst()
                .orElseThrow(() -> new UnsupportedCommandException(thesaurus, type.toString(), endDevice
                        .getMRID()));
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        return deviceService.findByUniqueMrid(endDevice.getMRID())
                .orElseThrow(() -> new IllegalArgumentException(MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat() + " " + endDevice
                        .getMRID()));
    }

    private void updateCommandPropertySpec(EndDeviceCommand endDeviceCommand, String propertySpecName, Object value) {
        Optional<PropertySpec> propertySpec = endDeviceCommand.getCommandArgumentSpecs()
                .stream()
                .filter(spec -> spec.getName()
                        .equals(propertySpecName))
                .findFirst();
        //will only update an existing one
        if (propertySpec.isPresent()) {
            endDeviceCommand.setPropertyValue(propertySpec.get(), value);
        }
    }


    @Override
    public String getComponentName() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        //TBD
        return null;
    }
}
