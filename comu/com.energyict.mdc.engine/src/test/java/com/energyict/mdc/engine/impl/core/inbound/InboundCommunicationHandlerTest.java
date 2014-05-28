package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateInboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
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
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.assertj.core.api.Condition;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
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
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private EngineService engineService;

    private FakeTransactionService transactionService = new FakeTransactionService();
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private InboundCommunicationHandler handler;
    private Clock clock = new ProgrammableClock();

    @Before
    public void setup () {
        EventPublisherImpl.setInstance(mock(EventPublisherImpl.class));
        ServiceProvider.instance.set(serviceProvider);
        serviceProvider.setClock(clock);
        serviceProvider.setTaskHistoryService(taskHistoryService);
        when(taskHistoryService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Date.class))).
            thenReturn(this.comSessionBuilder);
        when(this.comSessionBuilder.addSentBytes(anyLong())).thenReturn(this.comSessionBuilder);
        when(this.comSessionBuilder.addReceivedBytes(anyLong())).thenReturn(this.comSessionBuilder);
        when(this.comSessionBuilder.addSentPackets(anyLong())).thenReturn(this.comSessionBuilder);
        when(this.comSessionBuilder.addReceivedPackets(anyLong())).thenReturn(this.comSessionBuilder);
        this.comTaskExecutionSessionBuilder = mock(ComTaskExecutionSessionBuilder.class);
        when(this.comSessionBuilder.addComTaskExecutionSession(any(ComTaskExecution.class), any(Device.class), any(Date.class))).thenReturn(comTaskExecutionSessionBuilder);
        this.serviceProvider.setProtocolPluggableService(this.protocolPluggableService);
        this.serviceProvider.setDeviceConfigurationService(this.deviceConfigurationService);
        // The following prohibits the execution of every ComTask on all devices
        when(this.deviceConfigurationService.findComTaskEnablement(any(ComTask.class), any(DeviceConfiguration.class))).thenReturn(Optional.<ComTaskEnablement>absent());
        this.serviceProvider.setEngineService(this.engineService);
        when(this.engineService.findDeviceCacheByDeviceId(anyLong())).thenReturn(Optional.<DeviceCache>absent());
        this.serviceProvider.setTransactionService(this.transactionService);
        when(this.protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(anyString())).thenReturn(Collections.<InboundDeviceProtocolPluggableClass>emptyList());
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

    @After
    public void tearDown() {
        EventPublisherImpl.setInstance(null);
        ServiceProvider.instance.set(null);
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testBinaryCommunicationWithDeviceThatDoesNotExist () {
        this.testCommunicationWithDeviceThatDoesNotExist(this.newBinaryInboundDiscoveryContext());
    }

    // Todo (JP-3084)
    @Ignore
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

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testComSessionShadowForBinaryCommunicationWithDeviceThatDoesNotExist () {
        this.testComSessionShadowForCommunicationWithDeviceThatDoesNotExist(this.newBinaryInboundDiscoveryContext());
    }

    // Todo (JP-3084)
    @Ignore
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

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testBinaryCommunicationWithDeviceThatIsNotReadyForCommunication () {
        this.testCommunicationWithDeviceThatIsNotReadyForCommunication(this.newBinaryInboundDiscoveryContext());
    }

    // Todo (JP-3084)
    @Ignore
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

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testComSessionShadowForBinaryCommunicationWithDeviceThatIsNotReadyForCommunication () {
        this.testComSessionShadowForCommunicationWithDeviceThatIsNotReadyForCommunication(this.newBinaryInboundDiscoveryContext());
    }

    // Todo (JP-3084)
    @Ignore
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
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
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
        verify(this.comSessionBuilder).endSession(any(Date.class), eq(ComSession.SuccessIndicator.Broken));
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
        verify(this.comSessionBuilder).endSession(any(Date.class), eq(ComSession.SuccessIndicator.Success));
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
        when(device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(device.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
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
        assertThat(compositeDeviceCommand.getChildren()).haveAtLeast(1, new Condition<DeviceCommand>() {
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

    private InboundDiscoveryContextImpl newBinaryInboundDiscoveryContext () {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(comPort, new ComPortRelatedComChannelImpl(mock(ComChannel.class)), taskHistoryService);
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
        // TODO need initialization of ComSessionBuilder on context
    }

}