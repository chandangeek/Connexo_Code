/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.users.UserDirectory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    private static final String DESCRIPTION = "description";
    private static final String AUTH_NAME = "authName";
    private static final boolean printSql = true;
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

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
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new UserModule(),
        			//new EventsModule(),
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(printSql),
        			new NlsModule(),
                    new DataVaultModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
        	injector.getInstance(UserService.class);
        	ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCreateUser() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            UserDirectory userDirectory = userService.findDefaultUserDirectory();
            User user = userDirectory.newUser(AUTH_NAME, DESCRIPTION, false, true);
            user.update();

            assertThat(user.getName()).isEqualTo(AUTH_NAME);
            assertThat(user.getDescription()).isEqualTo(DESCRIPTION);
            // skip ctx.commit()
        }

    }

    @Test
    public void testCreateUserPersists() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            UserDirectory userDirectory = userService.findDefaultUserDirectory();
            User user = userDirectory.newUser(AUTH_NAME, DESCRIPTION, false, true);
            user.update();

            assertThat(userService.findUser(AUTH_NAME).isPresent()).isTrue();
            // skip ctx.commit()
    	}
    }

    @Test
    public void testLockUserOkCase() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            UserDirectory userDirectory = userService.findDefaultUserDirectory();
            User user = userDirectory.newUser(AUTH_NAME, DESCRIPTION, false, true);
            user.update();

            Optional<User> lockedUser = userService.findAndLockUserByIdAndVersion(user.getId(), user.getVersion());
            assertThat(lockedUser.isPresent()).isTrue();
        }
    }

    @Test
    public void testLockUserAlreadyModified() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            UserDirectory userDirectory = userService.findDefaultUserDirectory();
            User user = userDirectory.newUser(AUTH_NAME, DESCRIPTION, false, true);
            user.update();

            Optional<User> lockedUser = userService.findAndLockUserByIdAndVersion(user.getId(), user.getVersion() + 1);
            assertThat(lockedUser.isPresent()).isFalse();
        }
    }

    @Test
    public void testLockGroupOkCase() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            Group group = userService.createGroup("name", "description");
            group.update();

            Optional<Group> lockedGroup = userService.findAndLockGroupByIdAndVersion(group.getId(), group.getVersion());
            assertThat(lockedGroup.isPresent()).isTrue();
        }
    }

    @Test
    public void testLockWorkGroupOkCase() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            WorkGroup workGroup = userService.createWorkGroup("name", "description");
            workGroup.update();

            Optional<WorkGroup> lockedWorkGroup = userService.findAndLockWorkGroupByIdAndVersion(workGroup.getId(), workGroup.getVersion());
            assertThat(lockedWorkGroup.isPresent()).isTrue();
        }
    }

    @Test
    public void testLockGroupAlreadyModified() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            Group group = userService.createGroup("name", "description");
            group.update();

            Optional<Group> lockedGroup = userService.findAndLockGroupByIdAndVersion(group.getId(), group.getVersion() + 1);
            assertThat(lockedGroup.isPresent()).isFalse();
        }
    }

    @Test
    public void testLockWorkGroupAlreadyModified() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            WorkGroup workGroup = userService.createWorkGroup("name", "description");
            workGroup.update();
            Optional<WorkGroup> lockedWorkGroup = userService.findAndLockWorkGroupByIdAndVersion(workGroup.getId(), workGroup.getVersion() + 1);
            assertThat(lockedWorkGroup.isPresent()).isFalse();
        }
    }

    @Test
    public void testAddUsersToWorkGroup() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            WorkGroup workGroup = userService.createWorkGroup("name", "description");
            workGroup.update();
            UserDirectory userDirectory = userService.findDefaultUserDirectory();
            User user = userDirectory.newUser(AUTH_NAME, DESCRIPTION, false, true);
            user.update();
            workGroup.grant(user);
            Optional<WorkGroup> lockedWorkGroup = userService.findAndLockWorkGroupByIdAndVersion(workGroup.getId(), workGroup.getVersion());
            assertThat(lockedWorkGroup.isPresent()).isTrue();
            assertThat(lockedWorkGroup.get().getUsersInWorkGroup()).isEqualTo(Collections.singletonList(user));
        }
    }

    @Test
    public void testRemoveUsersFromWorkGroup() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            WorkGroup workGroup = userService.createWorkGroup("name", "description");
            UserDirectory userDirectory = userService.findDefaultUserDirectory();
            User user = userDirectory.newUser(AUTH_NAME, DESCRIPTION, false, true);
            user.update();
            workGroup.grant(user);
            workGroup.update();
            Optional<WorkGroup> lockedWorkGroup = userService.findAndLockWorkGroupByIdAndVersion(workGroup.getId(), workGroup.getVersion());
            assertThat(lockedWorkGroup.isPresent()).isTrue();
            assertThat(lockedWorkGroup.get().getUsersInWorkGroup()).isEqualTo(Collections.singletonList(user));
            assertThat(lockedWorkGroup.get().getUsersInWorkGroup().get(0).getName()).isEqualTo(AUTH_NAME);
            workGroup.revoke(user);
            workGroup.update();
            lockedWorkGroup = userService.findAndLockWorkGroupByIdAndVersion(workGroup.getId(), workGroup.getVersion());
            assertThat(lockedWorkGroup.isPresent()).isTrue();
            assertThat(lockedWorkGroup.get().getUsersInWorkGroup()).isEqualTo(Collections.emptyList());
        }
    }

    @Test
    public void testDeleteWorkGroup() {
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            WorkGroup workGroup = userService.createWorkGroup("name", "description");
            UserDirectory userDirectory = userService.findDefaultUserDirectory();
            User user = userDirectory.newUser(AUTH_NAME, DESCRIPTION, false, true);
            user.update();
            workGroup.grant(user);
            workGroup.update();
            workGroup.delete();
            assertThat(userService.getWorkGroups()).isEqualTo(Collections.emptyList());
        }
    }
}
