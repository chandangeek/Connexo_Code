/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.pluggable.PluggableClassUsageProperty;

/**
 * Holds the value of a property of a {@link DeviceProtocolDialect}.
 * Note that values of properties are versioned over time
 * so a DeviceProtocolDialectProperty has a activity period during
 * which the property was active.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-11 (13:50)
 */
public interface DeviceProtocolDialectProperty extends PluggableClassUsageProperty<DeviceProtocolDialect> {
}