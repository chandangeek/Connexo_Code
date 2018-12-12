/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;

import java.security.Principal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class LockTest {

    private Injector injector;
    //private OracleBootstrapModule bootstrapModule = new OracleBootstrapModule();
    private InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();

    @Mock
    private Principal principal;
    @Mock
    private BundleContext bundleContext;

    @Before
    public void setUp() {
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcurl")).thenReturn("jdbc:oracle:thin:@localhost:1521:orcl");
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcuser")).thenReturn("kore");
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcpassword")).thenReturn("kore");
        Module module = new AbstractModule() {
            @Override
            public void configure() {
                this.bind(BundleContext.class).toInstance(bundleContext);
            }
        };
        injector = Guice.createInjector(
                module,
                bootstrapModule,
                new UtilModule(),
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(false),
                new OrmModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(OrmService.class);
            ctx.commit();
        }
    }

    @After
    public void tearDown() throws SQLException {
        bootstrapModule.deactivate();
    }

    @Test
    public void testLockNoWait() throws InterruptedException {
        OrmService ormService = injector.getInstance(OrmService.class);
        final DataModel dataModel = getLockVersionDataModel(ormService);
        final TransactionService transactionService = injector.getInstance(TransactionService.class);
        dataModel.install(true, false);
        dataModel.register(bootstrapModule,
                new PubSubModule(),
                new TransactionModule(false));
        LockVersion version = transactionService.builder().principal(() -> "test").execute(() -> {
            LockVersion lockVersion = new LockVersionImpl();
            dataModel.persist(lockVersion);
            return lockVersion;
        });
        LockVersion version2 = transactionService.builder().principal(() -> "test").execute(() -> {
            LockVersion lockVersion = new LockVersionImpl();
            dataModel.persist(lockVersion);
            return lockVersion;
        });

        try (TransactionContext ctx = transactionService.getContext()) {
            assertThat(dataModel.mapper(LockVersion.class).lockNoWait(version.getId()).isPresent()).isTrue();
            Thread thread = new Thread(() -> {
                try (TransactionContext ctx1 = transactionService.getContext()) {
                    assertThat(dataModel.mapper(LockVersion.class).lockNoWait(version.getId()).isPresent()).isFalse();
                    assertThat(dataModel.mapper(LockVersion.class).lockNoWait(version2.getId()).isPresent()).isTrue();
                }
            });
            thread.start();
            thread.join();
        }
    }

    @Test
    public void testLockVersion() throws InterruptedException, ExecutionException {
        OrmService ormService = injector.getInstance(OrmService.class);
        DataModel dataModel = getLockVersionDataModel(ormService);
        dataModel.install(true, false);
        dataModel.register(bootstrapModule,
                new PubSubModule(),
                new TransactionModule(false));
        LockVersion version = injector.getInstance(TransactionService.class).builder().principal(() -> "test").execute(() -> {
            LockVersion lockVersion = new LockVersionImpl();
            dataModel.persist(lockVersion);
            return lockVersion;
        });


        FutureTask<Optional<LockVersion>> future = new FutureTask<>(() -> {
            try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
                return dataModel.mapper(LockVersion.class).lockObjectIfVersion(version.getVersion(), version.getId());
            }
        });
        injector.getInstance(TransactionService.class).builder().principal(() -> "test").run(() -> {
            Optional<LockVersion> lockVersion = dataModel.mapper(LockVersion.class).lockObjectIfVersion(version.getVersion(), version.getId());
            assertThat(lockVersion.isPresent()).isTrue();
            Thread lockedThread = new Thread(future);

            lockedThread.start();
            while (lockedThread.getState() != Thread.State.TIMED_WAITING) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            dataModel.touch(lockVersion.get());
        });
        Optional<LockVersion> lockVersion = future.get();
        assertThat(lockVersion).isEmpty();
    }

    private DataModel getLockVersionDataModel(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel("TST", "Test stuff");
        Table<LockVersion> lockVersionTable = dataModel.addTable("LOCKVERSION", LockVersion.class);
        lockVersionTable.map(LockVersionImpl.class);
        Column idColumn = lockVersionTable.addAutoIdColumn();
        lockVersionTable.addAuditColumns();
        lockVersionTable.primaryKey("PK_LOCKVERSION").on(idColumn).add();
        return dataModel;
    }

    @Test
    public void testLockVersionOnNoUpdate() throws InterruptedException, ExecutionException {
        OrmService ormService = injector.getInstance(OrmService.class);
        DataModel dataModel = getLockVersionDataModel(ormService);
        dataModel.install(true, false);
        dataModel.register(bootstrapModule,
                new PubSubModule(),
                new TransactionModule(false));
        LockVersion version = injector.getInstance(TransactionService.class).builder().principal(() -> "test").execute(() -> {
            LockVersion lockVersion = new LockVersionImpl();
            dataModel.persist(lockVersion);
            return lockVersion;
        });


        FutureTask<Optional<LockVersion>> future = new FutureTask<>(() -> {
            try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
                return dataModel.mapper(LockVersion.class).lockObjectIfVersion(version.getVersion(), version.getId());
            }
        });
        try {
            injector.getInstance(TransactionService.class).builder().principal(() -> "test").run(() -> {
                Optional<LockVersion> lockVersion = dataModel.mapper(LockVersion.class).lockObjectIfVersion(version.getVersion(), version.getId());
                assertThat(lockVersion.isPresent()).isTrue();
                Thread lockedThread = new Thread(future);

                lockedThread.start();
                while (lockedThread.getState() != Thread.State.TIMED_WAITING) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                throw new RuntimeException();
            });
        } catch (RuntimeException e) {
            // expected
        }
        Optional<LockVersion> lockVersion = future.get();
        assertThat(lockVersion).isPresent();
    }

    private interface LockVersion {
        long getId();

        long getVersion();
    }

    private static class LockVersionImpl implements LockVersion {
        private long id;
        private long version;
        private String userName;
        private Instant createTime;
        private Instant modTime;


        public long getId() {
            return id;
        }

        @Override
        public long getVersion() {
            return version;
        }
    }

}
