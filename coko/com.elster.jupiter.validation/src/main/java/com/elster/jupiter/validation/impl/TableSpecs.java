/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiChild;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiChildImpl;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiImpl;
import com.elster.jupiter.validation.kpi.DataValidationKpi;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    VAL_VALIDATIONRULESET {
        @Override
        void addTo(DataModel dataModel) {
            Table<ValidationRuleSet> table = dataModel.addTable(name(), ValidationRuleSet.class);
            table.map(ValidationRuleSetImpl.class);
            table.setJournalTableName("VAL_VALIDATIONRULESETJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("QUALITY_SYSTEM").number().conversion(ColumnConversion.NUMBER2ENUM).map("qualityCodeSystem").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(DESCRIPTION_LENGTH).map("description").add();
            Column obsoleteColumn = table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("VAL_PK_VALIDATIONRULESET").on(idColumn).add();
            table.unique("VAL_U_VALIDATIONRULESET").on(mRIDColumn).add();
            table.unique("VAL_UQ_RULESET_NAME").on(nameColumn, obsoleteColumn).add();
        }
    },
    VAL_VALIDATIONRULESETVERSION {
        @Override
        void addTo(DataModel dataModel) {
            Table<ValidationRuleSetVersion> table = dataModel.addTable(name(), ValidationRuleSetVersion.class);
            table.map(ValidationRuleSetVersionImpl.class);
            table.setJournalTableName("VAL_VALIDRULESETVERSIONJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("START_DATE").map("startDate").number().conversion(NUMBER2INSTANT).add();
            table.column("DESCRIPTION").varChar(DESCRIPTION_LENGTH).map("description").add();
            table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            Column ruleSetIdColumn = table.column("RULESETID").number().notNull().conversion(NUMBER2LONG).add();
            table.addAuditColumns();
            table.primaryKey("VAL_PK_VALIDRULESETVERS").on(idColumn).add();
            table.foreignKey("VAL_FK_VALIDRULESET").references("VAL_VALIDATIONRULESET").on(ruleSetIdColumn).onDelete(RESTRICT)
                    .map("ruleSet").reverseMap("versions").composition().add();
        }
    },
    VAL_VALIDATIONRULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ValidationRule> table = dataModel.addTable(name(), ValidationRule.class);
            table.map(ValidationRuleImpl.class);
            table.setJournalTableName("VAL_VALIDATIONRULEJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
            table.column("ACTION").number().notNull().conversion(NUMBER2ENUM).map("action").add();
            table.column("IMPLEMENTATION").varChar(NAME_LENGTH).map("implementation").add();
            Column ruleSetVersionIdColumn = table.column("RULESETVERSIONID").number().notNull().conversion(NUMBER2LONG).add();
            table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            Column obsoleteColumn = table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("VAL_PK_VALIDATIONRULE").on(idColumn).add();
            table.foreignKey("VAL_FK_RULESETVERSION").references("VAL_VALIDATIONRULESETVERSION").on(ruleSetVersionIdColumn).onDelete(RESTRICT)
                    .map("ruleSetVersion").reverseMap("rules").composition().reverseMapOrder("position").add();
            table.unique("VAL_UQ_RULE_NAME").on(ruleSetVersionIdColumn, nameColumn, obsoleteColumn).add();
        }
    },
    VAL_VALIDATIONRULEPROPS {
        @Override
        void addTo(DataModel dataModel) {
            Table<ValidationRuleProperties> table = dataModel.addTable(name(), ValidationRuleProperties.class);
            table.map(ValidationRulePropertiesImpl.class);
            table.setJournalTableName("VAL_VALIDATIONRULEPROPSJRNL");
            Column ruleIdColumn = table.column("RULEID").number().notNull().conversion(NUMBER2LONG).add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("VALUE").varChar(SHORT_DESCRIPTION_LENGTH).map("stringValue").add();
            //table.addQuantityColumns("VALUE", true, "value");
            table.primaryKey("VAL_PK_VALRULEPROPS").on(ruleIdColumn, nameColumn).add();
            table.foreignKey("VAL_FK_RULEPROPS").references("VAL_VALIDATIONRULE").onDelete(RESTRICT).map("rule").reverseMap("properties").composition().on(ruleIdColumn).add();
        }
    },
    VAL_MA_VALIDATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<ChannelsContainerValidation> table = dataModel.addTable(name(), ChannelsContainerValidation.class);
            table.map(ChannelsContainerValidationImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column ruleSetColumn = table.column("RULESETID").number().conversion(NUMBER2LONG).add();
            Column meterActivationColumn = table.column("METERACTIVATIONID").number().conversion(NUMBER2LONG).upTo(version(10, 2)).add();
            Column channelContainer = table.column("CHANNEL_CONTAINER").number().conversion(NUMBER2LONG).since(version(10, 2)).previously(meterActivationColumn).add();
            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").add();
            table.column("ACTIVE").bool().map("active").add();
            Column obsoleteColumn = table.column("OBSOLETETIME").number().conversion(NUMBER2INSTANT).map("obsoleteTime").add();
            table.primaryKey("VAL_PK_MA_VALIDATION").on(idColumn).add();
            table.foreignKey("VAL_FK_MA_VALIDATION_MA").references(ChannelsContainer.class).onDelete(RESTRICT).map("channelsContainer").on(channelContainer).add();
            table.foreignKey("VAL_FK_MA_VALIDATION_VRS").references(VAL_VALIDATIONRULESET.name()).on(ruleSetColumn).onDelete(DeleteRule.RESTRICT)
                    .map("ruleSet", ValidationRuleSetVersionImpl.class, ValidationRuleImpl.class, ReadingTypeInValidationRule.class).add();
            table.unique("VAL_MA_VALIDATION_U").on(ruleSetColumn, channelContainer, obsoleteColumn).add();
        }
    },
    VAL_CH_VALIDATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<ChannelValidation> table = dataModel.addTable(name(), ChannelValidation.class);
            table.map(ChannelValidationImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column channelRef = table.column("CHANNELID").number().notNull().conversion(NUMBER2LONG).map("channelId").add();
            Column channelsContainerValidationColumn = table.column("MAV_ID").number().conversion(NUMBER2LONG).add();
            table.column("LASTCHECKED").number().conversion(NUMBER2INSTANT).map("lastChecked").add();
            table.column("ACTIVERULES").bool().map("activeRules").add();
            table.primaryKey("VAL_PK_CH_VALIDATION").on(idColumn).add();
            table.foreignKey("VAL_FK_CH_VALIDATION_CH").references(Channel.class).onDelete(RESTRICT).on(channelRef).map("channel").add();
            table.foreignKey("VAL_FK_CH_VALIDATION_MA_VAL")
                    .references(VAL_MA_VALIDATION.name())
                    .onDelete(DeleteRule.CASCADE)
                    .map("channelsContainerValidation")
                    .reverseMap("channelValidations")
                    .composition()
                    .on(channelsContainerValidationColumn)
                    .add();
        }
    },
    VAL_METER_VALIDATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<MeterValidationImpl> table = dataModel.addTable(name(), MeterValidationImpl.class);
            table.map(MeterValidationImpl.class);
            Column meterId = table.column("METERID").number().notNull().conversion(NUMBER2LONG).add();
            table.column("ACTIVE").bool().map("isActive").add();
            table.column("VALIDATEONSTORAGE").bool().map("validateOnStorage").add();
            table.primaryKey("VAL_PK_MA_METER_VALIDATION").on(meterId).add();
            table
                    .foreignKey("VAL_FK_MA_METER_VALIDATION")
                    .references(EndDevice.class)
                    .onDelete(RESTRICT)
                    .map("meter")
                    .on(meterId)
                    .add();
        }
    },

    VAL_READINGTYPEINVALRULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeInValidationRule> table = dataModel.addTable(name(), ReadingTypeInValidationRule.class);
            table.map(ReadingTypeInValidationRuleImpl.class);
            Column ruleIdColumn = table.column("RULEID").number().notNull().conversion(NUMBER2LONG).add();
            Column readingTypeMRIDColumn = table.column("READINGTYPEMRID").varChar(NAME_LENGTH).notNull().map("readingTypeMRID").add();
            table.primaryKey("VAL_PK_RTYPEINVALRULE").on(ruleIdColumn, readingTypeMRIDColumn).add();
            table.foreignKey("VAL_FK_RTYPEINVALRULE_RULE").references(VAL_VALIDATIONRULE.name()).onDelete(DeleteRule.CASCADE).map("rule").reverseMap("readingTypesInRule").composition()
                    .on(ruleIdColumn).add();
            table
                    .foreignKey("VAL_FK_RTYPEINVALRULE_RTYPE")
                    .references(ReadingType.class)
                    .onDelete(RESTRICT)
                    .map("readingType")
                    .on(readingTypeMRIDColumn).add();
        }
    },
    VAL_DATAVALIDATIONTASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<DataValidationTask> table = dataModel.addTable(name(), DataValidationTask.class);
            table.map(DataValidationTaskImpl.class);
            table.setJournalTableName("VAL_DATAVALIDATIONTASKJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column endDeviceGroupId = table.column("ENDDEVICEGROUP").number().conversion(ColumnConversion.NUMBER2LONG).add();
            Column metrologyContractId = table.column("METROLOGYCONTRACT").number().conversion(ColumnConversion.NUMBER2LONG).upTo(version(10, 3)).add();
            Column usagePointGroupId = table.column("USAGEPOINTGROUP").number().conversion(ColumnConversion.NUMBER2LONG).since(version(10, 3)).add();
            Column recurrentTaskId = table.column("RECURRENTTASK").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").notAudited().add();
            table.column("QUALITY_SYSTEM").number().conversion(ColumnConversion.NUMBER2ENUM).map("qualityCodeSystem").add();
            table.addAuditColumns();
            table.foreignKey("VAL_FK_VALTASK2DEVICEGROUP")
                    .on(endDeviceGroupId)
                    .references(EndDeviceGroup.class)
                    .map("endDeviceGroup")
                    .add();

            table.foreignKey("VAL_FK_METROLOGYCONTRACT")
                    .on(metrologyContractId)
                    .references(MetrologyContract.class)
                    .map("metrologyContract")
                    .upTo(version(10,3))
                    .add();
            table.foreignKey("VAL_FK_USAGEPOUNTGROUP")
                    .on(usagePointGroupId)
                    .references(UsagePointGroup.class)
                    .map("usagePointGroup")
                    .since(version(10, 3))
                    .add();
            table.primaryKey("VAL_PK_DATAVALIDATIONTASK")
                    .on(idColumn)
                    .add();
            table.foreignKey("VAL_FK_RECURRENTTASK")
                    .on(recurrentTaskId)
                    .references(RecurrentTask.class)
                    .map("recurrentTask")
                    .add();
        }
    },
    VAL_OCCURRENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<DataValidationOccurrence> table = dataModel.addTable(name(), DataValidationOccurrence.class);
            table.map(DataValidationOccurrenceImpl.class);
            Column taskOccurrence = table.column("TASKOCC").number().notNull().add();
            Column dataValidationTask = table.column("DATAVALIDATIONTASK").number().notNull().add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map("status").add();
            table.column("MESSAGE").varChar(Table.SHORT_DESCRIPTION_LENGTH).map("failureReason").add();

            table.primaryKey("VAL_PK_VALIDATIONOCC")
                    .on(taskOccurrence)
                    .add();
            table.foreignKey("VAL_FK_VALOCC_TSKOCC")
                    .on(taskOccurrence)
                    .references(TaskOccurrence.class)
                    .map("taskOccurrence").refPartition().add();
            table.foreignKey("VAL_FK_OCC_VALIDATIONTASK").on(dataValidationTask).references(VAL_DATAVALIDATIONTASK.name())
                    .map("dataValidationTask").add();

        }
    },
    VAL_DATA_VALIDATION_KPI {
        @Override
        void addTo(DataModel dataModel) {
            Table<DataValidationKpi> table = dataModel.addTable(name(), DataValidationKpi.class);
            table.map(DataValidationKpiImpl.class);
            table.since(version(10, 2));
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column endDeviceGroup = table.column("ENDDEVICEGROUP").number().notNull().add();
            Column dataValidationKpiTask = table.column("DATAVALIDATIONKPI_TASK").number().add();

            table.primaryKey("PK_DDC_DATA_VALIDATION_KPI").on(id).add();
            table.foreignKey("FK_DDC_VAL_ENDDEVICEGROUP").
                    on(endDeviceGroup).
                    references(EndDeviceGroup.class).
                    map(DataValidationKpiImpl.Fields.END_DEVICE_GROUP.fieldName()).
                    add();
            table.foreignKey("FK_DDC_VAL_KPI_TASK").
                    on(dataValidationKpiTask).
                    references(RecurrentTask.class).
                    map(DataValidationKpiImpl.Fields.DATA_VALIDATION_KPI_TASK.fieldName()).
                    add();
        }
    },

    VAL_DATAVALIDATIONKPICHILDREN {
        @Override
        void addTo(DataModel dataModel) {
            Table<DataValidationKpiChild> table = dataModel.addTable(name(), DataValidationKpiChild.class);
            table.map(DataValidationKpiChildImpl.class);
            table.since(version(10, 2));
            Column dataValidationKpiColumn = table.column("DATAVALIDATIONKPI").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column childKpiColumn = table.column("CHILDKPI").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.primaryKey("VAL_PK_DATAVALKPICHILDREN").on(dataValidationKpiColumn, childKpiColumn).add();
            table.foreignKey("VAL_FK_DATAVALKPICHILDRENKPI")
                    .references(VAL_DATA_VALIDATION_KPI.name())
                    .on(dataValidationKpiColumn)
                    .onDelete(CASCADE)
                    .map(DataValidationKpiChildImpl.Fields.DATAVALIDATIONKPI.fieldName())
                    .reverseMap(DataValidationKpiImpl.Fields.CHILDREN_KPIS.fieldName())
                    .composition().add();
            table.foreignKey("VAL_FK_VALKPICHILDRENKPICHILD")
                    .references(Kpi.class)
                    .on(childKpiColumn)
                    .onDelete(CASCADE)
                    .map(DataValidationKpiChildImpl.Fields.CHILDKPI.fieldName())
                    .composition().add();
        }
    };

    abstract void addTo(DataModel component);

}