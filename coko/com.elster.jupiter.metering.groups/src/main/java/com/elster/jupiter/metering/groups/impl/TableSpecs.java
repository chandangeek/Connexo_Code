package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
    MTG_UP_GROUP {
        @Override
        void addTo(DataModel dataModel) {
			Table<UsagePointGroup> table = dataModel.addTable(name(),UsagePointGroup.class);
            table.map(AbstractUsagePointGroup.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").map("name").add();
            Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
            table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
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
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column usagePointColumn = table.column("USAGEPOINT_ID").type("number").notNull().conversion(NUMBER2LONG).map("usagePointId").add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.primaryKey("MTG_PK_ENUM_UP_GROUP_ENTRY").on(groupColumn, usagePointColumn, intervalColumns.get(0)).add();
            table.foreignKey("MTG_FK_UPGE_UPG").references(MTG_UP_GROUP.name()).onDelete(CASCADE).map("usagePointGroup").on(groupColumn).add();
            table.foreignKey("MTG_FK_UPGE_UP").references(MeteringService.COMPONENTNAME, "MTR_USAGEPOINT").onDelete(RESTRICT).map("usagePoint").on(usagePointColumn).add();
        }
    },
    MTG_QUERY_UP_GROUP_OP {
        @Override
        void addTo(DataModel dataModel) {
			Table<QueryBuilderOperation> table = dataModel.addTable(name(),QueryBuilderOperation.class);
            table.map(AbstractQueryBuilderOperation.IMPLEMENTERS);
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
            table.addDiscriminatorColumn("OPERATORTYPE", "char(3)");
            table.column("OPERATOR").type("VARCHAR2(80)").map("operator").add();
            table.column("FIELDNAME").type("VARCHAR2(80)").map("fieldName").add();
            table.column("BINDVALUES").type("VARCHAR2(256)").conversion(CHAR2JSON).map("values").add();

            table.primaryKey("MTG_PK_QUPGOP").on(groupColumn, positionColumn).add();
            table.foreignKey("MTG_FK_QUPG_QUPGOP").references(MTG_UP_GROUP.name()).onDelete(CASCADE).map("usagePointGroup").reverseMap("operations").reverseMapOrder("position").on(groupColumn).add();

        }
    };

   
    abstract void addTo(DataModel component);
}