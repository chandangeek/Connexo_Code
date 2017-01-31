/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.io.ComChannel;

/**
 * @author sva
 * @since 22/04/13 - 10:03
 */
public class AtFlushCommand extends AbstractAtPostDialCommand {

    public static final char FLUSH_COMMAND = 'F';
    public static final long DEFAULT_MILLISECONDS_OF_SILENCE = 1000L;

    private long milliSecondsOfSilence;

    public AtFlushCommand(String command) {
        super(command);
    }

    @Override
    public void initAndVerifyCommand() {
        try {
            setMilliSecondsOfSilence(getCommand().trim().isEmpty() ? DEFAULT_MILLISECONDS_OF_SILENCE : Long.valueOf(getCommand()));
        } catch (NumberFormatException e) {
            throw new ApplicationException("The provided post dial commands string is not valid.");
        }
    }

    @Override
    public void execute(AtModemComponent modemComponent, ComChannel comChannel) {
        modemComponent.flush(comChannel,  getMilliSecondsOfSilence());
    }

    public long getMilliSecondsOfSilence() {
        return milliSecondsOfSilence;
    }

    public void setMilliSecondsOfSilence(long milliSecondsOfSilence) {
        this.milliSecondsOfSilence = milliSecondsOfSilence;
    }
}