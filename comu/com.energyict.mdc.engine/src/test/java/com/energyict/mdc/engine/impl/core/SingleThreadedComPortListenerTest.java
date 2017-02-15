/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundCapableComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.EngineServiceImpl;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactory;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactoryImpl;
import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.services.HexService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SingleThreadedComPortListenerTest {

    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 1;

    private static final TimeDuration TIME_DURATION = new TimeDuration(1, TimeDuration.TimeUnit.SECONDS);

    @Mock
    private ManagementBeanFactory managementBeanFactory;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private IssueService issueService;
    @Mock
    private SocketService socketService;
    @Mock
    private HexService hexService;
    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private InboundComPortExecutorImpl.ServiceProvider inboundComPortExecutorServiceProvider;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private RunningComServer runningComServer;
    @Mock
    private ThreadPrincipalService threadPrincipalService;

    private Clock clock = Clock.systemDefaultZone();

    private Thread mockedThread() {
        return mock(Thread.class);
    }

    @Before
    public void initializeMocks() throws IOException {
        when(this.inboundComPortExecutorServiceProvider.issueService()).thenReturn(this.issueService);
        when(this.inboundComPortExecutorServiceProvider.clock()).thenReturn(this.clock);
        when(this.socketService.newInboundTCPSocket(anyInt())).thenReturn(mock(ServerSocket.class));
        when(this.socketService.newSocketComChannel(any(Socket.class))).thenReturn(new SystemOutComChannel());
        when(this.userService.findUser(EngineServiceImpl.COMSERVER_USER)).thenReturn(Optional.of(user));
        when(user.getLocale()).thenReturn(Optional.of(Locale.ENGLISH));
    }

    @Test(timeout = 5000)
    public void testSimulatedVoidComChannelWithNoHandOver() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch progressLatch = new CountDownLatch(1);
        InboundComPortConnector connector = spy(new LatchDrivenTimeOutInboundComPortConnector(startLatch, progressLatch));

        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getName()).thenReturn("testSimulatedVoidComChannelWithNoHandOver");
        ThreadFactory threadFactory = new ComServerThreadFactory(comServer);

        InboundComPort inboundComPort = this.mockComPort("testSimulatedVoidComChannelWithNoHandOver");
        InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
        when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(connector);
        ComChannelBasedComPortListenerImpl.ServiceProvider serviceProvider = mock(ComChannelBasedComPortListenerImpl.ServiceProvider.class);

        when(serviceProvider.threadFactory()).thenReturn(threadFactory);
        when(serviceProvider.inboundComPortConnectorFactory()).thenReturn(inboundComPortConnectorFactory);
        when(serviceProvider.clock()).thenReturn(this.clock);
        when(serviceProvider.userService()).thenReturn(this.userService);
        when(serviceProvider.managementBeanFactory()).thenReturn(managementBeanFactory);
        ComServerDAO mockedComServerDAO = getMockedComServerDAO();
        when(serviceProvider.comServerDAO()).thenReturn(mockedComServerDAO);
        when(serviceProvider.threadPrincipalService()).thenReturn(threadPrincipalService);

        SingleThreadedComPortListener singleThreadedComPortListener =
                spy(new SingleThreadedComPortListener(runningComServer, inboundComPort, this.deviceCommandExecutor, serviceProvider));
        // business method
        singleThreadedComPortListener.start();
        startLatch.await(); // wait until the accept has occurred

        //Asserts
        verify(connector, atLeast(1)).accept(); // accept should have been called twice (one time it should have returned a VoidComChannel
        verify(singleThreadedComPortListener, never()).handleInboundDeviceProtocol(any(ComPortRelatedComChannel.class));
    }

    private ComServerDAO getMockedComServerDAO() {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getComServerUser()).thenReturn(user);
        return comServerDAO;
    }

    @Test(timeout = 5000)
    public void testStartupCleansInterruptedTasks() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch progressLatch = new CountDownLatch(1);
        VoidTestComChannel voidTestComChannel = new VoidTestComChannel();
        ComPortRelatedComChannel comPortRelatedComChannel = mock(ComPortRelatedComChannel.class);
        when(comPortRelatedComChannel.getActualComChannel()).thenReturn(voidTestComChannel);
        InboundComPortConnector connector = spy(new LatchDrivenTimeOutInboundComPortConnector(startLatch, progressLatch));

        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getName()).thenReturn("testStartupCleansInterruptedTasks");
        ThreadFactory threadFactory = new ComServerThreadFactory(comServer);

        InboundComPort inboundComPort = this.mockComPort("testStartupCleansInterruptedTasks");
        InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
        when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(connector);
        ComChannelBasedComPortListenerImpl.ServiceProvider serviceProvider = mock(ComChannelBasedComPortListenerImpl.ServiceProvider.class);
        when(serviceProvider.threadFactory()).thenReturn(threadFactory);
        when(serviceProvider.inboundComPortConnectorFactory()).thenReturn(inboundComPortConnectorFactory);
        when(serviceProvider.clock()).thenReturn(this.clock);
        when(serviceProvider.userService()).thenReturn(this.userService);
        when(serviceProvider.managementBeanFactory()).thenReturn(managementBeanFactory);
        ComServerDAO comServerDAO = getMockedComServerDAO();
        when(serviceProvider.comServerDAO()).thenReturn(comServerDAO);
        when(serviceProvider.threadPrincipalService()).thenReturn(threadPrincipalService);

        SingleThreadedComPortListener singleThreadedComPortListener =
                spy(new SingleThreadedComPortListener(runningComServer, inboundComPort, this.deviceCommandExecutor, serviceProvider));
        // business method
        singleThreadedComPortListener.start();
        startLatch.await(); // wait until the accept has occurred

        //Asserts
        verify(comServerDAO, times(1)).releaseTasksFor(inboundComPort);
    }

    @Test
    public void testStart() throws InterruptedException {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);
        InboundComPort inboundComPort = this.mockComPort("testStart");
        InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
        when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(mock(InboundComPortConnector.class));
        ComChannelBasedComPortListenerImpl.ServiceProvider serviceProvider = mock(ComChannelBasedComPortListenerImpl.ServiceProvider.class);
        when(serviceProvider.threadFactory()).thenReturn(threadFactory);
        when(serviceProvider.inboundComPortConnectorFactory()).thenReturn(inboundComPortConnectorFactory);
        when(serviceProvider.clock()).thenReturn(this.clock);
        when(serviceProvider.managementBeanFactory()).thenReturn(managementBeanFactory);
        SingleThreadedComPortListener singleThreadedComPortListener =
                new SingleThreadedComPortListener(
                        runningComServer,
                        inboundComPort,
                        this.deviceCommandExecutor,
                        serviceProvider,
                        new InboundComPortExecutorFactoryImpl(this.inboundComPortExecutorServiceProvider));

        // business method
        singleThreadedComPortListener.start();

        // Asserts
        verify(threadFactory, times(1)).newThread(any(Runnable.class));
        verify(mockedThread, times(1)).start();
    }

    @Test
    public void testShutdown() {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);
        InboundComPort inboundComPort = this.mockComPort("testShutdown");
        InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
        when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(mock(InboundComPortConnector.class));
        ComChannelBasedComPortListenerImpl.ServiceProvider serviceProvider = mock(ComChannelBasedComPortListenerImpl.ServiceProvider.class);
        when(serviceProvider.threadFactory()).thenReturn(threadFactory);
        when(serviceProvider.inboundComPortConnectorFactory()).thenReturn(inboundComPortConnectorFactory);
        when(serviceProvider.clock()).thenReturn(this.clock);
        when(serviceProvider.managementBeanFactory()).thenReturn(managementBeanFactory);
        SingleThreadedComPortListener singleThreadedComPortListener =
                new SingleThreadedComPortListener(
                        runningComServer,
                        inboundComPort,
                        this.deviceCommandExecutor,
                        serviceProvider,
                        new InboundComPortExecutorFactoryImpl(this.inboundComPortExecutorServiceProvider));

        // business method
        singleThreadedComPortListener.start();
        singleThreadedComPortListener.shutdown();

        // Asserts
        verify(threadFactory, times(1)).newThread(any(Runnable.class));
        verify(mockedThread, times(1)).interrupt();
    }

    @Test(timeout = 5000)
    public void testAcceptedInboundCall() throws InterruptedException {
        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getName()).thenReturn("testAcceptedInboundCall");
        ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch progressLatch = new CountDownLatch(1);
        CountDownLatch protocolLatch = new CountDownLatch(1);
        final ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        final InboundComPortConnector connector = spy(new LatchDrivenAcceptInboundComPortConnector(startLatch, progressLatch, comChannel));
        InboundComPort inboundComPort = this.mockComPort("accept");
        InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
        when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(connector);
        ComChannelBasedComPortListenerImpl.ServiceProvider serviceProvider = mock(ComChannelBasedComPortListenerImpl.ServiceProvider.class);
        when(serviceProvider.threadFactory()).thenReturn(threadFactory);
        when(serviceProvider.inboundComPortConnectorFactory()).thenReturn(inboundComPortConnectorFactory);
        when(serviceProvider.clock()).thenReturn(this.clock);
        when(serviceProvider.userService()).thenReturn(this.userService);
        when(serviceProvider.managementBeanFactory()).thenReturn(managementBeanFactory);
        ComServerDAO mockedComServerDAO = getMockedComServerDAO();
        when(serviceProvider.comServerDAO()).thenReturn(mockedComServerDAO);
        when(serviceProvider.threadPrincipalService()).thenReturn(threadPrincipalService);
        LatchDrivenSingleThreadedComPortListener singleThreadedComPortListener =
                spy(new LatchDrivenSingleThreadedComPortListener(
                        runningComServer,
                        inboundComPort,
                        this.deviceCommandExecutor,
                        serviceProvider,
                        new InboundComPortExecutorFactoryImpl(this.inboundComPortExecutorServiceProvider)));
        singleThreadedComPortListener.setCounter(protocolLatch);

        // Business method
        singleThreadedComPortListener.start();
        startLatch.await(); // wait until the accept has occurred
        protocolLatch.await(); // wait until the handOver has happened

        //Asserts
        verify(connector, atLeast(1)).accept(); // accept should have been called twice (one time it should have returned a VoidComChannel
        verify(singleThreadedComPortListener, times(1)).handleInboundDeviceProtocol(comChannel);
        verify(singleThreadedComPortListener, times(1)).setThreadPrinciple();
    }

    private InboundComPort mockComPort(String name) {
        return this.mockComPort(name, mock(InboundCapableComServer.class));
    }

    private InboundComPort mockComPort(String name, InboundCapableComServer comServer) {
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TIME_DURATION);
        when(comServer.getChangesInterPollDelay()).thenReturn(TIME_DURATION);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getName()).thenReturn("SingleThreadedComPortListener#" + name);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private class LatchDrivenTimeOutInboundComPortConnector implements InboundComPortConnector {

        private CountDownLatch startLatch;
        private CountDownLatch progressLatch;

        private LatchDrivenTimeOutInboundComPortConnector(CountDownLatch startLatch, CountDownLatch progressLatch) {
            super();
            this.startLatch = startLatch;
            this.progressLatch = progressLatch;
        }

        @Override
        public ComPortRelatedComChannel accept() {
            if (this.startLatch.getCount() > 0) {
                this.startLatch.countDown();
                return this.doAccept();
            } else {
                try {
                    progressLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return this.doAccept();
            }
        }

        @Override
        public void close() {
            //nothing to do
        }

        protected ComPortRelatedComChannel doAccept() {
            return null;
        }
    }

    private class LatchDrivenAcceptInboundComPortConnector implements InboundComPortConnector {

        private CountDownLatch startLatch;
        private CountDownLatch progressLatch;
        private ComPortRelatedComChannel comChannel;  // the used ComChannel

        private LatchDrivenAcceptInboundComPortConnector(CountDownLatch startLatch, CountDownLatch progressLatch, ComPortRelatedComChannel comChannel) {
            super();
            this.startLatch = startLatch;
            this.progressLatch = progressLatch;
            this.comChannel = comChannel;
        }

        @Override
        public ComPortRelatedComChannel accept() {
            if (this.startLatch.getCount() > 0) {
                this.startLatch.countDown();
                return this.doAccept();
            } else {
                try {
                    progressLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return this.comChannel;
            }
        }

        @Override
        public void close() throws Exception {
            // Nothing to close for now
        }

        protected ComPortRelatedComChannel doAccept() {
            // Unit testing commands typically don't do anything useful
            System.out.println(this.toString() + " is now executing, creating Mock ComChannel ...");
            return comChannel;
        }
    }

    private class LatchDrivenSingleThreadedComPortListener extends SingleThreadedComPortListener {

        private CountDownLatch counter;

        private LatchDrivenSingleThreadedComPortListener(RunningComServer runningComServer, InboundComPort comPort, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, InboundComPortExecutorFactory inboundComPortExecutorFactory) {
            super(runningComServer, comPort, deviceCommandExecutor, serviceProvider, inboundComPortExecutorFactory);
        }

        private void setCounter(CountDownLatch countDownLatch) {
            this.counter = countDownLatch;
        }

        @Override
        protected void handleInboundDeviceProtocol(ComPortRelatedComChannel comChannel) {
            this.counter.countDown(); // don't do any handover to actual protocol :o) (otherwise you need to much mocking)
        }
    }
}