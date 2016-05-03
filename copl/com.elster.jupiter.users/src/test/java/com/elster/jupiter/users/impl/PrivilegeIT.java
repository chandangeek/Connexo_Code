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
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PrivilegeIT extends EqualsContractTest {
    private Privilege privilege;
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private DataModel dataModel;
    @Mock
    private Resource resource;

    private static final String NAME = "Privilege1";
    private static final String OTHER_NAME = "Privilege2";

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
                new UserModule(),
                new InMemoryMessagingModule(),
                new NlsModule(),
                new DataVaultModule());
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(UserService.class);
            return null;
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Override
    protected Object getInstanceA() {
        if (privilege == null) {
            privilege = PrivilegeImpl.from(dataModel, NAME, resource);
        }
        return privilege;
    }

    @Override
    protected Object getInstanceEqualToA() {
        Privilege privilegeB = PrivilegeImpl.from(dataModel, NAME, resource);
        return privilegeB;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        PrivilegeImpl privilege = PrivilegeImpl.from(dataModel, OTHER_NAME, resource);
        return Collections.singletonList(privilege);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testCreateResource() {
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                UserService userService = injector.getInstance(UserService.class);

                userService.saveResourceWithPrivileges("USR", "User", "Test user resource", new String[]{"Test privilege"});



                Optional<Resource> found = userService.findResource("User");

                assertThat(found.isPresent());
                assertThat(found.get()).isInstanceOf(Resource.class);
                assertThat(found.get().getComponentName()).isEqualTo("USR");
                assertThat(found.get().getName()).isEqualTo("User");
                assertThat(found.get().getDescription()).isEqualTo("Test user resource");
                assertThat(found.get().getPrivileges()).hasSize(1);
                assertThat(found.get().getPrivileges().get(0)).isInstanceOf(Privilege.class);
                assertThat(found.get().getPrivileges().get(0).getName()).isEqualTo("Test privilege");
            }
        });
    }


}
