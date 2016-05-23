package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.EndDeviceControlTypeMapping;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.UnsupportedCommandException;
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
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.metering.endDeviceCommandFactory",
        service = {HeadEndInterface.class, TranslationKeyProvider.class},
        property = "name=EndDeviceCommandFactory", immediate = true)
public class CommandFactoryImpl implements CommandFactory {
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;

    public CommandFactoryImpl(){}

    public CommandFactoryImpl(MeteringService meteringService, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, NlsService nlsService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.nlsService = nlsService;
        this.thesaurus = thesaurus;
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
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
        System.out.println("HeadEnd CommandFactory activating");
    }


    @Override
    public EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, Instant activationDate, Quantity limit) {
        EndDeviceCommand endDeviceCommand = null;
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
        if (endDeviceControlType.equals(EndDeviceControlTypeMapping.LOAD_CONTROL_TERMINATE
                .getControlType(meteringService)
                .get())) {
            endDeviceCommand = createDisableLoadLimitCommand(endDevice);
        }
        return endDeviceCommand;
    }


    @Override
    public EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen, Instant activationDate) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.ARM.getName();
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(activationDate != null && armForOpen ? DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_ARM);
        if (armForOpen) {
            deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_OPEN);
        } else {
            deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_CLOSE);
        }
        Map<String, Object> attributes = new HashMap<>();
        if (activationDate != null) {
            attributes.put(DeviceMessageConstants.contactorActivationDateAttributeName, Date.from(activationDate));
        }
        return armForOpen ? new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN
                .getControlType(meteringService)
                .get()), deviceMessageIds, attributes, deviceMessageSpecificationService)
                : new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_CLOSURE
                .getControlType(meteringService)
                .get()), deviceMessageIds, attributes, deviceMessageSpecificationService);
    }


    @Override
    public EndDeviceCommand createConnectCommand(EndDevice endDevice, Instant activationDate) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.CONNECT.getName();
        Map<String, Object> attributes = new HashMap<>();
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();

        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_CLOSE);
        if (activationDate != null) {
            attributes.put(DeviceMessageConstants.contactorActivationDateAttributeName, Date.from(activationDate));
        }

        return new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH
                .getControlType(meteringService)
                .get()), deviceMessageIds, attributes, deviceMessageSpecificationService);
    }

    @Override
    public EndDeviceCommand createDisconnectCommand(EndDevice endDevice, Instant activationDate) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.DISCONNECT.getName();
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_OPEN);


        Map<String, Object> attributes = new HashMap<>();
        if (activationDate != null) {
            attributes.put(DeviceMessageConstants.contactorActivationDateAttributeName, Date.from(activationDate));
        }
        return new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH
                .getControlType(meteringService)
                .get()), deviceMessageIds, attributes, deviceMessageSpecificationService);
    }

    @Override
    public EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice, Quantity limit) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.ENABLE_LOAD_LIMIT.getName();;
        //TODO: improve by handling load balancing -
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        //deviceMessageIds.add(tariffs != null ? DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD_WITH_TARIFFS : DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(DeviceMessageConstants.normalThresholdAttributeName, limit);
        return new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.LOAD_CONTROL_INITITATE
                .getControlType(meteringService)
                .get()), deviceMessageIds, attributes, deviceMessageSpecificationService);
    }

    @Override
    public EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice) {
        String commandName = EndDeviceCommandImpl.EndDeviceCommandType.DISABLE_LOAD_LIMIT.getName();;
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING);
        Map<String, Object> attributes = new HashMap<>();
        return new EndDeviceCommandImpl(commandName, endDevice, supportsCommand(endDevice, EndDeviceControlTypeMapping.LOAD_CONTROL_TERMINATE
                .getControlType(meteringService)
                .get()), deviceMessageIds, attributes, deviceMessageSpecificationService);
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
                .orElseThrow(() -> new IllegalArgumentException(MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat() + " " + endDevice.getMRID()));
    }


}
