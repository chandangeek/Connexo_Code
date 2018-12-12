package com.energyict.mdc.channel.serial.modemproperties.postdialcommand;

import com.energyict.mdc.protocol.ComChannel;

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
    public void execute(ModemComponent modemComponent, ComChannel comChannel) {
        modemComponent.write(comChannel, this.getCommand(), false);
    }
}