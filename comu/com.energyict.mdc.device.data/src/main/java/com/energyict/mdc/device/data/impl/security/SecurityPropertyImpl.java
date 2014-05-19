package com.energyict.mdc.device.data.impl.security;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.device.BaseDevice;

/**
 * Provides an implementation for the {@link SecurityProperty} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-02 (11:39)
 */
public class SecurityPropertyImpl implements SecurityProperty {

    private Device device;
    private SecurityPropertySet securityPropertySet;
    private PropertySpec specification;
    private Object value;
    private Interval activePeriod;

    public SecurityPropertyImpl (Device device, SecurityPropertySet securityPropertySet, PropertySpec specification, Object value, Interval activePeriod) {
        super();
        this.device = device;
        this.securityPropertySet = securityPropertySet;
        this.specification = specification;
        this.value = value;
        this.activePeriod = activePeriod;
    }

    public BaseDevice getDevice () {
        return device;
    }

    public SecurityPropertySet getSecurityPropertySet () {
        return securityPropertySet;
    }

    @Override
    public AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel() {
        return this.getSecurityPropertySet().getAuthenticationDeviceAccessLevel();
    }

    @Override
    public EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel() {
        return this.getSecurityPropertySet().getEncryptionDeviceAccessLevel();
    }

    @Override
    public String getName () {
        return this.specification.getName();
    }

    @Override
    public Object getValue () {
        return value;
    }

    @Override
    public Interval getActivePeriod () {
        return this.activePeriod;
    }

}