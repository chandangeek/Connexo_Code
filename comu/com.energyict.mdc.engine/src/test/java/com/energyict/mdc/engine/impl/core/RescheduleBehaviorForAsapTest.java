package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.communication.tasks.ProtocolDialectConfigurationPropertiesFactory;
import com.energyict.mdc.communication.tasks.ProtocolDialectPropertiesFactory;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.journal.CompletionCode;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.shadow.journal.ComCommandJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComTaskExecutionJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComTaskExecutionSessionShadow;
import org.fest.assertions.core.Condition;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.RescheduleBehaviorForAsap} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/06/13
 * Time: 9:08
 */
@RunWith(MockitoJUnitRunner.class)
public class RescheduleBehaviorForAsapTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    private static final long PROTOCOL_DIALECT_CONFIG_PROPS_ID = 6516;

    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private ScheduledComTaskExecutionGroup scheduledComTaskExecutionGroup;
    private ScheduledConnectionTask connectionTask;
    @Mock
    private Device device;
    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    @Mock
    private ProtocolDialectConfigurationPropertiesFactory protocolDialectConfigurationPropertiesFactory;
    @Mock
    protected ServerManager manager;
    @Mock
    private ProtocolDialectPropertiesFactory protocolDialectPropertiesFactory;

    @Before
    public void setUp() {
        when(this.protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIG_PROPS_ID);
        when(this.protocolDialectConfigurationPropertiesFactory.find((int) PROTOCOL_DIALECT_CONFIG_PROPS_ID)).thenReturn(this.protocolDialectConfigurationProperties);
        when(this.manager.getProtocolDialectConfigurationPropertiesFactory()).thenReturn(this.protocolDialectConfigurationPropertiesFactory);
        when(this.manager.getProtocolDialectPropertiesFactory()).thenReturn(this.protocolDialectPropertiesFactory);
        ManagerFactory.setCurrent(this.manager);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(connectionTask.getDevice()).thenReturn(device);
    }

    @Test
    public void rescheduleSingleSuccessTest() {
        ComTaskExecution successfulComTaskExecution = getMockedComTaskExecution();

        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(
                comServerDAO, Arrays.<ComTaskExecution>asList(successfulComTaskExecution),
                Collections.<ComTaskExecution>emptyList(), Collections.<ComTaskExecution>emptyList(),
                connectionTask, newTestExecutionContext());

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.COMTASKS);

        // asserts
        verify(comServerDAO, times(1)).executionCompleted(Arrays.asList(successfulComTaskExecution));
        verify(comServerDAO, never()).executionFailed(any(ComTaskExecution.class));
        verify(comServerDAO, never()).executionFailed(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleSuccessFulConnectionTaskTest() {
        ComTaskExecution successfulComTaskExecution = getMockedComTaskExecution();

        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(
                comServerDAO, Arrays.<ComTaskExecution>asList(successfulComTaskExecution),
                Collections.<ComTaskExecution>emptyList(), Collections.<ComTaskExecution>emptyList(),
                connectionTask, newTestExecutionContext());

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.COMTASKS);

        // asserts
        verify(comServerDAO, never()).executionFailed(any(ConnectionTask.class));
        verify(comServerDAO, times(1)).executionCompleted(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleSingleFailedComTaskTest() throws SQLException, BusinessException {
        ComTaskExecution failedComTaskExecution = getMockedComTaskExecution();

        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(
                comServerDAO, Collections.<ComTaskExecution>emptyList(),
                Arrays.<ComTaskExecution>asList(failedComTaskExecution), Collections.<ComTaskExecution>emptyList(),
                connectionTask, newTestExecutionContext());

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.COMTASKS);

        // asserts
        verify(comServerDAO, times(2)).executionCompleted(Collections.<ComTaskExecution>emptyList()); // once for the successful and once for the notExecuted
        verify(comServerDAO, times(1)).executionFailed(Arrays.asList(failedComTaskExecution));
        verify(comServerDAO, times(1)).executionFailed(any(ConnectionTask.class));
        verify(comServerDAO, never()).executionCompleted(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleDueToConnectionSetupErrorTest() {
        ComTaskExecution notExecutedComTaskExecution = getMockedComTaskExecution();

        final JobExecution.ExecutionContext executionContext = newTestExecutionContext();
        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(
                comServerDAO, Collections.<ComTaskExecution>emptyList(),
                Collections.<ComTaskExecution>emptyList(), Arrays.<ComTaskExecution>asList(notExecutedComTaskExecution),
                connectionTask, executionContext);

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.CONNECTION_SETUP);

        // asserts
        verify(comServerDAO, never()).executionCompleted(any(ComTaskExecution.class));
        verify(comServerDAO, times(1)).executionFailed(any(ComTaskExecution.class)); // we want the comTask to be rescheduled in ASAP
        verify(comServerDAO, times(1)).executionFailed(connectionTask);
        final List<ComTaskExecutionSessionShadow> comTaskExecutionSessionShadows = executionContext.getComSessionBuilder().getComTaskExecutionSessionShadows();
        Assertions.assertThat(comTaskExecutionSessionShadows).hasSize(1);
        Assertions.assertThat(comTaskExecutionSessionShadows.get(0).getJournalEntryShadows()).isNotEmpty();
        Assertions.assertThat(comTaskExecutionSessionShadows.get(0).getJournalEntryShadows()).has(new Condition<List<ComTaskExecutionJournalEntryShadow>>() {
            @Override
            public boolean matches(List<ComTaskExecutionJournalEntryShadow> comTaskExecutionJournalEntryShadows) {
                boolean correctCompletionCode = false;
                for (ComTaskExecutionJournalEntryShadow comTaskExecutionJournalEntryShadow : comTaskExecutionJournalEntryShadows) {
                    if(comTaskExecutionJournalEntryShadow instanceof ComCommandJournalEntryShadow){
                        correctCompletionCode |= ((ComCommandJournalEntryShadow) comTaskExecutionJournalEntryShadow).getCompletionCode() ==  CompletionCode.ConnectionError;
                    }
                }
                return correctCompletionCode;
            }
        });
    }

    @Test
    public void rescheduleDueToConnectionBrokenDuringExecutionTest() {
        ComTaskExecution notExecutedComTaskExecution = getMockedComTaskExecution();
        ComTaskExecution failedComTaskExecution = getMockedComTaskExecution();
        ComTaskExecution successfulComTaskExecution1 = getMockedComTaskExecution();
        ComTaskExecution successfulComTaskExecution2 = getMockedComTaskExecution();
        ComTaskExecution successfulComTaskExecution3 = getMockedComTaskExecution();

        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(
                comServerDAO,
                Arrays.<ComTaskExecution>asList(successfulComTaskExecution1, successfulComTaskExecution2, successfulComTaskExecution3),
                Arrays.<ComTaskExecution>asList(failedComTaskExecution),
                Arrays.<ComTaskExecution>asList(notExecutedComTaskExecution),
                connectionTask, newTestExecutionContext());

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.CONNECTION_BROKEN);

        // asserts
        verify(comServerDAO, times(1)).executionCompleted(Arrays.asList(successfulComTaskExecution1, successfulComTaskExecution2, successfulComTaskExecution3));
        verify(comServerDAO, times(1)).executionFailed(Arrays.asList(failedComTaskExecution)); // we want the comTask to be rescheduled in ASAP
        verify(comServerDAO, times(1)).executionFailed(notExecutedComTaskExecution); // we want the comTask to be rescheduled in ASAP
        verify(comServerDAO, times(1)).executionFailed(connectionTask);
    }

    private ComTaskExecution getMockedComTaskExecution() {
        final ComTaskExecution comTaskExecution = mock(ComTaskExecution.class, withSettings().extraInterfaces(ServerComTaskExecution.class));
        when(comTaskExecution.getDevice()).thenReturn(device);
        ProtocolDialectConfigurationProperties mockedProtocolDialectProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(mockedProtocolDialectProperties.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(mockedProtocolDialectProperties);
        return comTaskExecution;
    }

    private JobExecution.ExecutionContext newTestExecutionContext() {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    private JobExecution.ExecutionContext newTestExecutionContext(Logger logger) {
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getComServer()).thenReturn(comServer);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        JobExecution.ExecutionContext executionContext =
                new JobExecution.ExecutionContext(
                        mock(JobExecution.class),
                        connectionTask,
                        comPort,
                        mock(IssueService.class));
        executionContext.setLogger(logger);
        return executionContext;
    }
}
