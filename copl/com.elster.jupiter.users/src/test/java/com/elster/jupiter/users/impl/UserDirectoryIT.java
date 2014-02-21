package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;

import static org.assertj.guava.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UserDirectoryIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new UserModule());
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(UserService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersistenceInternalDirectory() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            UserDirectory internalDirectory = userService.createInternalDirectory("MyDomain");
            internalDirectory.setDefault(true);
            internalDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> found = userService.findUserDirectory("MyDomain");

        assertThat(found).isPresent();

        UserDirectory userDirectory = found.get();
        assertThat(userDirectory).isInstanceOf(InternalDirectoryImpl.class);
        assertThat(userDirectory.isDefault()).isTrue();
        assertThat(userDirectory.getDomain()).isEqualTo("MyDomain");
    }

    @Test
    public void testPersistenceActiveDirectory() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = userService.createActiveDirectory("MyDomain");
            activeDirectory.setDefault(true);
            activeDirectory.setDirectoryUser("MyUser");
            activeDirectory.setPassword("MyPassword");
            activeDirectory.setUrl("MyUrl");
            activeDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> found = userService.findUserDirectory("MyDomain");

        assertThat(found).isPresent();

        UserDirectory userDirectory = found.get();
        assertThat(userDirectory).isInstanceOf(ActiveDirectoryImpl.class);
        assertThat(userDirectory.isDefault()).isTrue();
        assertThat(userDirectory.getDomain()).isEqualTo("MyDomain");
        assertThat(((LdapUserDirectory)userDirectory).getDirectoryUser()).isEqualTo("MyUser");
        assertThat(((LdapUserDirectory)userDirectory).getPassword()).isEqualTo("MyPassword");
        assertThat(((LdapUserDirectory)userDirectory).getUrl()).isEqualTo("MyUrl");
    }

    @Test
    public void testPersistenceApacheDirectory() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = userService.createApacheDirectory("MyDomain");
            activeDirectory.setDefault(true);
            activeDirectory.setDirectoryUser("MyUser");
            activeDirectory.setPassword("MyPassword");
            activeDirectory.setUrl("MyUrl");
            activeDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> found = userService.findUserDirectory("MyDomain");

        assertThat(found).isPresent();

        UserDirectory userDirectory = found.get();
        assertThat(userDirectory).isInstanceOf(ApacheDirectoryImpl.class);
        assertThat(userDirectory.isDefault()).isTrue();
        assertThat(userDirectory.getDomain()).isEqualTo("MyDomain");
        assertThat(((LdapUserDirectory)userDirectory).getDirectoryUser()).isEqualTo("MyUser");
        assertThat(((LdapUserDirectory)userDirectory).getPassword()).isEqualTo("MyPassword");
        assertThat(((LdapUserDirectory)userDirectory).getUrl()).isEqualTo("MyUrl");
    }
}
