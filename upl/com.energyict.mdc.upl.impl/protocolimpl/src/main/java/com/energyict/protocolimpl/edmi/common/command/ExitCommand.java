package com.energyict.protocolimpl.edmi.common.command;

/**
 *
 * @author koen
 */
public class ExitCommand extends AbstractCommand {

    private static final char EXIT_COMMAND = 'X';

    /** Creates a new instance of ExitCommand */
    public ExitCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    protected byte[] prepareBuild() {
       return new byte[]{EXIT_COMMAND};
    }
    
    protected void parse(byte[] data) {
        
    }    
}