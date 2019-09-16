package com.energyict.mdc.engine.impl;

import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.data.Reading;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.data.TextReading;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.RegisterExtractor;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Optional;

/**
 * Provides an implementation for the {@link RegisterExtractor} interface
 * that assumes that all UPL objects are in fact {@link Register}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (09:26)
 */
@Component(name = "com.energyict.mdc.device.data.upl.register.extractor", service = {RegisterExtractor.class}, immediate = true)
@SuppressWarnings("unused")
public class RegisterExtractorImpl implements RegisterExtractor {

    @Activate
    public void activate() {
        Services.registerExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.registerExtractor(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<RegisterExtractor.RegisterReading> lastReading(com.energyict.mdc.upl.meterdata.Register uplRegister) {
        Register<Reading, RegisterSpec> register = (Register<Reading, RegisterSpec>) uplRegister;
        return register.getLastReading().map(RegisterReading::new);
    }

    private static class RegisterReading implements RegisterExtractor.RegisterReading {
        private final Reading actual;

        private RegisterReading(Reading actual) {
            this.actual = actual;
        }

        @Override
        public String text() {
            if (this.actual instanceof TextReading) {
                TextReading textReading = (TextReading) this.actual;
                return textReading.getValue();
            } else {
                // All other types of readings do not have
                return null;
            }
        }
    }

}