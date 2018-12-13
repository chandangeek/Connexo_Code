/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryPersistence;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
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
import java.sql.Connection;
import java.sql.ResultSet;
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
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TableDdlGenerator6IT {

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

    public static class Device {
        public long id;
        public Reference<PassiveCalendar> passiveCalendar = ValueReference.absent();
    }

    public static class PassiveCalendar {
        public long id;
        Reference<DeviceMessage> deviceMessage = ValueReference.absent();
    }

    public static class DeviceMessage {
        public long id;
        Reference<Device> device = ValueReference.absent();
    }


    @Test
    public void testUpgradeWithAddingJournalAndShorteningPrimaryKeyName() throws SQLException {
        DataModel dataModel = ormService.newDataModel("TEST", "TestModel");

        Table<Device> deviceTable = dataModel.addTable("DEVICE", Device.class);
        deviceTable.map(Device.class);
        Column houseIdColumn = deviceTable.column("ID")
                .map("id")
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .notNull()
                .add();
//        Column ownerColumn = deviceTable.column("OWNER")
//                .number()
//                .add();
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
        Column deviceColumn = deviceMessageTable.column("DEVICE_ID").number().add();
        deviceMessageTable.primaryKey("PK_MESSAGE_ID")
                .on(personIdColumn)
                .add();
        deviceMessageTable.foreignKey("FK_MESSAGE_DEVICE")
                .on(deviceColumn)
                .references(Device.class)
                .map("device")
                .add();

        Table<PassiveCalendar> passiveCalendarTable = dataModel.addTable("PASSIVE_CALENDAR", PassiveCalendar.class);
        passiveCalendarTable.map(PassiveCalendar.class);
        passiveCalendarTable.since(version(2, 0));
        Column passiveCalendarIdColumn = passiveCalendarTable.column("ID")
                .map("id")
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .notNull()
                .add();
        Column deviceMessageColumn = passiveCalendarTable.column("DEVICE_MESSAGE_ID").number().add();
        passiveCalendarTable.primaryKey("PK_PASSIVE_CALENDAR")
                .on(passiveCalendarIdColumn)
                .add();
        passiveCalendarTable.foreignKey("FK_PAS_CAL_MESS")
                .on(deviceMessageColumn)
                .references(DeviceMessage.class)
                .map("deviceMessage")
                .add();

        Table<?> deviceTable1 = dataModel.getTable("DEVICE");
        Column passiveCalendarId = deviceTable1.column("PASSIVE_CALENDAR_ID")
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .since(version(2, 0))
                .add();
        deviceTable1.foreignKey("FK_DEVICE_CAL")
                .since(version(2, 0))
                .on(passiveCalendarId)
                .references(PassiveCalendar.class)
                .map("passiveCalendar")
                .add();

        dataModel.register();
        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(1, 0));

        // now upgrade :

        ormService.getDataModelUpgrader(Logger.getAnonymousLogger()).upgrade(dataModel, version(2, 0));

        try (Connection connection = InMemoryPersistence.getDataSource().getConnection()) {
            try (ResultSet resultSet = connection.getMetaData().getCrossReference(null, null, "PASSIVE_CALENDAR", null, null, "DEVICE")) {
                assertThat(resultSet.next()).isTrue();
            }
        }
    }
}