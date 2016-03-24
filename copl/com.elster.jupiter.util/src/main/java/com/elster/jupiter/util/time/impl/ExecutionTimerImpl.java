package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.ExecutionStatistics;
import com.elster.jupiter.util.time.ExecutionTimer;
import com.elster.jupiter.util.time.StopWatch;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.time.Duration;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provides an implementation for the {@link ExecutionTimer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-10 (12:02)
 */
public final class ExecutionTimerImpl implements ExecutionTimer {
    private final String name;
    private final Duration timeout;
    private final ExecutionStatisticsImpl statistics = new ExecutionStatisticsImpl();
    private final List<ExecutionTimerImpl> listeners = new CopyOnWriteArrayList<>();
    private ServiceRegistration<ExecutionStatisticsImpl> serviceRegistration;

    public ExecutionTimerImpl(String name, Duration timeout) {
        this.name = name;
        this.timeout = timeout;
    }

    public void activate(BundleContext bundleContext) {
        Dictionary<String, String> serviceProperties =  new Hashtable<>();
        serviceProperties.put("name", "com.elster.jupiter.time.timer." + this.name.replace(" ", "_") + ".jmx");
        serviceProperties.put("jmx.objectname", "com.elster.jupiter:type=" + this.name);
        this.serviceRegistration = bundleContext.registerService(ExecutionStatisticsImpl.class, this.statistics, serviceProperties);
    }

    @Override
    public void deactivate() {
        this.serviceRegistration.unregister();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public ExecutionStatistics getStatistics() {
        return this.statistics;
    }

    @Override
    public void reset() {
        this.statistics.reset();
    }

    @Override
    public void join(ExecutionTimer other) {
        ((ExecutionTimerImpl) other).notify(this);
    }

    void notify(ExecutionTimerImpl listener) {
        this.listeners.add(listener);
    }

    @Override
    public void time(Runnable runnable) {
        StopWatch stopWatch = new StopWatch();
        runnable.run();
        stopWatch.stop();
        this.registerExecution(stopWatch);
    }

    @Override
    public <V> V time(Callable<V> callable) throws Exception {
        StopWatch stopWatch = new StopWatch();
        V result = callable.call();
        stopWatch.stop();
        this.registerExecution(stopWatch);
        return result;
    }

    private void registerExecution(StopWatch stopWatch) {
        long elapsedNanos = stopWatch.getElapsed();
        if (elapsedNanos <= this.timeout.toNanos()) {
            this.statistics.registerExecution(elapsedNanos);
        } else {
            this.statistics.registerTimeout();
        }
        this.listeners.stream().forEach(l -> l.registerExecution(stopWatch));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutionTimerImpl that = (ExecutionTimerImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}