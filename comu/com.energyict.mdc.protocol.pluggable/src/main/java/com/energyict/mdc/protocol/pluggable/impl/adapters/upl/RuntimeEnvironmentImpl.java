package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.RuntimeEnvironment;
import com.energyict.mdc.upl.Services;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Optional;

/**
 * Provides an implementation for the {@link RuntimeEnvironment} interface
 * that uses the BundleContext to obtain access to environment variables.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-16 (14:52)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.runtime", service = {RuntimeEnvironment.class})
@SuppressWarnings("unused")
public class RuntimeEnvironmentImpl implements RuntimeEnvironment {

    private BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        Services.runtimeEnvironment(this);
    }

    @Deactivate
    public void deactivate() {
        Services.runtimeEnvironment(null);
    }

    @Override
    public Optional<String> getProperty(String name) {
        return Optional.ofNullable(this.bundleContext.getProperty(name));
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        return this.getProperty(name).orElse(defaultValue);
    }

}