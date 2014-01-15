package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectProperty;

import java.util.List;

/**
 * Provides {@link DeviceProtocolDialectProperty properties} of a {@link DeviceProtocolDialect}.
 *
 * Copyrights EnergyICT
 * Date: 1/10/12
 * Time: 13:37
 */
public interface DeviceProtocolDialectPropertyProvider {

    public List<DeviceProtocolDialectProperty> getProperties ();

}