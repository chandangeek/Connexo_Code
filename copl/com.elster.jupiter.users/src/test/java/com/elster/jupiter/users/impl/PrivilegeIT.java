/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.util.streams.Predicates.on;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    @Mock
    private PrivilegeCategory privilegeCategory;

    private static final String NAME = "Privilege1";
    private static final String OTHER_NAME = "Privilege2";

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
        when(dataModel.getInstance(ResourceImpl.class)).thenAnswer(invocation -> new ResourceImpl(dataModel, getUserService()));

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
            privilege = PrivilegeImpl.from(dataModel, NAME, resource, privilegeCategory);
        }
        return privilege;
    }

    @Override
    protected Object getInstanceEqualToA() {
        Privilege privilegeB = PrivilegeImpl.from(dataModel, NAME, resource, privilegeCategory);
        return privilegeB;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        PrivilegeImpl privilege = PrivilegeImpl.from(dataModel, OTHER_NAME, resource, privilegeCategory);
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
        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                UserService userService = getUserService();

                userService.saveResourceWithPrivileges("USR", "User", "Test user resource", new String[]{"Test privilege"});

                Optional<Resource> found = userService.findResource("User");

                assertThat(found.isPresent());
                Resource resource = found.get();
                assertThat(resource.getComponentName()).isEqualTo("USR");
                assertThat(resource.getName()).isEqualTo("User");
                assertThat(resource.getDescription()).isEqualTo("Test user resource");
                assertThat(resource.getPrivileges()).hasSize(1);
                Privilege privilege = resource.getPrivileges().get(0);
                assertThat(privilege).isInstanceOf(Privilege.class);
                assertThat(privilege.getName()).isEqualTo("Test privilege");
                assertThat(privilege.getCategory().getName()).isEqualTo("Default");
            }
        });
    }

    @Test
    public void testCreateResourceWithGrantPrivilege() {
        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                UserService userService = getUserService();

                PrivilegeCategory specialCategory = userService.createPrivilegeCategory("special");

                PrivilegeCategory defaultPrivilegeCategory = userService.getDefaultPrivilegeCategory();

                ResourceDefinition resourceDefinition = userService.createModuleResourceWithPrivileges("TTT", "Test", "test", Collections
                        .emptyList());

                userService.addModulePrivileges(new PrivilegesProvider() {
                    @Override
                    public String getModuleName() {
                        return "TTT";
                    }

                    @Override
                    public List<ResourceDefinition> getModuleResources() {
                        return Arrays.asList(resourceDefinition);
                    }
                });

                Resource resource = userService.findResource("Test").get();

                GrantPrivilege grantPrivilege = resource.createGrantPrivilege("normal.grant")
                        .forCategory(defaultPrivilegeCategory)
                        .forCategory(specialCategory)
                        .create();

            }
        });

        Optional<Resource> foundResource = getUserService().findResource("Test");
        assertThat(foundResource).isPresent();

        Resource resource = foundResource.get();

        Optional<Privilege> foundPrivilege = resource.getPrivileges()
                .stream()
                .filter(on(Privilege::getName).test("normal.grant"::equals))
                .findAny();

        assertThat(foundPrivilege).isPresent()
                .containsInstanceOf(GrantPrivilege.class);

        GrantPrivilege grantPrivilege = (GrantPrivilege) foundPrivilege.get();

        assertThat(grantPrivilege.grantableCategories()).containsOnly(
                getUserService().getDefaultPrivilegeCategory(),
                getUserService().findPrivilegeCategory("special").get()
        );

        assertThat(grantPrivilege.canGrant(grantPrivilege));

    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private UserService getUserService() {
        return injector.getInstance(UserService.class);
    }

    @Test
    public void testCreatePrivilegeCategory() {
        UserService userService = getUserService();
        try (TransactionContext context = getTransactionService().getContext()) {
            PrivilegeCategory category = userService.createPrivilegeCategory("TEST");
            context.commit();
        }

        Optional<PrivilegeCategory> found = userService.findPrivilegeCategory("TEST");
        assertThat(found).isPresent();

        PrivilegeCategory category = found.get();
        assertThat(category.getName()).isEqualTo("TEST");
    }

}
