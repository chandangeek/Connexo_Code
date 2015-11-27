package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.Pluggable;

/**
 * Abstract Class to map certain <i>new</i> functionality for existing protocols.
 * Introduced after we made MeterProtocol {@link Pluggable}.
 *
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 13:32
 */
public abstract class PluggableMeterProtocol implements MeterProtocol {

    private final PropertySpecService propertySpecService;

    protected PluggableMeterProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public void addProperties(TypedProperties properties) {
        try {
            setProperties(properties.toStringProperties());
        } catch (InvalidPropertyException | MissingPropertyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getVersion() {
        return getProtocolVersion();
    }

}