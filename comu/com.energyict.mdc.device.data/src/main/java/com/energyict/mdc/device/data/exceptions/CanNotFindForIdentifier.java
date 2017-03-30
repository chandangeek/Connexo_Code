/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

/**
 * Generate exceptions for situations where an identifier could not find a corresponding object based on
 * the parameters in the identifier
 * <p>
 *
 * Date: 12/18/14
 * Time: 1:15 PM
 */
public class CanNotFindForIdentifier extends ComServerRuntimeException {

    private CanNotFindForIdentifier(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public static CanNotFindForIdentifier device(DeviceIdentifier deviceIdentifier, MessageSeed messageSeed) {
        return new CanNotFindForIdentifier(messageSeed, deviceIdentifier);
    }

    public static CanNotFindForIdentifier loadProfile(LoadProfileIdentifier loadProfileIdentifier, MessageSeed messageSeed) {
        return new CanNotFindForIdentifier(messageSeed, loadProfileIdentifier);
    }

    public static CanNotFindForIdentifier logBook(LogBookIdentifier logBookIdentifier, MessageSeed messageSeed) {
        return new CanNotFindForIdentifier(messageSeed, logBookIdentifier);
    }

    public static CanNotFindForIdentifier message(MessageIdentifier messageIdentifier, MessageSeed messageSeed) {
        return new CanNotFindForIdentifier(messageSeed, messageIdentifier);
    }

}