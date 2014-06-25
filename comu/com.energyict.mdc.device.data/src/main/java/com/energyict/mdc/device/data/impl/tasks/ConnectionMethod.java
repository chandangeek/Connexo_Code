package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.pluggable.PluggableClassUsage;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

/**
 * Models the fact that a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * uses a {@link ConnectionType} as a method of connecting to a device.
 * This will allow you to specify values for the properties of the ConnectionType.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (16:28)
 * @see ConnectionType#getPropertySpecs()
 */
public interface ConnectionMethod extends PluggableClassUsage<ConnectionType, ConnectionTypePluggableClass, ConnectionTaskProperty>, ConnectionTaskPropertyProvider {

    /**
     * Mark the ConnectionMethod as Obsolete.
     */
    public void makeObsolete();

    /**
     * Returns if the {@link ConnectionType} allows simultaneous
     * connections to be created or not.
     *
     * @return <code>true</code> iff the ConnectionType allows simultaneous connections
     * @see ConnectionType#allowsSimultaneousConnections()
     */
    public boolean allowsSimultaneousConnections ();

    public void save ();

    public void saveAllProperties ();

    public void delete();

}