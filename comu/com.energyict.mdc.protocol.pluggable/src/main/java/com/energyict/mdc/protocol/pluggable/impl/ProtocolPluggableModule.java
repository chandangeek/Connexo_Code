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
    }

}