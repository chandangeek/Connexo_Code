/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.ExecutionTimer;
import com.elster.jupiter.util.time.ExecutionTimerService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * provides an implementation for the {@link ExecutionTimerService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-10 (11:12)
 */
@Component(name = "com.elster.jupiter.time.ExecutionTimerService", service = {ExecutionTimerService.class})
public class ExecutionTimerServiceImpl implements IExecutionTimerService {
    private BundleContext bundleContext;
    private final Set<ExecutionTimer> timers = ConcurrentHashMap.newKeySet();

    // For OSGi purposes
    public ExecutionTimerServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public ExecutionTimerServiceImpl(BundleContext bundleContext) {
        this();
        this.activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public ExecutionTimer newTimer(String name, Duration timeout) {
        this.checkUniqueName(name);
        ExecutionTimerImpl timer = new ExecutionTimerImpl(this, name, timeout);
        timer.activate(this.bundleContext);
        this.timers.add(timer);
        return timer;
    }

    private void checkUniqueName(String name) {
        if (this.timers.stream().map(ExecutionTimer::getName).anyMatch(name::equals)) {
            throw new IllegalArgumentException("ExecutionTimer with name " + name + " already exist");
        }
    }

    @Override
    public void deactivated(ExecutionTimerImpl executionTimer) {
        timers.remove(executionTimer);
    }

}