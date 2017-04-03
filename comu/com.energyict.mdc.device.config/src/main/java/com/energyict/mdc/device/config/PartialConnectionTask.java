/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import aQute.bnd.annotation.ProviderType;

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
    ComPortPool getComPortPool ();

    /**
     * Tests if this PartialConnectionTask is marked as the default
     * that should be used when a connection to a Device
     * needs to be established.
     *
     * @return A flag that indicates if this is the default
     */
    boolean isDefault();

    /**
     * Gets the list of {@link PartialConnectionTaskProperty PartialConnectionTaskProperties}
     * for this {@link PartialConnectionTask}.
     *
     * @return The List of PartialConnectionTaskProperties
     */
    List<PartialConnectionTaskProperty> getProperties();

    /**
     * Provides the current properties ({@link #getProperties()} in the TypedProperties format.
     *
     * @return the TypedProperties
     */
    TypedProperties getTypedProperties();

    /**
     * Gets the {@link PartialConnectionTaskProperty} with the specified name
     * or <code>null</code> if no such property exists.
     *
     * @param name The property name
     * @return The PartialConnectionTaskProperty
     */
    PartialConnectionTaskProperty getProperty(String name);

    /**
     * Gets the {@link DeviceCommunicationConfiguration} that owns this {@link PartialConnectionTask}.
     *
     * @return The DeviceConfiguration
     */
    DeviceConfiguration getConfiguration ();

    /**
     * Gets the {@link ConnectionType} that knows exactly how to connect
     * to a device and the properties it needs to do that.
     *
     * @return The ConnectionType
     */
    ConnectionType getConnectionType();

    /**
     * Gets the {@link ConnectionTypePluggableClass} that knows exactly how to connect
     * to a device and the properties it needs to do that.
     *
     * @return The ConnectionTypePluggableClass
     */
    ConnectionTypePluggableClass getPluggableClass ();

    void save();

    void setConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass);

    void setProperty(String key, Object value);

    void removeProperty(String key);

    void setName(String name);

    long getVersion();

    /**
     * Gets the ProtocolDialectConfigurationProperties.
     *
     * @return the ProtocolDialectConfigurationProperties
     */
    ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

    void setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties);

}