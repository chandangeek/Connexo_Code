/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.InboundDeviceProtocol;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.impl.commands.store.core.ComTaskExecutionComCommand;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.time.Clock;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProvideInboundResponseDeviceCommandImplTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    private static final long COM_TASK_EXECUTION_ID = DEVICE_ID + 1;
    private static final long PROTOCOL_DIALECT_CONFIG_PROPS_ID = 6516;

    @Mock
    protected EventPublisherImpl eventPublisher;
    private ExecutionContext executionContext;
    private Clock clock = Clock.systemDefaultZone();
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private CompositeDeviceCommand compositeDeviceCommand;
    @Mock
    private DeviceIdentifier deviceIdentifier;
    @Mock
    private Reading reading;
    @Mock
    private ExecutionContext.ServiceProvider executionContextServiceProvider;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private InboundCommunicationHandler inboundCommunicationHandler;
    @Mock
    private InboundDeviceProtocol inboundDeviceProtocol;

    @Before
    public void setup() {
        when(nlsService.getThesaurus(any(), any())).thenReturn(thesaurus);
        when(thesaurus.getString(any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        DeviceService deviceService = mock(DeviceService.class, RETURNS_DEEP_STUBS);
        IssueServiceImpl issueService = new IssueServiceImpl(this.clock, this.nlsService);
        when(executionContextServiceProvider.clock()).thenReturn(this.clock);
        when(executionContextServiceProvider.issueService()).thenReturn(issueService);
        when(executionContextServiceProvider.connectionTaskService()).thenReturn(mock(ConnectionTaskService.class, RETURNS_DEEP_STUBS));
        when(executionContextServiceProvider.deviceService()).thenReturn(deviceService);
        when(executionContextServiceProvider.eventPublisher()).thenReturn(this.eventPublisher);

        executionContext = newTestExecutionContext();
    }

    @Test
    public void provideResponseFailedTest() {
        doThrow(new RuntimeException("It's oké, the exception is just for my test purposes")).when(inboundCommunicationHandler)
                .provideResponse(inboundDeviceProtocol, com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResponseType.SUCCESS);
        ProvideInboundResponseDeviceCommandImpl testInstance = getTestInstance();
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        CreateComSessionDeviceCommand createComSessionDeviceCommand = mock(CreateComSessionDeviceCommand.class);
        when(createComSessionDeviceCommand.getComSessionBuilder()).thenReturn(comSessionBuilder);
        executionContext.getStoreCommand().add(createComSessionDeviceCommand);

        testInstance.doExecute(comServerDAO);

        verify(createComSessionDeviceCommand).addIssue(Matchers.<CompletionCode>any(), Matchers.<Issue>any(), Matchers.<ComTaskExecution>any());
        verify(createComSessionDeviceCommand).updateSuccessIndicator(ComSession.SuccessIndicator.Broken);
    }

    private ProvideInboundResponseDeviceCommandImpl getTestInstance() {
        return new ProvideInboundResponseDeviceCommandImpl(inboundCommunicationHandler, inboundDeviceProtocol, executionContext);
    }

    private ExecutionContext newTestExecutionContext() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
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
        when(connectionTask.getProtocolDialectConfigurationProperties()).thenReturn(mock(ProtocolDialectConfigurationProperties.class));
        JobExecution jobExecution = mock(JobExecution.class);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(jobExecution.getComServerDAO()).thenReturn(comServerDAO);
        ExecutionContext executionContext =
                new ExecutionContext(
                        jobExecution,
                        connectionTask,
                        comPort,
                        true,
                        executionContextServiceProvider);
        executionContext.setLogger(Logger.getAnonymousLogger());
        ComTaskExecutionComCommand comTaskExecutionComCommand = mock(ComTaskExecutionComCommand.class);
        when(comTaskExecutionComCommand.getComTaskExecution()).thenReturn(comTaskExecution);
        executionContext.start(comTaskExecutionComCommand);
        return executionContext;
    }

}