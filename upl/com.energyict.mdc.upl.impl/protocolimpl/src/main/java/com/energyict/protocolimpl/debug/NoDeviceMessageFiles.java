package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;

import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceMessageFileFinder} interface
 * that never returns any {@link DeviceMessageFile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (09:35)
 */
final class NoDeviceMessageFiles implements DeviceMessageFileFinder {
    @Override
    public Optional<DeviceMessageFile> from(String identifier) {
        return Optional.empty();
    }
}