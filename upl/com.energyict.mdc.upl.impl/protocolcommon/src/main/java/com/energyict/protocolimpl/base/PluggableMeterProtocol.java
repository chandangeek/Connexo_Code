package com.energyict.protocolimpl.base;

import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.*;

/**
 * Abstract Class to map certain <i>new</i> functionality for existing protocols.
 * Introduced after we made MeterProtocol {@link com.energyict.mdw.core.Pluggable}
 *
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 13:32
 */
public abstract class PluggableMeterProtocol implements MeterProtocol {

    @Override
    public void addProperties(TypedProperties properties) {
        try {
            setProperties(properties.toStringProperties());
        } catch (InvalidPropertyException e) {
            throw new IllegalArgumentException(e);
        } catch (MissingPropertyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getVersion() {
        return getProtocolVersion();
    }
}
