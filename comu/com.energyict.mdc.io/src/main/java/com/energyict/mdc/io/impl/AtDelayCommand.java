package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.protocol.ComChannel;

/**
 * @author sva
 * @since 22/04/13 - 10:01
 */
public class AtDelayCommand extends AbstractAtPostDialCommand {

    public static final char DELAY_COMMAND = 'D';
    public static final long DEFAULT_DELAY = 1000L;

    private long delay;

    public AtDelayCommand(String command) {
        super(command);
    }

    @Override
    public void initAndVerifyCommand() {
        try {
            setDelay(getCommand().trim().isEmpty() ? DEFAULT_DELAY : Long.valueOf(getCommand()));
        } catch (NumberFormatException e) {
            throw new ApplicationException("The provided post dial commands string is not valid.");
        }
    }

    @Override
    public void execute(AtModemComponent modemComponent, ComChannel comChannel) {
        AtModemComponent.delay(getDelay());
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}