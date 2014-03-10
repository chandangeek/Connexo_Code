package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.engine.model.EngineModelService;
import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:00)
 */
public enum MessageSeeds implements MessageSeed {
    SHOULD_BE_AT_LEAST(1, Constants.MDC_VALUE_TOO_SMALL, "Minimal acceptable value is {min}, was {value}", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, Constants.MDC_CAN_NOT_BE_EMPTY, "This field can not be empty", Level.SEVERE),
    VALUE_NOT_IN_RANGE(3, Constants.MDC_VALUE_NOT_IN_RANGE, "{value} not in range {min} to {max}", Level.SEVERE),
    INVALID_URL(4, Constants.MDC_INVALID_URL, "{value} is not a valid URL", Level.SEVERE),
    INVALID_CHARS(5, Constants.MDC_INVALID_CHARS, "This field contains invalid chars, should obey {regex}", Level.SEVERE),
    REQUIRED_FOR_HTTPS(6, Constants.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS, "This field is mandatory in case https is chosen", Level.SEVERE),

    DUPLICATE_COMSERVER(101, Constants.MDC_DUPLICATE_COM_SERVER, "ComServer with name {value} already exists", Level.SEVERE),
    OBSOLETE_COMSERVER_CANT_BE_UPDATED(103, Constants.MDC_COMSERVER_NO_UPDATE_ALLOWED, "Obsolete ComServers can no longer be updated", Level.SEVERE),
    ONLINE_COMSERVER_STILL_REFERENCED(104, Constants.MDC_ONLINE_COM_SERVER_STILL_REFERENCED, "Online Comserver is still referenced by remote comserver(s)", Level.SEVERE),
    DUPLICATE_COMPORT(105, Constants.MDC_DUPLICATE_COM_PORT, "ComPort by this name already exists", Level.SEVERE),
    OBSOLETE_COMPORT_CANT_BE_UPDATED(106, Constants.MDC_COMPORT_NO_UPDATE_ALLOWED, "Obsolete ComPorts can no longer be updated", Level.SEVERE),
    COMPORTPOOL_DOES_NOT_MATCH_COMPORT(107, Constants.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL, "The type of the comPortPool does not match the comPort type", Level.SEVERE),
    DUPLICATE_COM_PORT_NUMBER(108, Constants.MDC_DUPLICATE_COM_PORT_PER_COM_SERVER, "The port number of a ComPort must be unique per ComServer", Level.SEVERE),
    DUPLICATE_COMPORTPOOL(109, Constants.MDC_DUPLICATE_COM_PORT_POOL, "ComPortPool with name {value} already exists", Level.SEVERE),
    OBSOLETE_COMPORTPOOL_CANT_BE_UPDATED(110, Constants.MDC_COMPORTPOOL_NO_UPDATE_ALLOWED, "Obsolete ComPortPool can no longer be updated", Level.SEVERE);

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
        return EngineModelService.COMPONENT_NAME;
    }

}

final class Constants {

    public static final String MDC_VALUE_TOO_SMALL = "MDC.ValueTooSmall";
    public static final String MDC_CAN_NOT_BE_EMPTY = "MDC.CanNotBeEmpty";
    public static final String MDC_VALUE_NOT_IN_RANGE = "MDC.ValueNotInRange";
    public static final String MDC_INVALID_URL = "MDC.InvalidURL";
    public static final String MDC_INVALID_CHARS = "MDC.InvalidChars";
    public static final String MDC_CAN_NOT_BE_EMPTY_IF_HTTPS = "MDC.CanNotBeEmptyIfHttps";

    public static final String MDC_DUPLICATE_COM_SERVER = "MDC.DuplicateComServer";
    public static final String MDC_COMSERVER_NO_UPDATE_ALLOWED = "MDC.comserver.noUpdateAllowed";
    public static final String MDC_ONLINE_COM_SERVER_STILL_REFERENCED = "MDC.OnlineComServerXStillReferenced";

    public static final String MDC_DUPLICATE_COM_PORT = "MDC.DuplicateComPort";
    public static final String MDC_COMPORT_NO_UPDATE_ALLOWED = "MDC.comport.noUpdateAllowed";
    public static final String MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL = "MDC.ComPortTypeOfComPortDoesNotMatchWithComPortPool";
    public static final String MDC_DUPLICATE_COM_PORT_PER_COM_SERVER = "MDC.DuplicateComPortPerComServer";
    public static final String MDC_DUPLICATE_COM_PORT_POOL = "MDC.DuplicateComPortPool";
    public static final String MDC_COMPORTPOOL_NO_UPDATE_ALLOWED = "MDC.comportpool.noUpdateAllowed";
}
