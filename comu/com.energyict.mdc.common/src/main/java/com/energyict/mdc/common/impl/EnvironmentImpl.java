package com.energyict.mdc.common.impl;

import com.energyict.mdc.common.Environment;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.inject.Inject;
import java.util.logging.Logger;

@Component(name="com.energyict.mdc.environment", service = Environment.class)
public class EnvironmentImpl implements Environment {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentImpl.class.getName());
    private BundleContext context;

    @Inject
    public EnvironmentImpl(BundleContext bundleContext) {
        super();
        this.activate(bundleContext);
    }

    @Activate
    public void activate (BundleContext context) {
        this.context = context;
        Environment.DEFAULT.set(this);
        LOGGER.fine("MDC environment is actived");
    }

    @Deactivate
    public void deactivate () {
        Environment.DEFAULT.set(null);
        this.context = null;
        LOGGER.fine("MDC environment is deactived");
    }

    @Override
    public String getProperty (String key) {
        return this.context.getProperty(key);
    }

}