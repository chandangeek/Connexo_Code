package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link InboundCommunicationHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (15:21)
 */
@RunWith(MockitoJUnitRunner.class)
public class InboundCommunicationHandlerTest {

    private static final long COMSERVER_ID = 1;
    private static final long COMPORT_POOL_ID = COMSERVER_ID + 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    private static final long INBOUND_COMPORT_POOL_ID = DEVICE_ID + 1;

    @Mock
    private ComServer comServer;
    @Mock
    private InboundComPortPool comPortPool;
    @Mock
    private InboundComPort comPort;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private TaskHistoryService taskHistoryService;
    @Mock
    private ComSessionBuilder comSessionBuilder;

    private FakeServiceProvider serviceProvider;
    private InboundCommunicationHandler handler;

    @Before
    public void setup () {
        when(taskHistoryService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Date.class))).thenReturn(comSessionBuilder);
        this.serviceProvider = new FakeServiceProvider();
        this.serviceProvider.setTaskHistoryService(this.taskHistoryService);
        when(this.comServer.getId()).thenReturn(Long.valueOf(COMSERVER_ID));
        when(this.comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(this.comPortPool.getId()).thenReturn(Long.valueOf(COMPORT_POOL_ID));
        when(this.comPort.getId()).thenReturn(Long.valueOf(COMPORT_ID));
        when(this.comPort.getComPortPool()).thenReturn(this.comPortPool);
        when(this.comPort.getComServer()).thenReturn(this.comServer);
        this.handler = new InboundCommunicationHandler(this.comPort, this.comServerDAO, this.deviceCommandExecutor, this.serviceProvider);

        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(this.comTaskEnablement));
    }

    @Test
    public void testBinaryCommunicationWithDeviceThatDoesNotExist () {
        this.testCommunicationWithDeviceThatDoesNotExist(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testServletCommunicationWithDeviceThatDoesNotExist () {
        this.testCommunicationWithDeviceThatDoesNotExist(this.newServletInboundDiscoveryContext());
    }

    private void testCommunicationWithDeviceThatDoesNotExist (InboundDiscoveryContextImpl context) {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        when(inboundDeviceProtocol.getDeviceIdentifier()).thenReturn(mock(DeviceIdentifier.class));
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(null);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO, never()).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor, never()).tryAcquireTokens(anyInt());
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.DEVICE_NOT_FOUND);
    }

    @Test
    public void testComSessionShadowForBinaryCommunicationWithDeviceThatDoesNotExist () {
        this.testComSessionShadowForCommunicationWithDeviceThatDoesNotExist(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testComSessionShadowForServletCommunicationWithDeviceThatDoesNotExist () {
        this.testComSessionShadowForCommunicationWithDeviceThatDoesNotExist(this.newServletInboundDiscoveryContext());
    }

    private void testComSessionShadowForCommunicationWithDeviceThatDoesNotExist (InboundDiscoveryContextImpl context) {
        Date connectionEstablishedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 0, 0).toDate();
        Date sessionStartClock = new DateTime(2012, 10, 25, 13, 0, 0, 1).toDate();  // 1 milli second later
        Date discoveryStartedLogMessageClock = new DateTime(2012, 10, 25, 13, 0, 1, 0).toDate();   // 1 sec later
        Date discoveryResultLogEvent = new DateTime(2012, 10, 25, 13, 0, 2, 0).toDate();      // another sec later
        Date deviceNotFoundLogEvent = new DateTime(2012, 10, 25, 13, 0, 3, 0).toDate();          // another sec later
        Date sessionStopClock = new DateTime(2012, 10, 25, 13, 0, 5, 0).toDate();   // 5 secs later
        Date connectionClosedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 5, 1).toDate();   // 5001 milli seconds later
        Clock clock = mock(Clock.class);
        when(clock.now()).thenReturn(
                        connectionEstablishedEventOccurrenceClock,
                        sessionStartClock,
                        discoveryStartedLogMessageClock, // Once to actually log the message
                        discoveryStartedLogMessageClock, // Once to send the message in a LoggingEvent
                        discoveryResultLogEvent,    // Once to actually log the message
                        discoveryResultLogEvent,    // Once to send the message in a LoggingEvent
                        deviceNotFoundLogEvent,     // Once to actually log the message
                        deviceNotFoundLogEvent,     // Once to send the message in a LoggingEvent
                        sessionStopClock,
                        connectionClosedEventOccurrenceClock);
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        when(inboundDeviceProtocol.getDeviceIdentifier()).thenReturn(mock(DeviceIdentifier.class));
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(null);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(taskHistoryService).buildComSession(mock(ConnectionTask.class), comPortPool, comPort, sessionStartClock);
        verify(comSessionBuilder, never()).addComTaskExecutionSession(any(ComTaskExecution.class), any(Device.class), any(Date.class));
        verify(comSessionBuilder.endSession(sessionStopClock, ComSession.SuccessIndicator.Success));
        verify(comSessionBuilder, times(3)).addJournalEntry(any(Date.class), anyString(), any(Throwable.class));   // Expect three journal entries (discovery start, discovery result, device not found)
    }

    @Test
    public void testBinaryCommunicationWithDeviceThatIsNotReadyForCommunication () {
        this.testCommunicationWithDeviceThatIsNotReadyForCommunication(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testServletCommunicationWithDeviceThatIsNotReadyForCommunication () {
        this.testCommunicationWithDeviceThatIsNotReadyForCommunication(this.newServletInboundDiscoveryContext());
    }

    private void testCommunicationWithDeviceThatIsNotReadyForCommunication (InboundDiscoveryContextImpl context) {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(device);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(new ArrayList<ComTaskExecution>(0));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor, never()).tryAcquireTokens(anyInt());
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.DEVICE_DOES_NOT_EXPECT_INBOUND);

    }

    @Test
    public void testComSessionShadowForBinaryCommunicationWithDeviceThatIsNotReadyForCommunication () {
        this.testComSessionShadowForCommunicationWithDeviceThatIsNotReadyForCommunication(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testComSessionShadowForServletCommunicationWithDeviceThatIsNotReadyForCommunication () {
        this.testComSessionShadowForCommunicationWithDeviceThatIsNotReadyForCommunication(this.newServletInboundDiscoveryContext());
    }

    private void testComSessionShadowForCommunicationWithDeviceThatIsNotReadyForCommunication (InboundDiscoveryContextImpl context) {
        Date connectionEstablishedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 0, 0).toDate();
        Date sessionStartClock = new DateTime(2012, 10, 25, 13, 0, 0, 1).toDate();  // 1 milli second later
        Date discoveryStartedLogMessageClock = new DateTime(2012, 10, 25, 13, 0, 1, 0).toDate();   // 1 sec later
        Date discoveryResultLogEventClock = new DateTime(2012, 10, 25, 13, 0, 2, 0).toDate();      // another sec later
        Date noInboundConnectionTaskOnDeviceLogEventClock = new DateTime(2012, 10, 25, 13, 0, 3, 0).toDate();      // another sec later
        Date sessionStopClock = new DateTime(2012, 10, 25, 13, 0, 5, 0).toDate();   // 5 secs later
        Date connectionClosedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 5, 1).toDate();   // 5001 milli seconds later
        Clock clock =
                new ProgrammableClock().ticksAt(
                        connectionEstablishedEventOccurrenceClock,
                        sessionStartClock,
                        discoveryStartedLogMessageClock, // Once to actually log the message
                        discoveryStartedLogMessageClock, // Once to send the message in a LoggingEvent
                        discoveryResultLogEventClock,    // Once to actually log the message
                        discoveryResultLogEventClock,    // Once to send the message in a LoggingEvent
                        noInboundConnectionTaskOnDeviceLogEventClock,   // Once to actually log the message
                        noInboundConnectionTaskOnDeviceLogEventClock,   // Once to send the message in a LoggingEvent
                        sessionStopClock,
                        connectionClosedEventOccurrenceClock);
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(device);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(new ArrayList<ComTaskExecution>(0));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(taskHistoryService).buildComSession(mock(ConnectionTask.class), comPortPool, comPort, sessionStartClock);
        verify(comSessionBuilder, never()).addComTaskExecutionSession(any(ComTaskExecution.class), any(Device.class), any(Date.class));
        verify(comSessionBuilder.endSession(sessionStopClock, ComSession.SuccessIndicator.SetupError));
        verify(comSessionBuilder, times(3)).addJournalEntry(any(Date.class), anyString(), any(Throwable.class));   // Expect three journal entries (discovery start, discovery result, device not found)
    }

    @Test
    public void testBinaryCommunicationWhenServerIsBusy () {
        this.testCommunicationWhenServerIsBusy(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testServletCommunicationWhenServerIsBusy () {
        this.testCommunicationWhenServerIsBusy(this.newServletInboundDiscoveryContext());
    }

    private void testCommunicationWhenServerIsBusy (InboundDiscoveryContextImpl context) {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(new ArrayList<DeviceCommandExecutionToken>(0));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor).tryAcquireTokens(anyInt());
        verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.SERVER_BUSY);
    }

    @Test
    public void testComSessionShadowForBinaryCommunicationWhenServerIsBusy () {
        this.testComSessionShadowWhenServerIsBusy(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testComSessionShadowForServletCommunicationWhenServerIsBusy () {
        this.testComSessionShadowWhenServerIsBusy(this.newServletInboundDiscoveryContext());
    }

    private void testComSessionShadowWhenServerIsBusy (InboundDiscoveryContextImpl context) {
        Date connectionEstablishedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 0, 0).toDate();
        Date sessionStartClock = new DateTime(2012, 10, 25, 13, 0, 0, 0).toDate();  // 1 milli second later
        Date discoveryStartedLogMessageClock = new DateTime(2012, 10, 25, 13, 0, 1, 0).toDate();   // 1 sec later
        Date discoveryResultLogMessageClock = new DateTime(2012, 10, 25, 13, 0, 2, 0).toDate();    // another sec later
        Date serverBusyLogMessageClock = new DateTime(2012, 10, 25, 13, 0, 3, 0).toDate();         // another sec later
        Date sessionStopClock = new DateTime(2012, 10, 25, 13, 0, 5, 0).toDate();   // 5 secs later
        Date connectionClosedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 5, 1).toDate();   // 5001 milli seconds later
        Clock clock =
                new ProgrammableClock().ticksAt(
                        connectionEstablishedEventOccurrenceClock,
                        sessionStartClock,
                        discoveryStartedLogMessageClock, // Once to actually log the message
                        discoveryStartedLogMessageClock, // Once to send the message in a LoggingEvent
                        discoveryResultLogMessageClock,  // Once to actually log the message
                        discoveryResultLogMessageClock,  // Once to send the message in a LoggingEvent
                        serverBusyLogMessageClock,       // Once to actually log the message
                        serverBusyLogMessageClock,       // Once to send the message in a LoggingEvent
                        sessionStopClock,
                        connectionClosedEventOccurrenceClock);
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(new ArrayList<DeviceCommandExecutionToken>(0));
        when(this.taskHistoryService.buildComSession(any(ConnectionTask.class), eq(this.comPortPool), eq(this.comPort), any(Date.class))).thenReturn(this.comSessionBuilder);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comSessionBuilder).endSession(sessionStopClock, ComSession.SuccessIndicator.Broken);
        verify(this.comSessionBuilder, never()).addComTaskExecutionSession(any(ComTaskExecution.class), any(Device.class), any(Date.class));
    }

    @Test
    public void testSuccessFulBinaryCommunication () {
        this.testSuccessFulCommunication(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testSuccessFulSevletCommunication () {
        this.testSuccessFulCommunication(this.newServletInboundDiscoveryContext());
    }

    private void testSuccessFulCommunication (InboundDiscoveryContextImpl context) {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(mock(RegisterIdentifier.class));
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(collectedRegister);
        when(inboundDeviceProtocol.getCollectedData()).thenReturn(collectedData);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(device);
        ComTask comTask = mock(ComTask.class);
// Todo (JP-2013): wait for ComTaskConfiguration interface to be committed
//        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(true);
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));
        when(this.taskHistoryService.buildComSession(eq(connectionTask), eq(this.comPortPool), eq(this.comPort), any(Date.class))).thenReturn(this.comSessionBuilder);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor).tryAcquireTokens(anyInt());
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.SUCCESS);
        DeviceCommand deviceCommand = deviceCommandArgumentCaptor.getValue();
        assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) deviceCommand;
        assertThat(compositeDeviceCommand.getChildren()).hasSize(1);
        DeviceCommand childDeviceCommand = compositeDeviceCommand.getChildren().get(0);
        assertThat(childDeviceCommand).isInstanceOf(CreateInboundComSession.class);
        verify(this.comSessionBuilder).endSession(any(Date.class), any(ComSession.SuccessIndicator.class));
    }

    @Test
    public void testComSessionShadowForSuccessFulBinaryCommunication () {
        this.testComSessionShadowForSuccessFulCommunication(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testComSessionShadowForSuccessFulSevletCommunication () {
        this.testComSessionShadowForSuccessFulCommunication(this.newServletInboundDiscoveryContext());
    }

    private void testComSessionShadowForSuccessFulCommunication (InboundDiscoveryContextImpl context) {
        Date undiscoveredConnectionEstablishedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 0, 0).toDate();
        Date sessionStartClock = new DateTime(2012, 10, 25, 13, 0, 0, 1).toDate();  // 1 milli second later
        Date discoveryStartedLogMessageClock = new DateTime(2012, 10, 25, 13, 0, 1, 0).toDate();   // 1 sec later
        Date discoveryResultLogMessageClock = new DateTime(2012, 10, 25, 13, 0, 2, 0).toDate();    // another sec later
        Date connectionEstablishedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 2, 1).toDate();   // 1 milli second later
        Date deviceFoundLogMessageClock = new DateTime(2012, 10, 25, 13, 0, 2, 1).toDate();   // 1 milli second later
        Date comTaskExecutionStartClock = new DateTime(2012, 10, 25, 13, 0, 2, 1).toDate();   // another milli second later
        Date comTaskExecutionCompleteClock = new DateTime(2012, 10, 25, 13, 0, 2, 2).toDate();   // another milli second later
        Date sessionStopClock = new DateTime(2012, 10, 25, 13, 0, 5, 0).toDate();   // 5 secs later
        Date connectionClosedEventOccurrenceClock = new DateTime(2012, 10, 25, 13, 0, 5, 1).toDate();   // 5001 milli seconds later
        Clock clock =
                new ProgrammableClock().ticksAt(
                        undiscoveredConnectionEstablishedEventOccurrenceClock,
                        sessionStartClock,
                        discoveryStartedLogMessageClock, // Once to actually log the message
                        discoveryStartedLogMessageClock, // Once to send the message in a LoggingEvent
                        discoveryResultLogMessageClock,  // Once to actually log the message
                        discoveryResultLogMessageClock,  // Once to send the message in a LoggingEvent
                        connectionEstablishedEventOccurrenceClock,
                        deviceFoundLogMessageClock, // Once to actually log the message
                        deviceFoundLogMessageClock, // Once to send the message in a LoggingEvent
                        comTaskExecutionStartClock, // processing collected data
                        comTaskExecutionStartClock, // init comsessionshadow
                        comTaskExecutionCompleteClock, // completed comsTask
                        comTaskExecutionCompleteClock, // after processing collected data
                        sessionStopClock,
                        connectionClosedEventOccurrenceClock);
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(mock(RegisterIdentifier.class));
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(collectedRegister);
        when(inboundDeviceProtocol.getCollectedData()).thenReturn(collectedData);
        Device device = getMockedDevice();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getId()).thenReturn(DEVICE_ID);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(offlineDevice);
        ComTask comTask = mock(ComTask.class);
