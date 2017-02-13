/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.ReadingTypeInEstimationRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.RelativePeriod;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    EST_ESTIMATIONRULESET {
        @Override
        void addTo(DataModel dataModel) {
            Table<EstimationRuleSet> table = dataModel.addTable(name(), EstimationRuleSet.class);
            table.map(EstimationRuleSetImpl.class);
            table.setJournalTableName("EST_ESTIMATIONRULESETJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("QUALITY_SYSTEM").number().conversion(NUMBER2ENUM).notNull().map("qualityCodeSystem").since(version(10, 2)).installValue("2").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(DESCRIPTION_LENGTH).map("description").add();
            Column obsoleteColumn = table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("EST_PK_ESTIMATIONRULESET").on(idColumn).add();
            table.unique("EST_U_ESTIMATIONRULESET").on(mRIDColumn).add();
            table.unique("EST_U_RULE_SET_NAME").on(nameColumn, obsoleteColumn).add();
        }
    },
    EST_ESTIMATIONRULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<EstimationRule> table = dataModel.addTable(name(), EstimationRule.class);
            table.map(EstimationRuleImpl.class);
            table.setJournalTableName("EST_ESTIMATIONRULEJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
            table.column("IMPLEMENTATION").varChar(NAME_LENGTH).map("implementation").add();
            Column ruleSetIdColumn = table.column("RULESETID").number().notNull().conversion(NUMBER2LONG).add();
            table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            Column obsoleteColumn = table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("EST_PK_ESTIMATIONRULE").on(idColumn).add();
            table.foreignKey("EST_FK_RULE").references("EST_ESTIMATIONRULESET").on(ruleSetIdColumn).onDelete(RESTRICT)
                    .map("ruleSet").reverseMap("rules").composition().reverseMapOrder("position").add();
            table.unique("EST_U_RULE_NAME").on(ruleSetIdColumn, nameColumn, obsoleteColumn).add();
        }
    },
    EST_ESTIMATIONRULEPROPS {
        @Override
        void addTo(DataModel dataModel) {
            Table<EstimationRuleProperties> table = dataModel.addTable(name(), EstimationRuleProperties.class);
            table.map(EstimationRulePropertiesImpl.class);
            Column ruleIdColumn = table.column("RULEID").number().notNull().conversion(NUMBER2LONG).add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("ESTUE").varChar(SHORT_DESCRIPTION_LENGTH).map("stringValue").add();
            table.setJournalTableName("EST_ESTIMATIONRULEPROPSJRNL").since(version(10, 2));

            table.addCreateTimeColumn("CREATETIME", "createTime").since(version(10, 2));
            table.addModTimeColumn("MODTIME", "modTime").since(version(10, 2));
            table.addUserNameColumn("USERNAME", "userName").since(version(10, 2));
            table.primaryKey("EST_PK_ESTRULEPROPS").on(ruleIdColumn, nameColumn).add();
            table.foreignKey("EST_FK_RULEPROPS").references("EST_ESTIMATIONRULE").onDelete(RESTRICT).map("rule").reverseMap("properties").composition().on(ruleIdColumn).add();
        }
    },
    EST_READINGTYPEINESTRULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeInEstimationRule> table = dataModel.addTable(name(), ReadingTypeInEstimationRule.class);
            table.map(ReadingTypeInEstimationRuleImpl.class);
            Column ruleIdColumn = table.column("RULEID").number().notNull().conversion(NUMBER2LONG).add();
            Column readingTypeMRIDColumn = table.column("READINGTYPEMRID").varChar(NAME_LENGTH).notNull().map("readingTypeMRID").add();
            table.setJournalTableName("EST_READINGTYPEINESTRULEJRNL");
            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.primaryKey("EST_PK_RTYPEINESTRULE").on(ruleIdColumn, readingTypeMRIDColumn).add();
            table
                .foreignKey("EST_FK_RTYPEINESTRULE_RULE")
                .references(EST_ESTIMATIONRULE.name())
                .onDelete(DeleteRule.RESTRICT)
                .map("rule")
                .reverseMap("readingTypesInRule")
                .composition()
                .on(ruleIdColumn)
                .add();
            table
                .foreignKey("EST_FK_RTYPEINESTRULE_RTYPE")
                .references(ReadingType.class)
                .onDelete(RESTRICT)
                .map("readingType")
                .on(readingTypeMRIDColumn)
                .add();
        }
    },
    EST_ESTIMATIONTASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<IEstimationTask> table = dataModel.addTable(name(), IEstimationTask.class);
            table.map(EstimationTaskImpl.class);
            table.setJournalTableName("EST_ESTIMATIONTASKJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column recurrentTaskId = table.column("RECURRENTTASK").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column endDeviceGroupIdOld = table.column("ENDDEVICEGROUP").number().notNull().conversion(ColumnConversion.NUMBER2LONG).upTo(version(10, 3)).add();
            Column endDeviceGroupId = table.column("ENDDEVICEGROUP").number().conversion(ColumnConversion.NUMBER2LONG).since(version(10, 3)).previously(endDeviceGroupIdOld).add();
            Column usagePointGroupId = table.column("USAGEPOINTGROUP").number().conversion(ColumnConversion.NUMBER2LONG).since(version(10, 3)).add();
            Column metrologyPurposeId = table.column("METROLOGYPURPOSE").number().conversion(ColumnConversion.NUMBER2LONG).since(version(10, 3)).add();
            Column relativePeriod = table.column("PERIOD").number().add();

            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").notAudited().add();
            table.column("QUALITY_SYSTEM").number().conversion(NUMBER2ENUM).notNull().map("qualityCodeSystem").since(version(10, 2)).installValue("2").add();
            table.addAuditColumns();

            table.foreignKey("EST_FK_ETSK_RECURRENTTASK")
                    .on(recurrentTaskId)
                    .references(RecurrentTask.class)
                    .map("recurrentTask")
                    .add();
            table.foreignKey("EST_FK_ETSK_ENDDEVICEFROUP")
                    .on(endDeviceGroupId)
                    .references(EndDeviceGroup.class)
                    .map("endDeviceGroup")
                    .add();
            table.foreignKey("EST_FK_ETSK_USAGEPOUNTGROUP")
                    .on(usagePointGroupId)
                    .references(UsagePointGroup.class)
                    .map("usagePointGroup")
                    .add();
            table.foreignKey("EST_FK_RTET_PERIOD")
                    .on(relativePeriod)
                    .references(RelativePeriod.class)
                    .map("period")
                    .add();
            table.foreignKey("EST_FK_METROLOGYPURPOSE")
                    .on(metrologyPurposeId)
                    .references(MetrologyPurpose.class)
                    .map("metrologyPurpose")
                    .since(version(10, 3))
                    .add();
            table.primaryKey("EST_PK_ESTIMATIONTASK").on(idColumn).add();
        }
    };

    abstract void addTo(DataModel component);
}