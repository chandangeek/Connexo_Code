package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;

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
                .map(this::toConnexo)
                .collect(Collectors.toList());
    }

    private PropertySpec toConnexo(com.energyict.mdc.upl.properties.PropertySpec propertySpec) {
        if (propertySpec instanceof ConnexoToUPLPropertSpecAdapter) {
            return ((ConnexoToUPLPropertSpecAdapter) propertySpec).getConnexoPropertySpec();
        } else {
            return new UPLToConnexoPropertySpecAdapter(propertySpec);
        }
    }

}
