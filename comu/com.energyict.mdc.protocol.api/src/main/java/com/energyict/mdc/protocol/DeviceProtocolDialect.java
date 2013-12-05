package com.energyict.mdc.protocol;

import com.energyict.mdc.protocol.dynamic.HasDynamicProperties;

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
     * Provides a <b>unique</b> name for this DeviceProtocolDialect.
     * This name will be used in the RelationType that is going to be created for this
     * {@link DeviceProtocolDialect} and his corresponding {@link com.energyict.mdc.protocol.DeviceProtocol DeviceProtocol}
     * <p/>
     * <b>NOTE: The length of the name is limited to 24 characters!</b>
     *
     * @return the unique name for this DeviceProtocolDialect
     */
    public String getDeviceProtocolDialectName();

    /**
     * The display name of this Dialect.
     * @return the name the User will see for this dialect
     */
    public String getDisplayName();

}