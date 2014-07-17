package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (10:21)
 */
public enum MessageSeeds implements MessageSeed {
    PROTOCOL_NOT_ALLOWED_BY_LICENSE(1001, "protocolXNotAllowedByLicense", "Usage of protocol \"{0}\" is not allowedd by the current licensed", Level.SEVERE),
    PLUGGABLE_CLASS_LACKS_RELATED_INTERFACE(1002, "PluggableClassXShouldImplementYForTypeZ", "Pluggable class \"{2}\" should implement \"{1}\" because the type is \"{0}\"", Level.SEVERE),
    PLUGGABLE_CLASS_CREATION_FAILURE(1003, Constants.PLUGGABLE_CLASS_NEW_INSTANCE_FAILURE, "Failure to create instance of pluggable class {0}", Level.SEVERE),
    NOT_A_PLUGGABLE_PROPERTY(1004, "PluggableClass.properties.unknown", "Cannot specify value for properties ({0}) that are not supported by the pluggable class {1}", Level.SEVERE),
    PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC(2023, Constants.PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY, "The protocol dialect ''{0}'' does not contain a specification for attribute ''{1}''", Level.SEVERE),
    PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE(2024, Constants.PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY, "''{0}'' is not a valid value for attribute ''{1}'' of device protocol ''{2}''", Level.SEVERE),
    PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING(2025, Constants.PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY, "A value is missing for required attribute ''{0}'' of device protocol''{1}''", Level.SEVERE);

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
        return ProtocolPluggableService.COMPONENTNAME;
    }

    public class Constants {
        public static final String PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY = "protocolDialectPropertyXIsNotInSpec";
        public static final String PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY = "protocolDialectProperty.value.invalid";
        public static final String PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY = "protocolDialectProperty.required";
        public static final String PLUGGABLE_CLASS_NEW_INSTANCE_FAILURE = "PluggableClass.newInstance.failure";
    }
}

