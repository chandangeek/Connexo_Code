package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts a given UPL DeviceAccessLevel into a proper CXO DeviceAccessLevel
 * <p>
 *
 *
 * @author khe
 * @since 3/01/2017 - 14:25
 */
public class UPLDeviceAccessLevelAdapter implements DeviceAccessLevel {

    protected final com.energyict.mdc.upl.security.DeviceAccessLevel uplDeviceAccessLevel;

    public UPLDeviceAccessLevelAdapter(com.energyict.mdc.upl.security.DeviceAccessLevel uplDeviceAccessLevel) {
        this.uplDeviceAccessLevel = uplDeviceAccessLevel;
    }

    public com.energyict.mdc.upl.security.DeviceAccessLevel getUplDeviceAccessLevel() {
        return uplDeviceAccessLevel;
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
                .map(UPLToConnexoPropertySpecAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return uplDeviceAccessLevel != null ? uplDeviceAccessLevel.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UPLDeviceAccessLevelAdapter) {
            return uplDeviceAccessLevel.equals(((UPLDeviceAccessLevelAdapter) obj).uplDeviceAccessLevel);
        } else {
            return uplDeviceAccessLevel.equals(obj);
        }
    }
}
