package com.elster.jupiter.hsm.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

// This test works fine however it is ignored while it uses sleep and threads which is unsafe in a C.I environment
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class HsmConfigurationObserverTest {

    public static final long RELOAD_TIME = 1000L;

    @Mock
    public HsmConfigurationService hsmConfigurationService;

    private Thread thread;

    @After
    public void tearDown() throws InterruptedException {
        if (thread.isAlive()) {
            thread.stop();
        }
    }

    @Test
    public void testNullConfigFile() throws InterruptedException {
        thread = start(new HsmConfigurationObserver(hsmConfigurationService, null, true, RELOAD_TIME));
        Thread.sleep(RELOAD_TIME * 3);
        Mockito.verifyNoMoreInteractions(hsmConfigurationService);
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void testEmptyConfigFile() throws InterruptedException {
        thread = start(new HsmConfigurationObserver(hsmConfigurationService, "", true, RELOAD_TIME));
        Thread.sleep(RELOAD_TIME * 3);
        Mockito.verifyNoMoreInteractions(hsmConfigurationService);
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void testNoAutomaticReload() throws InterruptedException {
        HsmConfigurationObserver hsmConfigurationObserver = new HsmConfigurationObserver(hsmConfigurationService, "file", false, RELOAD_TIME);
        thread = start(hsmConfigurationObserver);
        Thread.sleep(RELOAD_TIME * 3);
        Mockito.verify(hsmConfigurationService, Mockito.times(1)).reload();
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void testAutomaticReload() throws InterruptedException {
        HsmConfigurationObserver hsmConfigurationObserver = new HsmConfigurationObserver(hsmConfigurationService, "file", true, RELOAD_TIME);
        thread = start(hsmConfigurationObserver);
        Thread.sleep(RELOAD_TIME * 3);
        Mockito.verify(hsmConfigurationService, Mockito.atLeastOnce()).reload();
        Mockito.verify(hsmConfigurationService, Mockito.atMost(4)).reload();
        Assert.assertTrue(thread.isAlive());
        hsmConfigurationObserver.stop();
        Assert.assertTrue(thread.isAlive());
    }

    private Thread start(HsmConfigurationObserver hsmConfigurationObserver) {
        Thread thread = new Thread(hsmConfigurationObserver);
        thread.start();
        return thread;
    }

}
