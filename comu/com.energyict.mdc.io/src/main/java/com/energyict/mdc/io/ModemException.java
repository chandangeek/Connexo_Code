/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.io.SerialComponentService;

import java.util.logging.Level;

public class ModemException extends CommunicationException {

    public ModemException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    private ModemException(MessageSeed messageSeed, Exception cause) {
        super(messageSeed, cause);
    }

    public static ModemException from(com.energyict.mdc.upl.io.ModemException uplException) {
        switch (uplException.getType()) {
            case MODEM_READ_TIMEOUT: {
                return new ModemException(MessageSeeds.MODEM_READ_TIMEOUT, uplException);
            }
            case MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE: {
                return new ModemException(MessageSeeds.MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE, uplException);
            }
            case MODEM_COULD_NOT_HANG_UP: {
                return new ModemException(MessageSeeds.MODEM_COULD_NOT_HANG_UP, uplException);
            }
            case MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE: {
                return new ModemException(MessageSeeds.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE, uplException);
            }
            case MODEM_COULD_NOT_SEND_INIT_STRING: {
                return new ModemException(MessageSeeds.MODEM_COULD_NOT_SEND_INIT_STRING, uplException);
            }
            case MODEM_CONNECT_TIMEOUT: {
                return new ModemException(MessageSeeds.MODEM_CONNECT_TIMEOUT, uplException);
            }
            case MODEM_COULD_NOT_ESTABLISH_CONNECTION: {
                return new ModemException(MessageSeeds.MODEM_COULD_NOT_ESTABLISH_CONNECTION, uplException);
            }
            case MODEM_CALL_ABORTED: {
                return new ModemException(MessageSeeds.MODEM_CALL_ABORTED, uplException);
            }
            case AT_MODEM_BUSY: {
                return new ModemException(MessageSeeds.AT_MODEM_BUSY, uplException);
            }
            case AT_MODEM_ERROR: {
                return new ModemException(MessageSeeds.AT_MODEM_ERROR, uplException);
            }
            case AT_MODEM_NO_ANSWER: {
                return new ModemException(MessageSeeds.AT_MODEM_NO_ANSWER, uplException);
            }
            case AT_MODEM_NO_CARRIER: {
                return new ModemException(MessageSeeds.AT_MODEM_NO_CARRIER, uplException);
            }
            case AT_MODEM_NO_DIALTONE: {
                return new ModemException(MessageSeeds.AT_MODEM_NO_DIALTONE, uplException);
            }
            default: {
                throw new IllegalArgumentException("Unknown upl modem exception type:" + uplException.getType());
            }
        }
    }

    public enum MessageSeeds implements MessageSeed {
        MODEM_READ_TIMEOUT(1000, "modemexception.modem.read.timeout", "Modem read timeout"),
        MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE(1001, "modemexception.modem.restore.default.profile.failed", "Failure to restore default profile"),
        MODEM_COULD_NOT_HANG_UP(1002, "modemexception.modem.hangup.failed", "Failure to hangup modem"),
        MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE(1003, "modemexception.modem.initialize.command.state.failure", "Failure to initialize command state"),
        MODEM_COULD_NOT_SEND_INIT_STRING(1004, "modemexception.modem.send.init.string.failed", "Failure to send init string"),
        MODEM_CONNECT_TIMEOUT(1005, "modemexception.modem.connect.timeout", "Modem connect timeout"),
        MODEM_COULD_NOT_ESTABLISH_CONNECTION(1006, "modemexception.modem.connection.failed", "Failure to establish connection"),
        MODEM_CALL_ABORTED(1007, "modemexception.modem.call aborted", "Call aborted"),
        AT_MODEM_BUSY(1008, "modemexception.modem.busy", "Modem busy"),
        AT_MODEM_ERROR(1009, "modemexception.modem.general.error", "General modem error"),
        AT_MODEM_NO_ANSWER(1010, "modemexception.modem.no.answer", "No answer from modem on other side"),
        AT_MODEM_NO_CARRIER(1011, "modemexception.modem.no.carrier", "No carrier"),
        AT_MODEM_NO_DIALTONE(1012, "modemexception.modem.no.dialtone", "No dialtone");

        private final int number;
        private final String key;
        private final String defaultFormat;

        MessageSeeds(int number, String key, String defaultFormat) {
            this.number = number;
            this.key = key;
            this.defaultFormat = defaultFormat;
        }


        @Override
        public String getModule() {
            return SerialComponentService.COMPONENT_NAME;
        }

        @Override
        public int getNumber() {
            return this.number;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }

        @Override
        public Level getLevel() {
            return Level.SEVERE;
        }
    }

}