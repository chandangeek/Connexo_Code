package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.util.streams.Currying.use;

/**
 * Provides an implementation for the {@link RegisterService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-23 (12:46)
 */
public class RegisterServiceImpl implements ServerRegisterService {

    /**
     * Enum listing up all different Introspector types that can be used in method RegisterServiceImpl#find(Introspector, Device)
     */
    public enum IntrospectorTypes {
        DatabaseId("databaseValue", "device", "obisCode"),
        DeviceIdentifierAndObisCode("device", "obisCode"),
        PrimeRegisterForChannel("device", "channelIndex", "obisCode"),
        Actual("actual");

        private final String[] roles;

        IntrospectorTypes(String... roles) {
            this.roles = roles;
        }

        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList(roles));
        }

        public static Optional<IntrospectorTypes> forName(String name) {
            return Arrays.stream(values()).filter(type -> type.name().equals(name)).findFirst();
        }
    }

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public RegisterServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
        Services.registerFinder(this);
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.Register> find(RegisterIdentifier identifier) {
        return this.findByIdentifier(identifier).map(com.energyict.mdc.upl.meterdata.Register.class::cast);
    }

    @Override
    public Optional<Register> findByIdentifier(RegisterIdentifier identifier) {
        try {
            Optional<Device> device = deviceDataModelService.deviceService().findDeviceByIdentifier(identifier.getDeviceIdentifier());
            return device.flatMap(device1 -> this.find(identifier.forIntrospection(), device1));
        } catch (UnsupportedRegisterIdentifierTypeName | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<Register> find(Introspector introspector, Device device) throws UnsupportedRegisterIdentifierTypeName {
        if (introspector.getTypeName().equals(IntrospectorTypes.Actual.name())) {
            return Optional.of((Register) introspector.getValue(IntrospectorTypes.Actual.roles[0]));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.DatabaseId.name())) {
            return this.find(device, Long.valueOf(introspector.getValue(IntrospectorTypes.DatabaseId.roles[0]).toString()));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.PrimeRegisterForChannel.name())) {
            int channelIndex = (int) introspector.getValue(IntrospectorTypes.PrimeRegisterForChannel.roles[1]);
            return Optional.of(device).flatMap(use(this::findByDeviceAndChannelIndex).with(channelIndex));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.DeviceIdentifierAndObisCode.name())) {
            ObisCode registerObisCode = (ObisCode) introspector.getValue(IntrospectorTypes.DeviceIdentifierAndObisCode.roles[1]);
            return Optional.of(device).flatMap(use(this::findByDeviceAndObisCode).with(registerObisCode));
        } else {
            throw new UnsupportedRegisterIdentifierTypeName();
        }
    }

    protected Optional<Register> find(Device device, long id) {
        return device.getRegisters().stream().filter(register -> register.getRegisterSpecId() == id).findAny();
    }

    protected Optional<Register> findByDeviceAndChannelIndex(Device device, int channelIndex) {
        if (channelIndex <= device.getChannels().size()) {
            return Optional.of(device.getRegisters().get(channelIndex));
        } else {
            // Not enough channels
            return Optional.empty();
        }
    }

    protected Optional<Register> findByDeviceAndObisCode(Device device, ObisCode obisCode) {
        return device.getRegisterWithDeviceObisCode(obisCode);
    }

    private static class UnsupportedRegisterIdentifierTypeName extends RuntimeException {
    }

}