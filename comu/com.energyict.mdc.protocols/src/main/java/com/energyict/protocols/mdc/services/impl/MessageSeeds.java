/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    GENERIC_JAVA_REFLECTION_ERROR(214, "genericJavaReflectionError", "Unable to create an instance of the class {0}", Level.SEVERE),
    UNSUPPORTED_LEGACY_PROTOCOL_TYPE(215, "unsupportedLegacyProtocolType", "The legacy protocol class {0} is not or no longer supported", Level.SEVERE),
    UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS(216, "unknownDeviceSecuritySupportClass", "The DeviceSecuritySupport class ''{0}'' is not known on the classpath", Level.SEVERE),
    DEVICE_MESSAGE_CONVERTER_CREATION_FAILURE(217, "deviceMessageConverterCreationFailure", "Failure to create instance of DeviceSecuritySupport class ''{0}''", Level.SEVERE),;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getModule() {
        return DeviceProtocolService.COMPONENT_NAME;
    }
}