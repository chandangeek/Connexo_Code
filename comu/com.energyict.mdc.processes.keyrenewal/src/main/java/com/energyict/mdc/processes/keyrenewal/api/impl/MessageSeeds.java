/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FIELD_TOO_LONG(1002, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),
    NO_SUCH_DEVICE(1012, "NoSuchDevice", "No device with MRID {0}"),
    NO_HEAD_END_INTERFACE(1013, "NoHeadEndInterface", "Could not find the head-end interface for end device with MRID {0}"),
    COMMAND_ARGUMENT_SPEC_NOT_FOUND(1014, "CommandArgumentSpecNotFound", "Could not find the command argument spec {0} for command {1}"),
    COULD_NOT_FIND_SERVICE_CALL_TYPE(1015, "CouldNotFindServiceCallType", "Could not find service call type {0} having version {1}"),
    COULD_NOT_FIND_SERVICE_CALL(1017, "CouldNotFindServiceCall", "Could not find service call with ID {0}"),
    COULD_NOT_FIND_DESTINATION_SPEC(1018, "CouldNotFindDestinationSpec", "Could not find destination spec with name {0}"),
    CALL_BACK_ERROR_URI_NOT_SPECIFIED(1019, "CallBackErrorUrlNotSpecified", "Not possible to send back the error response, as the error callback uri was not specified"),
    CALL_BACK_SUCCESS_URI_NOT_SPECIFIED(1020, "CallBackSuccessUrlNotSpecified", "Not possible to send back the success response, as the success callback uri was not specified"),
    UNKNOWN_KEYACCESSORTYPE(1021, "UnknownKeyAccessorType", "No security accessor type with name {0}"),
    NO_CSR_PRESENT(1022, "noCsrPresent", "No CSR found"),
    NO_SUCH_CERTIFICATE(1023, "NoSuchCertificate", "No such certificate could be located"),
    NO_APPLICABLE_CERTIFICATE_TYPE(1024, "NoApplicableCertificateType", "No applicable certificate type for security accessor {0}"),
    UNKNOWN_SECURITYPROPERTYSET(1025, "UnknownSecuritySet", "No security set with name {0}"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return "PKR";
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public static final class Keys {
        public static final String FIELD_TOO_LONG = "FieldTooLong";
    }
}
