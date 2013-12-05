package com.energyict.mdc.issues;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds onto the {@link IssueService} as soon as it activates.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (11:34)
 */
public class Bus {
    private static AtomicReference<IssueService> issueServiceProvider = new AtomicReference<>();

    public static IssueService getIssueService () {
        return issueServiceProvider.get();
    }

    public static void setIssueService (IssueService issueService) {
        issueServiceProvider.set(issueService);
    }

    public static void clearIssueService (IssueService old) {
        issueServiceProvider.compareAndSet(old, null);
    }

}