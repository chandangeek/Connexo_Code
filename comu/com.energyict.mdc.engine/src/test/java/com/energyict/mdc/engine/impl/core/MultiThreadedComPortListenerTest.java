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
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundCapableComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.inbound.BinaryInboundDeviceProtocol;

import com.energyict.protocols.mdc.channels.VoidComChannel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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

    private static final TimeDuration INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.SECONDS);

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private IssueService issueService;

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    private Thread mockedThread() {
        return mock(Thread.class);
    }

    @Before
    public void setupServiceProvider () {
        this.serviceProvider.setIssueService(this.issueService);
    }

    @Test(timeout = 5000)
    public void testStart() throws BusinessException, InterruptedException {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);

        MultiThreadedComPortListener multiThreadedComPortListener = new MultiThreadedComPortListener(this.mockComPort("testStart"), mock(ComServerDAO.class), this.deviceCommandExecutor, threadFactory, new InboundComPortExecutorFactoryImpl(), this.serviceProvider);

        // business method
        multiThreadedComPortListener.start();

        // Asserts
        verify(threadFactory, times(2)).newThread(any(Runnable.class));
        verify(mockedThread, times(2)).start();
        assertThat(multiThreadedComPortListener.getResourceManager().getCapacity()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);

        multiThreadedComPortListener.shutdown();
    }

    @Test(timeout = 5000)
    public void testShutdown() throws BusinessException {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);
        MultiThreadedComPortListener comPortListener = new MultiThreadedComPortListener(this.mockComPort("testShutDown"), mock(ComServerDAO.class), this.deviceCommandExecutor, threadFactory, new InboundComPortExecutorFactoryImpl(), this.serviceProvider);

        comPortListener.start();

        // Business method
        comPortListener.shutdown();

        // Asserts
        verify(threadFactory, times(2)).newThread(any(Runnable.class));
        verify(mockedThread, times(2)).interrupt();
    }

    @Test(timeout = 5000)
    public void testAcceptedInboundCall() throws Exception {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("testAcceptedInboundCall");
        ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
        final InboundComPortConnector connector = mock(InboundComPortConnector.class);
        final ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        when(connector.accept()).thenReturn(comChannel);
        final InboundComPort inboundComPort = this.mockComPort("accept", connector);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);
        InboundComPortExecutorFactory inboundComPortExecutorFactory = mock(InboundComPortExecutorFactory.class);
        CountDownLatch startLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1);
        CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(inboundComPortExecutorFactory.create(any(InboundComPort.class), any(ComServerDAO.class), any(DeviceCommandExecutor.class), this.serviceProvider)).thenReturn(new LatchDrivenInboundComPortExecutor(startLatch, stopLatch));
        MultiThreadedComPortListener multiThreadedComPortListener = spy(new MultiThreadedComPortListener(inboundComPort, comServerDAO, this.deviceCommandExecutor, threadFactory, inboundComPortExecutorFactory, this.serviceProvider));
        // business method
        multiThreadedComPortListener.start();

        startLatch.countDown();

        // Now wait until the commands complete
        stopLatch.await();

        //Asserts
        verify(connector, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)).accept();
        verify(inboundComPortExecutorFactory, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)).create(any(InboundComPort.class), any(ComServerDAO.class), any(DeviceCommandExecutor.class), this.serviceProvider); // accept should have been called twice (one time it should have returned a VoidComChannel

        multiThreadedComPortListener.shutdown();
    }

    @Test(timeout = 5000)
    public void testOverLoad() throws BusinessException, InterruptedException {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("testOverload");
        ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
        CountDownLatch connectorStartLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        final InboundComPortConnector connector = new LatchDrivenInboundComPortConnector(connectorStartLatch);
        final InboundComPort inboundComPort = this.mockComPort("OverLoad", connector);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);
        InboundComPortExecutorFactory inboundComPortExecutorFactory = mock(InboundComPortExecutorFactory.class);
        CountDownLatch startLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1);
        CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(inboundComPortExecutorFactory.create(any(InboundComPort.class), any(ComServerDAO.class), any(DeviceCommandExecutor.class), this.serviceProvider)).thenReturn(new LatchDrivenInboundComPortExecutor(startLatch, stopLatch));
        MultiThreadedComPortListener multiThreadedComPortListener = spy(new MultiThreadedComPortListener(inboundComPort, comServerDAO, this.deviceCommandExecutor, threadFactory, inboundComPortExecutorFactory, this.serviceProvider));
        // business method
        multiThreadedComPortListener.start();

        connectorStartLatch.await();    // wait until all three connections are accepted
        assertThat(multiThreadedComPortListener.prepareExecution()).isFalse();   // now all accepts should be refused

        startLatch.countDown();

        // Now wait until the commands complete
        stopLatch.await();

        //Asserts
        verify(multiThreadedComPortListener, atLeast(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1)).doRun();

        multiThreadedComPortListener.shutdown();
    }

    @Test(timeout = 5000)
    public void testWorkComplete() throws InterruptedException, BusinessException {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("testWorkComplete");
        ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
        CountDownLatch connectorStartLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        final InboundComPortConnector connector = new LatchDrivenInboundComPortConnector(connectorStartLatch);
        final InboundComPort inboundComPort = this.mockComPort("WorkComplete", connector);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);

        InboundComPortExecutorFactory inboundComPortExecutorFactory = mock(InboundComPortExecutorFactory.class);
        CountDownLatch startLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1);
        CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(inboundComPortExecutorFactory.create(any(InboundComPort.class), any(ComServerDAO.class), any(DeviceCommandExecutor.class), this.serviceProvider)).thenReturn(new LatchDrivenInboundComPortExecutor(startLatch, stopLatch));

        LatchDrivenMultiThreadedComPortListener multiThreadedComPortListener = spy(new LatchDrivenMultiThreadedComPortListener(inboundComPort, comServerDAO, threadFactory, this.deviceCommandExecutor, this.serviceProvider, inboundComPortExecutorFactory));
        CountDownLatch completeCounter = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        multiThreadedComPortListener.setCounter(completeCounter);
        // business method
        multiThreadedComPortListener.start();

        connectorStartLatch.await();    // wait until all three connections are accepted

        startLatch.countDown();

        // Now wait until the commands complete
        stopLatch.await();

        completeCounter.await();

        //Asserts
        verify(multiThreadedComPortListener, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)).workerCompleted();

        multiThreadedComPortListener.shutdown();
    }

    @Test(timeout = 5000)
    public void testWorkFailed() throws BusinessException, InterruptedException {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("testWorkFailed");
        ThreadFactory threadFactory = new ComServerThreadFactory(comServer);
        final InboundComPortConnector connector = new FixedNumberOfAcceptsComPortConnector(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        final InboundComPort inboundComPort = this.mockComPort("workfailed", connector);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CountDownLatch startLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1 -1);  // minus one because we mock one
        CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS - 1);      // minus one because we mock one
        InboundComPortExecutorFactory inboundComPortExecutorFactory = mock(InboundComPortExecutorFactory.class);
        InboundComPortExecutor inboundComPortExecutor1 = new LatchDrivenInboundComPortExecutor(startLatch, stopLatch);
        InboundComPortExecutor inboundComPortExecutor2 = mock(InboundComPortExecutor.class);
        doThrow(new RuntimeException("Just for testing purposes")).when(inboundComPortExecutor2).execute(any(ComChannel.class));
        InboundComPortExecutor inboundComPortExecutor3 = new LatchDrivenInboundComPortExecutor(startLatch, stopLatch);
        when(inboundComPortExecutorFactory.create(any(InboundComPort.class),
                any(ComServerDAO.class), any(DeviceCommandExecutor.class), this.serviceProvider)).
                thenReturn(inboundComPortExecutor1, inboundComPortExecutor2, inboundComPortExecutor3);
        LatchDrivenMultiThreadedComPortListener multiThreadedComPortListener =
                spy(new LatchDrivenMultiThreadedComPortListener(
                        inboundComPort, comServerDAO, threadFactory,
                        this.deviceCommandExecutor, this.serviceProvider, inboundComPortExecutorFactory));
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

        multiThreadedComPortListener.shutdown();
    }

    @Test
    public void testApplyChangesForNewComPort() throws BusinessException {
        final InboundComPortConnector connector = new FixedNumberOfAcceptsComPortConnector(0);
        final InboundComPort inboundComPort = this.mockComPort("applyChanges", connector);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);

        MultiThreadedComPortListener multiThreadedComPortListener = new MultiThreadedComPortListener(inboundComPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);
        multiThreadedComPortListener.start();

        assertThat(multiThreadedComPortListener.getResourceManager().getCapacity()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        assertThat(((ThreadPoolExecutor)multiThreadedComPortListener.getExecutorService()).getCorePoolSize()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);

        int addedCapacity = 10;
        InboundComPort newComPort = this.mockComPort("newerComPort", connector);
        when(newComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);

        multiThreadedComPortListener.applyChangesForNewComPort(newComPort);

        assertThat(multiThreadedComPortListener.getResourceManager().getCapacity()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);
        assertThat(((ThreadPoolExecutor)multiThreadedComPortListener.getExecutorService()).getCorePoolSize()).isEqualTo(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);
    }

    @Test
    public void testNewComPortIsReturned() throws BusinessException {
        final InboundComPortConnector connector = new FixedNumberOfAcceptsComPortConnector(0);
        final InboundComPort inboundComPort = this.mockComPort("applyChanges", connector);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);

        MultiThreadedComPortListener multiThreadedComPortListener = new MultiThreadedComPortListener(inboundComPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);
        multiThreadedComPortListener.start();

        int addedCapacity = 10;
        InboundComPort newComPort = this.mockComPort("newerComPort", connector);
        when(newComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);

        InboundComPort finalComPort = multiThreadedComPortListener.applyChanges(newComPort, inboundComPort);

        assertThat(finalComPort).isEqualTo(newComPort);
    }

    private InboundComPort mockComPort(String name) throws BusinessException {
        return this.mockComPort(name, mock(InboundComPortConnector.class));
    }

    private InboundComPort mockComPort(String name, InboundComPortConnector connector) throws BusinessException {
        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getChangesInterPollDelay()).thenReturn(INTER_POLL_DELAY);
        PluggableClass discoveryProtocol = mock(PluggableClass.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getName()).thenReturn("MultiThreadedComPortListener#" + name);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private BinaryInboundDeviceProtocol getMockedBinaryInboundDeviceProtocol() {
        return mock(BinaryInboundDeviceProtocol.class);
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
        public void execute(ComChannel comChannel) {
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
            return new ComPortRelatedComChannelImpl(new VoidComChannel());
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

        protected LatchDrivenMultiThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, InboundComPortExecutorFactory inboundComPortExecutorFactory) {
            super(comPort, comServerDAO, deviceCommandExecutor, threadFactory, inboundComPortExecutorFactory, serviceProvider);
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