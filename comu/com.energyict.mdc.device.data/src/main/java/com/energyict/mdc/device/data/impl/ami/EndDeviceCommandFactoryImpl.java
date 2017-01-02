package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.UnsupportedCommandException;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.EndDeviceCommandFactory;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.device.data.impl.ami.EndDeviceCommandFactory",
        service = {EndDeviceCommandFactory.class},
        property = "name=EndDeviceCommandFactory", immediate = true)
public class EndDeviceCommandFactoryImpl implements EndDeviceCommandFactory {

    private static final String UNDEFINED_UNIT = "undefined";

    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;

    //For OSGI purposes
    public EndDeviceCommandFactoryImpl() {
    }

    public EndDeviceCommandFactoryImpl(MeteringService meteringService, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
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

    @Activate
    public void activate() {
    }

    @Override
    public EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType) {
        EndDeviceControlTypeMapping endDeviceControlTypeMapping = EndDeviceControlTypeMapping.getMappingFor(endDeviceControlType);
        if (!multiSenseDeviceHasSupportForEndDeviceControlType(endDevice, endDeviceControlTypeMapping)) {
            throw new UnsupportedCommandException(thesaurus, endDeviceControlType.getName(), endDevice.getMRID());
        }
        return endDeviceControlTypeMapping.getNewEndDeviceCommand(
                endDevice,
                endDeviceControlType,
                endDeviceControlTypeMapping.getPossibleDeviceMessageIds(),
                deviceService,
                deviceMessageSpecificationService,
                thesaurus).orElseThrow(() -> new UnsupportedCommandException(thesaurus, endDeviceControlType.getName(), endDevice.getMRID()));
    }

    /**
     * Finds out if the multisense device supports execution of the necessary device commands, who are
     * associated with the given endDeviceControlTypeMapping.
     *
     * @param endDevice
     * @param endDeviceControlTypeMapping
     * @return true in case the device supports the given EndDeviceControlType, false otherwise
     */
    private boolean multiSenseDeviceHasSupportForEndDeviceControlType(EndDevice endDevice, EndDeviceControlTypeMapping endDeviceControlTypeMapping) {
        List<DeviceMessageId> supportedMessages = findDeviceForEndDevice(endDevice).getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getId)
                        .map(DeviceMessageId::havingId)
                        .collect(Collectors.toList())).orElse(Collections.emptyList());

        return endDeviceControlTypeMapping.getPossibleDeviceMessageIdGroups().stream().anyMatch(supportedMessages::containsAll);
    }

    @Override
    public EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen, Instant activationDate) {
        EndDeviceControlType endDeviceControlType = armForOpen
                ? findEndDeviceControlType(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN)
                : findEndDeviceControlType(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_CLOSURE);
        EndDeviceCommand command = this.createCommand(endDevice, endDeviceControlType);
        if (activationDate != null) {
            command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.contactorActivationDateAttributeName), Date.from(activationDate));
        }
        return command;
    }

    @Override
    public EndDeviceCommand createConnectCommand(EndDevice endDevice, Instant activationDate) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH));
        if (activationDate != null) {
            command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.contactorActivationDateAttributeName), Date.from(activationDate));
        }
        return command;
    }

    @Override
    public EndDeviceCommand createDisconnectCommand(EndDevice endDevice, Instant activationDate) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH));
        if (activationDate != null) {
            command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.contactorActivationDateAttributeName), Date.from(activationDate));
        }
        return command;
    }

    @Override
    public EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice, Quantity quantity) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.LOAD_CONTROL_INITIATE));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.normalThresholdAttributeName), quantity.getValue());
        Unit unit = Unit.get(quantity.getUnit().getSymbol(), quantity.getMultiplier());
        command.setPropertyValue(
                getCommandArgumentSpec(command, DeviceMessageConstants.unitAttributeName),
                unit.isUndefined() ? UNDEFINED_UNIT : unit.toString()
        );
        return command;
    }

    @Override
    public EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice) {
        return this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.LOAD_CONTROL_TERMINATE));
    }

    private EndDeviceControlType findEndDeviceControlType(EndDeviceControlTypeMapping controlTypeMapping) {
        String mrid = controlTypeMapping.getEndDeviceControlTypeMRID();
        return meteringService.getEndDeviceControlType(mrid).orElseThrow(NoSuchElementException.endDeviceControlTypeWithMRIDNotFound(thesaurus, mrid));
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        return deviceService.findByUniqueMrid(endDevice.getMRID()).orElseThrow(NoSuchElementException.deviceWithMRIDNotFound(thesaurus, endDevice.getMRID()));
    }

    private PropertySpec getCommandArgumentSpec(EndDeviceCommand endDeviceCommand, String commandArgumentName) {
        return endDeviceCommand.getCommandArgumentSpecs().stream()
                .filter(propertySpec -> propertySpec.getName().equals(commandArgumentName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COMMAND_ARGUMENT_SPEC_NOT_FOUND)
                        .format(commandArgumentName, endDeviceCommand.getEndDeviceControlType().getName())));
    }
}