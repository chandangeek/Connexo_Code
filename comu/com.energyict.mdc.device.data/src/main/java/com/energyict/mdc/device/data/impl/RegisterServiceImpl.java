package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.util.streams.Currying.use;

/**
 * Provides an implementation for the {@link RegisterService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-23 (12:46)
 */
public class RegisterServiceImpl implements RegisterService {
    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public RegisterServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public Optional<Register> find(RegisterIdentifier identifier) {
        try {
            Optional<Device> device = deviceDataModelService.deviceService().findDeviceByIdentifier(identifier.getDeviceIdentifier());
            if (device.isPresent()) {
                return this.find(identifier.forIntrospection(), device.get());
            } else {
                return Optional.empty();
            }
        } catch (UnsupportedRegisterIdentifierTypeName | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<Register> find(Introspector introspector, Device device) throws UnsupportedRegisterIdentifierTypeName {
        switch (introspector.getTypeName()) {
            case "Actual": {
                return Optional.of((Register) introspector.getValue("actual"));
            }
            case "DatabaseId": {
                return this.find(device, Long.valueOf(introspector.getValue("databaseValue").toString()));
            }
            case "PrimeRegisterForChannel": {
                DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue("device");
                int channelIndex = (int) introspector.getValue("channelIndex");
                return this.deviceDataModelService.deviceService()
                        .findDeviceByIdentifier(deviceIdentifier)
                        .flatMap(use(this::findByDeviceAndChannelIndex).with(channelIndex));
            }
            case "DeviceIdentifierAndObisCode": {
                DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue("device");
                ObisCode registerObisCode = (ObisCode) introspector.getValue("obisCode");
                return this.deviceDataModelService.deviceService()
                        .findDeviceByIdentifier(deviceIdentifier)
                        .flatMap(use(this::findByDeviceAndObisCode).with(registerObisCode));
            }
            default: {
                throw new UnsupportedRegisterIdentifierTypeName();
            }
        }
    }

    private Optional<Register> find(Device device, long id) {
        return device.getRegisters().stream().filter(register -> register.getRegisterSpecId() == id).findAny();
    }

    private Optional<Register> findByDeviceAndChannelIndex(Device device, int channelIndex) {
        if (channelIndex <= device.getChannels().size()) {
            return Optional.of(device.getRegisters().get(channelIndex));
        } else {
            // Not enough channels
            return Optional.empty();
        }
    }

    private Optional<Register> findByDeviceAndObisCode(Device device, ObisCode obisCode) {
        return device.getRegisterWithDeviceObisCode(obisCode);
    }

    private static class UnsupportedRegisterIdentifierTypeName extends RuntimeException {
    }

}