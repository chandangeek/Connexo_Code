package com.elster.jupiter.http.whiteboard.impl.token;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.users.blacklist.BlackListModule;
import com.elster.jupiter.http.whiteboard.TokenModule;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.blacklist.BlackListTokenService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;

public class TokenServiceBootstrapModule {

    private ProgrammableClock clock = new ProgrammableClock(ZoneId.systemDefault(), Instant.now());

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private Injector injector;

    private TransactionService transactionService;

    private UserService userService;

    private TokenService tokenService;

    private BlackListTokenService blackListTokenService = mock(BlackListTokenService.class);

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    public void initializeDatabase() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new H2OrmModule(),
                new UtilModule(clock),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new UserModule(),
                new TokenModule(),
                new DataVaultModule(),
                new BlackListModule()
        );

        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            userService = injector.getInstance(UserService.class);
            tokenService = injector.getInstance(TokenService.class);
            return null;
        });
    }

    public void cleanUpDatabase() {
        inMemoryBootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public UserService getUserService() {
        return userService;
    }

    public BlackListTokenService getBlackListTokenService() {
        return blackListTokenService;
    }

    public TokenService getTokenService() {
        return tokenService;
    }
}
