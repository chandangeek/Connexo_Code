/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

/**
 * @author sva
 * @since 22/04/13 - 9:53
 */
public abstract class AbstractAtPostDialCommand implements AtPostDialCommand {

    private String command;

    protected AbstractAtPostDialCommand(String command) {
        if (command.length() > 2) {
            this.command = command.substring(2);
        } else {
            this.command = "";
        }
    }

    @Override
    public String getCommand() {
        return command;
    }

}