package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageHandlerLauncherServiceTest {

    private static final String SUBSCRIBER = "Subscriber";
    private static final String BATCH_EXECUTOR = "batch executor";
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
    private ServiceLocator serviceLocator;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private TransactionService transactionService;
    @Mock
    private MessageHandler handler;

    @Before
    public void setUp() {
        messageHandlerLauncherService = new MessageHandlerLauncherService();
        when(subscriberExecutionSpec.getSubscriberSpec()).thenReturn(subscriberSpec);
        when(subscriberSpec.getName()).thenReturn(SUBSCRIBER);
        when(subscriberSpec.receive()).thenReturn(message);
        when(subscriberExecutionSpec.getThreadCount()).thenReturn(1);
        when(serviceLocator.getThreadPrincipalService()).thenReturn(threadPrincipalService);
        when(serviceLocator.getUserService()).thenReturn(userService);
        when(serviceLocator.getTransactionService()).thenReturn(transactionService);
        when(userService.findUser(BATCH_EXECUTOR)).thenReturn(Optional.of(user));
        when(factory.newMessageHandler()).thenReturn(handler);

        initFakeTransactionService();

        messageHandlerLauncherService.setAppService(appService);

        Bus.setServiceLocator(serviceLocator);
    }

    private void initFakeTransactionService() {
        when(transactionService.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
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
        final CountDownLatch arrivalLatch = new CountDownLatch(1);
        when(appService.getSubscriberExecutionSpecs()).thenReturn(Arrays.asList(subscriberExecutionSpec));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                arrivalLatch.countDown();
                return null;
            }
        }).when(handler).process(message);

        Map<String, Object> map = new HashMap<>();
        map.put("subscriber", SUBSCRIBER);

        try {
            messageHandlerLauncherService.addResource(factory, map);

            arrivalLatch.await();

            verify(subscriberSpec, atLeastOnce()).receive();
        } finally {
            messageHandlerLauncherService.deactivate(null);
        }
    }


}
