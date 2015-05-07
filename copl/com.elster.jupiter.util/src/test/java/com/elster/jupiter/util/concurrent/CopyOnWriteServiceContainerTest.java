package com.elster.jupiter.util.concurrent;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

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

}