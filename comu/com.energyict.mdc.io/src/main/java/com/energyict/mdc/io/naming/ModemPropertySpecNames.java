/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.naming;

/**
 * Contains the names of {@link com.elster.jupiter.properties.PropertySpec}s
 * that relate to modem technology.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-04 (12:08)
 */
public class ModemPropertySpecNames {

    /**
     * The name of the property that determines the
     * delay to wait before we send a command.
     */
    public static final String DELAY_BEFORE_SEND = "modem_senddelay";

    /**
     * The name of the property that determines the
     * timeout to wait after a connect command has been received.
     */
    public static final String DELAY_AFTER_CONNECT = "modem_delay_after_con";

    /**
     * The name of the property that determines the
     * timeout for regular AT commands.
     */
    public static final String COMMAND_TIMEOUT = "modem_command_timeout";

    /**
     * The name of the property that determines the
     * timeout for the AT connect command.
     */
    public static final String CONNECT_TIMEOUT = "modem_connect_timeout";

    /**
     * The name of the property that determines the
     * the number of attempts a command should be send to the modem before.
     */
    public static final String COMMAND_TRIES = "modem_command_tries";

    /**
     * The name of the property that determines the colon separated
     * list initialization strings for the modem.
     */
    public static final String GLOBAL_INIT_STRINGS = "modem_global_init_string";

    /**
     * The name of the property that determines the colon separated
     * list of initialization strings for the modem.
     */
    public static final String INIT_STRINGS = "modem_init_string";

    /**
     * The name of the property that determines the
     * the prefix at command which goes between the "ATD" and the actual phoneNumber.
     */
    public static final String DIAL_PREFIX = "modem_dial_prefix";

    /**
     * The name of the property that determines the
     * the address selector to use after a physical connect.
     */
    public static final String ADDRESS_SELECTOR = "modem_address_select";

    /**
     * The name of the property that determines the
     * the set of post dial commandos to launch after a physical connect.
     */
    public static final String POST_DIAL_COMMANDS = "modem_postdial_command";

    /**
     * The name of the property that determines the
     * the delay between DTR line toggles,
     * which are used to disconnect the active connection.
     */
    public static final String DTR_TOGGLE_DELAY = "disconnect_line_toggle_delay";

}