/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeServiceImplIT {

    private FileSystem fileSystem;

    private static class Installer implements FullInstaller {
        public static int instances = 0;
        public static int invocations = 0;

        public Installer() {
            instances++;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
            invocations++;
        }
    }

    private static class UpgraderV3 implements Upgrader {
        public static int instances = 0;
        public static int invocations = 0;

        public UpgraderV3() {
            instances++;
        }

        @Override
        public void migrate(DataModelUpgrader dataModelUpgrader) {
            invocations++;
        }
    }

    private static class UpgraderV2 implements Upgrader {
        public static int instances = 0;
        public static int invocations = 0;

        public UpgraderV2() {
            instances++;
        }

        @Override
        public void migrate(DataModelUpgrader dataModelUpgrader) {
            invocations++;
        }
    }

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
//            bind(SchemaInfoProvider.class).to(H2SchemaInfo.class);
//            bind(ServiceCallTypeOneCustomPropertySet.class).to(ServiceCallTypeOneCustomPropertySet.class);
        }
    }

    private static final String IMPORTER_NAME = "someImporter";

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private Injector injector;

    private TransactionService transactionService;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LogService logService;
    @Mock
    private MessageInterpolator messageInterpolator;

    private Clock clock;
    private OrmService ormService;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectory(fileSystem.getPath("./logs"));
        Files.createFile(fileSystem.getPath("./logs/upgrade.log"));


        clock = new ProgrammableClock(ZoneId.of("UTC"), now);
        when(bundleContext.getProperty("upgrade")).thenReturn("true");
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new OrmModule(),
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.run(() -> {
            ormService = injector.getInstance(OrmService.class);
            SchemaInfoProvider schemaInfoProvider = injector.getInstance(SchemaInfoProvider.class);
            ((OrmServiceImpl) ormService).setSchemaInfoProvider(schemaInfoProvider);
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testUpgradeOverTime() {
        BootstrapService bootstrapService = injector.getInstance(BootstrapService.class);
        UpgradeServiceImpl upgradeService = new UpgradeServiceImpl(bootstrapService, transactionService, ormService, bundleContext, fileSystem);

        DataModel dataModel = ormService.newDataModel("TST", "");
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });

        assertThat(upgradeService.isInstalled(identifier("Example", "TST"), version(1, 0))).isFalse();

        // initial install, Only the installer should be invoked

        upgradeService.register(identifier("Example", "TST"), dataModel, Installer.class, ImmutableMap.of(version(2, 0), UpgraderV2.class));

        assertThat(Installer.instances).isEqualTo(1);
        assertThat(UpgraderV2.instances).isEqualTo(1);
        assertThat(Installer.invocations).isEqualTo(1);
        assertThat(UpgraderV2.invocations).isEqualTo(0);

        assertThat(upgradeService.isInstalled(identifier("Example", "TST"), version(1, 0))).isTrue();

        // assume a normal system restart (no Installer /Upgrader should be instantiated

        when(bundleContext.getProperty("upgrade")).thenReturn(null);
        upgradeService = new UpgradeServiceImpl(bootstrapService, transactionService, ormService, bundleContext, fileSystem);
        upgradeService.register(identifier("Example", "TST"), dataModel, Installer.class, ImmutableMap.of(version(2, 0), UpgraderV2.class));

        assertThat(Installer.instances).isEqualTo(1);
        assertThat(UpgraderV2.instances).isEqualTo(1);
        assertThat(Installer.invocations).isEqualTo(1);
        assertThat(UpgraderV2.invocations).isEqualTo(0);

        assertThat(upgradeService.isInstalled(identifier("Example", "TST"), version(1, 0))).isTrue();

        // assume an upgrade (only the Upgrader should run)

        when(bundleContext.getProperty("upgrade")).thenReturn("true");
        upgradeService = new UpgradeServiceImpl(bootstrapService, transactionService, ormService, bundleContext, fileSystem);
        upgradeService.register(identifier("Example", "TST"), dataModel, Installer.class, ImmutableMap.of(version(2, 0), UpgraderV2.class, version(3, 0), UpgraderV3.class));

        assertThat(Installer.instances).isEqualTo(2);
        assertThat(UpgraderV2.instances).isEqualTo(2);
        assertThat(UpgraderV3.instances).isEqualTo(1);
        assertThat(Installer.invocations).isEqualTo(1);
        assertThat(UpgraderV2.invocations).isEqualTo(0);
        assertThat(UpgraderV3.invocations).isEqualTo(1);

        assertThat(upgradeService.isInstalled(identifier("Example", "TST"), version(1, 0))).isTrue();
    }

    // TODO test for exception when upgrade is needed but not set to upgrade
}
