package com.energyict.mdc.protocol.pluggable.impl.adapters.upl.accesslevel;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLToConnexoPropertySpecAdapter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts a given UPL DeviceAccessLevel into a proper CXO DeviceAccessLevel
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/01/2017 - 14:25
 */
public class UPLDeviceAccessLevelAdapter implements DeviceAccessLevel {

    private final com.energyict.mdc.upl.security.DeviceAccessLevel uplDeviceAccessLevel;

    public UPLDeviceAccessLevelAdapter(com.energyict.mdc.upl.security.DeviceAccessLevel uplDeviceAccessLevel) {
        this.uplDeviceAccessLevel = uplDeviceAccessLevel;
    }

    @Override
    public int getId() {
        return uplDeviceAccessLevel.getId();
    }

    @Override
    public String getTranslation() {
        return uplDeviceAccessLevel.getDefaultTranslation();
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return uplDeviceAccessLevel.getSecurityProperties().stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }
}
