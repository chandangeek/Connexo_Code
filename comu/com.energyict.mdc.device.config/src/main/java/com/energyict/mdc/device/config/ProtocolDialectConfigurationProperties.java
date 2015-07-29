package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

/**
 * @author sva
 * @since 5/03/13 - 14:15
 */
@ProviderType
public interface ProtocolDialectConfigurationProperties extends HasName, HasId, HasDynamicProperties {

    /**
     * Gets the {@link com.energyict.mdc.protocol.api.DeviceProtocolDialect} for this ProtocolDialectConfigurationProperties.
     *
     * @return the DeviceConfiguration
     */
    DeviceConfiguration getDeviceConfiguration();

    /**
      * The device protocol dialect {@link com.energyict.mdc.protocol.api.DeviceProtocolDialect}
      * holding the property specs.
      *
      * @return The DeviceProtocolDialect
      */
    DeviceProtocolDialect getDeviceProtocolDialect();

    /**
      * The name of the {@link com.energyict.mdc.protocol.api.DeviceProtocolDialect}
      * that provides the specifications for the property values.
      *
      * @return The DeviceProtocolDialect
      */
     String getDeviceProtocolDialectName();

    /**
     * Provides a view of the current properties in the TypedProperties format
     *
     * @return the TypedProperties of this ProtocolDialectProperties
     */
    TypedProperties getTypedProperties();

    void setProperty(String name, Object value);

    void removeProperty(String name);

    void save();

    Object getProperty(String name);

    /**
     * Checks if there is a value for each required property. Does not check the validity of the value.
     * @return true if all required values have a value, false if for at least one required property the value is not set.
     */
    boolean isComplete();

}