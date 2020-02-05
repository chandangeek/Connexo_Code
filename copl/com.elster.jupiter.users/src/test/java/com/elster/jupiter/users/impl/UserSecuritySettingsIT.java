package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserSecuritySettings;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class UserSecuritySettingsIT extends EqualsContractTest {


    private UserSecuritySettings userSecuritySettings;
    @Mock
    private DataModel dataModel;
    private Injector injector;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private final static Boolean LOCK_ACCOUNT_ACTIVE = true;
    private final static long ID = 0;
    private static final long OTHER_ID = 1;
    private final static int FAILED_LOGIN_ATTEMPTS = 2;
    private final static int LOCK_OUT_MINUTES = 5;

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

    }

    @Override
    protected Object getInstanceA() {
        if (userSecuritySettings == null) {
            userSecuritySettings = new UserSecuritySettingsImpl(dataModel).init(LOCK_ACCOUNT_ACTIVE, FAILED_LOGIN_ATTEMPTS, LOCK_OUT_MINUTES);
            setId(userSecuritySettings, ID);
        }
        return userSecuritySettings;
    }

    @Override
    protected Object getInstanceEqualToA() {
        UserSecuritySettings userSecuritySettingsB = new UserSecuritySettingsImpl(dataModel).init(LOCK_ACCOUNT_ACTIVE, FAILED_LOGIN_ATTEMPTS, LOCK_OUT_MINUTES);
        setId(userSecuritySettingsB, ID);
        return userSecuritySettingsB;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        UserSecuritySettings userSecuritySettingsC = new UserSecuritySettingsImpl(dataModel).init(LOCK_ACCOUNT_ACTIVE, FAILED_LOGIN_ATTEMPTS, LOCK_OUT_MINUTES);
        setId(userSecuritySettingsC, OTHER_ID);
        return Collections.singletonList(userSecuritySettingsC);
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
    public void testCreateUserSecuritySettings() {
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                UserService userService = injector.getInstance(UserService.class);
                UserSecuritySettings userSecuritySettings = userService.findOrCreateUserSecuritySettings(true, 3, 10);
                userSecuritySettings.save();
                //test get user security settings
                Optional<UserSecuritySettings> found = userService.getLockingAccountSettings();
                assertThat(found.get()).isEqualTo(userSecuritySettings);
                assertThat(found.get().getLockOutMinutes()).isEqualTo(10);
                assertThat(found.get().getFailedLoginAttempts()).isEqualTo(3);
                assertThat(found.get().isLockAccountActive()).isTrue();
            }
        });
    }
}