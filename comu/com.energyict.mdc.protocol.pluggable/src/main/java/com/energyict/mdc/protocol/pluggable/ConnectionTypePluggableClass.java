package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassWithRelationSupport;
import com.energyict.mdc.protocol.api.ConnectionType;

import java.util.List;

/**
 * Models a {@link ConnectionType} that was registered
 * in EIServer as a {@link PluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-30 (14:10)
 */
public interface ConnectionTypePluggableClass extends PluggableClassWithRelationSupport {

    /**
     * Returns the {@link PropertySpec} with the specified name
     * or <code>null</code> if no such PropertySpec exists.
     *
     * @param name The name of the property specification
     * @return The PropertySpec or <code>null</code>
     *         if no such PropertySpec exists
     */
    public PropertySpec getPropertySpec (String name);

    /**
     * Returns the version of the {@link ConnectionType} and removes
     * any technical details that relate to development tools.
     *
     * @return The DeviceProtocol version
     */
    public String getVersion ();

    /**
     * Returns a {@link ConnectionType} that may be reused over different calls
     * so you may need to consider threading issues when used.
     *
     * @return The ConnectionType
     */
    public ConnectionType getConnectionType ();

    /**
     * Tests if the specified {@link ConnectionType}
     * is an instance of this ConnectionTypePluggableClass.
     * Note that if the same java class was registered twice as
     * a ConnectionTypePluggableClass then both registered ConnectionTypePluggableClass
     * will return <code>true</code>.
     *
     * @param connectionType The ConnectionType
     * @return A flag that indicates if the ConnectionType is an instance of this ConnectionTypePluggableClass
     */
    public boolean isInstance (ConnectionType connectionType);

    public List<PropertySpec> getPropertySpecs();

}