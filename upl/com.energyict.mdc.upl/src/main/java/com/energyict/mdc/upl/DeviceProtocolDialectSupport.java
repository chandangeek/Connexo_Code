package com.energyict.mdc.upl;

import com.energyict.mdc.upl.properties.TypedProperties;

import java.util.List;

/**
 * Provides functionality to support a {@link DeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (10:51)
 */
public interface DeviceProtocolDialectSupport {

    /**
     * Provides the set of supported {@link DeviceProtocolDialect}s.
     *
     * @return the List of DeviceProtocolDialect
     */
    List<? extends DeviceProtocolDialect> getDeviceProtocolDialects();

    /**
     * Adds the set of TypedProperties of the specific DeviceProtocolDialect.
     *
     * @param dialectProperties the DeviceProtocolDialectProperties to add to the DeviceProtocol
     */
    void addDeviceProtocolDialectProperties(TypedProperties dialectProperties);

}