package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import java.util.List;

/**
 * Partial version of a ConnectionTask when it comes to
 * the properties required to establish a connection with a device.
 * It also enables the device's capability to use the {@link ConnectionType}
 * on the {@link DeviceConfiguration} against which it is being created.
 * As an example, a device might have the capability to communicate
 * via TCP/IP, GPRS and infra-red but on the configuration level,
 * the communication expert may decide to only enable TCP/IP and infra-red.
 * This way, it will not be possible to create a ConnectionTask
 * that uses GPRS because that was not enabled.
 *
 * @author sva
 * @since 21/01/13 - 15:04
 */
@ProviderType
public interface PartialConnectionTask extends HasName, HasId {

    /**
     * Gets the ComPortPool that is used
     * by preference for actual ConnectionTasks.
     *
     * @return The ComPortPool
     */
    public ComPortPool getComPortPool ();

    /**
     * Tests if this PartialConnectionTask is marked as the default
     * that should be used when a connection to a Device
     * needs to be established.
     *
     * @return A flag that indicates if this is the default
     */
    public boolean isDefault();

    /**
     * Gets the list of {@link PartialConnectionTaskProperty PartialConnectionTaskProperties}
     * for this {@link PartialConnectionTask}.
     *
     * @return The List of PartialConnectionTaskProperties
     */
    public List<PartialConnectionTaskProperty> getProperties();

    /**
     * Provides the current properties ({@link #getProperties()} in the TypedProperties format.
     *
     * @return the TypedProperties
     */
    public TypedProperties getTypedProperties();

    /**
     * Gets the {@link PartialConnectionTaskProperty} with the specified name
     * or <code>null</code> if no such property exists.
     *
     * @param name The property name
     * @return The PartialConnectionTaskProperty
     */
    public PartialConnectionTaskProperty getProperty(String name);

    /**
     * Gets the {@link DeviceCommunicationConfiguration} that owns this {@link PartialConnectionTask}.
     *
     * @return The DeviceConfiguration
     */
    public DeviceConfiguration getConfiguration ();

    /**
     * Gets the {@link ConnectionType} that knows exactly how to connect
     * to a device and the properties it needs to do that.
     *
     * @return The ConnectionType
     */
    public ConnectionType getConnectionType();

    /**
     * Gets the {@link ConnectionTypePluggableClass} that knows exactly how to connect
     * to a device and the properties it needs to do that.
     *
     * @return The ConnectionTypePluggableClass
     */
    public ConnectionTypePluggableClass getPluggableClass ();

    void save();

    void setConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass);

    void setProperty(String key, Object value);

    void removeProperty(String key);

    void setName(String name);
}