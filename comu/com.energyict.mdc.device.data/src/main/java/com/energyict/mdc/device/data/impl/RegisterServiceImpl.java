package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

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

    public RegisterServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public Optional<Register> find(RegisterIdentifier identifier) {
        try {
            return this.find(identifier.forIntrospection());
        } catch (UnsupportedRegisterIdentifierTypeName | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<Register> find(Introspector introspector) throws UnsupportedRegisterIdentifierTypeName {
        switch (introspector.getTypeName()) {
            case "Actual": {
                return Optional.of((Register) introspector.getValue("actual"));
            }
            case "DatabaseId": {
                return this.find((long) introspector.getValue("databaseValue"));
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

    private Optional<Register> find(long id) {
        return this.deviceDataModelService.dataModel().mapper(Register.class).getOptional(id);
    }

    private Optional<Register> findByDeviceAndChannelIndex(Device device, int channelIndex) {
        if (channelIndex <= device.getChannels().size()) {
            return Optional.of(device.getRegisters().get(channelIndex));
        }
        else {
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