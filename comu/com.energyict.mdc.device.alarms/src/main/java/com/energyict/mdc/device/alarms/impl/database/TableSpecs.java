package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.AlarmRelatedEvents;
import com.energyict.mdc.device.alarms.impl.records.AlarmRelatedEventsImpl;
import com.energyict.mdc.device.alarms.impl.records.HistoricalDeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.DeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmImpl;

import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_CLEARED_STATUS;
import static com.energyict.mdc.device.alarms.impl.database.DatabaseConst.DAL_ALARM_EVENT_TYPE;
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

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ALARM_HISTORY", DAL_ALARM_HISTORY_PK,
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

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ALARM_OPEN", DAL_ALARM_OPEN_PK,
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
            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ALARM_ALL", DAL_ALARM_PK,
                    // Foreign key
                    DAL_ALARM_FK_TO_ALARM);
            table.addAuditColumns();
        }
    },

    DAL_OPEN_ALARM_RELATED_EVENTS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<AlarmRelatedEvents> table = dataModel.addTable(name(), AlarmRelatedEvents.class);
            table.map(AlarmRelatedEventsImpl.class);
            table.since(version(10, 3));
            Column alarmColumn = table.column("ALARM").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column eventRecordColumn = table.column("EVENTRECORD").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.primaryKey("VAL_PK_ALARM_RELATED_EVENTS").on(alarmColumn, eventRecordColumn).add();
            table.foreignKey("VAL_FK_ALARM_RELATED_EVENTS")
                    .references(DAL_ALARM_OPEN.name())
                    .on(alarmColumn)
                    .onDelete(CASCADE)
                    .map(AlarmRelatedEventsImpl.Fields.AlARM.fieldName())
                    .reverseMap(AlarmRelatedEventsImpl.Fields.EVENTRECORD.fieldName())
                    .composition().add();
            //NO FK for record
        }
    },

    DAL_HISTORY_ALARM_RELATED_EVENTS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<AlarmRelatedEvents> table = dataModel.addTable(name(), AlarmRelatedEvents.class);
            table.map(AlarmRelatedEventsImpl.class);
            table.since(version(10, 3));
            Column alarmColumn = table.column("ALARM").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column eventRecordColumn = table.column("EVENTRECORD").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.primaryKey("VAL_PK_ALARM_RELATED_EVENTS").on(alarmColumn, eventRecordColumn).add();
            table.foreignKey("VAL_FK_ALARM_RELATED_EVENTS")
                    .references(DAL_ALARM_HISTORY.name())
                    .on(alarmColumn)
                    .onDelete(CASCADE)
                    .map(AlarmRelatedEventsImpl.Fields.AlARM.fieldName())
                    .reverseMap(AlarmRelatedEventsImpl.Fields.EVENTRECORD.fieldName())
                    .composition().add();
            //NO FK for record
        }


    };

	public abstract void addTo(DataModel dataModel);

    private static class TableBuilder{
        private static final int EXPECTED_FK_KEYS_LENGTH = 1;

        static void buildIssueTable(Table<?> table, Column idColumn, String issueTable, String pkKey, String... fkKeys){
            Column alarmColRef = table.column(DAL_BASE_ALARM).number().conversion(NUMBER2LONG).notNull().add();
            table.column(DAL_DEVICE_MRID).varChar(NAME_LENGTH).map("deviceMRID").add();
            table.column(DAL_ALARM_CLEARED_STATUS).bool().map("clearedStatus").add();
            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH){
                throw new IllegalArgumentException("Passed arguments don't match foreigen keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next()).map("baseAlarm").on(alarmColRef).references(IssueService.class).add();
        }
    }
}