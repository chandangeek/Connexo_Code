package com.energyict.mdc.engine.impl.core.devices;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import java.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.NoResourcesAcquiredException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComServerThreadFactory;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.aspects.ComServerEventServiceProviderAdapter;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComServer;
import com.google.common.base.Optional;
import org.joda.time.DateTimeConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-21 (14:24)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceCommandExecutorImplTest {

    @Rule
    public TimeZoneNeutral pinguinLand = Using.timeZoneOfMcMurdo();

    private static final int CAPACITY = 10;
    private static final int NUMBER_OF_THREADS = 1;
    private static final int THREAD_PRIORITY = Thread.NORM_PRIORITY;

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    private Clock clock = new ProgrammableClock();

    @Mock
    private ComServer comServer;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionTaskService connectionTaskService;

    @Before
    public void setupComServer() {
        ServiceProvider.instance.set(serviceProvider);
        serviceProvider.setClock(clock);
        serviceProvider.setConnectionTaskService(this.connectionTaskService);
        when(userService.findUser(anyString())).thenReturn(Optional.of(user));
        EventPublisherImpl.setInstance(this.eventPublisher);
        when(this.eventPublisher.serviceProvider()).thenReturn(new ComServerEventServiceProviderAdapter());
        when(this.comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
    }

    @After
    public void tearDown() {
        EventPublisherImpl.setInstance(null);
        ServiceProvider.instance.set(null);
    }

    @Test
    public void testStart() {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        DeviceCommandExecutor deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);

        // Business method
        deviceCommandExecutor.start();

        // Asserts
        assertThat(deviceCommandExecutor.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
        verify(threadFactory, never()).newThread(any(Runnable.class));  // We are not expecting that new Threads are already started
    }

    @Test
    public void testPrepareExecution() {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        DeviceCommandExecutor deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
        deviceCommandExecutor.start();

        // Business method
        int numberOfCommands = 10;
        List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(numberOfCommands);

        // Asserts
        assertThat(tokens).hasSize(numberOfCommands);
    }

    @Test
    public void testExecuteCreatesNewThreads() {
        Thread mockedThread = mock(Thread.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);
        DeviceCommandExecutor deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
        deviceCommandExecutor.start();
        List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(1);
        DeviceCommandExecutionToken token = tokens.get(0);
        SignalReceiver receiver = mock(SignalReceiver.class);

        // Business method
        deviceCommandExecutor.execute(new SignalSender(receiver), token);

        // Asserts
        verify(threadFactory).newThread(any(Runnable.class));
    }

    @Test
    public void testExecuteWithHighPriority() {
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            TrackingThreadFactory threadFactory = new TrackingThreadFactory(new ComServerThreadFactory(this.comServer));
            int threadPriority = Thread.MAX_PRIORITY;
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, threadPriority, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(1);
            DeviceCommandExecutionToken token = tokens.get(0);
            SignalReceiver receiver = mock(SignalReceiver.class);

            // Business method
            deviceCommandExecutor.execute(new SignalSender(receiver), token);

            // Asserts
            assertThat(threadFactory.getThread(0).getPriority()).isEqualTo(threadPriority);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testExecuteWithPreparation() throws InterruptedException {
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(1);
            DeviceCommandExecutionToken token = tokens.get(0);
            SignalReceiver receiver = mock(SignalReceiver.class);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch stopLatch = new CountDownLatch(1);

            // Business method
            deviceCommandExecutor.execute(new SignalSender(receiver, startLatch, stopLatch), token);

            // Count down on the start latch to trigger the SignalSender
            startLatch.countDown();

            // Wait for the SignalSender to finish
            stopLatch.await();

            // Asserts
            verify(receiver).receiveSignal();
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testExecuteInReverseOrder() throws InterruptedException {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(CAPACITY);
            Collections.reverse(tokens);
            SignalReceiver receiver = mock(SignalReceiver.class);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch stopLatch = new CountDownLatch(CAPACITY);

            // Business method
            for (DeviceCommandExecutionToken token : tokens) {
                deviceCommandExecutor.execute(new SignalSender(receiver, startLatch, stopLatch), token);
            }

            // Signal the senders to start
            startLatch.countDown();

            // Wait for all SignalSenders to finish
            stopLatch.await();

            // Asserts
            verify(receiver, times(CAPACITY)).receiveSignal();
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test(expected = NoResourcesAcquiredException.class)
    public void testExecuteWithoutPreparation() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        DeviceCommandExecutor deviceCommandExecutor = null;

        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            DeviceCommandExecutionToken token = mock(DeviceCommandExecutionToken.class);

            // Business method
            deviceCommandExecutor.execute(new SleepCommand(), token);

            // Expected a NoResourcesAcquiredException because the command was not prepared
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testExecuteWhenAlreadyShutdown() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        DeviceCommandExecutor deviceCommandExecutor = null;

        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(1);
            DeviceCommandExecutionToken token = tokens.get(0);
            deviceCommandExecutor.shutdown();

            // Business method
            deviceCommandExecutor.execute(new SleepCommand(), token);

            // Expected an IllegalStateException because the DeviceCommandExecutor was already shutdown
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    /**
     * Tests that preparation of execution fails because the number of commands would exceed the capacity.
     * <ul>
     * <li>Capacity = 10</li>
     * <li>Already prepared = 0</li>
     * <li>Currently executing = 0</li>
     * <li>Currently waiting for execution = 0</li>
     * <li>number of commands to prepare = 12</li>
     * </ul>
     */
    @Test
    public void testPrepareExecutionWithTooManyCommands1() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        DeviceCommandExecutor deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
        deviceCommandExecutor.start();

        // Business method
        List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(CAPACITY + 2);

        // Asserts
        assertThat(tokens).isEmpty();
    }

    /**
     * Tests that preparation of execution fails because the number of commands would exceed the capacity.
     * <ul>
     * <li>Capacity = 10</li>
     * <li>Already prepared = capacity - 1</li>
     * <li>Currently executing = 0</li>
     * <li>Currently waiting for execution = 0</li>
     * <li>number of commands to prepare = 2</li>
     * </ul>
     */
    @Test
    public void testPrepareExecutionWithTooManyCommands2() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        DeviceCommandExecutor deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
        deviceCommandExecutor.start();
        deviceCommandExecutor.tryAcquireTokens(CAPACITY - 1);

        // Business method
        List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(2);

        // Asserts
        assertThat(tokens).isEmpty();
    }

    /**
     * Tests that preparation of execution fails because the number of commands would exceed the capacity.
     * <ul>
     * <li>Capacity = 10</li>
     * <li>Prepared but not executed yet = capacity - 2</li>
     * <li>Currently executing = 1</li>
     * <li>Currently waiting for execution = zero</li>
     * <li>number of commands to prepare = 2</li>
     * </ul>
     */
    @Test
    public void testPrepareExecutionWithTooManyCommands3() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            int numberOfCommands = CAPACITY - 1;
            List<DeviceCommandExecutionToken> alreadyPrepared = deviceCommandExecutor.tryAcquireTokens(numberOfCommands);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch stopLatch = new CountDownLatch(1);
            deviceCommandExecutor.execute(new LatchDrivenCommand(startLatch, stopLatch), alreadyPrepared.get(0));

            // Never countdown on the start latch to avoid that the DeviceCommand effectively executes

            // Business method
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(2);

            // Asserts
            assertThat(tokens).isEmpty();
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    /**
     * Tests that preparation of execution fails initially because the number of commands would exceed the capacity
     * but succeeds once the executing {@link DeviceCommand}s complete.
     * <ul>
     * <li>Capacity = 10</li>
     * <li>Already prepared = zero</li>
     * <li>Currently executing = capacity - 1</li>
     * <li>Currently waiting for execution = zero</li>
     * <li>number of commands to prepare = 2</li>
     * </ul>
     */
    @Test
    public void testPrepareExecutionAfterSuccesfullExecution() throws InterruptedException {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        int numberOfExecutingCommands = CAPACITY - 1;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(numberOfExecutingCommands);
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, numberOfExecutingCommands, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> alreadyPrepared = deviceCommandExecutor.tryAcquireTokens(numberOfExecutingCommands);
            for (DeviceCommandExecutionToken token : alreadyPrepared) {
                deviceCommandExecutor.execute(new LatchDrivenCommand(startLatch, stopLatch), token);
            }

            // Triggering the start latch, kicks the executing command(s)
            startLatch.countDown();

            // Now wait until the commands complete
            stopLatch.await();

            // Business method
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(2);

            // Asserts
            assertThat(tokens).hasSize(2);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    /**
     * Tests that preparation of execution fails initially because the number of commands would exceed the capacity
     * but succeeds once an unused token is freed.
     * <ul>
     * <li>Capacity = 10</li>
     * <li>Already prepared = zero</li>
     * <li>Currently executing = capacity - 1</li>
     * <li>Currently waiting for execution = zero</li>
     * <li>number of commands to free = 2</li>
     * <li>number of commands to prepare = 2</li>
     * </ul>
     */
    @Test
    public void testPrepareExecutionAfterFree() throws InterruptedException {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        int numberOfExecutingCommands = CAPACITY - 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(numberOfExecutingCommands);
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, CAPACITY - 1, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> alreadyPrepared = deviceCommandExecutor.tryAcquireTokens(numberOfExecutingCommands + 2);
            DeviceCommandExecutionToken unused1 = alreadyPrepared.remove(0);
            DeviceCommandExecutionToken unused2 = alreadyPrepared.remove(1);
            for (DeviceCommandExecutionToken token : alreadyPrepared) {
                deviceCommandExecutor.execute(new LatchDrivenCommand(startLatch, stopLatch), token);
            }

            // Triggering the start latch, kicks the executing command(s)
            startLatch.countDown();

            // Free the unused tokens.
            deviceCommandExecutor.free(unused1);
            deviceCommandExecutor.free(unused2);

            // Now wait until the commands complete
            stopLatch.await();

            // Business method
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(2);

            // Asserts
            assertThat(tokens).hasSize(2);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test(expected = NoResourcesAcquiredException.class)
    public void testFreeTokeThatWasNotPrepared() throws InterruptedException {
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            ComServer comServer = mock(ComServer.class);
            when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
            int numberOfExecutingCommands = CAPACITY - 1;
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, numberOfExecutingCommands, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();

            // Business method
            deviceCommandExecutor.free(mock(DeviceCommandExecutionToken.class));

            // Was expecting a NoResourcesAcquiredException
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    /**
     * Tests that {@link DeviceCommand}s that fail with a DataAccessException
     * also releases resources so that subsequent preparation calls succeed.
     */
    @Test
    public void testPrepareExecutionAfterDataAccessExceptionFailure() throws InterruptedException {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("DeviceCommandExecutorImplTest");
        int numberOfExecutingCommands = CAPACITY - 1;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(numberOfExecutingCommands);
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, numberOfExecutingCommands, THREAD_PRIORITY, ComServer.LogLevel.ERROR, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> alreadyPrepared = deviceCommandExecutor.tryAcquireTokens(numberOfExecutingCommands);
            for (DeviceCommandExecutionToken token : alreadyPrepared) {
                deviceCommandExecutor.execute(new DataAccessExceptionCommand(startLatch, stopLatch), token);
            }

            // Triggering the start latch, kicks the executing command(s)
            startLatch.countDown();

            // Now wait until the commands complete
            stopLatch.await();

            // Business method
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(2);

            // Asserts
            assertThat(tokens).hasSize(2);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    /**
     * Tests that {@link DeviceCommand}s that fail with an ApplicationException
     * also release resources so that subsequent preparation calls succeed.
     */
    @Test
    public void testPrepareExecutionAfterApplicationExceptionFailure() throws InterruptedException {
        int numberOfExecutingCommands = CAPACITY - 1;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(numberOfExecutingCommands);
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, numberOfExecutingCommands, THREAD_PRIORITY, ComServer.LogLevel.ERROR, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> alreadyPrepared = deviceCommandExecutor.tryAcquireTokens(numberOfExecutingCommands);
            for (DeviceCommandExecutionToken token : alreadyPrepared) {
                deviceCommandExecutor.execute(new ApplicationExceptionCommand(startLatch, stopLatch), token);
            }

            // Triggering the start latch, kicks the executing command(s)
            startLatch.countDown();

            // Now wait until the commands complete
            stopLatch.await();

            // Business method
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(2);

            // Asserts
            assertThat(tokens).hasSize(2);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    /**
     * Tests that {@link DeviceCommand}s that fail with a RuntimeException
     * also release resources so that subsequent preparation calls succeed.
     */
    @Test
    public void testPrepareExecutionAfterRuntimeExceptionFailure() throws InterruptedException {
        int numberOfExecutingCommands = CAPACITY - 1;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(numberOfExecutingCommands);
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, numberOfExecutingCommands, THREAD_PRIORITY, ComServer.LogLevel.ERROR, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> alreadyPrepared = deviceCommandExecutor.tryAcquireTokens(numberOfExecutingCommands);
            for (DeviceCommandExecutionToken token : alreadyPrepared) {
                deviceCommandExecutor.execute(new RuntimeExceptionCommand(startLatch, stopLatch), token);
            }

            // Triggering the start latch, kicks the executing command(s)
            startLatch.countDown();

            // Now wait until the commands complete
            stopLatch.await();

            // Business method
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(2);

            // Asserts
            assertThat(tokens).hasSize(2);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testPrepareExecutionWhenNotRunning() {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        DeviceCommandExecutor deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);

        // Business method
        deviceCommandExecutor.tryAcquireTokens(10);

        // Expected an IllegalStateException because the DeviceCommandExecutor has not been started
    }

    @Test
    public void testShutdownWithoutCommands() {
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();

            // Business method
            deviceCommandExecutor.shutdown();

            // Asserts
            assertThat(deviceCommandExecutor.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testShutdownWithCommandsOnSingleThread() {
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, 1, THREAD_PRIORITY, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(CAPACITY);
            SignalReceiver receiver = mock(SignalReceiver.class);
            final CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch stopLatch = new CountDownLatch(CAPACITY);
            for (DeviceCommandExecutionToken token : tokens) {
                deviceCommandExecutor.execute(new SignalSender(receiver, startLatch, stopLatch), token);
            }

            // Triggering the start latch, kicks the executing command(s)
            startLatch.countDown();

            // Business method
            deviceCommandExecutor.shutdown();

            // Asserts
            assertThat(deviceCommandExecutor.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
            verify(receiver, times(CAPACITY)).receiveSignal();
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testShutdownWithCommandsOnMultipleThreads() {
        ComServerThreadFactory realThreadFactory = new ComServerThreadFactory(this.comServer);
        TrackingThreadFactory threadFactory = new TrackingThreadFactory(realThreadFactory);
        DeviceCommandExecutor deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, CAPACITY, THREAD_PRIORITY, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(CAPACITY);
            SignalReceiver receiver = mock(SignalReceiver.class);
            final CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch stopLatch = new CountDownLatch(CAPACITY);
            for (DeviceCommandExecutionToken token : tokens) {
                deviceCommandExecutor.execute(new SignalSender(receiver, startLatch, stopLatch), token);
            }

            Runnable delay = () -> {
                try {
                    // Delay the triggering or commands a little bit
                    Thread.sleep(50);
                    // Triggering the start latch, kicks the executing command(s)
                    startLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            realThreadFactory.newThread(delay).start();

            // Business method
            deviceCommandExecutor.shutdown();

            // Asserts
            assertThat(deviceCommandExecutor.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
            verify(receiver, times(CAPACITY)).receiveSignal();
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    /**
     * Tests that shutting down the DeviceCommandExecutorImpl
     * stops all running tasks and calls executeDuringShutdown on all others.
     * Creates 3 commands with a single thread so all commands will execute
     * one after the other.
     * Shutdown is only initiated after the first command completes
     * to make sure that the execution engine is working full force.
     * The latch of the second command is only count down on after
     * the shutdown method returns so it must be running while shutting down.
     * This leaves the third command to execute during shutdown.
     *
     * @throws InterruptedException Indicates test failure
     */
    @Test
    public void testShutdownImmediateOnlyExecutesImmediateCommands() throws InterruptedException {
        TrackingThreadFactory threadFactory = new TrackingThreadFactory(new ComServerThreadFactory(this.comServer));
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        DeviceCommandExecutorImpl deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, 1, Thread.MIN_PRIORITY, ComServer.LogLevel.INFO, threadFactory, comServerDAO, mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> setOfTokens = deviceCommandExecutor.tryAcquireTokens(3);
            CountDownLatch startLatchForCommand1 = new CountDownLatch(1);
            CountDownLatch startLatchForCommand2 = new CountDownLatch(10);  // Make sure not to coundDown on this to avoid that commands 2 and 3 kick off
            CountDownLatch stopLatch = new CountDownLatch(1);   // 1 suffices as we only want to know when the first command completes
            DeviceCommand command1 = spy(new LatchDrivenCommand(startLatchForCommand1, stopLatch));
            DeviceCommand command2 = spy(new LatchDrivenCommand(startLatchForCommand2, stopLatch));
            DeviceCommand command3 = spy(new LatchDrivenCommand(startLatchForCommand2, stopLatch));
            deviceCommandExecutor.execute(command1, setOfTokens.get(0));
            deviceCommandExecutor.execute(command2, setOfTokens.get(1));
            deviceCommandExecutor.execute(command3, setOfTokens.get(2));

            // Count down on the start latch to trigger the first command
            startLatchForCommand1.countDown();

            // Await on the stop latch to know when the first command completes
            stopLatch.await();

            // Business method
            deviceCommandExecutor.shutdownImmediate();

            // Asserts
            verify(command1).execute(comServerDAO);
            verify(command3).executeDuringShutdown(comServerDAO);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testChangeThreadPriority() {
        TrackingThreadFactory threadFactory = new TrackingThreadFactory(new ComServerThreadFactory(this.comServer));
        int threadPriority = Thread.MIN_PRIORITY;
        DeviceCommandExecutorImpl deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, NUMBER_OF_THREADS, threadPriority, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> tokens = deviceCommandExecutor.tryAcquireTokens(1);
            DeviceCommandExecutionToken token = tokens.get(0);
            // Execute at least one DeviceCommand to create at least one Thread
            SignalReceiver receiver = mock(SignalReceiver.class);
            deviceCommandExecutor.execute(new SignalSender(receiver), token);

            int newThreadPriority = Thread.MAX_PRIORITY;

            // Business method
            deviceCommandExecutor.changeThreadPriority(newThreadPriority);

            // Asserts
            assertThat(threadFactory.getThread(0).getPriority()).isEqualTo(newThreadPriority);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testIncreaseQueueCapacity() {
        int threadPriority = Thread.MIN_PRIORITY;
        int initialCapacity = CAPACITY;
        DeviceCommandExecutorImpl deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), initialCapacity, NUMBER_OF_THREADS, threadPriority, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            deviceCommandExecutor.tryAcquireTokens(CAPACITY);

            int changedCapacity = initialCapacity + 2;

            // Business method
            deviceCommandExecutor.changeQueueCapacity(changedCapacity);
            List<DeviceCommandExecutionToken> extraTokens = deviceCommandExecutor.tryAcquireTokens(2);

            // Asserts
            assertThat(extraTokens).hasSize(2);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testReduceQueueCapacity() {
        int threadPriority = Thread.MIN_PRIORITY;
        int initialCapacity = CAPACITY;
        DeviceCommandExecutorImpl deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), initialCapacity, NUMBER_OF_THREADS, threadPriority, ComServer.LogLevel.INFO, new ComServerThreadFactory(this.comServer), mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            deviceCommandExecutor.tryAcquireTokens(CAPACITY - 2);

            int changedCapacity = initialCapacity - 2;

            // Business method
            deviceCommandExecutor.changeQueueCapacity(changedCapacity);
            List<DeviceCommandExecutionToken> extraTokens = deviceCommandExecutor.tryAcquireTokens(1);

            // Asserts
            assertThat(extraTokens).isEmpty();
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testIncreaseNumberOfThreads() throws InterruptedException {
        TrackingThreadFactory threadFactory = new TrackingThreadFactory(new ComServerThreadFactory(this.comServer));
        int threadPriority = Thread.MIN_PRIORITY;
        int initialNumberOfThreads = NUMBER_OF_THREADS;
        DeviceCommandExecutorImpl deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, initialNumberOfThreads, threadPriority, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> firstSetOfTokens = deviceCommandExecutor.tryAcquireTokens(CAPACITY);
            CountDownLatch firstStartLatch = new CountDownLatch(1);
            CountDownLatch firstStopLatch = new CountDownLatch(CAPACITY);
            for (DeviceCommandExecutionToken token : firstSetOfTokens) {
                deviceCommandExecutor.execute(new LatchDrivenCommand(firstStartLatch, firstStopLatch), token);
            }

            int changedNumberOfThreads = initialNumberOfThreads + 2;

            // Count down on the start latch to trigger the commands
            firstStartLatch.countDown();

            // Business method
            deviceCommandExecutor.changeNumberOfThreads(changedNumberOfThreads);

            // To be able to assert that more threads are created, we will need to execute more work.
            List<DeviceCommandExecutionToken> secondSetOfTokens = deviceCommandExecutor.tryAcquireTokens(CAPACITY);
            CountDownLatch secondStartLatch = new CountDownLatch(1);
            CountDownLatch secondStopLatch = new CountDownLatch(CAPACITY);
            for (DeviceCommandExecutionToken token : secondSetOfTokens) {
                deviceCommandExecutor.execute(new LatchDrivenCommand(secondStartLatch, secondStopLatch), token);
            }

            // Asserts
            assertThat(threadFactory.getThreads()).hasSize(changedNumberOfThreads);
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    @Test
    public void testDecreaseNumberOfThreads() throws InterruptedException {
        TrackingThreadFactory threadFactory = new TrackingThreadFactory(new ComServerThreadFactory(this.comServer));
        int threadPriority = Thread.MIN_PRIORITY;
        int initialNumberOfThreads = 5;
        DeviceCommandExecutorImpl deviceCommandExecutor = null;
        try {
            deviceCommandExecutor = new DeviceCommandExecutorImpl(this.comServer.getName(), CAPACITY, initialNumberOfThreads, threadPriority, ComServer.LogLevel.INFO, threadFactory, mock(ComServerDAO.class), mock(ThreadPrincipalService.class), userService);
            deviceCommandExecutor.start();
            List<DeviceCommandExecutionToken> firstSetOfTokens = deviceCommandExecutor.tryAcquireTokens(CAPACITY);
            CountDownLatch firstStartLatch = new CountDownLatch(1);
            CountDownLatch firstStopLatch = new CountDownLatch(CAPACITY);
            for (DeviceCommandExecutionToken token : firstSetOfTokens) {
                deviceCommandExecutor.execute(new LatchDrivenCommand(firstStartLatch, firstStopLatch), token);
            }

            int changedNumberOfThreads = 2;

            // Count down on the start latch to trigger the commands
            firstStartLatch.countDown();

            // Business method
            deviceCommandExecutor.changeNumberOfThreads(changedNumberOfThreads);

            // To be able to assert that more threads are created, we will need to execute more work.
            threadFactory.reset();  // Forget about the old threads
            List<DeviceCommandExecutionToken> secondSetOfTokens = deviceCommandExecutor.tryAcquireTokens(CAPACITY);
            CountDownLatch secondStartLatch = new CountDownLatch(1);
            CountDownLatch secondStopLatch = new CountDownLatch(CAPACITY);
            for (DeviceCommandExecutionToken token : secondSetOfTokens) {
                deviceCommandExecutor.execute(new LatchDrivenCommand(secondStartLatch, secondStopLatch), token);
            }

            // Asserts that no new thread has been created
            assertThat(threadFactory.getThreads()).isEmpty();
        } finally {
            shutdown(deviceCommandExecutor);
        }
    }

    private void shutdown(DeviceCommandExecutor deviceCommandExecutor) {
        if (deviceCommandExecutor != null) {
            deviceCommandExecutor.shutdownImmediate();
        }
    }

    /**
     * Serves as the root for all DeviceCommand classes that are created
     * in the context of this test. Will provide default implementations
     * for DeviceCommand methods that do not apply to this test.
     */
    private abstract class DeviceCommandForTestingPurposes implements DeviceCommand {

        @Override
        public void executeDuringShutdown(ComServerDAO comServerDAO) {
            // Do not execute while shutting down in unit test mode for now
        }

        @Override
        public ComServer.LogLevel getJournalingLogLevel() {
            return ComServer.LogLevel.TRACE;
        }

        @Override
        public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
            return this.toString();
        }
    }

    /**
     * A {@link DeviceCommand} that simply sleeps for a predefined amount of time.
     * The default amount of time is 1 seconds.
     */
    private class SleepCommand extends DeviceCommandForTestingPurposes {

        private long millis;

        private SleepCommand() {
            this(DateTimeConstants.MILLIS_PER_SECOND);
        }

        private SleepCommand(long millis) {
            super();
            this.millis = millis;
        }

        @Override
        public void execute(ComServerDAO comServerDAO) {
            try {
                Thread.sleep(this.millis);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        @Override
        public void logExecutionWith(ExecutionLogger logger) {
            // Not interesting for testing
        }
    }

    private class LatchDrivenCommand extends DeviceCommandForTestingPurposes {

        private CountDownLatch startLatch;
        private CountDownLatch stopLatch;

        private LatchDrivenCommand(CountDownLatch startLatch, CountDownLatch stopLatch) {
            super();
            this.startLatch = startLatch;
            this.stopLatch = stopLatch;
        }

        @Override
        public void execute(ComServerDAO comServerDAO) {
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
            // Unit testing commands typically don't do anything usefull
            System.out.println(this.toString() + " is now executing...");
        }

        @Override
        public void logExecutionWith(ExecutionLogger logger) {
            // Not interesting for testing
        }
    }

    private class DataAccessExceptionCommand extends LatchDrivenCommand {

        private DataAccessExceptionCommand(CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(startLatch, stopLatch);
        }

        @Override
        protected void doExecute() {
            super.doExecute();
            throw new DataAccessException(new SQLException(this.getClass().getName() + " - For unit testing purposes only"));
        }

    }

    private class ApplicationExceptionCommand extends LatchDrivenCommand {

        private ApplicationExceptionCommand(CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(startLatch, stopLatch);
        }

        @Override
        protected void doExecute() {
            super.doExecute();
            throw new ApplicationException(this.getClass().getName() + " - For unit testing purposes only");
        }

    }

    private class RuntimeExceptionCommand extends LatchDrivenCommand {

        private RuntimeExceptionCommand(CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(startLatch, stopLatch);
        }

        @Override
        protected void doExecute() {
            super.doExecute();
            throw new RuntimeException(this.getClass().getName() + " - For unit testing purposes only");
        }

    }

    private interface SignalReceiver {

        public void receiveSignal();

    }

    private class SignalSender extends LatchDrivenCommand {

        private SignalReceiver receiver;

        private SignalSender(SignalReceiver receiver) {
            this(receiver, new CountDownLatch(1), new CountDownLatch(1));
        }

        private SignalSender(SignalReceiver receiver, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(startLatch, stopLatch);
            this.receiver = receiver;
        }

        @Override
        public void doExecute() {
            super.doExecute();
            this.receiver.receiveSignal();
        }

    }

    /**
     * Provides an implementation of the ThreadFactory interface
     * that keeps track of the created Threads and uses an existing
     * ThreadFactory to do the actual work.
     */
    private class TrackingThreadFactory implements ThreadFactory {

        private ThreadFactory actualFactory;
        private List<Thread> threads;

        private TrackingThreadFactory(ThreadFactory actualFactory) {
            super();
            this.actualFactory = actualFactory;
            this.doReset();
        }

        public List<Thread> getThreads() {
            return threads;
        }

        public Thread getThread(int index) {
            return this.threads.get(index);
        }

        public void reset() {
            this.doReset();
        }

        private void doReset() {
            this.threads = new ArrayList<>();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = this.actualFactory.newThread(r);
            this.threads.add(thread);
            return thread;
        }

    }

}