//        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(true);
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(this.comServerDAO.findExecutableInboundComTasks(offlineDevice, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));
        when(this.taskHistoryService.buildComSession(eq(connectionTask), eq(this.comPortPool), eq(this.comPort), any(Date.class))).thenReturn(this.comSessionBuilder);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comSessionBuilder).endSession(sessionStopClock, ComSession.SuccessIndicator.Success);
        verify(this.comSessionBuilder, never()).addComTaskExecutionSession(any(ComTaskExecution.class), any(Device.class), any(Date.class));
    }

    @Test
    public void testSuccessFulBinaryCommunicationWithHandOverToProtocol () throws BusinessException {
        this.testSuccessFulCommunicationWithHandOverToProtocol(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testSuccessFulServletCommunicationWithHandOverToProtocol () throws BusinessException {
        this.testSuccessFulCommunicationWithHandOverToProtocol(this.newServletInboundDiscoveryContext());
    }

    private void testSuccessFulCommunicationWithHandOverToProtocol (InboundDiscoveryContextImpl inboundDiscoveryContext) throws BusinessException {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.IDENTIFIER);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        Device device = getMockedDevice();
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(offlineDevice);
        ComTask comTask = mock(ComTask.class);
// Todo (JP-2013): wait for ComTaskConfiguration interface to be committed
//        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(false);
//        when(comTask.isConfiguredToCheckClock()).thenReturn(false);
//        when(comTask.isConfiguredToCollectEvents()).thenReturn(false);
//        when(comTask.isConfiguredToCollectLoadProfileData()).thenReturn(false);
//        when(comTask.isConfiguredToReadStatusInformation()).thenReturn(false);
//        when(comTask.isConfiguredToRunBasicChecks()).thenReturn(false);
//        when(comTask.isConfiguredToSendMessages()).thenReturn(false);
//        when(comTask.isConfiguredToUpdateTopology()).thenReturn(false);
        InboundComPortPool inboundComPortPool = mock(InboundComPortPool.class);
        when(inboundComPortPool.getId()).thenReturn(Long.valueOf(INBOUND_COMPORT_POOL_ID));
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(inboundComPortPool);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getDevice()).thenReturn(device);
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(this.comServerDAO.findExecutableInboundComTasks(offlineDevice, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));

        // Business method
        final InboundCommunicationHandler spy = spy(this.handler);
        spy.handle(inboundDeviceProtocol, inboundDiscoveryContext);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor).tryAcquireTokens(anyInt());
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.SUCCESS);
        DeviceCommand deviceCommand = deviceCommandArgumentCaptor.getValue();
        assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) deviceCommand;
        assertThat(compositeDeviceCommand.getChildren()).hasSize(2);
        assertThat(compositeDeviceCommand.getChildren()).haveAtLeast(1, new org.assertj.core.api.Condition<DeviceCommand>() {
            @Override
            public boolean matches(DeviceCommand deviceCommand) {
                return deviceCommand instanceof CreateInboundComSession;
            }
        });
        verify(spy, times(1)).handOverToDeviceProtocol(token);
    }

    private Device getMockedDevice(){
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getDeviceType()).thenReturn(this.deviceType);
        when(device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfiguration.getCommunicationConfiguration()).thenReturn(this.deviceCommunicationConfiguration);
        return device;
    }

    @Test
    public void testSuccessFulBinaryCommunicationWithAllCollectedDataFiltered () {
        this.testSuccessFulCommunicationWithAllCollectedDataFiltered(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testSuccessFulSevletCommunicationWithAllCollectedDataFiltered () {
        this.testSuccessFulCommunicationWithAllCollectedDataFiltered(this.newServletInboundDiscoveryContext());
    }

    private void testSuccessFulCommunicationWithAllCollectedDataFiltered (InboundDiscoveryContextImpl context) {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(mock(RegisterIdentifier.class));
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(collectedRegister);
        when(inboundDeviceProtocol.getCollectedData()).thenReturn(collectedData);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(device);
        ComTask comTask = mock(ComTask.class);
// Todo (JP-2013): wait for ComTaskConfiguration interface to be committed
//        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(false);
//        when(comTask.isConfiguredToCheckClock()).thenReturn(false);
//        when(comTask.isConfiguredToCollectEvents()).thenReturn(false);
//        when(comTask.isConfiguredToCollectLoadProfileData()).thenReturn(false);
//        when(comTask.isConfiguredToReadStatusInformation()).thenReturn(false);
//        when(comTask.isConfiguredToRunBasicChecks()).thenReturn(false);
//        when(comTask.isConfiguredToSendMessages()).thenReturn(false);
//        when(comTask.isConfiguredToUpdateTopology()).thenReturn(false);
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));
        when(this.taskHistoryService.buildComSession(connectionTask, eq(this.comPortPool), eq(this.comPort), any(Date.class))).thenReturn(this.comSessionBuilder);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor).tryAcquireTokens(anyInt());
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.SUCCESS);
        DeviceCommand deviceCommand = deviceCommandArgumentCaptor.getValue();
        assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) deviceCommand;
        assertThat(compositeDeviceCommand.getChildren()).hasSize(1);
        DeviceCommand childDeviceCommand = compositeDeviceCommand.getChildren().get(0);
        assertThat(childDeviceCommand).isInstanceOf(CreateInboundComSession.class);
        verify(this.comSessionBuilder).endSession(any(Date.class), any(ComSession.SuccessIndicator.class));
    }

    private InboundDiscoveryContextImpl newBinaryInboundDiscoveryContext () {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(comPort, mock(ComChannel.class), taskHistoryService);
        this.initializeInboundDiscoveryContext(context);
        return context;
    }

    private InboundDiscoveryContextImpl newServletInboundDiscoveryContext () {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(comPort, mock(HttpServletRequest.class), mock(HttpServletResponse.class), taskHistoryService);
        this.initializeInboundDiscoveryContext(context);
        return context;
    }

    private void initializeInboundDiscoveryContext (InboundDiscoveryContext context) {
        context.setLogger(Logger.getAnonymousLogger());
        Cryptographer cryptographer = mock(Cryptographer.class);
        when(cryptographer.wasUsed()).thenReturn(true);
        context.setCryptographer(cryptographer);
    }

}