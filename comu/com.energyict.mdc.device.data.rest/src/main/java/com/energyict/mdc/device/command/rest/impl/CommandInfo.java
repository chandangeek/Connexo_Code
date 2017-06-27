/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CommandInfo {
    public String category;
    public String command;
    public String commandName;

    public CommandInfo() {

    }

    public CommandInfo(String category, String command, String commandName) {
        this.category = category;
        this.command = command;
        this.commandName = commandName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommandInfo that = (CommandInfo) o;
        return Objects.equals(category, that.category) &&
                Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, command);
    }

    public int compareTo(CommandInfo o2) {
        int result = this.category.compareTo(o2.category);
        if (result != 0) {
            return result;
        }
        return this.command.compareTo(o2.command);
    }
}
