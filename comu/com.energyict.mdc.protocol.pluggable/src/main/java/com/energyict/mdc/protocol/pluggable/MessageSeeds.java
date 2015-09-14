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
    INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION(100, "dataParseException.indexOutOfBounds", "Referenced a non-existing index: {0}"),
    CONFIG_NOT_ACCESSIBLE(101, "configNotAccessible", "The configuration for loadprofile with OBIS code {0} is not accessible"),
    PROTOCOL_CONNECT(102, "protocolConnect", "The logical connect to a device failed: {0}"),
    PROTOCOL_DISCONNECT(103, "protocolDisonnect", "The logical disconnect to a device failed: {0}"),
    UNSUPPORTED_METHOD(104, "unsupportedMethod", "Method {1} is not supported for class {0}"),
    NON_EXISTING_MAP_ELEMENT(105, "nonExistingMapElement", "'{2}' is not known to the Map '{1}' in '{0}', only predefined protocols can make use of the DeviceProtocolAdapters"),
    GENERIC_JAVA_REFLECTION_ERROR(106, "genericJavaReflectionError", "Unable to create an instance of the class {0}."),
    UNSUPPORTED_LEGACY_PROTOCOL_TYPE(107, "unsupportedLegacyProtocolType", "The legacy protocol class {0} is not or no longer supported"),
    UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS(108, "unknownDeviceSecuritySupportClass", "The DeviceSecuritySupport class '{0}' is not known on the classpath"),
    UNKNOWN_LEGACY_VALUEFACTORY_CLASS(109, "unknownLegacyValueFactoryClass", "The legacy value factory class '{0}' is not known on the classpath"),
    LEGACY_IO(110, "lecagyIoError", "IO related exception occurred in adapter framework: {0}"),
    MESSAGEADAPTER_APPLYMESSAGES_ISSUE(111, "messageadapter.applymessages.issue", "An error occurred during the applyMesssages call in the message adapters, see following stacktrace: {0}", Level.INFO),
    DEVICE_TOPOLOGY_NOT_SUPPORTED_BY_ADAPTER(112, "devicetopologynotsupportedbyadapter", "Device topology update not supported by the legacy protocol adapter", Level.INFO),

    PROTOCOL_NOT_ALLOWED_BY_LICENSE(1001, "protocolXNotAllowedByLicense", "Usage of protocol \"{0}\" is not allowed by the current license."),
    PLUGGABLE_CLASS_LACKS_RELATED_INTERFACE(1002, "PluggableClassXShouldImplementYForTypeZ", "Pluggable class \"{2}\" should implement \"{1}\" because the type is \"{0}\""),
    PLUGGABLE_CLASS_CREATION_FAILURE(1003, Keys.PLUGGABLE_CLASS_NEW_INSTANCE_FAILURE, "Failure to create instance of pluggable class {0}"),
    NOT_A_PLUGGABLE_PROPERTY(1004, "PluggableClass.properties.unknown", "Cannot specify value for properties ({0}) that are not supported by the pluggable class {1}"),
    PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC(2023, Keys.PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY, "The protocol dialect ''{0}'' does not contain a specification for attribute ''{1}''"),
    PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE(2024, Keys.PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY, "Not a valid value for this attribute"),
    PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING(2025, Keys.PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY, "A value is missing for required attribute ''{0}'' of device protocol''{1}''"),
    UNEXPECTED_IO_EXCEPTION(2026, "unexpectedIOException", "Exception occurred while communication with a device"),
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

    public class Keys {
        public static final String PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY = "protocolDialectPropertyXIsNotInSpec";
        public static final String PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY = "protocolDialectProperty.value.invalid";
        public static final String PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY = "protocolDialectProperty.required";
        public static final String PLUGGABLE_CLASS_NEW_INSTANCE_FAILURE = "PluggableClass.newInstance.failure";
    }
}

