package com.elster.jupiter.util.concurrent;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Holds on to optional Services, and allows waiting for optional Services to be registered.
 *
 * @param <S> the type of service
 */
@ProviderType
public interface OptionalServiceContainer<S> {

    /**
     * Registers a service
     * @param s
     */
    void register(S s);

    /**
     * Unregisters a service
     * @param s
     */
    void unregister(S s);

    /**
     * Gets a service that matches the given predicate. Blocks and waits until such a service is registered.
     * @return a service that matches the given predicate
     * @throws InterruptedException if the thread is interrupted while waiting for a matching service
     */
    S get(Predicate<? super S> matcher) throws InterruptedException;

    /**
     * Gets a service that matches the given predicate. Blocks and waits until such a service is registered, or until the timeout has passed.
     * @return an Optional of a service that matches the given predicate or empty() if no such service was available after the given timeout.
     * @throws InterruptedException if the thread is interrupted while waiting for a matching service
     */
    Optional<S> get(Predicate<? super S> matcher, Duration timeout) throws InterruptedException;

    /**
     * Gets a service that matches the given predicate.
     * @return an Optional of a service that matches the given predicate or empty() if no such service was available.
     */
    Optional<S> poll(Predicate<? super S> matcher);

    /**
     * Gets all registered services
     * @return list of registered services
     */
    List<S> getServices();
}
