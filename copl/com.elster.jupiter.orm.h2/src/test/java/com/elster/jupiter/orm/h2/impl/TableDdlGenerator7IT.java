/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.h2.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.sql.SQLException;
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

@RunWith(MockitoJUnitRunner.class)
public class TableDdlGenerator7IT {

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
        }
    }

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
    public void setUp() {
        clock = new ProgrammableClock(ZoneId.of("UTC"), now);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new H2OrmModule(),
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

    public static class Device {
        public long id;
    }

    public static class DeviceMessage {
        public long id;
        Reference<Device> device = ValueReference.absent();
    }


    @Test
    public void testUpgradeWithNewReferencingColumn() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        Table<Device> deviceTable = dataModel.addTable("DEVICE", Device.class);
        deviceTable.map(Device.class);
        Column houseIdColumn = deviceTable.column("ID")
                .map("id")
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .notNull()
                .add();
        deviceTable.primaryKey("PK_DEVICE_ID")
                .on(houseIdColumn)
                .add();


        Table<DeviceMessage> deviceMessageTable = dataModel.addTable("DEVICE_MESSAGE", DeviceMessage.class);
        deviceMessageTable.map(DeviceMessage.class);
        Column personIdColumn = deviceMessageTable.column("ID")
                .map("id")
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .notNull()
                .add();
        Column deviceColumn1_0 = deviceMessageTable.column("DEVICE_ID").number().upTo(version(2, 0)).add();
        Column deviceColumn = deviceMessageTable.column("DEVICE_ID").number().since(version(2, 0)).add();
        deviceMessageTable.primaryKey("PK_MESSAGE_ID")
                .on(personIdColumn)
                .add();
        deviceMessageTable.foreignKey("FK_MESSAGE_DEVICE")
                .on(deviceColumn)
                .references(Device.class)
                .map("device")
                .add();


        dataModel.register();
        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, Version.latest());

        // now upgrade :

        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(2, 0));

    }
}