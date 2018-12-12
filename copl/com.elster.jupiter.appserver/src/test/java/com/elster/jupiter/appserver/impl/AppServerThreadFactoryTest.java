/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppServerThreadFactoryTest {

    @Mock
    private AppServer appServer;
    @Mock
    private AppService appService;

    @Before
    public void setUp() {
        when(appServer.getName()).thenReturn("name");
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAppServerThreadFactory() throws InterruptedException {
        ThreadGroup group = new ThreadGroup("tes");

        ExecutorService executorService = Executors.newSingleThreadExecutor(new AppServerThreadFactory(group, new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        }, appService, () -> "myThread"));

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
