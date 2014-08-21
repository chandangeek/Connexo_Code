package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactory;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactoryImpl;
import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundCapableComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.services.HexService;

import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.protocols.mdc.channels.VoidComChannel;
import com.energyict.protocols.mdc.services.SocketService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.MultiThreadedComPortListener} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (12:49)
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiThreadedComPortListenerTest {

    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 3;

    private static final TimeDuration INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.MINUTES);

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private IssueService issueService;
    @Mock
    private HexService hexService;
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

    @Test(timeout = 5000)
    public void testStart() throws BusinessException, InterruptedException {
        MultiThreadedComPortListener multiThreadedComPortListener = null;
        try {
            ThreadFactory threadFactory = mock(ThreadFactory.class);
            Thread mockedThread = this.mockedThread();
            when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);

            multiThreadedComPortListener = new MultiThreadedComPortListener(this.mockComPort("testStart"), mock(ComServerDAO.class), this.deviceCommandExecutor, threadFactory, new InboundComPortExecutorFactoryImpl(this.serviceProvider), this.serviceProvider);

            // business method
            multiThreadedComPortListener.start();

            // Asserts
            verify(threadFactory, times(2)).newThread(any(Runnable.class));
            verify(mockedThread, times(2)).start();
            assertThat(multiThreadedComPortListener.getResourceManager().getCapacity()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        } finally {
            if (multiThreadedComPortListener != null) {
                multiThreadedComPortListener.shutdown();
            }
        }

    }

    @Test(timeout = 5000)
    public void testShutdown() throws BusinessException {
        MultiThreadedComPortListener comPortListener = null;
        try {
            ThreadFactory threadFactory = mock(ThreadFactory.class);
            Thread mockedThread = this.mockedThread();
            when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);
            comPortListener = new MultiThreadedComPortListener(this.mockComPort("testShutDown"), mock(ComServerDAO.class), this.deviceCommandExecutor, threadFactory, new InboundComPortExecutorFactoryImpl(this.serviceProvider), this.serviceProvider);

            comPortListener.start();

            // Business method
            comPortListener.shutdown();

            // Asserts
            verify(threadFactory, times(2)).newThread(any(Runnable.class));
            verify(mockedThread, times(2)).interrupt();
        } finally {
            if (comPortListener != null) {
                comPortListener.shutdownImmediate();
            }
        }
    }

    @Test(timeout = 5000)
    public void testAcceptedInboundCall() throws InterruptedException {
        MultiThreadedComPortListener multiThreadedComPortListener = null;

        try {
            InboundCapableComServer comServer = mock(InboundCapableComServer.class);
            when(comServer.getName()).thenReturn("testAcceptedInboundCall");
            ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
            final InboundComPortConnector connector = mock(InboundComPortConnector.class);
            final ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
            when(connector.accept()).thenReturn(comChannel);
            final InboundComPort inboundComPort = this.mockComPort("accept", comServer);
            InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
            when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(connector);
            final ComServerDAO comServerDAO = mock(ComServerDAO.class);
            InboundComPortExecutorFactory inboundComPortExecutorFactory = mock(InboundComPortExecutorFactory.class);
            CountDownLatch startLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1);
            CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            when(inboundComPortExecutorFactory.
                    create(
                            any(InboundComPort.class),
                            any(ComServerDAO.class),
                            any(DeviceCommandExecutor.class))).
                    thenReturn(new LatchDrivenInboundComPortExecutor(startLatch, stopLatch));
            multiThreadedComPortListener = spy(new MultiThreadedComPortListener(
                    inboundComPort,
                    comServerDAO,
                    this.deviceCommandExecutor,
                    threadFactory,
                    inboundComPortExecutorFactory,
                    inboundComPortConnectorFactory,
                    this.serviceProvider));
            // business method
            multiThreadedComPortListener.start();

            startLatch.countDown();

            // Now wait until the commands complete
            stopLatch.await();

            //Asserts
            verify(connector, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)).accept();
            verify(inboundComPortExecutorFactory, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)).create(any(InboundComPort.class), any(ComServerDAO.class), any(DeviceCommandExecutor.class)); // accept should have been called twice (one time it should have returned a VoidComChannel
        } finally {
            if (multiThreadedComPortListener != null) {
                multiThreadedComPortListener.shutdownImmediate();
            }
        }

    }

    @Test(timeout = 5000)
    public void testOverLoad() throws BusinessException, InterruptedException {
        MultiThreadedComPortListener multiThreadedComPortListener = null;
        try {
            InboundCapableComServer comServer = mock(InboundCapableComServer.class);
            when(comServer.getName()).thenReturn("testOverload");
            ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
            CountDownLatch connectorStartLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            final InboundComPortConnector connector = new LatchDrivenInboundComPortConnector(connectorStartLatch);
            final TCPBasedInboundComPort inboundComPort = this.mockComPort("OverLoad", comServer);
            InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
            when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(connector);
            final ComServerDAO comServerDAO = mock(ComServerDAO.class);
            InboundComPortExecutorFactory inboundComPortExecutorFactory = mock(InboundComPortExecutorFactory.class);
            CountDownLatch startLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1);
            CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            when(inboundComPortExecutorFactory.
                    create(
                            any(InboundComPort.class),
                            any(ComServerDAO.class),
                            any(DeviceCommandExecutor.class))).
                    thenReturn(new LatchDrivenInboundComPortExecutor(startLatch, stopLatch));
            multiThreadedComPortListener = spy(new MultiThreadedComPortListener(
                    inboundComPort,
                    comServerDAO,
                    this.deviceCommandExecutor,
                    threadFactory,
                    inboundComPortExecutorFactory,
                    inboundComPortConnectorFactory,
                    this.serviceProvider));
            // business method
            multiThreadedComPortListener.start();

            connectorStartLatch.await();    // wait until all three connections are accepted
            assertThat(multiThreadedComPortListener.prepareExecution()).isFalse();   // now all accepts should be refused

            startLatch.countDown();

            // Now wait until the commands complete
            stopLatch.await();

            //Asserts
            verify(multiThreadedComPortListener, atLeast(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1)).doRun();
        } finally {
            if (multiThreadedComPortListener != null) {
                multiThreadedComPortListener.shutdownImmediate();
            }
        }

    }

    @Test(timeout = 5000)
    public void testWorkComplete() throws InterruptedException, BusinessException {
        LatchDrivenMultiThreadedComPortListener multiThreadedComPortListener = null;
        try {
            InboundCapableComServer comServer = mock(InboundCapableComServer.class);
            when(comServer.getName()).thenReturn("testWorkComplete");
            ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
            CountDownLatch connectorStartLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            final InboundComPortConnector connector = new LatchDrivenInboundComPortConnector(connectorStartLatch);
            final InboundComPort inboundComPort = this.mockComPort("WorkComplete", comServer);
            InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
            when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(connector);
            final ComServerDAO comServerDAO = mock(ComServerDAO.class);

            InboundComPortExecutorFactory inboundComPortExecutorFactory = mock(InboundComPortExecutorFactory.class);
            CountDownLatch startLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1);
            CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            when(inboundComPortExecutorFactory.
                    create(
                            any(InboundComPort.class),
                            any(ComServerDAO.class),
                            any(DeviceCommandExecutor.class))).
                    thenReturn(new LatchDrivenInboundComPortExecutor(startLatch, stopLatch));

            multiThreadedComPortListener = spy(new LatchDrivenMultiThreadedComPortListener(
                    inboundComPort,
                    comServerDAO,
                    this.deviceCommandExecutor,
                    threadFactory,
                    inboundComPortExecutorFactory,
                    inboundComPortConnectorFactory,
                    this.serviceProvider));
            CountDownLatch completeCounter = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            multiThreadedComPortListener.setCounter(completeCounter);

            // Business method
            multiThreadedComPortListener.start();

            connectorStartLatch.await();    // wait until all three connections are accepted
            startLatch.countDown();

            // Now wait until the commands complete
            stopLatch.await();
            completeCounter.await();

            //Asserts
            verify(multiThreadedComPortListener, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)).workerCompleted();
        } finally {
            if (multiThreadedComPortListener != null) {
                multiThreadedComPortListener.shutdownImmediate();
            }
        }
    }

    @Test(timeout = 5000)
    public void testWorkFailed() throws BusinessException, InterruptedException {
        LatchDrivenMultiThreadedComPortListener multiThreadedComPortListener = null;
        try {
            InboundCapableComServer comServer = mock(InboundCapableComServer.class);
            when(comServer.getName()).thenReturn("testWorkFailed");
            ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
            final InboundComPortConnector connector = new FixedNumberOfAcceptsComPortConnector(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            final InboundComPort inboundComPort = this.mockComPort("workfailed", comServer);
            InboundComPortConnectorFactory inboundComPortConnectorFactory = mock(InboundComPortConnectorFactory.class);
            when(inboundComPortConnectorFactory.connectorFor(inboundComPort)).thenReturn(connector);
            final ComServerDAO comServerDAO = mock(ComServerDAO.class);
            CountDownLatch startLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1 -1);  // minus one because we mock one
            CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS - 1);      // minus one because we mock one
            InboundComPortExecutorFactory inboundComPortExecutorFactory = mock(InboundComPortExecutorFactory.class);
            InboundComPortExecutor inboundComPortExecutor1 = new LatchDrivenInboundComPortExecutor(startLatch, stopLatch);
            InboundComPortExecutor inboundComPortExecutor2 = mock(InboundComPortExecutor.class);
            doThrow(new RuntimeException("Just for testing purposes")).when(inboundComPortExecutor2).execute(any(ComPortRelatedComChannel.class));
            InboundComPortExecutor inboundComPortExecutor3 = new LatchDrivenInboundComPortExecutor(startLatch, stopLatch);
            when(inboundComPortExecutorFactory.
                    create(
                            any(InboundComPort.class),
                            any(ComServerDAO.class),
                            any(DeviceCommandExecutor.class))).
                    thenReturn(inboundComPortExecutor1, inboundComPortExecutor2, inboundComPortExecutor3);
            multiThreadedComPortListener = spy(new LatchDrivenMultiThreadedComPortListener(
                    inboundComPort,
                    comServerDAO,
                    this.deviceCommandExecutor,
                    threadFactory,
                    inboundComPortExecutorFactory,
                    inboundComPortConnectorFactory,
                    this.serviceProvider));
            CountDownLatch completeCounter = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            multiThreadedComPortListener.setCounter(completeCounter);
            // business method
            multiThreadedComPortListener.start();

            startLatch.countDown();

            // wait until all receptors threads have completed
            completeCounter.await();

            //Asserts
            verify(multiThreadedComPortListener, times(1)).workerFailed(any(Throwable.class));
            verify(multiThreadedComPortListener, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS-1)).workerCompleted();
        } finally {
            if (multiThreadedComPortListener != null) {
                multiThreadedComPortListener.shutdownImmediate();
            }
        }
    }

    @Test
    public void testApplyChangesForNewComPort() throws BusinessException {
        MultiThreadedComPortListener multiThreadedComPortListener = null;
        try {
            final InboundComPort inboundComPort = this.mockComPort("applyChanges");
            final ComServerDAO comServerDAO = mock(ComServerDAO.class);

            multiThreadedComPortListener = new MultiThreadedComPortListener(inboundComPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);
            multiThreadedComPortListener.start();

            assertThat(multiThreadedComPortListener.getResourceManager().getCapacity()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
            assertThat(((ThreadPoolExecutor)multiThreadedComPortListener.getExecutorService()).getCorePoolSize()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);

            int addedCapacity = 10;
            InboundComPort newComPort = this.mockComPort("newerComPort");
            when(newComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);

            multiThreadedComPortListener.applyChangesForNewComPort(newComPort);

            assertThat(multiThreadedComPortListener.getResourceManager().getCapacity()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);
            assertThat(((ThreadPoolExecutor)multiThreadedComPortListener.getExecutorService()).getCorePoolSize()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);
        } finally {
            if (multiThreadedComPortListener != null) {
                multiThreadedComPortListener.shutdownImmediate();
            }
        }
    }

    @Test
    public void testNewComPortIsReturned() throws BusinessException {
        MultiThreadedComPortListener multiThreadedComPortListener = null;
        try {
            final InboundComPort inboundComPort = this.mockComPort("applyChanges");
            final ComServerDAO comServerDAO = mock(ComServerDAO.class);

            multiThreadedComPortListener = new MultiThreadedComPortListener(inboundComPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);
            multiThreadedComPortListener.start();

            int addedCapacity = 10;
            InboundComPort newComPort = this.mockComPort("newerComPort");
            when(newComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);

            InboundComPort finalComPort = multiThreadedComPortListener.applyChanges(newComPort, inboundComPort);

            assertThat(finalComPort).isEqualTo(newComPort);
        } finally {
            if (multiThreadedComPortListener != null) {
                multiThreadedComPortListener.shutdownImmediate();
            }
        }
    }

    private TCPBasedInboundComPort mockComPort(String name) {
        return this.mockComPort(name, mock(InboundCapableComServer.class));
    }

    private TCPBasedInboundComPort mockComPort(String name, InboundCapableComServer comServer) {
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getChangesInterPollDelay()).thenReturn(INTER_POLL_DELAY);
        TCPBasedInboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getName()).thenReturn("MultiThreadedComPortListener#" + name);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private class LatchDrivenInboundComPortExecutor implements InboundComPortExecutor {

        private CountDownLatch startLatch;
        private CountDownLatch stopLatch;

        private LatchDrivenInboundComPortExecutor(CountDownLatch startLatch, CountDownLatch stopLatch) {
            super();
            this.startLatch = startLatch;
            this.stopLatch = stopLatch;
        }

        @Override
        public void execute(ComPortRelatedComChannel comChannel) {
            this.startLatch.countDown();
            try {
                this.startLatch.await();
                this.doExecute();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                this.stopLatch.countDown();
            }
        }

        protected void doExecute() {
            // Unit testing commands typically don't do anything useful
        }
    }

    private class FixedNumberOfAcceptsComPortConnector implements InboundComPortConnector{

        private final int amountOfAccepts;
        private int accepts = 0;

        private FixedNumberOfAcceptsComPortConnector(int amountOfAccepts) {
            this.amountOfAccepts = amountOfAccepts;
        }

        @Override
        public ComPortRelatedComChannel accept() {
            if(accepts++ < amountOfAccepts){
                return mock(ComPortRelatedComChannel.class);
            } else {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return new ComPortRelatedComChannelImpl(new VoidComChannel(), hexService);
        }
    }

    private class LatchDrivenInboundComPortConnector implements InboundComPortConnector {

        private CountDownLatch startLatch;

        private LatchDrivenInboundComPortConnector(CountDownLatch startLatch) {
            super();
            this.startLatch = startLatch;
        }

        @Override
        public ComPortRelatedComChannel accept() {
            this.startLatch.countDown();
            return this.doAccept();
        }

        protected ComPortRelatedComChannel doAccept() {
            return mock(ComPortRelatedComChannel.class);
        }
    }

    private class LatchDrivenMultiThreadedComPortListener extends MultiThreadedComPortListener {

        private CountDownLatch counter;

        protected LatchDrivenMultiThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, InboundComPortExecutorFactory inboundComPortExecutorFactory, InboundComPortConnectorFactory inboundComPortConnectorFactory, ServiceProvider serviceProvider) {
            super(comPort, comServerDAO, deviceCommandExecutor, threadFactory, inboundComPortExecutorFactory, inboundComPortConnectorFactory, serviceProvider);
        }

        public void setCounter(CountDownLatch counter) {
            this.counter = counter;
        }

        @Override
        protected synchronized void workerCompleted() {
            super.workerCompleted();
            counter.countDown();
        }

        @Override
        protected synchronized void workerFailed(Throwable t) {
            super.workerFailed(t);
            counter.countDown();
        }
    }
}