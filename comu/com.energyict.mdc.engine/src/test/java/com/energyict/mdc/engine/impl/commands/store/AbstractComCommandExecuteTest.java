package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.CompositeThesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.tasks.ComTask;

import java.text.MessageFormat;
import java.time.Clock;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides code reuse opportunities for ComCommand execute tests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-16 (11:44)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractComCommandExecuteTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    private static final long COM_TASK_EXECUTION_ID = DEVICE_ID + 1;
    private static final long PROTOCOL_DIALECT_CONFIG_PROPS_ID = 6516;

    @Mock
    protected ThreadPrincipalService threadPrincipalService;
    @Mock
    protected ExecutionContext.ServiceProvider executionContextServiceProvider;
    @Mock
    protected CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);

    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    protected EventPublisherImpl eventPublisher;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurusISU, thesaurusCES, thesaurusJoined;

    private Clock clock = Clock.systemDefaultZone();

    @Before
    public void setupServiceProvider() {
        when(thesaurusISU.getString(any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(thesaurusISU.getString(any(), any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        when(thesaurusISU.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((TranslationKey) invocation.getArguments()[0]));
        when(thesaurusISU.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));
        when(nlsService.getThesaurus("ISU", Layer.DOMAIN)).thenReturn(thesaurusISU);

        when(thesaurusCES.getString(any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(thesaurusCES.getString(any(), any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        when(thesaurusCES.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((TranslationKey) invocation.getArguments()[0]));
        when(thesaurusCES.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));

        when(nlsService.getThesaurus("CES", Layer.DOMAIN)).thenReturn(thesaurusCES);
        when(thesaurusISU.join(thesaurusCES)).thenReturn(new CompositeThesaurus(threadPrincipalService),thesaurusISU, thesaurusCES );

        when(thesaurusJoined.getString(any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(thesaurusJoined.getString(any(), any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        when(thesaurusJoined.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((TranslationKey) invocation.getArguments()[0]));
        when(thesaurusJoined.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));

        when(thesaurusISU.join(thesaurusCES)).thenReturn(thesaurusJoined);

        DeviceService deviceService = mock(DeviceService.class, RETURNS_DEEP_STUBS);
        IssueServiceImpl issueService = new IssueServiceImpl(this.clock, this.nlsService);
        when(executionContextServiceProvider.clock()).thenReturn(this.clock);
        when(executionContextServiceProvider.issueService()).thenReturn(issueService);
        when(executionContextServiceProvider.connectionTaskService()).thenReturn(mock(ConnectionTaskService.class, RETURNS_DEEP_STUBS));
        when(executionContextServiceProvider.deviceService()).thenReturn(deviceService);
        when(executionContextServiceProvider.eventPublisher()).thenReturn(this.eventPublisher);

        when(commandRootServiceProvider.clock()).thenReturn(this.clock);
        when(commandRootServiceProvider.issueService()).thenReturn(issueService);
        when(commandRootServiceProvider.deviceService()).thenReturn(deviceService);
        when(commandRootServiceProvider.thesaurus()).thenReturn(thesaurusCES);
    }

    @After
    public void resetServiceProvider() {
        when(executionContextServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(commandRootServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Before
    public void setUpManager() throws Exception {
        when(this.protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIG_PROPS_ID);
        when(deviceConfigurationService.getProtocolDialectConfigurationProperties(PROTOCOL_DIALECT_CONFIG_PROPS_ID)).thenReturn(Optional.of(protocolDialectConfigurationProperties));
    }

    protected ExecutionContext newTestExecutionContext() {
        return newTestExecutionContext(Logger.getAnonymousLogger());
    }

    protected ExecutionContext newTestExecutionContext(Logger logger) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTask comTask = mock(ComTask.class);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(mock(ProtocolDialectConfigurationProperties.class));
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getComServer()).thenReturn(comServer);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.getDevice()).thenReturn(device);
        ExecutionContext executionContext =
                new ExecutionContext(
                        mock(JobExecution.class),
                        connectionTask,
                        comPort,
                        true,
                        executionContextServiceProvider);
        executionContext.setLogger(logger);
        executionContext.start(comTaskExecution, comTask);
        return executionContext;
    }

    class SimpleNlsMessageFormat implements NlsMessageFormat {

        private final String key;

        SimpleNlsMessageFormat(TranslationKey translationKey) {
            this.key = translationKey.getKey();
        }

        SimpleNlsMessageFormat(MessageSeed messageSeed) {
            this.key = messageSeed.getKey();
        }

        @Override
        public String format(Object... args) {
            return this.key;    // Don't format, just return the key
        }

        @Override
        public String format(Locale locale, Object... args) {
            return this.key;    // Don't format, just return the key
        }
    }
}