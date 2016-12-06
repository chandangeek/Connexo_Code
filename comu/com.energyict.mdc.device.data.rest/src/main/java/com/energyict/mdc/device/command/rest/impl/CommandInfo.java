package com.energyict.mdc.device.command.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class CommandInfo {
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
}
