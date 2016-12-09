package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.security.SecurityService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link SecurityService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (13:22)
 */
@SuppressWarnings("unused")
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.nlsservice", service = {UPLNlsServiceImpl.class})
public class UPLSecurityService implements SecurityService {

    @Activate
    public void activate() {
        Services.securityService(this);
    }

    @Override
    public Password passwordFromEncryptedString(String encrypted) {
        return new UPLPasswordAdapter(new com.energyict.mdc.common.Password(encrypted));
    }

    private static final class UPLPasswordAdapter implements Password {
        private final com.energyict.mdc.common.Password actual;

        private UPLPasswordAdapter(com.energyict.mdc.common.Password actual) {
            this.actual = actual;
        }

        @Override
        public String getValue() {
            return this.actual.getValue();
        }
    }

}