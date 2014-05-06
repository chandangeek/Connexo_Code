package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.comserver.commands.CompositeDeviceCommand;
import com.energyict.comserver.commands.CreateInboundComSession;
import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.comserver.commands.DeviceCommandExecutionToken;
import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.comserver.commands.DeviceCommandFactoryImpl;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.comserver.time.PredefinedTickingClock;
import com.energyict.mdc.InMemoryPersistence;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.communication.tasks.ProtocolDialectPropertiesFactory;
import com.energyict.mdc.communication.tasks.ServerComTask;
import com.energyict.mdc.communication.tasks.ServerComTaskEnablementFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.shadow.journal.ComSessionShadow;
import com.energyict.mdc.tasks.ComTask;
import org.fest.assertions.core.Condition;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
    private ServerManager manager;
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
    private ServerComTaskEnablementFactory comTaskEnablementFactory;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private ProtocolDialectPropertiesFactory protocolDialectPropertiesFactory;

    private InboundCommunicationHandler handler;

    @BeforeClass
    public static void initializeEnvironment () throws IOException, SQLException {
        InMemoryPersistence.initializeDatabase(false);
    }

    @AfterClass
    public static void cleanupDatabase () throws SQLException {
        InMemoryPersistence.cleanUpDataBase();
    }

    @Before
    public void setup () {
        ManagerFactory.setCurrent(this.manager);
        when(this.manager.getDeviceCommandFactory()).thenReturn(new DeviceCommandFactoryImpl());
        when(this.manager.getProtocolDialectPropertiesFactory()).thenReturn(this.protocolDialectPropertiesFactory);

        when(this.comServer.getId()).thenReturn(Long.valueOf(COMSERVER_ID));
        when(this.comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(this.comPortPool.getId()).thenReturn(Long.valueOf(COMPORT_POOL_ID));
        when(this.comPort.getId()).thenReturn(Long.valueOf(COMPORT_ID));
        when(this.comPort.getComPortPool()).thenReturn(this.comPortPool);
        when(this.comPort.getComServer()).thenReturn(this.comServer);
        this.handler = new InboundCommunicationHandler(this.comPort, this.comServerDAO, this.deviceCommandExecutor, mock(IssueService.class));

        when(this.manager.getComTaskEnablementFactory()).thenReturn(this.comTaskEnablementFactory);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                any(DeviceCommunicationConfiguration.class), any(ComTask.class))).thenReturn(comTaskEnablement);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
    }

    @After
    public void resetTimeFactory () throws SQLException {
        Clocks.resetAll();
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
        FrozenClock connectionEstablishedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 0, 0);
        FrozenClock sessionStartClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 0, 1);  // 1 milli second later
        FrozenClock discoveryStartedLogMessageClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 1, 0);   // 1 sec later
        FrozenClock discoveryResultLogEvent = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 2, 0);      // another sec later
        FrozenClock deviceNotFoundLogEvent = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 3, 0);          // another sec later
        FrozenClock sessionStopClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 5, 0);   // 5 secs later
        FrozenClock connectionClosedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 5, 1);   // 5001 milli seconds later
        PredefinedTickingClock clock =
                new PredefinedTickingClock(
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
        Clocks.setAppServerClock(clock);
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        when(inboundDeviceProtocol.getDeviceIdentifier()).thenReturn(mock(DeviceIdentifier.class));
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(null);

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        ComSessionShadow comSessionShadow = context.getComSessionShadow();
        Assertions.assertThat(comSessionShadow).isNotNull();
        Assertions.assertThat(comSessionShadow.getComPortId()).isEqualTo((int) COMPORT_ID);
        Assertions.assertThat(comSessionShadow.getComPortPoolId()).isEqualTo((int) COMPORT_POOL_ID);
        Assertions.assertThat(comSessionShadow.getComTaskExecutionSessionShadows()).isEmpty();
        Assertions.assertThat(comSessionShadow.getConnectionTaskId()).isZero();
        Assertions.assertThat(comSessionShadow.getStartDate()).isEqualTo(sessionStartClock.now());
        Assertions.assertThat(comSessionShadow.getStopDate()).isEqualTo(sessionStopClock.now());
        Assertions.assertThat(comSessionShadow.getSuccessIndicator()).isEqualTo(ComSession.SuccessIndicator.Success);
        Assertions.assertThat(comSessionShadow.getJournalEntryShadows()).hasSize(3);   // Expect three journal entries (discovery start, discovery result, device not found)
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
        FrozenClock connectionEstablishedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 0, 0);
        FrozenClock sessionStartClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 0, 1);  // 1 milli second later
        FrozenClock discoveryStartedLogMessageClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 1, 0);   // 1 sec later
        FrozenClock discoveryResultLogEventClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 2, 0);      // another sec later
        FrozenClock noInboundConnectionTaskOnDeviceLogEventClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 3, 0);      // another sec later
        FrozenClock sessionStopClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 5, 0);   // 5 secs later
        FrozenClock connectionClosedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 5, 1);   // 5001 milli seconds later
        PredefinedTickingClock clock =
                new PredefinedTickingClock(
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
        Clocks.setAppServerClock(clock);
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        OfflineDevice device = mock(OfflineDevice.class);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(device);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(new ArrayList<ComTaskExecution>(0));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        ComSessionShadow comSessionShadow = context.getComSessionShadow();
        Assertions.assertThat(comSessionShadow).isNotNull();
        Assertions.assertThat(comSessionShadow.getComPortId()).isEqualTo((int) COMPORT_ID);
        Assertions.assertThat(comSessionShadow.getComPortPoolId()).isEqualTo((int) COMPORT_POOL_ID);
        Assertions.assertThat(comSessionShadow.getComTaskExecutionSessionShadows()).isEmpty();
        Assertions.assertThat(comSessionShadow.getConnectionTaskId()).isZero();
        Assertions.assertThat(comSessionShadow.getStartDate()).isEqualTo(sessionStartClock.now());
        Assertions.assertThat(comSessionShadow.getStopDate()).isEqualTo(sessionStopClock.now());
        Assertions.assertThat(comSessionShadow.getSuccessIndicator()).isEqualTo(ComSession.SuccessIndicator.SetupError);
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
        FrozenClock connectionEstablishedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 0, 0);
        FrozenClock sessionStartClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 0, 0);  // 1 milli second later
        FrozenClock discoveryStartedLogMessageClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 1, 0);   // 1 sec later
        FrozenClock discoveryResultLogMessageClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 2, 0);    // another sec later
        FrozenClock serverBusyLogMessageClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 3, 0);         // another sec later
        FrozenClock sessionStopClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 5, 0);   // 5 secs later
        FrozenClock connectionClosedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 5, 1);   // 5001 milli seconds later
        PredefinedTickingClock clock =
                new PredefinedTickingClock(
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
        Clocks.setAppServerClock(clock);
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
        ComSessionShadow comSessionShadow = context.getComSessionShadow();
        Assertions.assertThat(comSessionShadow).isNotNull();
        Assertions.assertThat(comSessionShadow.getComPortId()).isEqualTo((int) COMPORT_ID);
        Assertions.assertThat(comSessionShadow.getComPortPoolId()).isEqualTo((int) COMPORT_POOL_ID);
        Assertions.assertThat(comSessionShadow.getComTaskExecutionSessionShadows()).isEmpty();
        Assertions.assertThat(comSessionShadow.getConnectionTaskId()).isZero();
        Assertions.assertThat(comSessionShadow.getStartDate()).isEqualTo(sessionStartClock.now());
        Assertions.assertThat(comSessionShadow.getStopDate()).isEqualTo(sessionStopClock.now());
        Assertions.assertThat(comSessionShadow.getSuccessIndicator()).isEqualTo(ComSession.SuccessIndicator.Broken);
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
        ServerComTask comTask = mock(ServerComTask.class);
        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(true);
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor).tryAcquireTokens(anyInt());
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.SUCCESS);
        DeviceCommand deviceCommand = deviceCommandArgumentCaptor.getValue();
        Assertions.assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) deviceCommand;
        Assertions.assertThat(compositeDeviceCommand.getChildren()).hasSize(1);
        DeviceCommand childDeviceCommand = compositeDeviceCommand.getChildren().get(0);
        Assertions.assertThat(childDeviceCommand).isInstanceOf(CreateInboundComSession.class);
        CreateInboundComSession createComSessionCommand = (CreateInboundComSession) childDeviceCommand;
        ComSessionShadow comSessionShadow = createComSessionCommand.getComSessionShadow();
        Assertions.assertThat(comSessionShadow).isNotNull();
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
        FrozenClock undiscoveredConnectionEstablishedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 0, 0);
        FrozenClock sessionStartClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 0, 1);  // 1 milli second later
        FrozenClock discoveryStartedLogMessageClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 1, 0);   // 1 sec later
        FrozenClock discoveryResultLogMessageClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 2, 0);    // another sec later
        FrozenClock connectionEstablishedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 2, 1);   // 1 milli second later
        FrozenClock deviceFoundLogMessageClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 2, 1);   // 1 milli second later
        FrozenClock comTaskExecutionStartClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 2, 1);   // another milli second later
        FrozenClock comTaskExecutionCompleteClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 2, 2);   // another milli second later
        FrozenClock sessionStopClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 5, 0);   // 5 secs later
        FrozenClock connectionClosedEventOccurrenceClock = FrozenClock.frozenOn(2012, Calendar.OCTOBER, 25, 13, 0, 5, 1);   // 5001 milli seconds later
        PredefinedTickingClock clock =
                new PredefinedTickingClock(
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
        Clocks.setAppServerClock(clock);
        InboundDeviceProtocol inboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(mock(RegisterIdentifier.class));
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(collectedRegister);
        when(inboundDeviceProtocol.getCollectedData()).thenReturn(collectedData);
        Device device = getMockedDevice();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getId()).thenReturn((int) DEVICE_ID);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(offlineDevice);
        ServerComTask comTask = mock(ServerComTask.class);
        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(true);
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

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        ComSessionShadow comSessionShadow = context.getComSessionShadow();
        Assertions.assertThat(comSessionShadow).isNotNull();
        Assertions.assertThat(comSessionShadow.getComPortId()).isEqualTo((int) COMPORT_ID);
        Assertions.assertThat(comSessionShadow.getComPortPoolId()).isEqualTo((int) COMPORT_POOL_ID);
        Assertions.assertThat(comSessionShadow.getComTaskExecutionSessionShadows()).isEmpty();
        Assertions.assertThat(comSessionShadow.getConnectionTaskId()).isEqualTo((int) CONNECTION_TASK_ID);
        Assertions.assertThat(comSessionShadow.getStartDate()).isEqualTo(sessionStartClock.now());
        Assertions.assertThat(comSessionShadow.getStopDate()).isEqualTo(sessionStopClock.now());
        Assertions.assertThat(comSessionShadow.getSuccessIndicator()).isEqualTo(ComSession.SuccessIndicator.Success);
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
        when(device.goOffline()).thenReturn(offlineDevice);
        when(device.goOffline(any(OfflineDeviceContext.class))).thenReturn(offlineDevice);
        when(this.comServerDAO.findDevice(any(DeviceIdentifier.class))).thenReturn(offlineDevice);
        ServerComTask comTask = mock(ServerComTask.class);
        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(false);
        when(comTask.isConfiguredToCheckClock()).thenReturn(false);
        when(comTask.isConfiguredToCollectEvents()).thenReturn(false);
        when(comTask.isConfiguredToCollectLoadProfileData()).thenReturn(false);
        when(comTask.isConfiguredToReadStatusInformation()).thenReturn(false);
        when(comTask.isConfiguredToRunBasicChecks()).thenReturn(false);
        when(comTask.isConfiguredToSendMessages()).thenReturn(false);
        when(comTask.isConfiguredToUpdateTopology()).thenReturn(false);
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
        Assertions.assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) deviceCommand;
        Assertions.assertThat(compositeDeviceCommand.getChildren()).hasSize(2);
        Assertions.assertThat(compositeDeviceCommand.getChildren()).haveAtLeast(1, new Condition<Object>() {
            @Override
            public boolean matches (Object o) {
                return o instanceof CreateInboundComSession;
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
        ServerComTask comTask = mock(ServerComTask.class);
        when(comTask.isConfiguredToCollectRegisterData()).thenReturn(false);
        when(comTask.isConfiguredToCheckClock()).thenReturn(false);
        when(comTask.isConfiguredToCollectEvents()).thenReturn(false);
        when(comTask.isConfiguredToCollectLoadProfileData()).thenReturn(false);
        when(comTask.isConfiguredToReadStatusInformation()).thenReturn(false);
        when(comTask.isConfiguredToRunBasicChecks()).thenReturn(false);
        when(comTask.isConfiguredToSendMessages()).thenReturn(false);
        when(comTask.isConfiguredToUpdateTopology()).thenReturn(false);
        ConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(this.comServerDAO.findExecutableInboundComTasks(device, this.comPort)).thenReturn(Arrays.asList(comTaskExecution));
        DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.asList(token));

        // Business method
        this.handler.handle(inboundDeviceProtocol, context);

        // Asserts
        verify(this.comServerDAO).findExecutableInboundComTasks(any(OfflineDevice.class), any(InboundComPort.class));
        verify(this.deviceCommandExecutor).tryAcquireTokens(anyInt());
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        verify(inboundDeviceProtocol).provideResponse(InboundDeviceProtocol.DiscoverResponseType.SUCCESS);
        DeviceCommand deviceCommand = deviceCommandArgumentCaptor.getValue();
        Assertions.assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) deviceCommand;
        Assertions.assertThat(compositeDeviceCommand.getChildren()).hasSize(1);
        DeviceCommand childDeviceCommand = compositeDeviceCommand.getChildren().get(0);
        Assertions.assertThat(childDeviceCommand).isInstanceOf(CreateInboundComSession.class);
        CreateInboundComSession createComSessionCommand = (CreateInboundComSession) childDeviceCommand;
        ComSessionShadow comSessionShadow = createComSessionCommand.getComSessionShadow();
        Assertions.assertThat(comSessionShadow).isNotNull();
    }

    private InboundDiscoveryContextImpl newBinaryInboundDiscoveryContext () {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(comPort, mock(ComChannel.class));
        this.initializeInboundDiscoveryContext(context);
        return context;
    }

    private InboundDiscoveryContextImpl newServletInboundDiscoveryContext () {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(comPort, mock(HttpServletRequest.class), mock(HttpServletResponse.class));
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