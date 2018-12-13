package com.energyict.mdc.channel.serial.modemproperties.postdialcommand;

import com.energyict.mdc.protocol.ComChannel;

/**
 * @author sva
 * @since 22/04/13 - 9:53
 */
public abstract class AbstractAtPostDialCommand {

    private String command;

    public abstract void initAndVerifyCommand();

    public abstract void execute(ModemComponent modemComponent, ComChannel comChannel);

    protected AbstractAtPostDialCommand(String command) {
        if (command.length() > 2) {
            this.command = command.substring(2);
        } else {
            this.command = "";
        }
    }

    public String getCommand() {
        return command;
    }
}