package com.energyict.mdc.protocol.api;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.energyict.protocol.exceptions.ConnectionException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Models a component that will know how to physically
 * setup a connection with a remote device
 * and what properties are required to do that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (16:16)
 */
public interface ConnectionType extends Pluggable, com.energyict.mdc.upl.io.ConnectionType {

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
     * Establishes a connection with a device from the values
     * specified in the {@link ConnectionProperty ConnectionProperties}.
     *
     * @param properties The ConnectionTaskProperties
     * @return The ComChannel that can be used to communicate with the device
     * @throws ConnectionException Thrown when the connection to the device failed
     */
    ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException;

}