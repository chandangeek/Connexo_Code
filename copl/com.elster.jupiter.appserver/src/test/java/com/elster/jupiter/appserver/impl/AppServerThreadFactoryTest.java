package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppServerThreadFactoryTest {

    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private AppServer appServer;

    @Before
    public void setUp() {
        Bus.setServiceLocator(serviceLocator);

        when(serviceLocator.getAppServer()).thenReturn(Optional.of(appServer));
        when(appServer.getName()).thenReturn("name");
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
    }

    @Test
    public void testAppServerThreadFactory() throws InterruptedException {
        ThreadGroup group = new ThreadGroup("tes");

        ExecutorService executorService = Executors.newSingleThreadExecutor(new AppServerThreadFactory(group, new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        }));

        final CountDownLatch latch = new CountDownLatch(1);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });

        latch.await();
    }

}
