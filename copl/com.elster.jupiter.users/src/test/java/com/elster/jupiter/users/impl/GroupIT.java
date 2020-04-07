/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
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
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class GroupIT extends EqualsContractTest {

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private Injector injector;

    private Group group;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private Publisher publisher;

    private static final String TEST_GROUP_NAME = "groupName";
    private static final String TEST_GROUP_DESCRIPTION = "groupName";
    private static final String TEST_GROUP_EXTERNAL_ID = UUID.randomUUID().toString();
    private static final long ID = 0;
    private static final long OTHER_ID = 1;

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
        super.equalsContractSetUp();
        try {
            injector = Guice.createInjector(
                    new GroupIT.MockModule(),
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
        injector.getInstance(TransactionService.class).execute((Transaction<Void>) () -> {
            injector.getInstance(UserService.class);
            return null;
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private void setId(Object entity, long id) {

        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (group == null) {
            group = new GroupImpl(mock(QueryService.class), dataModel, userService, threadPrincipalService, thesaurus, publisher).init(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION, TEST_GROUP_EXTERNAL_ID);
            setId(group, ID);
        }
        return group;
    }

    @Override
    protected Object getInstanceEqualToA() {
        Group groupB = new GroupImpl(mock(QueryService.class), dataModel, userService, threadPrincipalService, thesaurus, publisher).init(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION, TEST_GROUP_EXTERNAL_ID);
        setId(groupB, ID);
        return groupB;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        Group groupC = new GroupImpl(mock(QueryService.class), dataModel, userService, threadPrincipalService, thesaurus, publisher).init(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION, TEST_GROUP_EXTERNAL_ID);
        setId(groupC, OTHER_ID);
        return Collections.singletonList(groupC);
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
    public void shouldCreateDefaultGroup() {
        injector.getInstance(TransactionService.class).execute(() -> {
            final UserService userService = injector.getInstance(UserService.class);

            final Group expectedGroup = userService.createGroup(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION);
            expectedGroup.update();

            final Optional<Group> actualGroupOptional = userService.findGroup(TEST_GROUP_NAME);

            assertThat(actualGroupOptional.get()).isNotNull();

            final Group actualGroup = actualGroupOptional.get();
            assertThat(actualGroup).isEqualToComparingOnlyGivenFields(expectedGroup,
                    "id",
                    "externalId",
                    "name",
                    "description",
                    "createTime",
                    "modTime");

            return null;
        });
    }

    @Test
    public void shouldCreateSCIMGroup() {
        injector.getInstance(TransactionService.class).execute(() -> {
            final UserService userService = injector.getInstance(UserService.class);

            final String RANDOM_EXTERNAL_ID = UUID.randomUUID().toString();
            final Group expectedSCIMGroup = userService.createSCIMGroup(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION, RANDOM_EXTERNAL_ID);
            expectedSCIMGroup.update();

            final Optional<Group> actualSCIMGroupOptional = userService.findGroup(TEST_GROUP_NAME);

            assertThat(actualSCIMGroupOptional.get()).isNotNull();

            final Group actualSCIMGroup = actualSCIMGroupOptional.get();
            assertThat(actualSCIMGroup).isEqualToComparingOnlyGivenFields(expectedSCIMGroup,
                    "id",
                    "externalId",
                    "name",
                    "description",
                    "createTime",
                    "modTime");

            return null;
        });
    }

    @Test
    public void shouldFindGroupByExternalId() {
        injector.getInstance(TransactionService.class).execute(() -> {
            final UserService userService = injector.getInstance(UserService.class);

            final Group expectedSCIMGroup = userService.createSCIMGroup(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION, TEST_GROUP_EXTERNAL_ID);
            expectedSCIMGroup.update();

            final Optional<Group> actualSCIMGroupOptional = userService.findGroupByExternalId(TEST_GROUP_EXTERNAL_ID);

            assertThat(actualSCIMGroupOptional.get()).isNotNull();

            return null;
        });
    }

}

