package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class UserIT extends EqualsContractTest {

    //userName placeholder as equals will be tested against the PK id
    private static final String TEST_USER_NAME = "userName";
    private static final String TEST_USER_DESCRIPTION = "userDescription";
    private static final boolean ALLOW_PWD_CHANGE = true;
    private static final boolean STATUS_ACTIVE = true;
    private static final long ID = 0;
    private static final long OTHER_ID = 1;
    @Mock
    UserDirectory userDirectory;
    private Injector injector;
    private User user;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private DataModel dataModel;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new UserModule(),
                    new NlsModule(),
                    new PubSubModule(),
                    new DataVaultModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    @Override
    protected Object getInstanceA() {
        if (user == null) {
            user = new UserImpl(dataModel, getPublisher()).init(userDirectory, TEST_USER_NAME, TEST_USER_DESCRIPTION, ALLOW_PWD_CHANGE, STATUS_ACTIVE);
            setId(user, ID);
        }
        return user;
    }

    private Publisher getPublisher() {
        return injector.getInstance(Publisher.class);
    }

    @Override
    protected Object getInstanceEqualToA() {
        User userB = new UserImpl(dataModel, getPublisher()).init(userDirectory, TEST_USER_NAME, TEST_USER_DESCRIPTION, ALLOW_PWD_CHANGE, STATUS_ACTIVE);
        setId(userB, ID);
        return userB;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        User userC = new UserImpl(dataModel, getPublisher()).init(userDirectory, TEST_USER_NAME, TEST_USER_DESCRIPTION, ALLOW_PWD_CHANGE, STATUS_ACTIVE);
        setId(userC, OTHER_ID);
        return Collections.singletonList(userC);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Test
    public void testCreateUser() {
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                UserService userService = injector.getInstance(UserService.class);

                UserDirectory userDirectory = userService.findDefaultUserDirectory();
                User user = userDirectory.newUser("authName", "description", false,true);
                user.setLocale(Locale.CANADA_FRENCH);
                user.setPassword("password");
                user.update();

                User user2 = userDirectory.newUser("authName2", "description2", false,true);
                user2.setLocale(Locale.CANADA_FRENCH);
                user2.setPassword("password2");
                user2.update();
                //test get user using authenticatin name
                Optional<User> found = userService.findUser("authName");
                assertThat(found.get()).isEqualTo(user);
                assertThat(found.get().getDescription()).isEqualTo("description");
                assertThat(found.get().check("password")).isTrue();
               assertThat(found.get().getLocale().get()).isEqualTo(Locale.CANADA_FRENCH);
                Optional<User> found2 = userService.findUser("authName2");
                assertThat(found2.get()).isNotEqualTo(user);
            }
        });
    }

}
