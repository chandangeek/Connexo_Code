/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.pluggable.PluggableClassUsage;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectProperty;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the fact that a {@link com.energyict.mdc.protocol.api.device.BaseDevice} uses a {@link DeviceProtocolDialect}
 * and will specify the values of the properties of the DeviceProtocolDialect.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-24 (11:32)
 */
@ProviderType
public interface ProtocolDialectProperties
    extends
        PluggableClassUsage<
                DeviceProtocolDialect,
                DeviceProtocolDialectUsagePluggableClass,
                DeviceProtocolDialectProperty>,
        DeviceProtocolDialectPropertyProvider {

    /**
     * The Device for which this ProtocolDialectProperties is being created.
     *
     * @return the Device referring to this ProtocolDialectProperties
     */
    Device getDevice();

    /**
     * The name of the {@link DeviceProtocolDialect}
     * that provides the specifications for the property values.
     *
     * @return The DeviceProtocolDialect
     */
    String getDeviceProtocolDialectName();

    /**
     * The {@link ProtocolDialectConfigurationProperties} from which this ProtocolDialectProperties inherits.
     *
     * @return the ProtocolDialectConfigurationProperties
     */
    ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

    /**
     * Returns the current properties in the TypedProperties format.
     *
     * @return the TypedProperties of this ProtocolDialectProperties object
     */
    TypedProperties getTypedProperties();

}