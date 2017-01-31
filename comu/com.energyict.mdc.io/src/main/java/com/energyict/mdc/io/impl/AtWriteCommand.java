/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannel;

/**
 * @author sva
 * @since 22/04/13 - 9:59
 */
public class AtWriteCommand extends AbstractAtPostDialCommand {

    public static final char WRITE_COMMAND = 'W';

    public AtWriteCommand(String command) {
        super(command);
    }

    @Override
    public void initAndVerifyCommand() {
        // Nothing to verify
    }

    @Override
    public void execute(AtModemComponent modemComponent, ComChannel comChannel) {
        modemComponent.writeRawData(comChannel, this.getCommand());
    }

}