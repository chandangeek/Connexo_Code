package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.scheduling.factories.InboundComPortExecutorFactoryImpl;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannelImpl;
import com.energyict.protocols.mdc.channels.VoidComChannel;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundCapableComServer;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link SingleThreadedComPortListener} component
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

    private Thread mockedThread() {
        return mock(Thread.class);
    }

    @Test
    public void testStart() throws BusinessException, InterruptedException {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);

        SingleThreadedComPortListener singleThreadedComPortListener = new SingleThreadedComPortListener(this.mockComPort("testStart"), mock(ComServerDAO.class), threadFactory, this.deviceCommandExecutor, new InboundComPortExecutorFactoryImpl(), issueService);

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

        SingleThreadedComPortListener singleThreadedComPortListener = new SingleThreadedComPortListener(this.mockComPort("testShutdown"), mock(ComServerDAO.class), threadFactory, this.deviceCommandExecutor, new InboundComPortExecutorFactoryImpl(), issueService);

        // business method
        singleThreadedComPortListener.start();
        singleThreadedComPortListener.shutdown();

        // Asserts
        verify(threadFactory, times(2)).newThread(any(Runnable.class));
        verify(mockedThread, times(2)).interrupt();
    }

    @Test(timeout = 5000)
    public void testSimulatedVoidComChannelWithNoHandOver() throws BusinessException, InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch progressLatch = new CountDownLatch(1);
        InboundComPortConnector connector = spy(new LatchDrivenTimeOutInboundComPortConnector(startLatch, progressLatch));

        SingleThreadedComPortListener singleThreadedComPortListener =
                spy(new SingleThreadedComPortListener(this.mockComPort("simTimeout", connector), mock(ComServerDAO.class), this.deviceCommandExecutor, issueService));
        // business method
        singleThreadedComPortListener.start();
        startLatch.await(); // wait until the accept has occurred

        //Asserts
        verify(connector, atLeast(1)).accept(); // accept should have been called twice (one time it should have returned a VoidComChannel
        verify(singleThreadedComPortListener, never()).handleInboundDeviceProtocol(any(ComChannel.class));
    }

    @Test(timeout = 5000)
    public void testAcceptedInboundCall() throws InterruptedException, BusinessException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch progressLatch = new CountDownLatch(1);
        CountDownLatch protocolLatch = new CountDownLatch(1);
        final ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);
        final InboundComPortConnector connector = spy(new LatchDrivenAcceptInboundComPortConnector(startLatch, progressLatch, comChannel));
        LatchDrivenSingleThreadedComPortListener singleThreadedComPortListener = spy(new LatchDrivenSingleThreadedComPortListener(this.mockComPort("accept", connector), mock(ComServerDAO.class), this.deviceCommandExecutor));
        singleThreadedComPortListener.setCounter(protocolLatch);
        // business method
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

        private LatchDrivenSingleThreadedComPortListener (InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor) {
            super(comPort, comServerDAO, deviceCommandExecutor, issueService);
        }

        private void setCounter(CountDownLatch countDownLatch){
            this.counter = countDownLatch;
        }

        @Override
        protected void handleInboundDeviceProtocol(ComChannel comChannel) {
            this.counter.countDown(); // don't do any handover to actual protocol :o) (otherwise you need to much mocking)
        }
    }

    private InboundComPort mockComPort(String name) {
        return this.mockComPort(name, mock(InboundComPortConnector.class));
    }

    private InboundComPort mockComPort(String name, InboundComPortConnector connector) {
        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TIME_DURATION);
        when(comServer.getChangesInterPollDelay()).thenReturn(TIME_DURATION);
        ServerComChannelBasedInboundComPort comPort = mock(ServerComChannelBasedInboundComPort.class);
        when(comPort.getName()).thenReturn("SingleThreadedComPortListener#" + name);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

}
