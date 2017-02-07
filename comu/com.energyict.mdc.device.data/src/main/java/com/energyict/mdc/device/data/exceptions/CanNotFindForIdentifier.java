/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;

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