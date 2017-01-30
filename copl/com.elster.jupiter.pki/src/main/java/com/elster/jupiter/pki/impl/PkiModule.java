package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.PkiService;

import com.google.inject.AbstractModule;

/**
 * Created by bvn on 1/26/17.
 */
public class PkiModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);

        bind(PkiService.class).to(PkiServiceImpl.class);
    }


}
