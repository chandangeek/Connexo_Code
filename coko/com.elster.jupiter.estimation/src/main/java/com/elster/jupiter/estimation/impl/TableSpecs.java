package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.ReadingTypeInEstimationRule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;

public enum TableSpecs {

    EST_ESTIMATIONRULESET {
        @Override
        void addTo(DataModel dataModel) {
            Table<EstimationRuleSet> table = dataModel.addTable(name(), EstimationRuleSet.class);
            table.map(EstimationRuleSetImpl.class);
            table.setJournalTableName("EST_ESTIMATIONRULESETJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(DESCRIPTION_LENGTH).map("description").add();
            table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("EST_PK_ESTIMATIONRULESET").on(idColumn).add();
            table.unique("EST_U_ESTIMATIONRULESET").on(mRIDColumn).add();
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
            table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("EST_PK_ESTIMATIONRULE").on(idColumn).add();
            table.foreignKey("EST_FK_RULE").references("EST_ESTIMATIONRULESET").on(ruleSetIdColumn).onDelete(RESTRICT)
                    .map("ruleSet").reverseMap("rules").composition().reverseMapOrder("position").add();
        }
    },
    EST_ESTIMATIONRULEPROPS {
        @Override
        void addTo(DataModel dataModel) {
            Table<EstimationRuleProperties> table = dataModel.addTable(name(), EstimationRuleProperties.class);
            table.map(EstimationRulePropertiesImpl.class);
            table.setJournalTableName("EST_ESTIMATIONRULEPROPSJRNL");
            Column ruleIdColumn = table.column("RULEID").number().notNull().conversion(NUMBER2LONG).add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("ESTUE").varChar(SHORT_DESCRIPTION_LENGTH).map("stringValue").add();
            //table.addQuantityColumns("ESTUE", true, "value");
            table.primaryKey("EST_PK_ESTRULEPROPS").on(ruleIdColumn, nameColumn).add();
            table.foreignKey("EST_FK_RULEPROPS").references("EST_ESTIMATIONRULE").onDelete(RESTRICT).map("rule").reverseMap("properties").composition().on(ruleIdColumn).add();
        }
    },
    EST_READINGTYPEINESTRULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeInEstimationRule> table = dataModel.addTable(name(), ReadingTypeInEstimationRule.class);
            table.map(ReadingTypeInEstimationRuleImpl.class);
            table.setJournalTableName("EST_READINGTYPEINESTRULEJRNL");
            Column ruleIdColumn = table.column("RULEID").number().notNull().conversion(NUMBER2LONG).add();
            Column readingTypeMRIDColumn = table.column("READINGTYPEMRID").varChar(NAME_LENGTH).notNull().map("readingTypeMRID").add();
            table.primaryKey("EST_PK_RTYPEINESTRULE").on(ruleIdColumn, readingTypeMRIDColumn).add();
            table.foreignKey("EST_FK_RTYPEINESTRULE_RULE").references(EST_ESTIMATIONRULE.name()).onDelete(DeleteRule.RESTRICT).map("rule").reverseMap("readingTypesInRule").composition().on(ruleIdColumn).add();
            table.foreignKey("EST_FK_RTYPEINESTRULE_RTYPE").references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE").onDelete(RESTRICT).map("readingType").on(readingTypeMRIDColumn).add();
        }
    },
    EST_ESTIMATIONTASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<IEstimationTask> table = dataModel.addTable(name(), IEstimationTask.class);
            table.map(EstimationTaskImpl.class);
            table.setJournalTableName("EST_ESTIMATIONTASKJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            Column recurrentTaskId = table.column("RECURRENTTASK").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column endDeviceGroupId = table.column("ENDDEVICEGROUP").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column relativePeriod = table.column("PERIOD").number().add();

            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").add();
            table.addAuditColumns();

            table.foreignKey("EST_FK_ETSK_RECURRENTTASK")
                    .on(recurrentTaskId)
                    .references(TaskService.COMPONENTNAME, "TSK_RECURRENT_TASK")
                    .map("recurrentTask")
                    .add();
            table.foreignKey("EST_FK_ETSK_ENDDEVICEFROUP")
                    .on(endDeviceGroupId)
                    .references(MeteringGroupsService.COMPONENTNAME, "MTG_ED_GROUP")
                    .map("endDeviceGroup")
                    .add();
            table.foreignKey("EST_FK_RTET_PERIOD")
                    .on(relativePeriod)
                    .references(TimeService.COMPONENT_NAME, "TME_RELATIVEPERIOD")
                    .map("period")
                    .add();
            table.primaryKey("EST_PK_ESTIMATIONTASK").on(idColumn).add();
        }
    };

    abstract void addTo(DataModel component);
}