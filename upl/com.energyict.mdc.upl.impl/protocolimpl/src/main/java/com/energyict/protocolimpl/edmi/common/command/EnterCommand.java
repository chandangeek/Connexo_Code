package com.energyict.protocolimpl.edmi.common.command;

/**
 *
 * @author koen
 */
public class EnterCommand extends AbstractCommand {

    /** Creates a new instance of EnterCommand */
    public EnterCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    protected byte[] prepareBuild() {
       return null;
    }

    protected void parse(byte[] data) {

    }
}