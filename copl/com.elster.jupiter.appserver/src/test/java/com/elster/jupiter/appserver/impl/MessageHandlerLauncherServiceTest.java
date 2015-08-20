package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Registration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.service.component.ComponentContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageHandlerLauncherServiceTest {

    private static final String SUBSCRIBER = "Subscriber";
    private static final String BATCH_EXECUTOR = "batch executor";
    private static final String NAME = "name";
    public static final String DESTINATION = "destination";
    private MessageHandlerLauncherService messageHandlerLauncherService;

    @Mock
    private MessageHandlerFactory factory;
    @Mock
    private IAppService appService;
    @Mock
    private SubscriberSpec subscriberSpec;
    @Mock
    private SubscriberExecutionSpec subscriberExecutionSpec;
    @Mock
    private Message message;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    private TransactionVerifier transactionService;
    @Mock
    private MessageHandler handler;
    @Mock
    private ComponentContext context;
    @Mock
    private AppServer appServer;
    @Mock
    private DestinationSpec destination;
    @Mock
    private Registration registration;

    @Before
    public void setUp() {
        messageHandlerLauncherService = new MessageHandlerLauncherService();
        when(subscriberExecutionSpec.getSubscriberSpec()).thenReturn(subscriberSpec);
        when(subscriberSpec.getName()).thenReturn(SUBSCRIBER);
        when(subscriberSpec.receive()).thenReturn(message);
        when(subscriberSpec.getDestination()).thenReturn(destination);
        when(destination.getName()).thenReturn(DESTINATION);
        when(subscriberExecutionSpec.getThreadCount()).thenReturn(1);
        when(userService.findUser(BATCH_EXECUTOR)).thenReturn(Optional.of(user));
        when(factory.newMessageHandler()).thenReturn(handler);
        when(appServer.getName()).thenReturn(NAME);
        when(appService.addCommandListener(any())).thenReturn(registration);

        transactionService = new TransactionVerifier(handler);

        messageHandlerLauncherService.setAppService(appService);
        messageHandlerLauncherService.setUserService(userService);
        messageHandlerLauncherService.setThreadPrincipalService(threadPrincipalService);
        messageHandlerLauncherService.setTransactionService(transactionService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddResourceDoNotLaunchIfNotDefinedOnThisAppServer() {
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Collections.<SubscriberExecutionSpec>emptyList());
        when(appServer.getSubscriberExecutionSpecs()).thenReturn(Collections.emptyList());
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(appServer.isActive()).thenReturn(true);

        Map<String, Object> map = new HashMap<>();
        map.put("subscriber", SUBSCRIBER);
        map.put("destination", DESTINATION);

        messageHandlerLauncherService.addResource(factory, map);

        verify(subscriberSpec, never()).receive();
    }

    @Test(timeout = 5000)
    public void testAddResourceStartReceivingMessages() throws InterruptedException {
        final CountDownLatch arrivalLatch = new CountDownLatch(2);
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Arrays.asList(subscriberExecutionSpec));
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(appServer.isActive()).thenReturn(true);
        Answer<Void> methodReached = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                arrivalLatch.countDown();
                return null;
            }
        };
        doAnswer(methodReached).when(handler).process(message);
        doAnswer(methodReached).when(handler).onMessageDelete(message);

        Map<String, Object> map = new HashMap<>();
        map.put("subscriber", SUBSCRIBER);
        map.put("destination", DESTINATION);

        try {
            messageHandlerLauncherService.activate();
            messageHandlerLauncherService.addResource(factory, map);

            arrivalLatch.await();

            verify(subscriberSpec, atLeastOnce()).receive();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            messageHandlerLauncherService.deactivate();
        }
    }

    @Test(timeout = 5000)
    public void testExceptionInHandlerProcess() throws InterruptedException {
        final CountDownLatch arrivalLatch = new CountDownLatch(1);
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Arrays.asList(subscriberExecutionSpec));
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(appServer.isActive()).thenReturn(true);
        Answer<Void> methodReached = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                try {
                    throw new RuntimeException();
                } finally {
                    arrivalLatch.countDown();
                }
            }
        };
        doAnswer(methodReached).when(handler).process(message);

        Map<String, Object> map = new HashMap<>();
        map.put("subscriber", SUBSCRIBER);
        map.put("destination", DESTINATION);

        try {
            messageHandlerLauncherService.activate();
            messageHandlerLauncherService.addResource(factory, map);

            arrivalLatch.await();

            verify(subscriberSpec, atLeastOnce()).receive();
            verify(handler).process(message);
            verify(handler, never()).onMessageDelete(message);

            verify(handler, transactionService.inTransaction()).process(message);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            messageHandlerLauncherService.deactivate();
        }
    }

    @Test(timeout = 5000)
    public void testRemoveResource() throws InterruptedException {
        final CountDownLatch arrivalLatch = new CountDownLatch(1);
        final CountDownLatch waitForCancel = new CountDownLatch(1);
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Arrays.asList(subscriberExecutionSpec));
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(appServer.isActive()).thenReturn(true);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                arrivalLatch.countDown();
                try {
                    waitForCancel.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
        }).when(handler).process(message);

        Map<String, Object> map = new HashMap<>();
        map.put("subscriber", SUBSCRIBER);
        map.put("destination", DESTINATION);

        try {
            messageHandlerLauncherService.activate();
            messageHandlerLauncherService.addResource(factory, map);

            arrivalLatch.await();

            messageHandlerLauncherService.removeResource(factory);

            waitForCancel.countDown();

            verify(subscriberSpec).cancel();
            InOrder inOrder = inOrder(handler);
            inOrder.verify(handler).process(message);
            inOrder.verify(handler).onMessageDelete(message);

            verify(handler, transactionService.inTransaction()).process(message);
            verify(handler, transactionService.notInTransaction()).onMessageDelete(message);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            messageHandlerLauncherService.deactivate();
        }
    }


    @Test
    @Ignore // fails intermittently -> destabilizes build -> COPL-1020
    public void testCorruptMessageHandlerFactory() throws InterruptedException {
        doThrow(RuntimeException.class).when(factory).newMessageHandler();

        final CountDownLatch arrivalLatch = new CountDownLatch(2);
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Arrays.asList(subscriberExecutionSpec));
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(appServer.isActive()).thenReturn(true);

        Map<String, Object> map = new HashMap<>();
        map.put("subscriber", SUBSCRIBER);
        map.put("destination", DESTINATION);

        int threadCount = 0;

        try {
            messageHandlerLauncherService.activate();
            threadCount = threadCount();

            messageHandlerLauncherService.addResource(factory, map);

            assertThat(messageHandlerLauncherService.futureReport()).isEmpty();
            assertThat(messageHandlerLauncherService.threadReport()).isEmpty();

            assertThat(threadCount()).isEqualTo(threadCount);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            messageHandlerLauncherService.deactivate();
        }
    }

    private int threadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while (threadGroup.getParent() != threadGroup && threadGroup.getParent() != null) {
            threadGroup = threadGroup.getParent();
        }
        int activeCount = threadGroup.activeCount();

        Thread[] threads = new Thread[activeCount * 2];
        int count = threadGroup.enumerate(threads, true);
        return count;
    }
}
