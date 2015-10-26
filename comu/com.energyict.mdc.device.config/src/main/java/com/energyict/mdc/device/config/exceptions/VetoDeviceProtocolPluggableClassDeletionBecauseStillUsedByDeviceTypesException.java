package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when a {@link DeviceProtocolPluggableClass}
 * is being deleted while it is still being used by one or more {@link DeviceType}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-19 (13:04)
 */
public class VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException extends LocalizedException {

    public VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException(DeviceProtocolPluggableClass deviceProtocolPluggableClass, List<DeviceType> deviceTypes, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, deviceProtocolPluggableClass.getName(), namesToStringListForDeviceTypes(deviceTypes));
    }

    private static String namesToStringListForDeviceTypes(List<DeviceType> deviceTypes) {
        return deviceTypes.stream().map(DeviceType::getName).collect(Collectors.joining(", "));
    }

}