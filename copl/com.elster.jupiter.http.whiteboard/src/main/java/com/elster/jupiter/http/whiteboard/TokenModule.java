package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.http.whiteboard.impl.token.DatabaseBasedTokenService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.blacklist.BlackListTokenService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class TokenModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(UserService.class);
        requireBinding(UpgradeService.class);
        requireBinding(BlackListTokenService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(OrmService.class);

        bind(TokenService.class).to(DatabaseBasedTokenService.class).in(Scopes.SINGLETON);
    }
}
