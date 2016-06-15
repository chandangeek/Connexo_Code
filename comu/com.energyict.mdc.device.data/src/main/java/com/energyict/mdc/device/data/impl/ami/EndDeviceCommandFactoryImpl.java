package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.exceptions.UnsupportedCommandException;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Set;

@Component(name = "com.energyict.mdc.device.data.impl.ami.EndDeviceCommandFactory",
        service = {CommandFactory.class},
        property = "name=EndDeviceCommandFactory", immediate = true)
public class EndDeviceCommandFactoryImpl implements CommandFactory {

    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;

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
        System.out.println("Activating Head End Command Factory");
    }

    @Override
    public EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType) {
        EndDeviceControlTypeMapping endDeviceControlTypeMapping = EndDeviceControlTypeMapping.getMappingFor(endDeviceControlType);
        if (!multiSenseDeviceHasSupportForEndDeviceControlType(endDevice, endDeviceControlTypeMapping)) {
            throw new UnsupportedCommandException(thesaurus, endDeviceControlType.getName(), endDevice.getMRID());
        }
        return new EndDeviceCommandImpl(
                endDevice,
                endDeviceControlType,
                endDeviceControlTypeMapping.getPossibleDeviceMessageIds(),
                deviceService,
                deviceMessageSpecificationService,
                thesaurus);
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
        Set<DeviceMessageId> supportedMessages = findDeviceForEndDevice(endDevice).getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();
        return endDeviceControlTypeMapping.getPossibleDeviceMessageIdGroups().stream().anyMatch(supportedMessages::containsAll);
    }

    @Override
    public EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen) {
        EndDeviceControlType endDeviceControlType = armForOpen
                ? findEndDeviceControlType(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN)
                : findEndDeviceControlType(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_CLOSURE);
        return this.createCommand(endDevice, endDeviceControlType);
    }

    @Override
    public EndDeviceCommand createConnectCommand(EndDevice endDevice) {
        return this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH));
    }

    @Override
    public EndDeviceCommand createDisconnectCommand(EndDevice endDevice) {
        return this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH));
    }

    @Override
    public EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice) {
        return this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.LOAD_CONTROL_INITITATE));
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
}
