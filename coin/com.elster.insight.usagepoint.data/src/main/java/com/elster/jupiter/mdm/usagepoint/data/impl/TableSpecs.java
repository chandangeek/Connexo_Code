/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.mdm.usagepoint.data.impl.favorites.FavoriteUsagePointGroupImpl;
import com.elster.jupiter.mdm.usagepoint.data.impl.favorites.FavoriteUsagePointImpl;
import com.elster.jupiter.mdm.usagepoint.data.impl.properties.ChannelValidationRuleOverriddenPropertiesImpl;
import com.elster.jupiter.mdm.usagepoint.data.impl.properties.ValidationEstimationOverriddenPropertyImpl;
import com.elster.jupiter.mdm.usagepoint.data.impl.properties.ValidationEstimationRuleOverriddenPropertiesImpl;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.users.User;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;

public enum TableSpecs {
    FAV_FAVUSAGEPOINT {
        @Override
        void addTo(DataModel dataModel) {
            Table<FavoriteUsagePoint> table = dataModel.addTable(name(), FavoriteUsagePoint.class)
                    .since(Version.version(10, 3));
            table.map(FavoriteUsagePointImpl.class);

            Column userColumn = table.column("USERID").number().notNull()
                    .conversion(ColumnConversion.NUMBER2LONG).add();
            Column usagePointColumn = table.column("USAGEPOINT").number().notNull()
                    .conversion(ColumnConversion.NUMBER2LONG).add();
            table.addCreateTimeColumn("CREATETIME", "creationDate");
            table.column("UPCOMMENT").type("CLOB").map("comment").conversion(ColumnConversion.CLOB2STRING).add();

            table.primaryKey("FAV_PK_FAVUP").on(userColumn, usagePointColumn).add();
            table.foreignKey("FAV_FK_FAVUP_USER").on(userColumn).references(User.class)
                    .map("user").onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("FAV_FK_FAVUP_UP").on(usagePointColumn).references(UsagePoint.class)
                    .map("usagePoint").onDelete(DeleteRule.CASCADE).add();
        }
    },
    FAV_FAVUSAGEPOINTGROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<FavoriteUsagePointGroup> table = dataModel.addTable(name(), FavoriteUsagePointGroup.class)
                    .since(Version.version(10, 3));
            table.map(FavoriteUsagePointGroupImpl.class);

            Column userColumn = table.column("USERID").number().notNull()
                    .conversion(ColumnConversion.NUMBER2LONG).add();
            Column usagePointGroupColumn = table.column("USAGEPOINTGROUP").number().notNull()
                    .conversion(ColumnConversion.NUMBER2LONG).add();
            table.addCreateTimeColumn("CREATETIME", "creationDate");
            table.column("UPGCOMMENT").type("CLOB").map("comment").conversion(ColumnConversion.CLOB2STRING).add();

            table.primaryKey("FAV_PK_FAVUPGROUP").on(userColumn, usagePointGroupColumn).add();
            table.foreignKey("FAV_FK_FAVUPGROUP_USER").on(userColumn).references(User.class)
                    .map("user").onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("FAV_FK_FAVUPGROUP_GROUP").on(usagePointGroupColumn).references(UsagePointGroup.class)
                    .map("usagePointGroup").onDelete(DeleteRule.CASCADE).add();
        }
    },

    UDC_CHANNELVALESTRULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ValidationEstimationRuleOverriddenPropertiesImpl> table = dataModel.addTable(name(), ValidationEstimationRuleOverriddenPropertiesImpl.class);
            table.setJournalTableName(name() + "JRNL");
            table.since(Version.version(10, 3));

            table.map(ValidationEstimationRuleOverriddenPropertiesImpl.IMPLEMENTERS);

            Column idColumn = table.addAutoIdColumn();
            table.addAuditColumns();
            Column discriminatorColumn = table.addDiscriminatorColumn("TYPE", "char(3)");
            Column usagePointColumn = table.column("USAGEPOINT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column readingTypeColumn = table.column("READINGTYPE").varChar(Table.NAME_LENGTH).notNull().add();
            Column ruleNameColumn = table.column("RULENAME").varChar(Table.NAME_LENGTH).notNull()
                    .map(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_NAME.fieldName()).add();
            Column ruleImplColumn = table.column("RULEIMPL").varChar(Table.NAME_LENGTH).notNull()
                    .map(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.RULE_IMPL.fieldName()).add();
            Column validationActionColumn = table.column("VALIDATIONACTION").number().conversion(NUMBER2ENUM)
                    .map(ChannelValidationRuleOverriddenPropertiesImpl.Fields.VALIDATION_ACTION.fieldName()).add();

            table.primaryKey("UDC_PK_CHANNELVALESTRULE").on(idColumn).add();

            table.foreignKey("UDC_FK_CHANNELRULE2USAGEPOINT")
                    .on(usagePointColumn)
                    .references(UsagePoint.class)
                    .map(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.USAGEPOINT.fieldName())
                    .add();
            table.foreignKey("UDC_FK_CHANNELRULE2READINGTYPE")
                    .on(readingTypeColumn)
                    .references(ReadingType.class)
                    .map(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.READINGTYPE.fieldName())
                    .add();

            table.unique("UDC_U_CHANNELVALESTRULE")
                    .on(discriminatorColumn, usagePointColumn, readingTypeColumn, ruleNameColumn, ruleImplColumn, validationActionColumn)
                    .add();
        }
    },

    UDC_CHANNELVALESTRULEPROP {
        @Override
        void addTo(DataModel dataModel) {
            Table<ValidationEstimationOverriddenPropertyImpl> table = dataModel.addTable(name(), ValidationEstimationOverriddenPropertyImpl.class);
            table.map(ValidationEstimationOverriddenPropertyImpl.class);
            table.setJournalTableName(name() + "JRNL");
            table.since(Version.version(10, 3));

            Column channelValEstRuleColumn = table.column("CHANNELVALESTRULE").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column propertyNameColumn = table.column("PROPERTYNAME").varChar(NAME_LENGTH).notNull()
                    .map(ValidationEstimationOverriddenPropertyImpl.Fields.PROPERTY_NAME.fieldName()).add();
            table.column("PROPERTYVALUE").varChar(SHORT_DESCRIPTION_LENGTH)
                    .map(ValidationEstimationOverriddenPropertyImpl.Fields.PROPERTY_VALUE.fieldName()).add();

            table.primaryKey("UDC_PK_CHANVALESTRULEPROP").on(channelValEstRuleColumn, propertyNameColumn).add();

            table.foreignKey("UDC_FK_PROP2CHANNELVALESTRULE")
                    .on(channelValEstRuleColumn)
                    .references(UDC_CHANNELVALESTRULE.name())
                    .map(ValidationEstimationOverriddenPropertyImpl.Fields.RULE.fieldName())
                    .reverseMap(ValidationEstimationRuleOverriddenPropertiesImpl.Fields.PROPERTIES.fieldName())
                    .composition()
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);
}
