package com.energyict.mdc.protocol.api.impl;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-16 (09:39)
 */
public class ProtocolApiModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(PropertySpecService.class);
        requireBinding(NlsService.class);

        bind(HexService.class).to(HexServiceImpl.class).in(Scopes.SINGLETON);
    }

}