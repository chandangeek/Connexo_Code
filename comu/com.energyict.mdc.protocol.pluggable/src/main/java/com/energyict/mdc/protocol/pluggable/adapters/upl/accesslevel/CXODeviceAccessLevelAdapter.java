package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

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
public class CXODeviceAccessLevelAdapter implements DeviceAccessLevel {

    private final com.energyict.mdc.protocol.api.security.DeviceAccessLevel cxoDeviceAccessLevel;

    public CXODeviceAccessLevelAdapter(com.energyict.mdc.protocol.api.security.DeviceAccessLevel cxoDeviceAccessLevel) {
        this.cxoDeviceAccessLevel = cxoDeviceAccessLevel;
    }

    @Override
    public int getId() {
        return cxoDeviceAccessLevel.getId();
    }

    @Override
    public String getTranslationKey() {
        return "";  //TODO is this key used in Connexo?
    }

    @Override
    public String getDefaultTranslation() {
        return cxoDeviceAccessLevel.getTranslation();
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return cxoDeviceAccessLevel.getSecurityProperties().stream()
                .map(ConnexoToUPLPropertSpecAdapter::new)
                .collect(Collectors.toList());
    }
}