package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {
    SHOULD_BE_AT_LEAST(1, Keys.MDC_VALUE_TOO_SMALL, "Minimal acceptable value is {value} seconds.", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, Keys.MDC_CAN_NOT_BE_EMPTY, "This field is required", Level.SEVERE),
    VALUE_NOT_IN_RANGE(3, Keys.MDC_VALUE_NOT_IN_RANGE, "Value not in range {min} to {max}", Level.SEVERE),
    INVALID_URL(4, Keys.MDC_INVALID_URL, "This is not a valid URL", Level.SEVERE),
    COMSERVER_NAME_INVALID_CHARS(5, Keys.COMSERVER_NAME_INVALID_CHARS, "The name of a communication server should comply with the domain name system (rfc 1035) and can therefore only contain a-z, A-Z, 0-9, . and - characters", Level.SEVERE),
    REQUIRED_FOR_HTTPS(6, Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS, "This field is mandatory in case https is chosen", Level.SEVERE),
    IS_ALREADY_OBSOLETE(7, Keys.MDC_IS_ALREADY_OBSOLETE, "Already obsolete", Level.SEVERE),
    NOT_UNIQUE(8, Keys.MDC_NOT_UNIQUE , "Expected to find at most one object that matches the SQL condition(s) but found multiple", Level.SEVERE),
    MUST_HAVE_DISCOVERY_PROTOCOL(9, Keys.DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_IS_MANDATORY_FOR_COMPORTPOOL, "Discovery protocol pluggable class is mandatory for comportpool", Level.SEVERE),
    ACTIVE_INBOUND_PORT_MUST_HAVE_POOL(10, Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL, "An active inbound communication port must have an inbound communication port pool", Level.SEVERE),
    FIELD_TOO_LONG(11, Keys.MDC_FIELD_TOO_LONG, "Field must not exceed {max} characters", Level.SEVERE),

    DUPLICATE_COMSERVER(101, Keys.MDC_DUPLICATE_COM_SERVER, "Name should be unique", Level.SEVERE),
    OBSOLETE_COMSERVER_CANT_BE_UPDATED(103, Keys.MDC_COMSERVER_NO_UPDATE_ALLOWED, "Obsolete ComServers can no longer be updated", Level.SEVERE),
    ONLINE_COMSERVER_STILL_REFERENCED(104, Keys.MDC_ONLINE_COM_SERVER_STILL_REFERENCED, "Online Comserver is still referenced by remote comserver(s)", Level.SEVERE),
    DUPLICATE_COMPORT(105, Keys.MDC_DUPLICATE_COM_PORT, "Name should be unique", Level.SEVERE),
    OBSOLETE_COMPORT_CANT_BE_UPDATED(106, Keys.MDC_COMPORT_NO_UPDATE_ALLOWED, "Obsolete ComPorts can no longer be updated", Level.SEVERE),
    COMPORTPOOL_DOES_NOT_MATCH_COMPORT(107, Keys.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL, "The type of the comPortPool does not match the comPort type", Level.SEVERE),
    DUPLICATE_COM_PORT_NUMBER(108, Keys.MDC_DUPLICATE_COM_PORT_PER_COM_SERVER, "The port number of a ComPort must be unique per ComServer", Level.SEVERE),
    DUPLICATE_COMPORTPOOL(109, Keys.MDC_DUPLICATE_COM_PORT_POOL, "Name should be unique", Level.SEVERE),
    OBSOLETE_COMPORTPOOL_CANT_BE_UPDATED(110, Keys.MDC_COMPORTPOOL_NO_UPDATE_ALLOWED, "Obsolete ComPortPool can no longer be updated", Level.SEVERE),
    COMPORTPOOL_STILL_REFERENCED(111, Keys.MDC_COMPORTPOOL_STILL_REFERENCED, "Comport pool is still referenced by comport(s)", Level.SEVERE),
    OUTBOUND_COMPORT_STILL_IN_POOL(112, Keys.OUTBOUND_COM_PORT_STILL_MEMBER_OF_POOL, "The outbound comport is still contained in a pool", Level.SEVERE),
    NO_SUCH_PLUGGABLE_CLASS(113, Keys.MDC_COM_PORT_POOL_PLUGGABLE_CLASS_INVALID, "The comportpool references a non-existing pluggable class", Level.SEVERE),
    VETO_DISCOVERYPROTOCOLPLUGGABLECLASS_DELETION(114, Keys.DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_XSTILL_IN_USE_BY_DEVICE_TYPES_Y, "The device protocol pluggable class {0} is still used by the following device types: {1}", Level.SEVERE);

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
        return EngineConfigurationService.COMPONENT_NAME;
    }

    public static final class Keys {

        public static final String MDC_VALUE_TOO_SMALL = "ValueTooSmall";
        public static final String MDC_CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String MDC_FIELD_TOO_LONG = "FieldTooLong";
        public static final String MDC_VALUE_NOT_IN_RANGE = "ValueNotInRange";
        public static final String MDC_INVALID_URL = "InvalidURL";
        public static final String COMSERVER_NAME_INVALID_CHARS = "InvalidChars";
        public static final String MDC_CAN_NOT_BE_EMPTY_IF_HTTPS = "CanNotBeEmptyIfHttps";
        public static final String MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL = "activeInboundPortMustHavePool";

        public static final String MDC_DUPLICATE_COM_SERVER = "DuplicateComServer";
        public static final String MDC_COMSERVER_NO_UPDATE_ALLOWED = "comserver.noUpdateAllowed";
        public static final String MDC_ONLINE_COM_SERVER_STILL_REFERENCED = "OnlineComServerXStillReferenced";
        public static final String MDC_COMPORTPOOL_STILL_REFERENCED = "ComPortPoolStillReferenced";

        public static final String MDC_DUPLICATE_COM_PORT = "DuplicateComPort";
        public static final String MDC_COMPORT_NO_UPDATE_ALLOWED = "comport.noUpdateAllowed";
        public static final String MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL = "ComPortTypeOfComPortDoesNotMatchWithComPortPool";
        public static final String MDC_DUPLICATE_COM_PORT_PER_COM_SERVER = "DuplicateComPortPerComServer";
        public static final String MDC_DUPLICATE_COM_PORT_POOL = "DuplicateComPortPool";
        public static final String MDC_COMPORTPOOL_NO_UPDATE_ALLOWED = "comportpool.noUpdateAllowed";
        public static final String MDC_IS_ALREADY_OBSOLETE = "isAlreadyObsolete";
        public static final String MDC_NOT_UNIQUE = "notUnique";
        public static final String DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_IS_MANDATORY_FOR_COMPORTPOOL = "discoveryProtocol.isMandatory";
        public static final String OUTBOUND_COM_PORT_STILL_MEMBER_OF_POOL = "outboundComPortXStillMemberOfPool";
        public static final String MDC_COM_PORT_POOL_PLUGGABLE_CLASS_INVALID = "ComPortPool.pluggableClass.invalid";
        public static final String DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_XSTILL_IN_USE_BY_DEVICE_TYPES_Y = "discoveryProtocolPluggableClass.XstillInUseByDeviceTypesY";
    }

}