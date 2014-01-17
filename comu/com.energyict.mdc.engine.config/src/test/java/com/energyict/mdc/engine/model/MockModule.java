package com.energyict.mdc.engine.model;

import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;

public class MockModule extends AbstractModule {
        private BundleContext bundleContext;

        public MockModule(BundleContext bundleContext) {
            super();
            this.bundleContext = bundleContext;
        }

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
        }
    }
