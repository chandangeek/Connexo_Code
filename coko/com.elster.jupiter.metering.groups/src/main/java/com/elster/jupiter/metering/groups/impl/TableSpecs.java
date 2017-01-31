/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UniqueConstraint;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

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
        final Map<String, Class<? extends UsagePointGroup>> USAGE_POINT_GROUP_IMPLEMENTERS = ImmutableMap.of(
                QueryUsagePointGroup.TYPE_IDENTIFIER, QueryUsagePointGroupImpl.class,
                EnumeratedUsagePointGroup.TYPE_IDENTIFIER, EnumeratedUsagePointGroupImpl.class);

        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointGroup> table = dataModel.addTable(name(), UsagePointGroup.class);
            table.map(USAGE_POINT_GROUP_IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            Column mRIDColumn_10_2 = table.column("MRID").varChar(NAME_LENGTH).upTo(version(10, 3)).add();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH + 4).map("mRID")
                    .since(version(10, 3)).previously(mRIDColumn_10_2).add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.addDiscriminatorColumn("GROUPTYPE", "char(3)");
            table.column("QUERYPROVIDERNAME").varChar(NAME_LENGTH)
                    .map(AbstractQueryGroup.Fields.QUERY_PROVIDER_NAME.fieldName()).since(version(10, 3)).add();
            table.column("LABEL").varChar(NAME_LENGTH).map("label").since(version(10, 3)).add();
            table.column("SEARCHDOMAIN").varChar(NAME_LENGTH)
                    .map(AbstractQueryGroup.Fields.SEARCH_DOMAIN.fieldName()).since(version(10, 3)).add();
            table.addAuditColumns();
            table.primaryKey("MTG_PK_ENUM_UP_GROUP").on(idColumn).add();
            UniqueConstraint uniqueMrid_10_2 = table.unique("MTG_U_ENUM_UP_GROUP").on(mRIDColumn).upTo(version(10, 3)).add();
            table.unique("MTG_U_UP_GROUP_MRID").on(mRIDColumn).since(version(10, 3)).previously(uniqueMrid_10_2).add();
            table.unique("MTG_U_UP_GROUP_NAME").on(nameColumn).since(version(10, 3)).add();
        }
    },
    MTG_ENUM_UP_IN_GROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<EnumeratedUsagePointGroupImpl.UsagePointEntryImpl> table = dataModel
                    .addTable(name(), EnumeratedUsagePointGroupImpl.UsagePointEntryImpl.class);
            table.map(EnumeratedUsagePointGroupImpl.UsagePointEntryImpl.class);
            Column groupColumn = table.column("GROUP_ID").number().notNull().conversion(NUMBER2LONG).add();
            Column usagePointColumn = table.column("USAGEPOINT_ID").number().notNull().conversion(NUMBER2LONG).add();
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
                .map("group")
                .on(groupColumn).add();
            table
                .foreignKey("MTG_FK_UPGE_UP")
                .references(UsagePoint.class)
                .onDelete(RESTRICT)
                .map("member")
                .on(usagePointColumn)
                .add();
        }
    },
    MTG_QUERY_UPG_CONDITION {
        @Override
        void addTo(DataModel dataModel) {
            Table<QueryUsagePointGroupImpl.QueryUsagePointGroupCondition> table = dataModel
                    .addTable(name(), QueryUsagePointGroupImpl.QueryUsagePointGroupCondition.class);
            table.since(version(10, 3));
            table.map(QueryUsagePointGroupImpl.QueryUsagePointGroupCondition.class);
            Column groupColumn = table.column("USAGEPOINTGROUP").number().notNull().add();
            Column searchablePropertyColumn =
                    table.column(QueryGroupCondition.Fields.PROPERTY.name()).varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull()
                            .map(QueryGroupCondition.Fields.PROPERTY.fieldName()).add();
            table.column(QueryGroupCondition.Fields.OPERATOR.name()).number().notNull().conversion(NUMBER2ENUM)
                    .map(QueryGroupCondition.Fields.OPERATOR.fieldName()).add();
            table.setJournalTableName("MTG_QUERY_UPG_COND_JRNL").since(version(10, 3));
            table.addAuditColumns();
            table.primaryKey("MTG_PK_QUERY_UPG_COND").on(groupColumn, searchablePropertyColumn).add();
            table.foreignKey("MTG_FK_QUERY_UPG_COND2GROUP")
                    .on(groupColumn)
                    .references(MTG_UP_GROUP.name())
                    .map(QueryGroupCondition.Fields.GROUP.fieldName())
                    .reverseMap(AbstractQueryGroup.Fields.CONDITIONS.fieldName())
                    .add();
        }
    },
    MTG_QUERY_UPG_CONDITION_VALUE {
        @Override
        void addTo(DataModel dataModel) {
            Table<QueryUsagePointGroupImpl.QueryUsagePointGroupConditionValue> table = dataModel
                    .addTable(name(), QueryUsagePointGroupImpl.QueryUsagePointGroupConditionValue.class);
            table.map(QueryUsagePointGroupImpl.QueryUsagePointGroupConditionValue.class);
            table.since(version(10, 3));
            Column groupColumn = table.column("USAGEPOINTGROUP").number().notNull().add();
            Column searchablePropertyColumn = table.column("PROPERTY").varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull().add();
            Column positionColumn = table.column(QueryGroupConditionValue.Fields.POSITION.name())
                    .number().notNull().conversion(NUMBER2INT)
                    .map(QueryGroupConditionValue.Fields.POSITION.fieldName()).add();
            table.column(QueryGroupConditionValue.Fields.VALUE.name()).varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull()
                    .map(QueryGroupConditionValue.Fields.VALUE.fieldName()).add();
            table.setJournalTableName("MTG_QUERY_UPG_COND_VAL_JRNL").since(version(10, 3));
            table.addAuditColumns();
            table.primaryKey("MTG_PK_QUERY_UPG_COND_VAL").on(groupColumn, searchablePropertyColumn, positionColumn).add();
            table.foreignKey("MTG_FK_QUERY_UPG_VALUE2COND")
                    .on(groupColumn, searchablePropertyColumn)
                    .references(MTG_QUERY_UPG_CONDITION.name())
                    .map(QueryGroupConditionValue.Fields.GROUP_CONDITION.fieldName())
                    .reverseMap(QueryGroupCondition.Fields.CONDITION_VALUES.fieldName()).composition()
                    .reverseMapOrder(QueryGroupConditionValue.Fields.POSITION.fieldName())
                    .add();
        }
    },
    MTG_QUERY_UP_GROUP_OP { // removed in 10.3
        @Override
        void addTo(DataModel dataModel) {
            Table<String> table = dataModel.addTable(name(), String.class);
            table.upTo(version(10, 3));
            Column groupColumn = table.column("GROUP_ID").number().notNull()
                    .conversion(NUMBER2LONG).upTo(version(10, 3)).add();
            Column positionColumn = table.column("POSITION").number().notNull()
                    .conversion(NUMBER2INT).upTo(version(10, 3)).add();
            table.addDiscriminatorColumn("OPERATORTYPE", "char(3)").upTo(version(10, 3));
            table.column("OPERATOR").number().conversion(NUMBER2ENUM).upTo(version(10, 3)).add();
            table.column("FIELDNAME").varChar(NAME_LENGTH).upTo(version(10, 3)).add();
            table.column("BINDVALUES").varChar(SHORT_DESCRIPTION_LENGTH)
                    .conversion(CHAR2JSON).upTo(version(10, 3)).add();
            table.setJournalTableName("MTG_QUERY_UP_GROUP_OPJRNL")
                    .during(Range.closedOpen(version(10, 2), version(10, 3)));
            table.addAuditColumns().forEach(column ->
                    column.during(Range.closedOpen(version(10, 2), version(10, 3))));
            table.primaryKey("MTG_PK_QUPGOP")
                    .on(groupColumn, positionColumn)
                    .upTo(version(10, 3))
                    .add();
            table.foreignKey("MTG_FK_QUPG_QUPGOP")
                    .references(MTG_UP_GROUP.name())
                    .map("group")
                    .on(groupColumn)
                    .upTo(version(10, 3))
                    .add();
        }
    },
    MTG_ED_GROUP {
        final Map<String, Class<? extends EndDeviceGroup>> END_DEVICE_GROUP_IMPLEMENTERS = ImmutableMap.of(
                QueryEndDeviceGroup.TYPE_IDENTIFIER, QueryEndDeviceGroupImpl.class,
                EnumeratedEndDeviceGroup.TYPE_IDENTIFIER, EnumeratedEndDeviceGroupImpl.class);

        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceGroup> table = dataModel.addTable(name(), EndDeviceGroup.class);
            table.map(END_DEVICE_GROUP_IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            Column mridColumn = table.column("MRID").varChar(NAME_LENGTH + 4).map("mRID").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.addDiscriminatorColumn("GROUPTYPE", "char(3)");
            table.column("QUERYPROVIDERNAME").varChar(NAME_LENGTH)
                    .map(AbstractQueryGroup.Fields.QUERY_PROVIDER_NAME.fieldName()).add();
            table.column("LABEL").varChar(NAME_LENGTH).map("label").add();
            table.column("SEARCHDOMAIN").varChar(NAME_LENGTH)
                    .map(AbstractQueryGroup.Fields.SEARCH_DOMAIN.fieldName()).add();
            table.addAuditColumns();
            table.primaryKey("MTG_PK_ENUM_ED_GROUP").on(idColumn).add();
            table.unique("MTG_U_ENUM_ED_GROUP").on(name).add();
            table.unique("MTG_U_ED_GROUP_MRID").on(mridColumn).since(version(10, 3)).add();
        }
    },
    MTG_ENUM_ED_IN_GROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<EnumeratedEndDeviceGroupImpl.EndDeviceEntryImpl> table = dataModel
                    .addTable(name(), EnumeratedEndDeviceGroupImpl.EndDeviceEntryImpl.class);
            table.map(EnumeratedEndDeviceGroupImpl.EndDeviceEntryImpl.class);
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
                .map("group")
                .on(groupColumn)
                .add();
            table
                .foreignKey("MTG_FK_EDGE_ED")
                .references(EndDevice.class)
                .onDelete(RESTRICT)
                .map("member")
                .on(endDeviceColumn)
                .add();
        }
    },
    MTG_QUERY_EDG_CONDITION {
        @Override
        void addTo(DataModel dataModel) {
            Table<QueryEndDeviceGroupImpl.QueryEndDeviceGroupCondition> table = dataModel
                    .addTable(name(), QueryEndDeviceGroupImpl.QueryEndDeviceGroupCondition.class);
            table.map(QueryEndDeviceGroupImpl.QueryEndDeviceGroupCondition.class);
            Column groupColumn = table.column("ENDDEVICEGROUP").number().notNull().add();
            Column searchablePropertyColumn =
                    table.column(QueryGroupCondition.Fields.PROPERTY.name()).varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull()
                    .map(QueryGroupCondition.Fields.PROPERTY.fieldName()).add();
            table.column(QueryGroupCondition.Fields.OPERATOR.name()).number().notNull().conversion(NUMBER2ENUM)
                    .map(QueryGroupCondition.Fields.OPERATOR.fieldName()).add();
            table.setJournalTableName("MTG_QUERY_EDG_CONDITIONJRNL").since(version(10, 2));
            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.primaryKey("MTG_PK_QUERY_EDG_CONDTION").on(groupColumn, searchablePropertyColumn).add();
            table.foreignKey("MTG_FK_QUERY_EDG_COND2GROUP")
                    .on(groupColumn)
                    .references(MTG_ED_GROUP.name())
                    .map(QueryGroupCondition.Fields.GROUP.fieldName())
                    .reverseMap(AbstractQueryGroup.Fields.CONDITIONS.fieldName())
                    .add();
        }
    },
    MTG_QUERY_EDG_CONDITION_VALUE {
        @Override
        void addTo(DataModel dataModel) {
            Table<QueryEndDeviceGroupImpl.QueryEndDeviceGroupConditionValue> table = dataModel
                    .addTable(name(), QueryEndDeviceGroupImpl.QueryEndDeviceGroupConditionValue.class);
            table.map(QueryEndDeviceGroupImpl.QueryEndDeviceGroupConditionValue.class);
            Column groupColumn = table.column("ENDDEVICEGROUP").number().notNull().add();
            Column searchablePropertyColumn = table.column("PROPERTY").varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull().add();
            Column positionColumn = table.column("POSITION").number().notNull().conversion(NUMBER2INT)
                    .map(QueryGroupConditionValue.Fields.POSITION.fieldName()).add();
            table.column("VALUE").varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull()
                    .map(QueryGroupConditionValue.Fields.VALUE.fieldName()).add();
            table.setJournalTableName("MTG_QUERY_EDG_COND_VALUEJRNL").since(version(10, 2));
            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.primaryKey("MTG_PK_QUERY_EDGCONDVALUE").on(groupColumn, searchablePropertyColumn, positionColumn).add();
            table.foreignKey("MTG_FK_QUERY_EDG_VALUE2COND")
                    .on(groupColumn, searchablePropertyColumn)
                    .references(MTG_QUERY_EDG_CONDITION.name())
                    .map(QueryGroupConditionValue.Fields.GROUP_CONDITION.fieldName())
                    .reverseMap(QueryGroupCondition.Fields.CONDITION_VALUES.fieldName()).composition()
                    .reverseMapOrder(QueryGroupConditionValue.Fields.POSITION.fieldName())
                    .add();
        }
    }
    ;

    abstract void addTo(DataModel component);
}
