package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BpmModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(MessageService.class);
        requireBinding(JsonService.class);
        requireBinding(NlsService.class);
        requireBinding(UserService.class);

        bind(BpmService.class).to(BpmServiceImpl.class).in(Scopes.SINGLETON);
    }
}
