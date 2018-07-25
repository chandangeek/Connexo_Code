/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConverterImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLNlsServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLPropertySpecServiceImpl;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-19 (14:38)
 */
public class ProtocolPluggableModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(PluggableService.class);
        requireBinding(CustomPropertySetService.class);
        requireBinding(LicenseService.class);
        requireBinding(IssueService.class);
        requireBinding(DataVaultService.class);
        requireBinding(UserService.class);
        requireBinding(MeteringService.class);

        bind(ProtocolPluggableService.class).to(ProtocolPluggableServiceImpl.class).in(Scopes.SINGLETON);
        bind(PropertySpecService.class).to(UPLPropertySpecServiceImpl.class).in(Scopes.SINGLETON);
        bind(Converter.class).to(ConverterImpl.class).in(Scopes.SINGLETON);
        bind(NlsService.class).to(UPLNlsServiceImpl.class).in(Scopes.SINGLETON);
        //TODO: do hsm service binding
//        bind(HsmService.class).to(HsmProtocolServiceImpl.class).in(Scopes.SINGLETON);
    }

}