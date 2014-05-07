package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Tests that a DeviceCommandImpl gets logged properly while executing.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-27 (13:17)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceCommandImplExecutionLoggingTest {

    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private ComServerDAO comServerDAO;

    @Test
    public void testExecuted () {
        ForTestingPurposesOnly command = new ForTestingPurposesOnly();
        command.logExecutionWith(this.executionLogger);

         // Business method
        command.execute(this.comServerDAO);

        // Asserts
        Assertions.assertThat(command.executed).isTrue();
    }

    @Test
    public void testLoggedAfterExecuted () {
        DeviceCommand command = new ForTestingPurposesOnly();
        command.logExecutionWith(this.executionLogger);

         // Business method
        command.execute(this.comServerDAO);

        // Asserts
        verify(this.executionLogger).executed(any(DeviceCommand.class));
    }

    private class ForTestingPurposesOnly extends DeviceCommandImpl {
        private boolean executed;

        public ForTestingPurposesOnly(IssueService issueService) {
            super(issueService, clock);
        }

        @Override
        protected void doExecute (ComServerDAO comServerDAO) {
            this.executed = true;
        }

        @Override
        protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
            builder.addLabel("For Testing purposes only");
        }

    }

}