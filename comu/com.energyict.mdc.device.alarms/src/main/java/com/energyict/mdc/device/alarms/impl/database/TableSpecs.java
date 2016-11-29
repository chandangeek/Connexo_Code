package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.HistoricalDeviceAlarmRelatedEvents;
import com.energyict.mdc.device.alarms.event.OpenDeviceAlarmRelatedEvents;
import com.energyict.mdc.device.alarms.impl.records.DeviceAlarmRelatedEventsImpl;
import com.energyict.mdc.device.alarms.impl.records.HistoricalDeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.DeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.HistoricalDeviceAlarmRelatedEventsImpl;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmRelatedEventsImpl;

import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_CLEARED_STATUS;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_BASE_ALARM;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_DEVICE_MRID;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ID;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_FK_TO_ALARM;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_HISTORY_FK_TO_ALARM;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_HISTORY_PK;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_OPEN_FK_TO_ALARM;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_OPEN_PK;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_PK;

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
                    DAL_ALARM_HISTORY_FK_TO_ALARM);
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
                    DAL_ALARM_OPEN_FK_TO_ALARM);//to_issue
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
                    DAL_ALARM_FK_TO_ALARM);
            table.addAuditColumns();
        }
    },

    DAL_OPEN_ALM_RELATED_EVT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<OpenDeviceAlarmRelatedEvents> table = dataModel.addTable(name(), OpenDeviceAlarmRelatedEvents.class);
            table.map(OpenDeviceAlarmRelatedEventsImpl.class);
            table.since(version(10, 3));
            Column alarmColumn = table.column(DAL_BASE_ALARM).number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column eventRecordColumn = table.column("EVENTRECORD").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.primaryKey("VAL_PK_OPNALM_REL_EVTS").on(alarmColumn, eventRecordColumn).add();
            table.foreignKey("VAL_FK_OPNALM_REL_EVTS")
                    .references(DAL_ALARM_OPEN.name())
                    .on(alarmColumn)
                    .onDelete(CASCADE)
                    .map(DeviceAlarmRelatedEventsImpl.Fields.AlARM.fieldName())
                    .reverseMap(DeviceAlarmImpl.Fields.DEVICEALARMRELATEDEVENTS.fieldName())
                    .composition().add();
            table.foreignKey("VAL_FK_OPNALM_REL_EVTSEVT")
                    .references(EndDeviceEventRecord.class)
                    .on(eventRecordColumn)
                    .onDelete(CASCADE)
                    .map(DeviceAlarmRelatedEventsImpl.Fields.EVENTRECORD.fieldName())
                    .composition().add();
        }
    },

    DAL_HIST_ALM_RELATED_EVT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalDeviceAlarmRelatedEvents> table = dataModel.addTable(name(), HistoricalDeviceAlarmRelatedEvents.class);
            table.map(HistoricalDeviceAlarmRelatedEventsImpl.class);
            table.since(version(10, 3));
            Column alarmColumn = table.column(DAL_BASE_ALARM).number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column eventRecordColumn = table.column("EVENTRECORD").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.primaryKey("VAL_PK_HSTALM_REL_EVTS").on(alarmColumn, eventRecordColumn).add();
            table.foreignKey("VAL_FK_HSTALM_REL_EVTSALM")
                    .references(DAL_ALARM_HISTORY.name())
                    .on(alarmColumn)
                    .map(DeviceAlarmRelatedEventsImpl.Fields.AlARM.fieldName())
                    .reverseMap(DeviceAlarmImpl.Fields.DEVICEALARMRELATEDEVENTS.fieldName())
                    .composition().onDelete(CASCADE).add();
            table.foreignKey("VAL_FK_HSTALM_REL_EVTSEVT")
                    .references(EndDeviceEventRecord.class)
                    .on(eventRecordColumn)
                    .map(DeviceAlarmRelatedEventsImpl.Fields.EVENTRECORD.fieldName())
                    .composition().onDelete(CASCADE).add();
        }
    };

    public abstract void addTo(DataModel dataModel);

    private static class TableBuilder {
        private static final int EXPECTED_FK_KEYS_LENGTH = 1;

        static void buildAlarmTable(Table<?> table, Column idColumn, String alarmTable, String pkKey, String... fkKeys) {
            Column alarmColRef = table.column(DAL_BASE_ALARM).number().conversion(NUMBER2LONG).notNull().add();
            table.column(DAL_DEVICE_MRID).varChar(NAME_LENGTH).map("deviceMRID").add();
            table.column(DAL_ALARM_CLEARED_STATUS).bool().map("clearedStatus").add();
            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH) {
                throw new IllegalArgumentException("Passed arguments don't match foreigen keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next())
                    .map(DeviceAlarmImpl.Fields.BASEALARM.fieldName()).on(alarmColRef)
                    .references(IssueService.COMPONENT_NAME, alarmTable).add();
        }
    }
}