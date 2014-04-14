package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.List;

/**
 * Models the exceptional situation that occurs when a {@link LogBookType}
 * is being deleted while it is still being used by one or more {@link DeviceType}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-14 (09:14)
 */
public class VetoLogBookTypeDeletionBecauseStillUsedByDeviceTypesException extends LocalizedException {

    public VetoLogBookTypeDeletionBecauseStillUsedByDeviceTypesException(Thesaurus thesaurus, LogBookType logBookType, List<DeviceType> deviceTypes) {
        super(thesaurus, MessageSeeds.VETO_LOGBOOKTYPE_DELETION, logBookType.getName(), namesToStringListForDeviceTypes(deviceTypes));
    }

    private static String namesToStringListForDeviceTypes(List<DeviceType> deviceTypes) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (DeviceType deviceType : deviceTypes) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(deviceType.getName());
            notFirst = true;
        }
        return builder.toString();
    }

}