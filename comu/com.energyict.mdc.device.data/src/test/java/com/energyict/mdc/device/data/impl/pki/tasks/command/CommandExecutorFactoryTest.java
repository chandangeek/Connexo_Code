package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.bpm.BpmService;

import java.time.Instant;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandExecutorFactoryTest {

    @Mock
    public CommandErrorHandler commandErrorHandler;

    @Mock
    public BpmService bpmService;

    @Mock
    Logger logger;

    @Test
    public void renewalCommands() {
        String bpmProcessId = "bpmProcessId";
        CommandExecutor renewal = new CommandExecutorFactory().renewal(commandErrorHandler, bpmService, bpmProcessId, Instant.now(), logger);
        Assert.assertNotNull(renewal);
        Command[] commands = renewal.getCommands();
        Assert.assertEquals(5, commands.length);
        Assert.assertEquals(SecAccFilter.class, commands[0].getClass());
        Assert.assertEquals(DeviceFilter.class, commands[1].getClass());
        Assert.assertEquals(DeviceSecurityAccessorFilter.class, commands[2].getClass());
        Assert.assertEquals(CheckSecuritySets.class, commands[3].getClass());
        Assert.assertEquals(TriggerBpm.class, commands[4].getClass());
    }

}
