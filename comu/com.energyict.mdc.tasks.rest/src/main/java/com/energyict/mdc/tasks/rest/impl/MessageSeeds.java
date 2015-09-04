package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    LOGBOOKS(1, "logbooks", "Logbooks"),
    REGISTERS(2, "registers", "Registers"),
    TOPOLOGY(3, "topology", "Topology"),
    LOADPROFILES(4, "loadprofiles", "Load profiles"),
    CLOCK(5, "clock", "Clock"),
    STATUS_INFORMATION(6, "statusInformation", "Status information"),

    READ(20, "read", "Read"),
    UPDATE(21, "update", "Update"),
    VERIFY(22, "verify", "Verify"),
    SET(23, "set", "Set"),
    FORCE(24, "force", "Force"),
    SYNCHRONIZE(25, "synchronize", "Synchronize"),
    ;

    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return ComTasksApplication.COMPONENT_NAME;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
