package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.groups.impl.query.AndOperation;
import com.elster.jupiter.metering.groups.impl.query.CloseBracketOperation;
import com.elster.jupiter.metering.groups.impl.query.NotOperation;
import com.elster.jupiter.metering.groups.impl.query.OpenBracketOperation;
import com.elster.jupiter.metering.groups.impl.query.OrOperation;
import com.elster.jupiter.metering.groups.impl.query.SimpleConditionOperation;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2JSON;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    MTG_UP_GROUP {
        @Override
        void addTo(DataModel dataModel) {
			Table<UsagePointGroup> table = dataModel.addTable(name(),UsagePointGroup.class);
            table.map(AbstractUsagePointGroup.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.addDiscriminatorColumn("GROUPTYPE", "char(3)");
            table.addAuditColumns();
            table.primaryKey("MTG_PK_ENUM_UP_GROUP").on(idColumn).add();
            table.unique("MTG_U_ENUM_UP_GROUP").on(mRIDColumn).add();
        }
    },
    MTG_ENUM_UP_IN_GROUP {
        @Override
        void addTo(DataModel dataModel) {
			Table<EnumeratedUsagePointGroup.Entry> table = dataModel.addTable(name(),EnumeratedUsagePointGroup.Entry.class);
            table.map(EnumeratedUsagePointGroupImpl.EntryImpl.class);
            Column groupColumn = table.column("GROUP_ID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column usagePointColumn = table.column("USAGEPOINT_ID").number().notNull().conversion(NUMBER2LONG).map("usagePointId").add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.setJournalTableName("MTG_ENUM_UP_IN_GROUPJRNL").since(version(10, 2));
            table.addCreateTimeColumn("CREATETIME", "createTime").since(version(10, 2));
            table.addUserNameColumn("USERNAME", "userName").since(version(10, 2));
            table
                .primaryKey("MTG_PK_ENUM_UP_IN_GROUP")
                .on(groupColumn, usagePointColumn, intervalColumns.get(0))
                .add();
            table
                .foreignKey("MTG_FK_UPGE_UPG")
                .references(MTG_UP_GROUP.name())
                .map("usagePointGroup")
                .on(groupColumn).add();
            table
                .foreignKey("MTG_FK_UPGE_UP")
                .references(UsagePoint.class)
                .onDelete(RESTRICT)
                .map("usagePoint")
                .on(usagePointColumn)
                .add();
        }
    },
    MTG_QUERY_UP_GROUP_OP {
        @Override
        void addTo(DataModel dataModel) {
			Table<UsagePointQueryBuilderOperation> table = dataModel.addTable(name(), UsagePointQueryBuilderOperation.class);
            table.map(initUsagePointQueryBuilderOperations());
            Column groupColumn = table.column("GROUP_ID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column positionColumn = table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
            table.addDiscriminatorColumn("OPERATORTYPE", "char(3)");
            table.column("OPERATOR").number().conversion(NUMBER2ENUM).map("operator").add();
            table.column("FIELDNAME").varChar(NAME_LENGTH).map("fieldName").add();
            table.column("BINDVALUES").varChar(SHORT_DESCRIPTION_LENGTH).conversion(CHAR2JSON).map("values").add();
            table.setJournalTableName("MTG_QUERY_UP_GROUP_OPJRNL").since(version(10, 2));
            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table
                .primaryKey("MTG_PK_QUPGOP")
                .on(groupColumn, positionColumn)
                .add();
            table
                .foreignKey("MTG_FK_QUPG_QUPGOP")
                .references(MTG_UP_GROUP.name())
                .map("group")
                .reverseMap("operations")
                .reverseMapOrder("position")
                .on(groupColumn)
                .add();
        }
    },
    MTG_ED_GROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceGroup> table = dataModel.addTable(name(), EndDeviceGroup.class);
            table.map(AbstractEndDeviceGroup.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("MRID").varChar(NAME_LENGTH + 4).map("mRID").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.addDiscriminatorColumn("GROUPTYPE", "char(3)");
            table.column("QUERYPROVIDERNAME").varChar(NAME_LENGTH).map("queryProviderName").add();
            table.column("LABEL").varChar(NAME_LENGTH).map("label").add();
            table.column("SEARCHDOMAIN").varChar(NAME_LENGTH).map(QueryEndDeviceGroupImpl.Fields.SEARCH_DOMAIN.fieldName()).add();
            table.addAuditColumns();
            table.primaryKey("MTG_PK_ENUM_ED_GROUP").on(idColumn).add();
            table.unique("MTG_U_ENUM_ED_GROUP").on(name).add();
        }
    },
    MTG_ENUM_ED_IN_GROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<EnumeratedEndDeviceGroup.Entry> table = dataModel.addTable(name(), EnumeratedEndDeviceGroup.Entry.class);
            table.map(EnumeratedEndDeviceGroupImpl.EntryImpl.class);
            Column groupColumn = table.column("GROUP_ID").number().notNull().conversion(NUMBER2LONG).add();
            Column endDeviceColumn = table.column("ENDDEVICE_ID").number().notNull().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addCreateTimeColumn("CREATETIME", "createTime").since(version(10, 2));
            table.addUserNameColumn("USERNAME", "userName").since(version(10, 2));
            table.setJournalTableName("MTG_ENUM_ED_IN_GROUPJRNL").since(version(10, 2));
            table.primaryKey("MTG_PK_ENUM_ED_IN_GROUP").on(groupColumn, endDeviceColumn, intervalColumns.get(0)).add();
            table
                .foreignKey("MTG_FK_EDGE_EDG")
                .references(MTG_ED_GROUP.name())
                .map("endDeviceGroup")
                .on(groupColumn)
                .add();
            table
                .foreignKey("MTG_FK_EDGE_ED")
                .references(EndDevice.class)
                .onDelete(RESTRICT)
                .map("endDevice")
                .on(endDeviceColumn)
                .add();
        }
    },
    MTG_QUERY_EDG_CONDITION {
        @Override
        void addTo(DataModel dataModel) {
            Table<QueryEndDeviceGroupCondition> table = dataModel.addTable(name(), QueryEndDeviceGroupCondition.class);
            table.map(QueryEndDeviceGroupCondition.class);
            Column groupColumn = table.column("ENDDEVICEGROUP").number().notNull().add();
            Column searchablePropertyColumn = table.column("PROPERTY").varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull().map(QueryEndDeviceGroupCondition.Fields.SEARCHABLE_PROPERTY.fieldName()).add();
            table.column("OPERATOR").number().notNull().conversion(NUMBER2ENUM).map(QueryEndDeviceGroupCondition.Fields.OPERATOR.fieldName()).add();
            table.setJournalTableName("MTG_QUERY_EDG_CONDITIONJRNL").since(version(10, 2));
            table.addAuditColumns().forEach(column -> column.since(version(20, 2)));
            table.primaryKey("MTG_PK_QUERY_EDG_CONDTION").on(groupColumn, searchablePropertyColumn).add();
            table.foreignKey("MTG_FK_QUERY_EDG_COND2GROUP")
                    .on(groupColumn)
                    .references(MTG_ED_GROUP.name())
                    .map(QueryEndDeviceGroupCondition.Fields.GROUP.fieldName())
                    .reverseMap(QueryEndDeviceGroupImpl.Fields.CONDITIONS.fieldName())
                    .add();
        }
    },
    MTG_QUERY_EDG_CONDITION_VALUE {
        @Override
        void addTo(DataModel dataModel) {
            Table<QueryEndDeviceGroupConditionValue> table = dataModel.addTable(name(), QueryEndDeviceGroupConditionValue.class);
            table.map(QueryEndDeviceGroupConditionValue.class);
            Column groupColumn = table.column("ENDDEVICEGROUP").number().notNull().add();
            Column searchablePropertyColumn = table.column("PROPERTY").varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull().add();
            Column positionColumn = table.column("POSITION").number().notNull().conversion(NUMBER2INT).map(QueryEndDeviceGroupConditionValue.Fields.POSITION.fieldName()).add();
            table.column("VALUE").varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull().map(QueryEndDeviceGroupConditionValue.Fields.VALUE.fieldName()).add();
            table.setJournalTableName("MTG_QUERY_EDG_COND_VALUEJRNL").since(version(10, 2));
            table.addAuditColumns().forEach(column -> column.since(version(20, 2)));
            table.primaryKey("MTG_PK_QUERY_EDGCONDVALUE").on(groupColumn, searchablePropertyColumn, positionColumn).add();
            table.foreignKey("MTG_FK_QUERY_EDG_VALUE2COND")
                    .on(groupColumn, searchablePropertyColumn)
                    .references(MTG_QUERY_EDG_CONDITION.name())
                    .map(QueryEndDeviceGroupConditionValue.Fields.DEVICE_GROUP_CONDITION.fieldName())
                    .reverseMap(QueryEndDeviceGroupCondition.Fields.CONDITION_VALUES.fieldName()).composition()
                    .reverseMapOrder(QueryEndDeviceGroupConditionValue.Fields.POSITION.fieldName())
                    .add();
        }
    }
    ;

    abstract void addTo(DataModel component);

    private static Map<String, Class<? extends UsagePointQueryBuilderOperation>> initUsagePointQueryBuilderOperations() {
        Map<String, Class<? extends UsagePointQueryBuilderOperation>> map = new HashMap<>();

        map.put(OpenBracketOperation.TYPE_IDENTIFIER, OpenBracketOperation.class);
        map.put(CloseBracketOperation.TYPE_IDENTIFIER, CloseBracketOperation.class);
        map.put(NotOperation.TYPE_IDENTIFIER, NotOperation.class);
        map.put(AndOperation.TYPE_IDENTIFIER, AndOperation.class);
        map.put(OrOperation.TYPE_IDENTIFIER, OrOperation.class);
        map.put(SimpleConditionOperation.TYPE_IDENTIFIER, SimpleConditionOperation.class);

        return map;
    }
}