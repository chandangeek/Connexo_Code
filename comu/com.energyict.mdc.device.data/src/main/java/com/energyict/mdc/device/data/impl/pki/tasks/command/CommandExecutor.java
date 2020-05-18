package com.energyict.mdc.device.data.impl.pki.tasks.command;


import com.energyict.mdc.common.device.data.SecurityAccessor;

public class CommandExecutor {

    private final CommandErrorHandler errorHandler;
    private final Command[] cmds;

    public CommandExecutor(CommandErrorHandler commandErrorHandler, Command... cmds) {
        this.errorHandler = commandErrorHandler;
        this.cmds = cmds;
    }

    public void execute(SecurityAccessor securityAccessor) {
        try {
            for (Command cmd : cmds) {
                cmd.run(securityAccessor);
            }
        } catch (CommandAbortException ex1) {
            errorHandler.handle(ex1);
        } catch (CommandErrorException ex2) {
            errorHandler.handle(ex2);
        }

    }

    public Command[] getCommands() {
        return cmds;
    }
}
