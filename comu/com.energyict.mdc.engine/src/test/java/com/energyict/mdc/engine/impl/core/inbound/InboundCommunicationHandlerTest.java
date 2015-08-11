package com.energyict.mdc.engine.impl.core.inbound;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.config.*;
import com.energyict.mdc.engine.impl.commands.store.*;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.InboundJobExecutionDataProcessor;
import com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.RegistersTask;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
    private ComPortDiscoveryLogger testDiscoveryLogger;
    @Mock
    private ComServer comServer;
    @Mock
    private InboundComPortPool comPortPool;
    @Mock
    private InboundComPort comPort;
    @Mock
    private DeviceCommandExecutorImpl deviceCommandExecutor;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private EngineService engineService;
    @Mock
    private EventService eventService;
    @Mock
    private HexService hexService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private InboundCommunicationHandler.ServiceProvider serviceProvider;
    @Mock
    private SecurityPropertySet securityPropertySet;
    @Mock
    private AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel;
    @Mock
    private EncryptionDeviceAccessLevel encryptionDeviceAccessLevel;

    private FakeTransactionService transactionService = new FakeTransactionService();
    private TestInboundCommunicationHandler handler;
    private Clock clock = Clock.systemDefaultZone();
    private EventPublisherImpl eventPublisher;

    @Before
    public void setup() {
        this.eventPublisher = mock(EventPublisherImpl.class);
        when(this.serviceProvider.clock()).thenReturn(clock);
        when(this.serviceProvider.eventService()).thenReturn(eventService);
        when(this.serviceProvider.eventPublisher()).thenReturn(eventPublisher);
        when(this.serviceProvider.identificationService()).thenReturn(identificationService);
        when(this.serviceProvider.connectionTaskService()).thenReturn(connectionTaskService);
        when(this.serviceProvider.hexService()).thenReturn(this.hexService);
        when(connectionTaskService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Instant.class))).
                thenReturn(this.comSessionBuilder);
        when(this.comSessionBuilder.addSentBytes(anyLong())).thenReturn(this.comSessionBuilder);
        when(this.comSessionBuilder.addReceivedBytes(anyLong())).thenReturn(this.comSessionBuilder);
        when(this.comSessionBuilder.addSentPackets(anyLong())).thenReturn(this.comSessionBuilder);
        when(this.comSessionBuilder.addReceivedPackets(anyLong())).thenReturn(this.comSessionBuilder);
        this.comTaskExecutionSessionBuilder = mock(ComTaskExecutionSessionBuilder.class);
        when(this.comSessionBuilder.addComTaskExecutionSession(any(ComTaskExecution.class), any(ComTask.class), any(Device.class), any(Instant.class))).thenReturn(comTaskExecutionSessionBuilder);
        when(this.serviceProvider.protocolPluggableService()).thenReturn(this.protocolPluggableService);
        when(this.serviceProvider.deviceConfigurationService()).thenReturn(this.deviceConfigurationService);
        when(this.serviceProvider.engineService()).thenReturn(this.engineService);
        when(this.engineService.findDeviceCacheByDevice(any(Device.class))).thenReturn(Optional.empty());
        when(this.serviceProvider.transactionService()).thenReturn(this.transactionService);
        when(this.protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(anyString())).thenReturn(Collections.<InboundDeviceProtocolPluggableClass>emptyList());
        when(this.comServer.getId()).thenReturn(Long.valueOf(COMSERVER_ID));
        when(this.comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(this.comPortPool.getId()).thenReturn(Long.valueOf(COMPORT_POOL_ID));
        when(this.comPort.getId()).thenReturn(Long.valueOf(COMPORT_ID));
        when(this.comPort.getComPortPool()).thenReturn(this.comPortPool);
        when(this.comPort.getComServer()).thenReturn(this.comServer);
        this.handler = new TestInboundCommunicationHandler(this.comPort, this.comServerDAO, this.deviceCommandExecutor);

        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(this.comTaskEnablement));
        // The following prohibits the execution of every ComTask on all devices of the configuration
        when(this.deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.empty());
        when(this.comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(2);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(3);
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testBinaryCommunicationWithDeviceThatDoesNotExist() {
        this.testCommunicationWithDeviceThatDoesNotExist(this.newBinaryInboundDiscoveryContext());
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testServletCommunicationWithDeviceThatDoesNotExist() {
        this.testCommunicationWithDeviceThatDoesNotExist(this.newServletInboundDiscoveryContext());
    }

    private void testCommunicationWithDeviceThatDoesNotExist(InboundDiscoveryContextImpl context) {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        when(inboundDeviceProtocol.getDeviceIdentifier()).thenReturn(mock(DeviceIdentifier.class));
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(null);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO, never()).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor, never()).tryAcquireTokens(anyInt());
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.DEVICE_NOT_FOUND);
        verify(this.eventService, never()).postEvent(anyString(), anyObject());
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testComSessionShadowForBinaryCommunicationWithDeviceThatDoesNotExist() {
        this.testComSessionShadowForCommunicationWithDeviceThatDoesNotExist(this.newBinaryInboundDiscoveryContext());
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testComSessionShadowForServletCommunicationWithDeviceThatDoesNotExist() {
        this.testComSessionShadowForCommunicationWithDeviceThatDoesNotExist(this.newServletInboundDiscoveryContext());
    }

    private void testComSessionShadowForCommunicationWithDeviceThatDoesNotExist(InboundDiscoveryContextImpl context) {
        Instant connectionEstablishedEventOccurrenceClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        Instant sessionStartClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 0, 1, ZoneId.of("UTC")).toInstant();  // 1 milli second later
        Instant discoveryStartedLogMessageClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 1, 0, ZoneId.of("UTC")).toInstant();   // 1 sec later
        Instant discoveryResultLogEvent = ZonedDateTime.of(2012, 10, 25, 13, 0, 2, 0, ZoneId.of("UTC")).toInstant();      // another sec later
        Instant deviceNotFoundLogEvent = ZonedDateTime.of(2012, 10, 25, 13, 0, 3, 0, ZoneId.of("UTC")).toInstant();          // another sec later
        Instant sessionStopClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 5, 0, ZoneId.of("UTC")).toInstant();   // 5 secs later
        Instant connectionClosedEventOccurrenceClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 5, 1, ZoneId.of("UTC")).toInstant();   // 5001 milli seconds later
        Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(
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
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(null);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(connectionTaskService).buildComSession(mock(ConnectionTask.class), comPortPool, comPort, sessionStartClock);
        verify(comSessionBuilder, never()).addComTaskExecutionSession(any(ComTaskExecution.class), any(ComTask.class), any(Device.class), any(Instant.class));
        verify(comSessionBuilder.endSession(sessionStopClock, ComSession.SuccessIndicator.Success));
        verify(comSessionBuilder, times(3)).addJournalEntry(any(Instant.class), ComServer.LogLevel.INFO, anyString(), any(Throwable.class));   // Expect three journal entries (discovery start, discovery result, device not found)
        verify(this.eventService, never()).postEvent(anyString(), anyObject());
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testBinaryCommunicationWithDeviceThatIsNotReadyForCommunication() {
        this.testCommunicationWithDeviceThatIsNotReadyForCommunication(this.newBinaryInboundDiscoveryContext());
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testServletCommunicationWithDeviceThatIsNotReadyForCommunication() {
        this.testCommunicationWithDeviceThatIsNotReadyForCommunication(this.newServletInboundDiscoveryContext());
    }

    private void testCommunicationWithDeviceThatIsNotReadyForCommunication(InboundDiscoveryContextImpl context) {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(device);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(new ArrayList<ComTaskExecution>(0));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor, never()).tryAcquireTokens(anyInt());
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.DEVICE_DOES_NOT_EXPECT_INBOUND);
        verify(this.eventService, never()).postEvent(anyString(), anyObject());
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testComSessionShadowForBinaryCommunicationWithDeviceThatIsNotReadyForCommunication() {
        this.testComSessionShadowForCommunicationWithDeviceThatIsNotReadyForCommunication(this.newBinaryInboundDiscoveryContext());
    }

    // Todo (JP-3084)
    @Ignore
    @Test
    public void testComSessionShadowForServletCommunicationWithDeviceThatIsNotReadyForCommunication() {
        this.testComSessionShadowForCommunicationWithDeviceThatIsNotReadyForCommunication(this.newServletInboundDiscoveryContext());
    }

    private void testComSessionShadowForCommunicationWithDeviceThatIsNotReadyForCommunication(InboundDiscoveryContextImpl context) {
        Instant connectionEstablishedEventOccurrenceClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        Instant sessionStartClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 0, 1, ZoneId.of("UTC")).toInstant();  // 1 milli second later
        Instant discoveryStartedLogMessageClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 1, 0, ZoneId.of("UTC")).toInstant();   // 1 sec later
        Instant discoveryResultLogEventClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 2, 0, ZoneId.of("UTC")).toInstant();      // another sec later
        Instant noInboundConnectionTaskOnDeviceLogEventClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 3, 0, ZoneId.of("UTC")).toInstant();      // another sec later
        Instant sessionStopClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 5, 0, ZoneId.of("UTC")).toInstant();   // 5 secs later
        Instant connectionClosedEventOccurrenceClock = ZonedDateTime.of(2012, 10, 25, 13, 0, 5, 1, ZoneId.of("UTC")).toInstant();   // 5001 milli seconds later
        Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(
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
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(device);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(new ArrayList<ComTaskExecution>(0));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(connectionTaskService).buildComSession(mock(ConnectionTask.class), comPortPool, comPort, sessionStartClock);
        verify(comSessionBuilder, never()).addComTaskExecutionSession(any(ComTaskExecution.class), any(ComTask.class), any(Device.class), any(Instant.class));
        verify(comSessionBuilder.endSession(sessionStopClock, ComSession.SuccessIndicator.SetupError));
        verify(comSessionBuilder, times(3)).addJournalEntry(any(Instant.class), ComServer.LogLevel.INFO, anyString(), any(Throwable.class));   // Expect three journal entries (discovery start, discovery result, device not found)
    }

    @Test
    public void testBinaryCommunicationWhenServerIsBusy() throws ExecutionException, InterruptedException {
        this.testCommunicationWhenServerIsBusy(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testServletCommunicationWhenServerIsBusy() throws ExecutionException, InterruptedException {
        this.testCommunicationWhenServerIsBusy(this.newServletInboundDiscoveryContext());
    }

    private void testCommunicationWhenServerIsBusy(InboundDiscoveryContextImpl context) throws ExecutionException, InterruptedException {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(new ArrayList<>(0));

        Future<? extends Object> future = mock(Future.class);
        when(future.get()).thenReturn(true);
        doReturn(future).when(deviceCommandExecutor).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor).tryAcquireTokens(anyInt());
        verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.SERVER_BUSY);
        verify(this.eventService, never()).postEvent(anyString(), anyObject());
    }

    @Test
    public void testComSessionShadowForBinaryCommunicationWhenServerIsBusy() throws ExecutionException, InterruptedException {
        this.testComSessionShadowWhenServerIsBusy(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testComSessionShadowForServletCommunicationWhenServerIsBusy() throws ExecutionException, InterruptedException {
        this.testComSessionShadowWhenServerIsBusy(this.newServletInboundDiscoveryContext());
    }

    private void testComSessionShadowWhenServerIsBusy(InboundDiscoveryContextImpl context) throws ExecutionException, InterruptedException {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(new ArrayList<>(0));
        when(this.connectionTaskService.buildComSession(any(ConnectionTask.class), eq(this.comPortPool), eq(this.comPort), any(Instant.class))).thenReturn(this.comSessionBuilder);

        Future<? extends Object> future = mock(Future.class);
        when(future.get()).thenReturn(true);
        doReturn(future).when(deviceCommandExecutor).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comSessionBuilder, never()).addComTaskExecutionSession(any(ComTaskExecution.class), any(ComTask.class), any(Device.class), any(Instant.class));
    }

    @Test
    public void testSuccessFulBinaryCommunication() throws ExecutionException, InterruptedException {
        this.testSuccessFulCommunication(this.newBinaryInboundDiscoveryContext(), true);
    }

    @Test
    public void testSuccessFulSevletCommunication() throws ExecutionException, InterruptedException {
        this.testSuccessFulCommunication(this.newServletInboundDiscoveryContext(), true);
    }

    @Test
    public void testSuccessFulBinaryCommunicationWithStoringFailure() throws ExecutionException, InterruptedException {
        this.testSuccessFulCommunication(this.newBinaryInboundDiscoveryContext(), false);
    }

    private void testSuccessFulCommunication(InboundDiscoveryContextImpl context, final boolean storingSuccess) throws ExecutionException, InterruptedException {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(mock(RegisterIdentifier.class), mock(ReadingType.class));
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(collectedRegister);
        when(inboundDeviceProtocol.getCollectedData(any(OfflineDevice.class))).thenReturn(collectedData);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(offlineDevice);
        ComTask comTask = mock(ComTask.class);
        RegistersTask registersTask = mock(RegistersTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(registersTask));
        Device device = mock(Device.class);
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.isConfiguredToCollectRegisterData()).thenReturn(true);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.getProtocolTasks()).thenReturn(Collections.singletonList(registersTask));
        when(this.comServerDAO.findExecutableInboundComTasks(offlineDevice, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(this.comServerDAO.findExecutableInboundComTasks(offlineDevice, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));

        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));
        when(this.connectionTaskService.buildComSession(eq(connectionTask), eq(this.comPortPool), eq(this.comPort), any(Instant.class))).thenReturn(this.comSessionBuilder);

        Future future = mock(Future.class);
        when(deviceCommandExecutor.execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class))).thenAnswer(invocationOnMock -> {
            ComSessionRootDeviceCommand comSessionRootDeviceCommand = (ComSessionRootDeviceCommand) invocationOnMock.getArguments()[0];
            comSessionRootDeviceCommand.getChildren().stream().filter(deviceCommand -> deviceCommand instanceof ProvideInboundResponseDeviceCommand).forEach(deviceCommand -> {
                ProvideInboundResponseDeviceCommand provideInboundResponseDeviceCommand = (ProvideInboundResponseDeviceCommand) deviceCommand;
                if (!storingSuccess) {
                    provideInboundResponseDeviceCommand.dataStorageFailed();
                }
                provideInboundResponseDeviceCommand.execute(comServerDAO);
            });
            return future;
        });

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor).tryAcquireTokens(anyInt());
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        verify(inboundDeviceProtocol).provideResponse(storingSuccess ? InboundDeviceProtocol.DiscoverResponseType.SUCCESS : InboundDeviceProtocol.DiscoverResponseType.STORING_FAILURE);
        DeviceCommand deviceCommand = deviceCommandArgumentCaptor.getValue();
        assertThat(deviceCommand).isInstanceOf(ComSessionRootDeviceCommand.class);
        ComSessionRootDeviceCommand compositeDeviceCommand = (ComSessionRootDeviceCommand) deviceCommand;
        assertThat(compositeDeviceCommand.getChildren()).hasSize(7);
        assertThat(compositeDeviceCommand.getChildren()).hasAtLeastOneElementOfType(CreateInboundComSession.class);
        assertThat(compositeDeviceCommand.getChildren()).hasAtLeastOneElementOfType(InboundDataProcessMeterDataStoreCommandImpl.class);
        assertThat(compositeDeviceCommand.getChildren()).hasAtLeastOneElementOfType(ProvideInboundResponseDeviceCommand.class);
        assertThat(compositeDeviceCommand.getChildren()).hasAtLeastOneElementOfType(PublishConnectionCompletionEvent.class);
        assertThat(compositeDeviceCommand.getChildren()).hasAtLeastOneElementOfType(RescheduleSuccessfulExecution.class);
        assertThat(compositeDeviceCommand.getChildren()).hasAtLeastOneElementOfType(UnlockScheduledJobDeviceCommand.class);
    }

    @Test
    public void testComSessionShadowForSuccessFulBinaryCommunication() throws ExecutionException, InterruptedException {
        this.testComSessionShadowForSuccessFulCommunication(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testComSessionShadowForSuccessFulSevletCommunication() throws ExecutionException, InterruptedException {
        this.testComSessionShadowForSuccessFulCommunication(this.newServletInboundDiscoveryContext());
    }

    private void testComSessionShadowForSuccessFulCommunication(InboundDiscoveryContextImpl context) throws ExecutionException, InterruptedException {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(mock(RegisterIdentifier.class), mock(ReadingType.class));
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(collectedRegister);
        when(inboundDeviceProtocol.getCollectedData(any(OfflineDevice.class))).thenReturn(collectedData);
        Device device = getMockedDevice();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getId()).thenReturn(DEVICE_ID);
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(offlineDevice);
        ComTask comTask = mock(ComTask.class);
//        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(true);
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(this.comServerDAO.findExecutableInboundComTasks(offlineDevice, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));
        when(this.connectionTaskService.buildComSession(eq(connectionTask), eq(this.comPortPool), eq(this.comPort), any(Instant.class))).thenReturn(this.comSessionBuilder);

        Future<? extends Object> future = mock(Future.class);
        when(future.get()).thenReturn(true);
        doReturn(future).when(deviceCommandExecutor).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comSessionBuilder, never()).addComTaskExecutionSession(any(ComTaskExecution.class), any(ComTask.class), any(Device.class), any(Instant.class));
    }

    @Test
    public void testSuccessFulBinaryCommunicationWithHandOverToProtocol() throws BusinessException, ExecutionException, InterruptedException {
        this.testSuccessFulCommunicationWithHandOverToProtocol(this.newBinaryInboundDiscoveryContext());
    }

    @Test
    public void testSuccessFulServletCommunicationWithHandOverToProtocol() throws BusinessException, ExecutionException, InterruptedException {
        this.testSuccessFulCommunicationWithHandOverToProtocol(this.newServletInboundDiscoveryContext());
    }

    private void testSuccessFulCommunicationWithHandOverToProtocol(InboundDiscoveryContextImpl inboundDiscoveryContext) throws BusinessException, ExecutionException, InterruptedException {
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.IDENTIFIER);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        Device device = getMockedDevice();
        when(this.comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(offlineDevice);
        when(device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(device.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(device.getProtocolDialectProperties(any(String.class))).thenReturn(Optional.empty());
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
        when(this.deviceConfiguration.getComTaskEnablementFor(comTask)).thenReturn(Optional.of(comTaskEnablement));
        InboundComPortPool inboundComPortPool = mock(InboundComPortPool.class);
        when(inboundComPortPool.getId()).thenReturn(Long.valueOf(INBOUND_COMPORT_POOL_ID));
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(inboundComPortPool);
        when(connectionTask.getDevice()).thenReturn(device);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.getDevice()).thenReturn(device);
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(this.comServerDAO.findExecutableInboundComTasks(offlineDevice, this.comPort)).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution));
        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));

        Future<? extends Object> future = mock(Future.class);
        when(future.get()).thenReturn(true);
        doReturn(future).when(deviceCommandExecutor).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));

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
        assertThat(compositeDeviceCommand.getChildren()).haveAtLeast(1, new Condition<DeviceCommand>() {
            @Override
            public boolean matches(DeviceCommand deviceCommand) {
                return deviceCommand instanceof CreateInboundComSession;
            }
        });
        verify(spy, times(1)).handOverToDeviceProtocol(token);
    }

    private Device getMockedDevice() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getDeviceType()).thenReturn(this.deviceType);
        when(device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        return device;
    }

    private InboundDiscoveryContextImpl newBinaryInboundDiscoveryContext() {
        InboundDiscoveryContextImpl context =
                new InboundDiscoveryContextImpl(
                        this.comPort,
                        new ComPortRelatedComChannelImpl(
                                mock(ComChannel.class),
                                this.comPort,
                                this.clock,
                                this.hexService,
                                this.eventPublisher),
                        this.connectionTaskService);
        this.initializeInboundDiscoveryContext(context);
        return context;
    }

    private InboundDiscoveryContextImpl newServletInboundDiscoveryContext() {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(this.comPort, mock(HttpServletRequest.class), mock(HttpServletResponse.class), this.connectionTaskService);
        this.initializeInboundDiscoveryContext(context);
        return context;
    }

    private void initializeInboundDiscoveryContext(InboundDiscoveryContext context) {
        context.setLogger(Logger.getAnonymousLogger());
        Cryptographer cryptographer = mock(Cryptographer.class);
        when(cryptographer.wasUsed()).thenReturn(true);
        context.setCryptographer(cryptographer);
        // TODO need initialization of ComSessionBuilder on context
    }

    private class TestInboundCommunicationHandler extends InboundCommunicationHandler {

        private TestInboundJobExecutionDataProcessor inboundJobExecutionDataProcessor;

        public TestInboundCommunicationHandler(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutorImpl deviceCommandExecutor) {
            super(comPort, comServerDAO, deviceCommandExecutor,serviceProvider);
        }

        protected void processCollectedData(InboundDeviceProtocol inboundDeviceProtocol, DeviceCommandExecutionToken token, OfflineDevice offlineDevice) {
            inboundJobExecutionDataProcessor = new TestInboundJobExecutionDataProcessor(
                    getComPort(),
                    comServerDAO,
                    deviceCommandExecutor,
                    getContext(),
                    inboundDeviceProtocol,
                    offlineDevice,
                    this
            );
            inboundJobExecutionDataProcessor.setToken(token);
            inboundJobExecutionDataProcessor.setConnectionTask(this.getConnectionTask());
            inboundJobExecutionDataProcessor.executeDeviceProtocol(this.getDeviceComTaskExecutions());
        }

        /**
         * Exposing the processor instance that is used to execute the inbound DeviceProtocol, so it can be used in this test
         */
        public TestInboundJobExecutionDataProcessor getInboundJobExecutionDataProcessor() {
            return inboundJobExecutionDataProcessor;
        }
    }


    private class TestInboundJobExecutionDataProcessor extends InboundJobExecutionDataProcessor {

        public TestInboundJobExecutionDataProcessor(ComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundDiscoveryContextImpl inboundDiscoveryContext, InboundDeviceProtocol inboundDeviceProtocol, OfflineDevice offlineDevice, InboundCommunicationHandler inboundCommunicationHandler) {
            super(comPort, comServerDAO, deviceCommandExecutor, inboundDiscoveryContext, inboundDeviceProtocol, offlineDevice, serviceProvider, inboundCommunicationHandler, testDiscoveryLogger);
        }

        @Override
        protected DeviceProtocol getDeviceProtocol() {
            return mock(DeviceProtocol.class);
        }
    }

}