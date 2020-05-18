package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.energyict.mdc.common.device.data.SecurityAccessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandExecutorTest {

    @Mock
    private CommandErrorHandler commandErrorHandler;
    @Mock
    private Command cmd1;
    @Mock
    private Command cmd2;
    @Mock
    private SecurityAccessor securityAccessor;


    @Test
    public void singleCommands() throws CommandErrorException, CommandAbortException {
        new CommandExecutor(commandErrorHandler, cmd1).execute(securityAccessor);
        Mockito.verify(cmd1, Mockito.times(1)).run(securityAccessor);
        Mockito.verifyNoMoreInteractions(cmd1);
    }

    @Test
    public void twoCommands() throws CommandErrorException, CommandAbortException {
        new CommandExecutor(commandErrorHandler, cmd1, cmd2).execute(securityAccessor);
        Mockito.verify(cmd1, Mockito.times(1)).run(securityAccessor);
        Mockito.verifyNoMoreInteractions(cmd1);
        Mockito.verify(cmd2, Mockito.times(1)).run(securityAccessor);
        Mockito.verifyNoMoreInteractions(cmd2);
    }

    @Test
    public void twoCommandsAbort() throws CommandErrorException, CommandAbortException {
        CommandAbortException toBeThrown = new CommandAbortException("msg");
        Mockito.doThrow(toBeThrown).when(cmd1).run(securityAccessor);
        new CommandExecutor(commandErrorHandler, cmd1, cmd2).execute(securityAccessor);
        Mockito.verify(cmd1, Mockito.times(1)).run(securityAccessor);
        Mockito.verifyNoMoreInteractions(cmd1);
        Mockito.verifyNoMoreInteractions(cmd2);
        Mockito.verify(commandErrorHandler, Mockito.times(1)).handle(toBeThrown);
    }
}
