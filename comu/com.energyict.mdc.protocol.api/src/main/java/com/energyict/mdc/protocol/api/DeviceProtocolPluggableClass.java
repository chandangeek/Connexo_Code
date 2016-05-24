package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.EnumSet;
import java.util.Set;

/**
 * Models a {@link DeviceProtocol} that was registered in the HeadEnd as a {@link PluggableClass}.
 *
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 8:58
 */
public interface DeviceProtocolPluggableClass extends PluggableClass {

    /**
     * Returns the version of the {@link DeviceProtocol} and removes
     * any technical details that relate to development tools.
     *
     * @return The DeviceProtocol version
     */
    public String getVersion ();

    public DeviceProtocol getDeviceProtocol ();

    public TypedProperties getProperties ();

    default boolean supportsFileManagement() {
        Set<DeviceMessageId> fileMessages = EnumSet.of(
                DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION,
                DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_METER_SCHEME,
                DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_SETTINGS,
                DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_UPDATE_SETTINGS,
                DeviceMessageId.ADVANCED_TEST_USERFILE_CONFIG
        );
        return this.getDeviceProtocol()
                .getSupportedMessages()
                .stream()
                .anyMatch(fileMessages::contains);
    }
}