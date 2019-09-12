package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.AbstractConnectionTypeDelegate;
import com.energyict.mdc.device.config.KeyAccessorPropertySpecWithPossibleValues;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.ConnectionType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConnectionTypeWithPossibleValues extends AbstractConnectionTypeDelegate {

    private Device device;

    protected ConnectionTypeWithPossibleValues() {
        super();
    }

    public ConnectionTypeWithPossibleValues(ConnectionType connectionType, Device device) {
        super(connectionType);
        this.device = device;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getInnerConnectionType().getPropertySpecs().stream().
                map(ps -> KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> getDevice().getDeviceType().getSecurityAccessorTypes(), ps)).
                collect(Collectors.toList());
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        return getInnerConnectionType().getPropertySpec(name).
                map(ps -> KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> getDevice().getDeviceType().getSecurityAccessorTypes(), ps));
    }

    public Device getDevice() {
        return this.device;
    }
}
