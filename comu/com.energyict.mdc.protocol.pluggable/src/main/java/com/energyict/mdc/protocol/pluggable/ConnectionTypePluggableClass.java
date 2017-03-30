/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Models a {@link ConnectionType} that was registered
 * in EIServer as a {@link PluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-30 (14:10)
 */
public interface ConnectionTypePluggableClass extends PluggableClass {

    enum TranslationKeys implements TranslationKey {
        NR_OF_RETRIES(NR_OF_RETRIES_ATTRIBUTE_NAME, "Number of retries");

        private final String key;
        private final String defaultTranslation;

        TranslationKeys(String key, String defaultTranslation) {
            this.key = key;
            this.defaultTranslation = defaultTranslation;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultTranslation;
        }
    }

    String NR_OF_RETRIES_ATTRIBUTE_NAME = "connectionTypeRetries";
    int DEFAULT_NR_OF_RETRIES = 3;

    List<PropertySpec> getPropertySpecs();

    /**
     * Returns the {@link PropertySpec} with the specified name
     * or an empty Optional if no such PropertySpec exists.
     *
     * @param name The name of the property specification
     * @return The PropertySpec or <code>null</code>
     * if no such PropertySpec exists
     */
    Optional<PropertySpec> getPropertySpec(String name);

    /**
     * Returns the version of the {@link ConnectionType} and removes
     * any technical details that relate to development tools.
     *
     * @return The DeviceProtocol version
     */
    String getVersion();

    /**
     * Returns a {@link ConnectionType} that may be reused over different calls
     * so you may need to consider threading issues when used.
     *
     * @return The ConnectionType
     */
    ConnectionType getConnectionType();

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
    boolean isInstance(ConnectionType connectionType);

    /**
     * Gets all the properties that were saved against
     * the specified {@link ConnectionProvider} and that
     * are effective on the specified point in time.
     *
     * @param connectionProvider The ConnectionProvider
     * @param effectiveTimestamp The point in time
     */
    CustomPropertySetValues getPropertiesFor(ConnectionProvider connectionProvider, Instant effectiveTimestamp);

    /**
     * Sets all the properties for the specified {@link ConnectionProvider}.
     *
     * @param connectionProvider The ConnectionProvider
     * @param value              The property values
     * @param effectiveTimestamp The point in time from which the new values are effective onwards
     */
    void setPropertiesFor(ConnectionProvider connectionProvider, CustomPropertySetValues value, Instant effectiveTimestamp);

    /**
     * Removes all the properties that were saved against
     * the specified {@link ConnectionProvider} before.
     *
     * @param connectionProvider The ConnectionProvider
     * @see #setPropertiesFor(ConnectionProvider, CustomPropertySetValues, Instant)
     */
    void removePropertiesFor(ConnectionProvider connectionProvider);

    /**
     * Provides the nr of retries this connectionTask can do before failing
     *
     * @return the number of retries
     */
    int getNrOfRetries();

    /**
     * Set the new value for the number or retries
     *
     * @param nrOfRetries the new value for the number of retries
     */
    void updateNrOfRetries(int nrOfRetries);

}