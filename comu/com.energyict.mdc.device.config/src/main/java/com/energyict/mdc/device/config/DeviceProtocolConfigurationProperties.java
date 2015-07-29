package com.energyict.mdc.device.config;

import com.energyict.mdc.common.TypedProperties;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.HasDynamicProperties;

/**
 * Models the general protocol properties that can be specified on the
 * configuration level. Any properties that are set at this level
 * can be overruled on the device level.
 * <br>
 * The dynamic properties that are provided {@link HasDynamicProperties}
 * are in fact the generic properties of the related device protocol.
 * Therefore, any of the method defined by HasDynamicProperties
 * an implemented by this ProtocolConfigurationProperties
 * can be regarded as convenience method calls for:
 * <code>getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs()</code>
 * <br>
 * Because the general properties are actually managed by the
 * {@link DeviceConfiguration}, saving the properties is actually
 * triggered by saving the entire DeviceConfiguration.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (09:03)
 */
@ProviderType
public interface DeviceProtocolConfigurationProperties extends HasDynamicProperties {

    public DeviceConfiguration getDeviceConfiguration();

    /**
     * Provides a view of the current properties in the TypedProperties format.
     *
     * @return the TypedProperties of this ProtocolConfigurationProperties
     */
    public TypedProperties getTypedProperties();

    /**
     * Gets the value of a single property.
     *
     * @param name The name of the property
     * @return The
     */
    public Object getProperty(String name);

    /**
     * Sets the value of a single property.
     *
     * @param name The name of the property
     * @param value The value of the propert
     */
    public void setProperty(String name, Object value);

    public void removeProperty(String name);

}