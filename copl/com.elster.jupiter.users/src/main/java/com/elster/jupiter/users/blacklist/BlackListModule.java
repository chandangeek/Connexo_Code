package com.elster.jupiter.users.blacklist;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.blacklist.impl.BlackListTokenServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class BlackListModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(QueryService.class);
        requireBinding(NlsService.class);
        requireBinding(UpgradeService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(OrmService.class);

        bind(BlackListTokenService.class).to(BlackListTokenServiceImpl.class).in(Scopes.SINGLETON);
    }
}
