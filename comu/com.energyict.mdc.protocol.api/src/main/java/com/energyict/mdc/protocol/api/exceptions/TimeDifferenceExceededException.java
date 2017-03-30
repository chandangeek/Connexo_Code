/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when
 * the actual time difference exceeds the maximum
 * allowed time difference.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-27 (13:35)
 */
public class TimeDifferenceExceededException extends DeviceConfigurationException {

    public TimeDifferenceExceededException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}