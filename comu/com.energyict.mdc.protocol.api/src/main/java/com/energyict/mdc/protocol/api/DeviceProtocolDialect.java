package com.energyict.mdc.protocol.api;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Models a component that will contain several properties for a specific DeviceProtocol.
 * With this set, a protocol should be able to fully understand how to connect with a device.
 * An example would be different flavors of DLMS properties:
 * <ul>
 * <li>One for DLMS TCP/IP</li>
 * <li>One for DLMS HDLC</li>
 * <li>One for DLMS HDLC over analogue line</li>
 * <li>...</li>
 * </ul>
 * <ul>
 * <li>One for standard WaveFlow</li>
 * <li>One for WaveFlow over IP</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (16:16)
 */
public interface DeviceProtocolDialect extends HasDynamicProperties {

    /**
     * Returns the {@link CustomPropertySet} that provides the storage area
     * for the properties of a {@link DeviceProtocolDialectPropertyProvider} for this dialect
     * or an empty Optional if this dialect does not have any properties.
     * In that case, {@link #getPropertySpecs()} should return
     * an empty collection as well for consistency.
     *
     * @return The CustomPropertySet
     */
    Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet();

    @Override
    default List<PropertySpec> getPropertySpecs() {
        return this.getCustomPropertySet()
                .map(CustomPropertySet::getPropertySpecs)
                .orElseGet(Collections::emptyList);
    }

    /**
     * Provides a <b>unique</b> name for this DeviceProtocolDialect.
     * This name will be used in the RelationType that is going to be created for this
     * {@link DeviceProtocolDialect} and his corresponding {@link DeviceProtocol DeviceProtocol}
     * <p/>
     * <b>NOTE: The length of the name is limited to 24 characters!</b>
     *
     * @return the unique name for this DeviceProtocolDialect
     */
    String getDeviceProtocolDialectName();

    /**
     * The display name of this Dialect.
     * @return the name the User will see for this dialect
     */
    String getDisplayName();

}