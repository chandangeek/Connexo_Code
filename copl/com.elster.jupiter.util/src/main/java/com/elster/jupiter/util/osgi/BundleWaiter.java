/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BundleWaiter {

    public interface Startable {
        void start(BundleContext context);
    }

    private static final int stateMask =
            Bundle.INSTALLED | Bundle.RESOLVED | Bundle.START_TRANSIENT | Bundle.STARTING | Bundle.ACTIVE |
                    Bundle.STOP_TRANSIENT | Bundle.STOPPING;
    private final Set<String> symbolicNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private final BundleContext context;
    private final Startable startable;
    private final BundleTracker<String> tracker;

    private BundleWaiter(Startable startable, BundleContext context, String[] bundleNames) {
        this.startable = startable;
        this.context = context;
        for (String symbolicName : bundleNames) {
            symbolicNames.add(symbolicName);
        }
        this.tracker = new BundleTracker<>(context, stateMask, new BundleWaiterTrackerCustomizer());
    }

    public static void wait(Startable startable, BundleContext context, String... symbolicNames) {
        if (symbolicNames.length == 0) {
            startable.start(context);
        } else {
            new BundleWaiter(startable, context, symbolicNames).doWait();
        }
    }

    private void doWait() {
        tracker.open();
    }

    private void start() {
        tracker.close();
        startable.start(context);
    }

    private class BundleWaiterTrackerCustomizer implements BundleTrackerCustomizer<String> {
        @Override
        public String addingBundle(Bundle bundle, BundleEvent event) {
            if (bundle.getSymbolicName() == null || !symbolicNames.contains(bundle.getSymbolicName())) {
                return null;
            } else {
                if (bundle.getState() == Bundle.ACTIVE) {
                    symbolicNames.remove(bundle.getSymbolicName());
                    if (symbolicNames.isEmpty()) {
                        start();
                    }
                    return null;
                } else {
                    return bundle.getSymbolicName();
                }
            }
        }

        @Override
        public void modifiedBundle(Bundle bundle, BundleEvent event, String symbolicName) {
            if (event.getType() == BundleEvent.STARTED) {
                symbolicNames.remove(symbolicName);
                if (symbolicNames.isEmpty()) {
                    start();
                }
            }
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event, String symbolicName) {
        }

    }

}
