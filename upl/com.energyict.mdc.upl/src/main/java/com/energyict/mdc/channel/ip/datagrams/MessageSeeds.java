/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channel.ip.datagrams;

import com.energyict.mdc.upl.nls.MessageSeed;

import java.util.logging.Level;

/**
 * @author Stijn Vanhoorelbeke
 * @since 06.10.17 - 15:26
 */
public enum MessageSeeds implements MessageSeed {

    SETUP_OF_INBOUND_CALL_FAILED(137, "setupOfInboundCallFailed", "An IOException occurred during the setup of an inbound call: {0}"),
    UNEXPECTED_IO_EXCEPTION(138, "unexpectedIOException", "Exception occurred while communication with a device"),
    COMMUNICATION_INTERRUPTED(139, "communicationInterrupted", "Communication was interrupted: {0}"),;

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
        return "PR1"; // As these message seeds are registered in com.energyict.protocols.mdc.services.impl.DeviceProtocolServiceImpl.getSeeds
    }
}