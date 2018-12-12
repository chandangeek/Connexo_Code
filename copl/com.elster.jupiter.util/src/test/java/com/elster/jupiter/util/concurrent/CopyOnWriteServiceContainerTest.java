/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.concurrent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CopyOnWriteServiceContainerTest {

    private CopyOnWriteServiceContainer<String> serviceContainer;

    @Before
    public void setUp() throws Exception {
        serviceContainer = new CopyOnWriteServiceContainer<>();
    }

    @Test
    public void testRegister() {

        serviceContainer.register("A");

        assertThat(serviceContainer.poll(Predicate.isEqual("A"))).contains("A");
    }

    @Test
    public void testRegisterWithGet() throws InterruptedException {

        serviceContainer.register("A");

        assertThat(serviceContainer.get(Predicate.isEqual("A"))).isEqualTo("A");
    }

    @Test
    public void testUnregister() {

        serviceContainer.register("A");

        assertThat(serviceContainer.poll(Predicate.isEqual("A"))).contains("A");

        serviceContainer.unregister("A");

        assertThat(serviceContainer.poll(Predicate.isEqual("A"))).isEmpty();
    }

    @Test
    public void testGet() throws InterruptedException, ExecutionException, TimeoutException {

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            Future<String> future = executorService.submit(() -> serviceContainer.get(Predicate.isEqual("A")));

            try {
                future.get(0, TimeUnit.NANOSECONDS);
                fail("should time out");
            } catch (TimeoutException e) {
                // expected
            }

            try {
                future.get(1000000, TimeUnit.NANOSECONDS); // forcing to wait on the listener
                fail("should time out");
            } catch (TimeoutException e) {
                // expected
            }

            serviceContainer.register("A");

            assertThat(future.get()).isEqualTo("A");
        } finally {
            executorService.shutdown();
        }


    }

    @Test
    public void testGetWithTimeOutTimeOut() throws InterruptedException, ExecutionException, TimeoutException {

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            Future<Optional<String>> future = executorService.submit(() -> serviceContainer.get(Predicate.isEqual("A"), Duration.ofMillis(200)));

            try {
                future.get(100, TimeUnit.NANOSECONDS);
                fail("should time out");
            } catch (TimeoutException e) {
                // expected
            }

            assertThat(future.get()).isEmpty();

        } finally {
            executorService.shutdown();
        }
    }

    @Test
    public void testGetWithTimeOutGetsIt() throws InterruptedException, ExecutionException, TimeoutException {

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            Future<Optional<String>> future = executorService.submit(() -> serviceContainer.get(Predicate.isEqual("A"), Duration.ofMillis(200)));

            try {
                future.get(100, TimeUnit.NANOSECONDS);
                fail("should time out");
            } catch (TimeoutException e) {
                // expected
            }

            serviceContainer.register("A");

            assertThat(future.get()).contains("A");

        } finally {
            executorService.shutdown();
        }
    }

    @Test
    public void testPollGetsIt() throws InterruptedException, ExecutionException, TimeoutException {

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            Future<Optional<String>> future = executorService.submit(() -> serviceContainer.get(Predicate.isEqual("A"), Duration.ofMillis(200)));

            try {
                future.get(100, TimeUnit.NANOSECONDS);
                fail("should time out");
            } catch (TimeoutException e) {
                // expected
            }

            serviceContainer.register("A");

            assertThat(future.get()).contains("A");

        } finally {
            executorService.shutdown();
        }
    }

    @Test
    public void testOnRegistrationTriggersOnCallingThreadForAvailableService() {

        List<String> handled = new ArrayList<>();

        serviceContainer.register("A");
        serviceContainer.onRegistration(s -> true, handled::add);

        assertThat(handled).containsExactly("A");
    }

    @Test
    public void testOnRegistrationTriggersOnCallingThreadForAvailableServices() {

        Set<String> handled = new HashSet<>();

        serviceContainer.register("A");
        serviceContainer.register("B");
        serviceContainer.onRegistration(s -> true, handled::add);

        assertThat(handled).containsExactly("A", "B");
    }

    @Test
    public void testOnRegistrationTriggersOnCallingThreadForNewServices() throws InterruptedException {

        Set<String> handled = Collections.newSetFromMap(new ConcurrentHashMap<>());

        serviceContainer.onRegistration(s -> true, handled::add);

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        CountDownLatch done = new CountDownLatch(1);

        executorService.submit(() -> {
            serviceContainer.register("A");
            serviceContainer.register("B");
            done.countDown();
        });

        done.await();

        assertThat(handled).containsExactly("A", "B");
    }

    @Test
    public void stressTestOnRegistration() throws InterruptedException {
        Map<String, LongAdder> counts = new ConcurrentHashMap<>();

        serviceContainer.onRegistration(s -> true, s -> counts.computeIfAbsent(s, str -> new LongAdder()).increment());

        int threads = 10;
        int rotations = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    start.await();
                    int letters = 'Z' - 'A' + 1;
                    IntStream.range(0, letters * rotations).mapToObj(n -> Character.valueOf((char) ((n % letters) + 'A')).toString()).forEach(serviceContainer::register);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        counts.values()
                .forEach(adder -> assertThat(adder.longValue()).isEqualTo(threads*rotations));
    }


}