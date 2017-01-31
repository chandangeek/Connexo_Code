/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Models a component that will know how to physically
 * setup a connection with a remote device
 * and what properties are required to do that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (16:16)
 */
public interface ConnectionType extends Pluggable {

    /**
     * Defines the direction of the ConnectionType.
     * <ul>
     *     <li>Outbound means that the Collection system will use the ConnectionType to contact the device.
     *     This means that some properties will be required in order to uniquely contact a Device.</li>
     *     <li>Inbound means that the Device itself will contact the Collection system.
     *     Most of the settings will be done in the Device and not on the ConnectionType.
     *     (This does not mean that we cannot provide a list of properties)</li>
     * </ul>
     */
    enum Direction {
        OUTBOUND,
        INBOUND,

        /**
         * The purpose of NULL is solely that the direction is not defined,
         * it should not be used as an implementation type
         */
        NULL;

        public static Direction fromString(String direction) {
            for (Direction connectionTypeDirection : Direction.values()) {
                if (connectionTypeDirection.name().equalsIgnoreCase(direction)) {
                    return connectionTypeDirection;
                }
            }
            return NULL;
        }
    }

    /**
     * Returns the {@link CustomPropertySet} that provides the storage area
     * for the properties of a {@link ConnectionProvider} of this type
     * or an empty Optional if this ConnectionType does not have any properties.
     * In that case, {@link #getPropertySpecs()} should return
     * an empty collection as well for consistency.
     *
     * @return The CustomPropertySet
     */
    Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet();

    @Override
    default List<PropertySpec> getPropertySpecs() {
        return this.getCustomPropertySet()
                .map(CustomPropertySet::getPropertySpecs)
                .orElseGet(Collections::emptyList);
    }

    /**
     * Returns if this ConnectionType allows simultaneous
     * connections to be created or not.
     *
     * @return <code>true</code> iff this ConnectionType allows simultaneous connections
     */
    boolean allowsSimultaneousConnections();

    /**
     * Returns <code>true</code> when this ConnectionType supports
     * the communication window concept.
     *
     * @return A flag that indicates if this ConnectionType supports ComWindows
     */
    boolean supportsComWindow();

    /**
     * Gets the {@link ComPortType}s that are supported by this ConnectionType.
     *
     * @return The Set of support ComPortType
     */
    Set<ComPortType> getSupportedComPortTypes();

    /**
     * Establishes a connection with a device from the values
     * specified in the {@link ConnectionProperty ConnectionProperties}.
     *
     * @param properties The ConnectionTaskProperties
     * @return The ComChannel that can be used to communicate with the device
     * @throws ConnectionException Thrown when the connection to the device failed
     */
    ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException;

    /**
     * Terminates the connection with the device and release resources.
     * E.g.: for modem-based connectionTypes, we should hang up the modem and release the line
     * Note: the implementer should not close the actual {@link ComChannel}, cause this is done elsewhere.
     *
     * @throws ConnectionException Thrown in case of an exception
     */
    void disconnect(ComChannel comChannel) throws ConnectionException;

    /**
     * Provides meta information for the Collection system to inform whether this ConnectionType
     * can be used for Inbound communication or for Outbound communication
     *
     * @return the direction of the ConnectionType
     */
    Direction getDirection();

}