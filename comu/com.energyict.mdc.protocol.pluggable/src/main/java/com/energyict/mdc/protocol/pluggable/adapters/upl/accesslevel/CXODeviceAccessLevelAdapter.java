package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.DeviceAccessLevel;

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
public class CXODeviceAccessLevelAdapter implements DeviceAccessLevel {

    protected final com.energyict.mdc.protocol.api.security.DeviceAccessLevel cxoDeviceAccessLevel;

    public CXODeviceAccessLevelAdapter(com.energyict.mdc.protocol.api.security.DeviceAccessLevel cxoDeviceAccessLevel) {
        this.cxoDeviceAccessLevel = cxoDeviceAccessLevel;
    }

    public com.energyict.mdc.protocol.api.security.DeviceAccessLevel getConnexoDeviceAccessLevel() {
        return cxoDeviceAccessLevel;
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
                .map(ConnexoToUPLPropertSpecAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return cxoDeviceAccessLevel != null ? cxoDeviceAccessLevel.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CXODeviceAccessLevelAdapter) {
            return cxoDeviceAccessLevel.equals(((CXODeviceAccessLevelAdapter) obj).cxoDeviceAccessLevel);
        } else {
            return cxoDeviceAccessLevel.equals(obj);
        }
    }
}