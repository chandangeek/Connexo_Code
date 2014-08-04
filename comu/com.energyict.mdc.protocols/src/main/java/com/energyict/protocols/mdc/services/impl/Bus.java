package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.util.time.Clock;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

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
    private static AtomicReference<PropertySpecService> propertySpecServiceProvider = new AtomicReference<>();
    private static AtomicReference<MdcReadingTypeUtilService> mdcReadingTypeUtilServiceProvider = new AtomicReference<>();

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

    public static PropertySpecService getPropertySpecService() {
        return propertySpecServiceProvider.get();
    }

    public static void setPropertySpecService(PropertySpecService propertySpecService) {
        propertySpecServiceProvider.set(propertySpecService);
    }

    public static void clearPropertySpecService(PropertySpecService old) {
        propertySpecServiceProvider.compareAndSet(old, null);
    }

    public static MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
        return mdcReadingTypeUtilServiceProvider.get();
    }

    public static void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        mdcReadingTypeUtilServiceProvider.set(mdcReadingTypeUtilService);
    }

    public static void clearMdcReadingTypeUtilService(MdcReadingTypeUtilService old){
        mdcReadingTypeUtilServiceProvider.compareAndSet(old, null);
    }
}