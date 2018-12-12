package com.energyict.mdc.upl;

import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;

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
public interface DeviceProtocolDialect {

    enum Property {
        DEVICE_PROTOCOL_DIALECT("DeviceProtocolDialect");
        private final String name;

        Property(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    List<PropertySpec> getUPLPropertySpecs();

    /**
     * Provides a <b>unique</b> name for this DeviceProtocolDialect.
     * This name will be used as part of a key to store
     * properties at various levels (configuration and device).
     * <b>The length is therefore limited to 24 characters!</b>
     *
     * @return the unique name for this DeviceProtocolDialect
     */
    String getDeviceProtocolDialectName();

    /**
     * The display name of this Dialect.
     * @return the name the User will see for this dialect
     */
    String getDeviceProtocolDialectDisplayName();

}