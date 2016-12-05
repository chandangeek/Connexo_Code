package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

public class DualControlServiceImpl implements DualControlService {

    private volatile ThreadPrincipalService threadPrincipalService;

    public DualControlServiceImpl() {
    }

    @Inject
    public DualControlServiceImpl(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public Monitor createMonitor() {
        return new MonitorImpl(threadPrincipalService);
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }
}
