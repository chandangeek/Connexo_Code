package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.impl.RelationServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-19 (14:38)
 */
public class MdcDynamicModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(com.elster.jupiter.properties.PropertySpecService.class);

        bind(PropertySpecService.class).to(PropertySpecServiceImpl.class).in(Scopes.SINGLETON);
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
    }

}