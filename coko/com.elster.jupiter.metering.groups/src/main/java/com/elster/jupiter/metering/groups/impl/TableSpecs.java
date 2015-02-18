package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.MeteringService;
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

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.*;

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
			Table<UsagePointQueryBuilderOperation> table = dataModel.addTable(name(), UsagePointQueryBuilderOperation.class);
            table.map(initUsagePointQueryBuilderOperations());
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
            table.addDiscriminatorColumn("OPERATORTYPE", "char(3)");
            table.column("OPERATOR").number().conversion(NUMBER2ENUM).map("operator").add();
            table.column("FIELDNAME").varChar(NAME_LENGTH).map("fieldName").add();
            table.column("BINDVALUES").varChar(SHORT_DESCRIPTION_LENGTH).conversion(CHAR2JSON).map("values").add();

            table.primaryKey("MTG_PK_QUPGOP").on(groupColumn, positionColumn).add();
            table.foreignKey("MTG_FK_QUPG_QUPGOP").references(MTG_UP_GROUP.name()).onDelete(CASCADE).map("group").reverseMap("operations").reverseMapOrder("position").on(groupColumn).add();

        }
    },
    MTG_ED_GROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceGroup> table = dataModel.addTable(name(), EndDeviceGroup.class);
            table.map(AbstractEndDeviceGroup.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH + 4).map("mRID").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.addDiscriminatorColumn("GROUPTYPE", "char(3)");
            table.column("QUERYPROVIDERNAME").varChar(NAME_LENGTH).map("queryProviderName").add();
            table.column("LABEL").varChar(NAME_LENGTH).map("label").add();
            table.addAuditColumns();
            table.primaryKey("MTG_PK_ENUM_ED_GROUP").on(idColumn).add();
            table.unique("MTG_U_ENUM_ED_GROUP").on(mRIDColumn).add();
        }
    },
    MTG_ENUM_ED_IN_GROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<EnumeratedEndDeviceGroup.Entry> table = dataModel.addTable(name(), EnumeratedEndDeviceGroup.Entry.class);
            table.map(EnumeratedEndDeviceGroupImpl.EntryImpl.class);
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column endDeviceColumn = table.column("ENDDEVICE_ID").type("number").notNull().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.primaryKey("MTG_PK_ENUM_ED_GROUP_ENTRY").on(groupColumn, endDeviceColumn, intervalColumns.get(0)).add();
            table.foreignKey("MTG_FK_EDGE_EDG").references(MTG_ED_GROUP.name()).onDelete(CASCADE).map("endDeviceGroup").on(groupColumn).add();
            table.foreignKey("MTG_FK_EDGE_ED").references(MeteringService.COMPONENTNAME, "MTR_ENDDEVICE").onDelete(RESTRICT).map("endDevice").on(endDeviceColumn).add();
        }
    },
    MTG_QUERY_ED_GROUP_OP {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceQueryBuilderOperation> table = dataModel.addTable(name(), EndDeviceQueryBuilderOperation.class);
            table.map(initEndDeviceQueryBuilderOperations());
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
            table.addDiscriminatorColumn("OPERATORTYPE", "char(3)");
            table.column("OPERATOR").number().conversion(NUMBER2ENUM).map("operator").add();
            table.column("FIELDNAME").varChar(NAME_LENGTH).map("fieldName").add();
            table.column("BINDVALUES").varChar(SHORT_DESCRIPTION_LENGTH).conversion(CHAR2JSON).map("values").add();

            table.primaryKey("MTG_PK_QEDGOP").on(groupColumn, positionColumn).add();
            table.foreignKey("MTG_FK_QEDG_QEDGOP").references(MTG_ED_GROUP.name()).onDelete(CASCADE).map("group").reverseMap("operations").reverseMapOrder("position").on(groupColumn).add();
        }
    };

   
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

    private static Map<String, Class<? extends EndDeviceQueryBuilderOperation>> initEndDeviceQueryBuilderOperations() {
        Map<String, Class<? extends EndDeviceQueryBuilderOperation>> map = new HashMap<>();

        map.put(OpenBracketOperation.TYPE_IDENTIFIER, OpenBracketOperation.class);
        map.put(CloseBracketOperation.TYPE_IDENTIFIER, CloseBracketOperation.class);
        map.put(NotOperation.TYPE_IDENTIFIER, NotOperation.class);
        map.put(AndOperation.TYPE_IDENTIFIER, AndOperation.class);
        map.put(OrOperation.TYPE_IDENTIFIER, OrOperation.class);
        map.put(SimpleConditionOperation.TYPE_IDENTIFIER, SimpleConditionOperation.class);

        return map;
    }


}