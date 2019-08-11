/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.masterdata.LogBookType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when a {@link LogBookType}
 * is being deleted while it is still being used by one or more {@link DeviceType}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-14 (09:14)
 */
public class VetoLogBookTypeDeletionBecauseStillUsedByDeviceTypesException extends LocalizedException {

    public VetoLogBookTypeDeletionBecauseStillUsedByDeviceTypesException(LogBookType logBookType, List<DeviceType> deviceTypes, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, logBookType.getName(), namesToStringListForDeviceTypes(deviceTypes));
    }

    private static String namesToStringListForDeviceTypes(List<DeviceType> deviceTypes) {
        return deviceTypes.stream().map(DeviceType::getName).collect(Collectors.joining(", "));
    }

}