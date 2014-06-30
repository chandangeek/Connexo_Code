package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.engine.model.EngineModelService;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    SHOULD_BE_AT_LEAST(1, Keys.MDC_VALUE_TOO_SMALL, "Minimal acceptable value is {min}, was {value}", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, Keys.MDC_CAN_NOT_BE_EMPTY, "This field can not be empty", Level.SEVERE),
    VALUE_NOT_IN_RANGE(3, Keys.MDC_VALUE_NOT_IN_RANGE, "{value} not in range {min} to {max}", Level.SEVERE),
    INVALID_URL(4, Keys.MDC_INVALID_URL, "{value} is not a valid URL", Level.SEVERE),
    INVALID_CHARS(5, Keys.MDC_INVALID_CHARS, "This field contains invalid chars, should obey {regex}", Level.SEVERE),
    REQUIRED_FOR_HTTPS(6, Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS, "This field is mandatory in case https is chosen", Level.SEVERE),
    IS_ALREADY_OBSOLETE(7, Keys.MDC_IS_ALREADY_OBSOLETE, "Already obsolete", Level.SEVERE),
    NOT_UNIQUE(8, Keys.MDC_NOT_UNIQUE , "the element is not unique", Level.SEVERE),
    MUST_HAVE_DISCOVERY_PROTOCOL(9, Keys.DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_IS_MANDATORY_FOR_COMPORTPOOL, "Discovery protocol pluggable class is mandatory for comportpool", Level.SEVERE),
    ACTIVE_INBOUND_PORT_MUST_HAVE_POOL(10, Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL, "An active inbound comport must have an inbound comportpool", Level.SEVERE),

    DUPLICATE_COMSERVER(101, Keys.MDC_DUPLICATE_COM_SERVER, "ComServer with name {value} already exists", Level.SEVERE),
    OBSOLETE_COMSERVER_CANT_BE_UPDATED(103, Keys.MDC_COMSERVER_NO_UPDATE_ALLOWED, "Obsolete ComServers can no longer be updated", Level.SEVERE),
    ONLINE_COMSERVER_STILL_REFERENCED(104, Keys.MDC_ONLINE_COM_SERVER_STILL_REFERENCED, "Online Comserver is still referenced by remote comserver(s)", Level.SEVERE),
    DUPLICATE_COMPORT(105, Keys.MDC_DUPLICATE_COM_PORT, "ComPort by this name already exists", Level.SEVERE),
    OBSOLETE_COMPORT_CANT_BE_UPDATED(106, Keys.MDC_COMPORT_NO_UPDATE_ALLOWED, "Obsolete ComPorts can no longer be updated", Level.SEVERE),
    COMPORTPOOL_DOES_NOT_MATCH_COMPORT(107, Keys.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL, "The type of the comPortPool does not match the comPort type", Level.SEVERE),
    DUPLICATE_COM_PORT_NUMBER(108, Keys.MDC_DUPLICATE_COM_PORT_PER_COM_SERVER, "The port number of a ComPort must be unique per ComServer", Level.SEVERE),
    DUPLICATE_COMPORTPOOL(109, Keys.MDC_DUPLICATE_COM_PORT_POOL, "ComPortPool with name {value} already exists", Level.SEVERE),
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
        this.key = stripComponentNameIfPresent(key);
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    private String stripComponentNameIfPresent(String key) {
        if (key.startsWith(EngineModelService.COMPONENT_NAME+".")) {
            return key.substring(EngineModelService.COMPONENT_NAME.length()+1);
        } else {
            return key;
        }
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
        return EngineModelService.COMPONENT_NAME;
    }

    static final class Keys {

        public static final String MDC_VALUE_TOO_SMALL = EngineModelService.COMPONENT_NAME+".ValueTooSmall";
        public static final String MDC_CAN_NOT_BE_EMPTY = EngineModelService.COMPONENT_NAME+".CanNotBeEmpty";
        public static final String MDC_VALUE_NOT_IN_RANGE = EngineModelService.COMPONENT_NAME+".ValueNotInRange";
        public static final String MDC_INVALID_URL = EngineModelService.COMPONENT_NAME+".InvalidURL";
        public static final String MDC_INVALID_CHARS = EngineModelService.COMPONENT_NAME+".InvalidChars";
        public static final String MDC_CAN_NOT_BE_EMPTY_IF_HTTPS = EngineModelService.COMPONENT_NAME+".CanNotBeEmptyIfHttps";
        public static final String MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL = EngineModelService.COMPONENT_NAME+".activeInboundPortMustHavePool";

        public static final String MDC_DUPLICATE_COM_SERVER = EngineModelService.COMPONENT_NAME+".DuplicateComServer";
        public static final String MDC_COMSERVER_NO_UPDATE_ALLOWED = EngineModelService.COMPONENT_NAME+".comserver.noUpdateAllowed";
        public static final String MDC_ONLINE_COM_SERVER_STILL_REFERENCED = EngineModelService.COMPONENT_NAME+".OnlineComServerXStillReferenced";
        public static final String MDC_COMPORTPOOL_STILL_REFERENCED = EngineModelService.COMPONENT_NAME+".ComPortPoolStillReferenced";

        public static final String MDC_DUPLICATE_COM_PORT = EngineModelService.COMPONENT_NAME+".DuplicateComPort";
        public static final String MDC_COMPORT_NO_UPDATE_ALLOWED = EngineModelService.COMPONENT_NAME+".comport.noUpdateAllowed";
        public static final String MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL = EngineModelService.COMPONENT_NAME+".ComPortTypeOfComPortDoesNotMatchWithComPortPool";
        public static final String MDC_DUPLICATE_COM_PORT_PER_COM_SERVER = EngineModelService.COMPONENT_NAME+".DuplicateComPortPerComServer";
        public static final String MDC_DUPLICATE_COM_PORT_POOL = EngineModelService.COMPONENT_NAME+".DuplicateComPortPool";
        public static final String MDC_COMPORTPOOL_NO_UPDATE_ALLOWED = EngineModelService.COMPONENT_NAME+".comportpool.noUpdateAllowed";
        public static final String MDC_IS_ALREADY_OBSOLETE = EngineModelService.COMPONENT_NAME+".isAlreadyObsolete";
        public static final String MDC_NOT_UNIQUE = EngineModelService.COMPONENT_NAME+".notUnique";
        public static final String DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_IS_MANDATORY_FOR_COMPORTPOOL = EngineModelService.COMPONENT_NAME+".discoveryProtocol.isMandatory";
        public static final String OUTBOUND_COM_PORT_STILL_MEMBER_OF_POOL = EngineModelService.COMPONENT_NAME+".outboundComPortXStillMemberOfPool";
        public static final String MDC_COM_PORT_POOL_PLUGGABLE_CLASS_INVALID = EngineModelService.COMPONENT_NAME+".ComPortPool.pluggableClass.invalid";
        public static final String DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_XSTILL_IN_USE_BY_DEVICE_TYPES_Y = EngineModelService.COMPONENT_NAME+".discoveryProtocolPluggableClass.XstillInUseByDeviceTypesY";
    }

}