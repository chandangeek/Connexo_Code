package com.energyict.mdc.channels.serial.modemproperties.postdialcommand;


import com.energyict.mdc.protocol.ComChannel;

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
            throw new IllegalArgumentException("The provided post dial commands string is not valid.");
        }
    }

    @Override
    public void execute(ModemComponent modemComponent, ComChannel comChannel) {
        modemComponent.flush(comChannel, getMilliSecondsOfSilence());
    }

    public long getMilliSecondsOfSilence() {
        return milliSecondsOfSilence;
    }

    public void setMilliSecondsOfSilence(long milliSecondsOfSilence) {
        this.milliSecondsOfSilence = milliSecondsOfSilence;
    }
}