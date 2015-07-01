package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

/**
 * @author sva
 * @since 5/03/13 - 14:15
 */
@ProviderType
public interface ProtocolDialectConfigurationProperties extends HasName, HasId, HasDynamicProperties {

    public DeviceConfiguration getDeviceConfiguration();

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

    void setProperty(String name, Object value);

    void removeProperty(String name);

    void save();

    Object getProperty(String name);

}