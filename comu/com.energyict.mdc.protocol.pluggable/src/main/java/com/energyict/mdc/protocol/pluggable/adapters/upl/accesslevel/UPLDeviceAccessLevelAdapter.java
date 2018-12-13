package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.elster.jupiter.nls.Thesaurus;
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
    private final Thesaurus thesaurus;

    public UPLDeviceAccessLevelAdapter(com.energyict.mdc.upl.security.DeviceAccessLevel uplDeviceAccessLevel, Thesaurus thesaurus) {
        this.uplDeviceAccessLevel = uplDeviceAccessLevel;
        this.thesaurus = thesaurus;
    }

    public com.energyict.mdc.upl.security.DeviceAccessLevel getUplDeviceAccessLevel() {
        return uplDeviceAccessLevel;
    }

    @Override
    public int getId() {
        return uplDeviceAccessLevel.getId();
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public String getTranslation() {
        return thesaurus != null
                ? thesaurus.getString(uplDeviceAccessLevel.getTranslationKey(), uplDeviceAccessLevel.getDefaultTranslation())
                : uplDeviceAccessLevel.getDefaultTranslation(); // Should only be the case for unit tests
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