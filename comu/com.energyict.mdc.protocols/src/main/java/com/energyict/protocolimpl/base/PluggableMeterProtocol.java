package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;

/**
 * Abstract Class to map certain <i>new</i> functionality for existing protocols.
 * Introduced after we made MeterProtocol {@link com.energyict.mdw.core.Pluggable}
 *
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 13:32
 */
public abstract class PluggableMeterProtocol implements MeterProtocol {

    public void addProperties(TypedProperties properties) {
        try {
            setProperties(properties.toStringProperties());
        } catch (InvalidPropertyException e) {
            throw new IllegalArgumentException(e);
        } catch (MissingPropertyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getVersion() {
        return getProtocolVersion();
    }
}
