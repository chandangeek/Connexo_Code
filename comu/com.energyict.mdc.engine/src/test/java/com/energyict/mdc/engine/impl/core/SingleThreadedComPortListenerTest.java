package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactory;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactoryImpl;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundCapableComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.issues.IssueService;

import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.protocols.mdc.channels.VoidComChannel;
import com.energyict.protocols.mdc.services.SocketService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

import org.junit.*;
import org.junit.runner.*;
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

/**
 * Tests for the {@link com.energyict.mdc.engine.impl.core.SingleThreadedComPortListener} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 19/10/12
 * Time: 9:57
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleThreadedComPortListenerTest {

    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 1;

    private static final TimeDuration TIME_DURATION = new TimeDuration(1, TimeDuration.SECONDS);

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private IssueService issueService;
    @Mock
    private SocketService socketService;
    @Mock
    private EventPublisherImpl eventPublisher;

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    private Thread mockedThread() {
        return mock(Thread.class);
    }

    @Before
    public void setupServiceProvider () throws IOException {
        this.serviceProvider.setIssueService(this.issueService);
        this.serviceProvider.setSocketService(this.socketService);
        this.serviceProvider.setClock(new DefaultClock());
        ServiceProvider.instance.set(this.serviceProvider);
        when(this.socketService.newTCPSocket(anyInt())).thenReturn(mock(ServerSocket.class));
        when(this.socketService.newSocketComChannel(any(Socket.class))).thenReturn(new SystemOutComChannel());
    }

    @Before
    public void setupEventPublisher () {
        EventPublisherImpl.setInstance(this.eventPublisher);
    }

    @After
    public void resetEventPublisher () {
        EventPublisherImpl.setInstance(null);
    }

    @Test
    public void testStart() throws BusinessException, InterruptedException {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);

        InboundComPort inboundComPort = this.mockComPort("testStart");
        InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
        when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(mock(InboundComPortConnector.class));
        SingleThreadedComPortListener singleThreadedComPortListener =
                new SingleThreadedComPortListener(
                        inboundComPort,
                        mock(ComServerDAO.class),
                        threadFactory,
                        this.deviceCommandExecutor,
                        new InboundComPortExecutorFactoryImpl(this.serviceProvider),
                        inboundComPortConnectorFactory
                );

        // business method
        singleThreadedComPortListener.start();

        // Asserts
        verify(threadFactory, times(2)).newThread(any(Runnable.class));
        verify(mockedThread, times(2)).start();
    }

    @Test
    public void testShutdown() throws BusinessException {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);

        InboundComPort inboundComPort = this.mockComPort("testShutdown");
        InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
        when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(mock(InboundComPortConnector.class));
        SingleThreadedComPortListener singleThreadedComPortListener =
                new SingleThreadedComPortListener(
                        inboundComPort,
                        mock(ComServerDAO.class),
                        threadFactory,
                        this.deviceCommandExecutor,
                        new InboundComPortExecutorFactoryImpl(this.serviceProvider),
                        inboundComPortConnectorFactory
                );

        // business method
        singleThreadedComPortListener.start();
        singleThreadedComPortListener.shutdown();

        // Asserts
        verify(threadFactory, times(2)).newThread(any(Runnable.class));
        verify(mockedThread, times(2)).interrupt();
    }

    @Test(timeout = 5000)
    public void testSimulatedVoidComChannelWithNoHandOver() throws BusinessException, InterruptedException {
        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getName()).thenReturn("testSimulatedVoidComChannelWithNoHandOver");
        ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch progressLatch = new CountDownLatch(1);
        InboundComPortConnector connector = spy(new LatchDrivenTimeOutInboundComPortConnector(startLatch, progressLatch));

        InboundComPort inboundComPort = this.mockComPort("simTimeout");
        InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
        when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(connector);
        SingleThreadedComPortListener singleThreadedComPortListener =
                spy(new SingleThreadedComPortListener(
                        inboundComPort,
                        mock(ComServerDAO.class),
                        threadFactory,
                        this.deviceCommandExecutor,
                        new InboundComPortExecutorFactoryImpl(this.serviceProvider),
                        inboundComPortConnectorFactory
                ));
        // business method
        singleThreadedComPortListener.start();
        startLatch.await(); // wait until the accept has occurred

        //Asserts
        verify(connector, atLeast(1)).accept(); // accept should have been called twice (one time it should have returned a VoidComChannel
        verify(singleThreadedComPortListener, never()).handleInboundDeviceProtocol(any(ComPortRelatedComChannel.class));
    }

    @Test(timeout = 5000)
    public void testAcceptedInboundCall() throws InterruptedException, BusinessException {
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
        LatchDrivenSingleThreadedComPortListener singleThreadedComPortListener =
                spy(new LatchDrivenSingleThreadedComPortListener(
                        inboundComPort,
                        mock(ComServerDAO.class),
                        threadFactory,
                        this.deviceCommandExecutor,
                        new InboundComPortExecutorFactoryImpl(this.serviceProvider),
                        inboundComPortConnectorFactory,
                        this.serviceProvider));
        singleThreadedComPortListener.setCounter(protocolLatch);

        // Business method
        singleThreadedComPortListener.start();
        startLatch.await(); // wait until the accept has occurred
        protocolLatch.await(); // wait until the handOver has happened

        //Asserts
        verify(connector, atLeast(1)).accept(); // accept should have been called twice (one time it should have returned a VoidComChannel
        verify(singleThreadedComPortListener, times(1)).handleInboundDeviceProtocol(comChannel);
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

        protected ComPortRelatedComChannel doAccept() {
            // Unit testing commands typically don't do anything useful
            System.out.println(this.toString() + " is now executing, creating Mock ComChannel ...");
            return new ComPortRelatedComChannelImpl(new VoidComChannel());
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
                return new ComPortRelatedComChannelImpl(new VoidComChannel());
            }
        }

        protected ComPortRelatedComChannel doAccept() {
            // Unit testing commands typically don't do anything useful
            System.out.println(this.toString() + " is now executing, creating Mock ComChannel ...");
            return comChannel;
        }
    }

    private class LatchDrivenSingleThreadedComPortListener extends SingleThreadedComPortListener{

        private CountDownLatch counter;

        private LatchDrivenSingleThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor, InboundComPortExecutorFactory inboundComPortExecutorFactory, InboundComPortConnectorFactory inboundComPortConnectorFactory, ServiceProvider serviceProvider) {
            super(comPort, comServerDAO, threadFactory, deviceCommandExecutor, inboundComPortExecutorFactory, inboundComPortConnectorFactory);
        }

        private void setCounter(CountDownLatch countDownLatch){
            this.counter = countDownLatch;
        }

        @Override
        protected void handleInboundDeviceProtocol(ComPortRelatedComChannel comChannel) {
            this.counter.countDown(); // don't do any handover to actual protocol :o) (otherwise you need to much mocking)
        }
    }

    private InboundComPort mockComPort(String name) {
        return this.mockComPort(name,  mock(InboundCapableComServer.class));
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

}
