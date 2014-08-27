package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Provides 'Exception' functionality which can be thrown when Duplicate objects are
 * found when a unique result was expected.
 *
 * Copyrights EnergyICT
 * Date: 9/25/13
 * Time: 10:06 AM
 */
public final class DuplicateException extends ComServerRuntimeException {

    public DuplicateException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}