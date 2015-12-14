package com.energyict.mdc.protocol.api;

import java.util.List;

/**
 * Provides {@link DeviceProtocolDialectProperty properties} of a {@link DeviceProtocolDialect}.
 *
 * Copyrights EnergyICT
 * Date: 1/10/12
 * Time: 13:37
 */
public interface DeviceProtocolDialectPropertyProvider {

    List<DeviceProtocolDialectProperty> getProperties();

}