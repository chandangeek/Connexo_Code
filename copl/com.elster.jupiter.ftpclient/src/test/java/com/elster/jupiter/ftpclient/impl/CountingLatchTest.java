package com.elster.jupiter.ftpclient.impl;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class CountingLatchTest {

    @Test
    public void test() throws InterruptedException {
        CountingLatch countingLatch = new CountingLatch();

        countingLatch.await(5, TimeUnit.SECONDS);

        countingLatch.acquire();

        try {
            countingLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        countingLatch.release();

        countingLatch.await(5, TimeUnit.SECONDS);
    }

}