package com.energyict.mdc.device.config;

import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.HasDynamicProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

/**
 * @author sva
 * @since 5/03/13 - 14:15
 */
public interface ProtocolDialectConfigurationProperties extends HasName, HasId, HasDynamicProperties {

    /**
     * Applies the updates that are specified
     * to this ProtocolDialectConfigurationProperties.
     *
     */
    public void update();

    /**
     * Gets the {@link com.energyict.mdc.device.config.DeviceCommunicationConfiguration} that owns this {@link ProtocolDialectConfigurationProperties}
     *
     * @return The DeviceCommunicationConfiguration
     */
    public DeviceCommunicationConfiguration getDeviceCommunicationConfiguration();

    /**
     * Gets the {@link com.energyict.mdc.protocol.api.DeviceProtocolDialect} for this ProtocolDialectConfigurationProperties.
     *
     * @return the DeviceConfiguration
     */
    public DeviceProtocolDialect getDeviceProtocolDialect();

    /**
     * The name of the {@link com.energyict.mdc.protocol.api.DeviceProtocolDialect}
     * that provides the specifications for the property values.
     *
     * @return The DeviceProtocolDialect
     */
    public String getDeviceProtocolDialectName();

    /**
     * Provides a view of the current properties in the TypedProperties format
     *
     * @return the TypedProperties of this ProtocolDialectProperties
     */
    public TypedProperties getTypedProperties();

    /**
     * @param name
     * @param value
     */
    void setProperty(String name, Object value);

    void removeProperty(String name);

    void save();

    void delete();

    Object getProperty(String name);
}
