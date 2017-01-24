package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.orm.MacException;
import com.energyict.mdc.common.Offline;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.upl.offline.Offline;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Represents an Offline version of a physical device which should contain all
 * necessary information needed to perform protocolTasks without the need to go to the database.
 *
 * @author gna
 * @since 11/04/12 - 10:01
 */
public interface OfflineDevice extends Offline, com.energyict.mdc.upl.offline.OfflineDevice {

    /**
     * Returns the {@link DeviceProtocolPluggableClass} configured for this device.
     *
     * @return The DeviceProtocolPluggableClass
     */
    DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    Optional<MacException> getMacException();

}