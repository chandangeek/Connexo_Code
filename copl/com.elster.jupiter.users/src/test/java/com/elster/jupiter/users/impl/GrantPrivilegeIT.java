package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.Expects.expect;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GrantPrivilegeIT {

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

    private UserService getUserService() {
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

            Group admins = userService.findGroup(UserService.USER_ADMIN_ROLE).get();
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
