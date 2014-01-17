package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.issues.IssueService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides access to third party services that were injected
 * at the time the protocols bundle was activated.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-09 (11:17)
 */
public final class Bus {

    private static AtomicReference<IssueService> issueServiceProvider = new AtomicReference<>();
    private static AtomicReference<Clock> clockProvider = new AtomicReference<>();

    public static IssueService getIssueService() {
        return issueServiceProvider.get();
    }

    public static void setIssueService(IssueService issueService) {
        issueServiceProvider.set(issueService);
    }

    public static void clearIssueService(IssueService old) {
        issueServiceProvider.compareAndSet(old, null);
    }

    public static Clock getClock() {
        return clockProvider.get();
    }

    public static void setClock(Clock clock) {
        clockProvider.set(clock);
    }

    public static void clearClock(Clock old) {
        clockProvider.compareAndSet(old, null);
    }

}