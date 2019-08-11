/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.energyict.mdc.common.pluggable.PluggableClassUsage;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectProperty;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.upl.TypedProperties;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the fact that a {@link com.energyict.mdc.upl.meterdata.Device} uses a {@link DeviceProtocolDialect}
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