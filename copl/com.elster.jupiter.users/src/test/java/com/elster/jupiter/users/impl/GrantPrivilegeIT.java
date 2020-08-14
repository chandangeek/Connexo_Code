/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.Expects.expect;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GrantPrivilegeIT {
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static DataModel dataModel;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() throws SQLException {
        dataModel = mock(DataModel.class);
        when(dataModel.getInstance(ResourceImpl.class)).thenAnswer(invocation -> new ResourceImpl(dataModel, getUserService()));

        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new H2OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new UserModule(),
                new NlsModule(),
                new DataVaultModule());
        injector.getInstance(TransactionService.class).run(() -> injector.getInstance(UserService.class));
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private static UserService getUserService() {
        return injector.getInstance(UserService.class);
    }

    @Test
    public void testGrantPrivilegeNotAllowedForCategory() {
        try (TransactionContext context = injector.getInstance(TransactionService.class).getContext()) {
            UserService userService = getUserService();

            User naughty = userService.createUser("naughty", "");

            PrivilegeCategory special = userService.createPrivilegeCategory("Special");

            PrivilegeCategory defaultPrivilegeCategory = userService.getDefaultPrivilegeCategory();
            Resource resource = userService.buildResource()
                    .component("TTT")
                    .name("main")
                    .description("")
                    .addPrivilege("createBlackHole")
                    .in(special)
                    .add()
                    .addPrivilege("viewBlackHole")
                    .in(defaultPrivilegeCategory)
                    .add()
                    .addGrantPrivilege("grantBlackHoleCreator")
                    .in(special)
                    .forCategory(special)
                    .add()
                    .create();
            GrantPrivilege grantPrivilege = decorate(resource.getPrivileges()
                    .stream())
                    .filterSubType(GrantPrivilege.class)
                    .findAny()
                    .get();
            Privilege createPrivilege = resource.getPrivileges()
                    .stream()
                    .filter(not(GrantPrivilege.class::isInstance))
                    .filter(privilege -> privilege.getCategory().equals(special))
                    .findAny()
                    .get();
            Privilege viewPrivilege = resource.getPrivileges()
                    .stream()
                    .filter(privilege -> privilege.getCategory().equals(defaultPrivilegeCategory))
                    .findAny()
                    .get();

            Group admins = userService.findGroup(UserService.DEFAULT_ADMIN_ROLE).get();
            naughty.join(admins);


            Group specials = userService.createGroup("Specials", "");
            specials.grant("TTT", grantPrivilege);

            ThreadPrincipalService threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);

            threadPrincipalService.set(naughty);
            expect(() -> admins.grant("TTT", createPrivilege))
                    .toThrow(ForbiddenException.class);
            threadPrincipalService.clear();

            threadPrincipalService.set(() -> "test");
            admins.grant("TTT", grantPrivilege);

            Group adminsReloaded = userService.findGroup(admins.getName()).get();

            threadPrincipalService.set(naughty);
            adminsReloaded.grant("TTT", createPrivilege); // should not throw an exception now
            threadPrincipalService.clear();
        }
    }
}
