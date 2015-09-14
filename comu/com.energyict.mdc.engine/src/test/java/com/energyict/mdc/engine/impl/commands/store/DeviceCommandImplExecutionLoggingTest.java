package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.elster.jupiter.events.EventService;

import java.time.Clock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock
    private IssueService issueService;

    @Test
    public void testExecuted () {
        ForTestingPurposesOnly command = new ForTestingPurposesOnly();
        command.logExecutionWith(this.executionLogger);

         // Business method
        command.execute(this.comServerDAO);

        // Asserts
        assertThat(command.executed).isTrue();
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

        private ForTestingPurposesOnly() {
            super(null, new IssueServiceOnly());
        }

        @Override
        protected void doExecute (ComServerDAO comServerDAO) {
            this.executed = true;
        }

        @Override
        protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
            builder.addLabel("For Testing purposes only");
        }

        @Override
        public String getDescriptionTitle() {
            return "For testing purposes only";
        }
    }

    private class IssueServiceOnly implements DeviceCommand.ServiceProvider {
        @Override
        public IssueService issueService() {
            return issueService;
        }

        @Override
        public Clock clock() {
            return null;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return null;
        }

        @Override
        public EngineService engineService() {
            return null;
        }

        @Override
        public NlsService nlsService() {
            return null;
        }

        @Override
        public EventService eventService() {
            return null;
        }
    }

}