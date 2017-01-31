/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;

/**
 * Models the exceptional situation that occurs when a protocol
 * could not be created because of some java reflection layer problem.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (15:27)
 */
public class ProtocolCreationException extends ComServerRuntimeException {

    public ProtocolCreationException(MessageSeed genericJavaReflectionErrorMessageSeed, String javaClassName) {
        super(genericJavaReflectionErrorMessageSeed, javaClassName);
    }

    public ProtocolCreationException(MessageSeed unsupportedLegacyClassMessageSeed, Class unsupportedLegacyClass) {
        super(unsupportedLegacyClassMessageSeed, unsupportedLegacyClass.getName());
    }

}