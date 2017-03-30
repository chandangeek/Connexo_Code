/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.HistoricalDeviceAlarmRelatedEvent;
import com.energyict.mdc.device.alarms.event.OpenDeviceAlarmRelatedEvent;
import com.energyict.mdc.device.alarms.impl.records.DeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.DeviceAlarmRelatedEventImpl;
import com.energyict.mdc.device.alarms.impl.records.HistoricalDeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.HistoricalDeviceAlarmRelatedEventImpl;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmRelatedEventImpl;

import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_CLEARED_STATUS;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_FK_TO_ISSUE;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_HISTORY_FK_TO_ISSUE;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_HISTORY_PK;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_OPEN_FK_TO_ISSUE;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_OPEN_PK;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_PK;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ID;

public enum TableSpecs {
    DAL_ALARM_HISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalDeviceAlarm> table = dataModel.addTable(name(), HistoricalDeviceAlarm.class);
            table.map(HistoricalDeviceAlarmImpl.class);
            table.since(version(10, 3));
            table.setJournalTableName("DAL_ALARM_HISTORY_JRNL").upTo(version(10, 3));
            Column idColumn = table.column(DAL_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildAlarmTable(table, idColumn, "ISU_ISSUE_HISTORY", DAL_ALARM_HISTORY_PK,
                    // Foreign keys
                    DAL_ALARM_HISTORY_FK_TO_ISSUE);
            table.addAuditColumns();
        }
    },
    DAL_ALARM_OPEN {
        @Override
        public void addTo(DataModel dataModel) {
            Table<OpenDeviceAlarm> table = dataModel.addTable(name(), OpenDeviceAlarm.class);
            table.map(OpenDeviceAlarmImpl.class);
            table.since(version(10, 3));
            table.setJournalTableName("DAL_ALARM_OPEN_JRNL").upTo(version(10, 3));
            Column idColumn = table.column(DAL_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildAlarmTable(table, idColumn, "ISU_ISSUE_OPEN", DAL_ALARM_OPEN_PK,
                    // Foreign key
                    DAL_ALARM_OPEN_FK_TO_ISSUE);//to_issue
            table.addAuditColumns();
        }
    },
    DAL_ALARM_ALL {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceAlarm> table = dataModel.addTable(name(), DeviceAlarm.class);
            table.map(DeviceAlarmImpl.class);
            table.since(version(10, 3));
            table.doNotAutoInstall(); // view
            Column idColumn = table.column(DAL_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();
            TableSpecs.TableBuilder.buildAlarmTable(table, idColumn, "ISU_ISSUE_ALL", DAL_ALARM_PK,
                    // Foreign key
                    DAL_ALARM_FK_TO_ISSUE);
            table.addAuditColumns();
        }
    },

     DAL_OPEN_ALM_RELATED_EVT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<OpenDeviceAlarmRelatedEvent> table = dataModel.addTable(name(), OpenDeviceAlarmRelatedEvent.class);
            table.map(OpenDeviceAlarmRelatedEventImpl.class);
            table.since(version(10, 3));
            Column alarmColumn = table.column(DAL_ALARM).number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column endDeviceColumn = table.column("ENDDEVICEID")
                    .number()
                    .notNull()
                    .map("endDeviceId")
                    .conversion(NUMBER2LONG)
                    .add();
            Column eventTypeColumn = table.column("EVENTTYPE")
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map("eventTypeCode")
                    .add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME")
                    .number()
                    .notNull()
                    .conversion(NUMBER2INSTANT)
                    .map("createdDateTime")
                    .add();
            table.primaryKey("VAL_PK_OPNALM_REL_EVTS").on(alarmColumn, endDeviceColumn, eventTypeColumn, createdDateTimeColumn).add();
            table.foreignKey("VAL_FK_OPNALM_REL_EVTS")
                    .on(alarmColumn)
                    .references(DAL_ALARM_OPEN.name())
                    .map(DeviceAlarmRelatedEventImpl.Fields.AlARM.fieldName())
                    .reverseMap(DeviceAlarmImpl.Fields.DEVICE_ALARM_RELATED_EVENTS.fieldName())
                    .onDelete(CASCADE)
                    .composition().add();
            table.foreignKey("VAL_FK_OPNALM_REL_EVTSEVT")
                    .on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn)
                    .references(EndDeviceEventRecord.class)
                    .map("eventRecord")
                    .onDelete(CASCADE)
                    .composition().add();
        }
    },

    DAL_HIST_ALM_RELATED_EVT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalDeviceAlarmRelatedEvent> table = dataModel.addTable(name(), HistoricalDeviceAlarmRelatedEvent.class);
            table.map(HistoricalDeviceAlarmRelatedEventImpl.class);
            table.since(version(10, 3));
            Column alarmColumn = table.column(DAL_ALARM).number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column endDeviceColumn = table.column("ENDDEVICEID")
                    .number()
                    .notNull()
                    .map(DeviceAlarmRelatedEventImpl.Fields.END_DEVICE_ID.fieldName())
                    .conversion(NUMBER2LONG)
                    .add();
            Column eventTypeColumn = table.column("EVENTTYPE")
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(DeviceAlarmRelatedEventImpl.Fields.EVENT_TYPE_CODE.fieldName())
                    .add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME")
                    .number()
                    .notNull()
                    .conversion(NUMBER2INSTANT)
                    .map(DeviceAlarmRelatedEventImpl.Fields.CREATE_DATE_TIME.fieldName())
                    .add();
            table.primaryKey("VAL_PK_HSTALM_REL_EVTS").on(alarmColumn, endDeviceColumn, eventTypeColumn, createdDateTimeColumn).add();
            table.foreignKey("VAL_FK_HSTALM_REL_EVTSALM")
                    .on(alarmColumn)
                    .references(DAL_ALARM_HISTORY.name())
                    .map(DeviceAlarmRelatedEventImpl.Fields.AlARM.fieldName())
                    .reverseMap(DeviceAlarmImpl.Fields.DEVICE_ALARM_RELATED_EVENTS.fieldName())
                    .onDelete(CASCADE)
                    .composition().add();
            table.foreignKey("VAL_FK_HSTALM_REL_EVTSEVT")
                    .on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn)
                    .references(EndDeviceEventRecord.class)
                    .map("eventRecord")
                    .onDelete(CASCADE)
                    .composition().add();
        }
    };

    public abstract void addTo(DataModel dataModel);

    private static class TableBuilder {
        private static final int EXPECTED_FK_KEYS_LENGTH = 1;

        static void buildAlarmTable(Table<?> table, Column idColumn, String alarmTable, String pkKey, String... fkKeys) {
            Column alarmColRef = table.column(DAL_ALARM).number().conversion(NUMBER2LONG).notNull().add();
            table.column(DAL_ALARM_CLEARED_STATUS).bool().map("clearedStatus").add();
            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH) {
                throw new IllegalArgumentException("Passed arguments don't match foreigen keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next())
                    .map(DeviceAlarmImpl.Fields.BASE_ISSUE.fieldName()).on(alarmColRef)
                    .references(IssueService.COMPONENT_NAME, alarmTable).add();
        }
    }
}