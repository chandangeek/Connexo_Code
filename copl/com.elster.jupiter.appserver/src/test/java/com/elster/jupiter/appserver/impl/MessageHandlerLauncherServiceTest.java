package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.junit.After;
import org.junit.Before;
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

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageHandlerLauncherServiceTest {

    private static final String SUBSCRIBER = "Subscriber";
    private static final String BATCH_EXECUTOR = "batch executor";
    private static final String NAME = "name";
    private MessageHandlerLauncherService messageHandlerLauncherService;

    @Mock
    private MessageHandlerFactory factory;
    @Mock
    private AppService appService;
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
    @Mock
    private TransactionVerifier transactionService;
    @Mock
    private MessageHandler handler;
    @Mock
    private ComponentContext context;
    @Mock
    private AppServer appServer;

    @Before
    public void setUp() {
        messageHandlerLauncherService = new MessageHandlerLauncherService();
        when(subscriberExecutionSpec.getSubscriberSpec()).thenReturn(subscriberSpec);
        when(subscriberSpec.getName()).thenReturn(SUBSCRIBER);
        when(subscriberSpec.receive()).thenReturn(message);
        when(subscriberExecutionSpec.getThreadCount()).thenReturn(1);
        when(userService.findUser(BATCH_EXECUTOR)).thenReturn(Optional.of(user));
        when(factory.newMessageHandler()).thenReturn(handler);
        when(appServer.getName()).thenReturn(NAME);

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

        Map<String, Object> map = new HashMap<>();
        map.put("subscriber", SUBSCRIBER);

        messageHandlerLauncherService.addResource(factory, map);

        verify(subscriberSpec, never()).receive();
    }

    @Test
    public void testAddResourceStartReceivingMessages() throws InterruptedException {
        final CountDownLatch arrivalLatch = new CountDownLatch(2);
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Arrays.asList(subscriberExecutionSpec));
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
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

        try {
            messageHandlerLauncherService.activate();
            messageHandlerLauncherService.addResource(factory, map);

            arrivalLatch.await();

            verify(subscriberSpec, atLeastOnce()).receive();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            messageHandlerLauncherService.deactivate();
        }
    }

    @Test
    public void testExceptionInHandlerProcess() throws InterruptedException {
        final CountDownLatch arrivalLatch = new CountDownLatch(1);
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Arrays.asList(subscriberExecutionSpec));
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
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

        try {
            messageHandlerLauncherService.activate();
            messageHandlerLauncherService.addResource(factory, map);

            arrivalLatch.await();

            verify(subscriberSpec, atLeastOnce()).receive();
            verify(handler).process(message);
            verify(handler, never()).onMessageDelete(message);

            verify(handler, transactionService.inTransaction()).process(message);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            messageHandlerLauncherService.deactivate();
        }
    }

    @Test
    public void testRemoveResource() throws InterruptedException {
        final CountDownLatch arrivalLatch = new CountDownLatch(1);
        final CountDownLatch waitForCancel = new CountDownLatch(1);
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Arrays.asList(subscriberExecutionSpec));
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                arrivalLatch.countDown();
                waitForCancel.await();
                return null;
            }
        }).when(handler).process(message);

        Map<String, Object> map = new HashMap<>();
        map.put("subscriber", SUBSCRIBER);

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


}
