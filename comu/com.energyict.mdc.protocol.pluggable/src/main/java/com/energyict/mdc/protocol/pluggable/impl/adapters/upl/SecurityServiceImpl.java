package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.security.SecurityService;

import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link SecurityService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (13:09)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.security", service = {SecurityService.class})
@SuppressWarnings("unused")
public class SecurityServiceImpl implements SecurityService {

    @Override
    public Password passwordFromEncryptedString(String encrypted) {
        return new com.energyict.mdc.common.Password(encrypted);
    }

}