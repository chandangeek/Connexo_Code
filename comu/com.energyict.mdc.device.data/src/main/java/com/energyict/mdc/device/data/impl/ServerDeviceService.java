package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.properties.PropertySpec;

/**
 * Adds behavior to {@link DeviceService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-28 (11:24)
 */
public interface ServerDeviceService extends DeviceService, ReferencePropertySpecFinderProvider {

    /**
     * Tests if there are {@link Device}s that were created
     * from the specified {@link DeviceConfiguration}.
     *
     * @param deviceConfiguration The DeviceConfiguration
     * @return <code>true</code> iff there is at least one Device created from the DeviceConfiguration
     */
    public boolean hasDevices(DeviceConfiguration deviceConfiguration);

    /**
     * Tests if there are {@link Device}s that overrule properties
     * defined by the same dialect as the {@link ProtocolDialectConfigurationProperties}.
     *
     * @param configurationProperties The DeviceConfiguration
     * @return <code>true</code> iff there is at least one Device with overruling properties
     */
    public boolean hasDevices(ProtocolDialectConfigurationProperties configurationProperties);

    /**
     * Counts the number of {@link Device}s that rely on the value
     * of the dialect {@link PropertySpec} that is specified on the configuration level.
     * In other words, counts the devices that have <strong>NOT</strong> specified
     * a value for that same PropertySpec and would therefore run into problems
     * if the property were to be removed from the {@link ProtocolDialectConfigurationProperties}.
     *
     * @param configurationProperties The DeviceConfiguration
     * @param propertySpec The PropertySpec
     * @return <code>true</code> iff there is at least one Device with overruling properties
     */
    public long countDevicesThatRelyOnRequiredProperty(ProtocolDialectConfigurationProperties configurationProperties, PropertySpec propertySpec);

    public Query<Device> deviceQuery();

}