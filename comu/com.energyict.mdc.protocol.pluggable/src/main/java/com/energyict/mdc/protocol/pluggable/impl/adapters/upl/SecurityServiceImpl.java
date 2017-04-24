package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.security.SecurityService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Provides an implementation for the {@link SecurityService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (13:09)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.security", service = {SecurityService.class}, immediate = true)
@SuppressWarnings("unused")
public class SecurityServiceImpl implements SecurityService {

    @Activate
    public void activate() {
        Services.securityService(this);
    }

    @Deactivate
    public void deactivate() {
        Services.securityService(null);
    }

    @Override
    public Password passwordFromEncryptedString(String encrypted) {
        return new com.energyict.mdc.common.Password(encrypted);
    }

}