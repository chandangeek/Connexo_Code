package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionType;

/**
 * Add behavior to {@link ConnectionType} that is specific to this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-20 (16:54)
 */
public interface ServerConnectionType extends ConnectionType {

    public void setPropertySpecService(PropertySpecService propertySpecService);

}