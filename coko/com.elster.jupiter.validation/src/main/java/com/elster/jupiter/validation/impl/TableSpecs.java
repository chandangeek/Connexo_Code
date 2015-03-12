package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.*;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.validation.*;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.*;

public enum TableSpecs {

    VAL_VALIDATIONRULESET {
    	@Override
        void addTo(DataModel dataModel) {
        	Table<ValidationRuleSet> table = dataModel.addTable(name(), ValidationRuleSet.class);
            table.map(ValidationRuleSetImpl.class);          
            table.setJournalTableName("VAL_VALIDATIONRULESETJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(DESCRIPTION_LENGTH).map("description").add();
            table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("VAL_PK_VALIDATIONRULESET").on(idColumn).add();
            table.unique("VAL_U_VALIDATIONRULESET").on(mRIDColumn).add();
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
            Column ruleSetIdColumn = table.column("RULESETID").number().notNull().conversion(NUMBER2LONG).add();
            table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("VAL_PK_VALIDATIONRULE").on(idColumn).add();
            table.foreignKey("VAL_FK_RULE").references("VAL_VALIDATIONRULESET").on(ruleSetIdColumn).onDelete(RESTRICT)
            	.map("ruleSet").reverseMap("rules").composition().reverseMapOrder("position").add();
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
        	Table<IMeterActivationValidation> table = dataModel.addTable(name(), IMeterActivationValidation.class);
        	table.map(MeterActivationValidationImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column ruleSetIdColumn = table.column("RULESETID").number().conversion(NUMBER2LONG).add();
            Column meterActivationId = table.column("METERACTIVATIONID").number().conversion(NUMBER2LONG).add();
            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").add();
            table.column("ACTIVE").bool().map("active").add();
            table.column("OBSOLETETIME").number().conversion(NUMBER2INSTANT).map("obsoleteTime").add();
            table.primaryKey("VAL_PK_MA_VALIDATION").on(idColumn).add();
            table.foreignKey("VAL_FK_MA_VALIDATION_MA").references(MeteringService.COMPONENTNAME, "MTR_METERACTIVATION").onDelete(RESTRICT).map("meterActivation").on(meterActivationId).add();
            table.foreignKey("VAL_FK_MA_VALIDATION_VRS").references(VAL_VALIDATIONRULESET.name()).on(ruleSetIdColumn).onDelete(DeleteRule.RESTRICT)
            	.map("ruleSet", ValidationRuleImpl.class, ReadingTypeInValidationRule.class).add();
        }
    },
    VAL_CH_VALIDATION {
        @Override
        void addTo(DataModel dataModel) {
        	Table<IChannelValidation> table = dataModel.addTable(name(), IChannelValidation.class);
            table.map(ChannelValidationImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column channelRef = table.column("CHANNELID").number().notNull().conversion(NUMBER2LONG).map("channelId").add();
            Column meterActivationValidationColumn = table.column("MAV_ID").number().conversion(NUMBER2LONG).add();
            table.column("LASTCHECKED").number().conversion(NUMBER2INSTANT).map("lastChecked").add();
            table.column("ACTIVERULES").bool().map("activeRules").add();
            table.primaryKey("VAL_PK_CH_VALIDATION").on(idColumn).add();
            table.foreignKey("VAL_FK_CH_VALIDATION_CH").references(MeteringService.COMPONENTNAME, "MTR_CHANNEL").onDelete(RESTRICT).on(channelRef).map("channel").add();
            table.foreignKey("VAL_FK_CH_VALIDATION_MA_VAL").references(VAL_MA_VALIDATION.name()).onDelete(DeleteRule.CASCADE).map("meterActivationValidation").reverseMap("channelValidations")
                    .composition().on(meterActivationValidationColumn).add();
        }
    },
    VAL_METER_VALIDATION {
        @Override
        void addTo(DataModel dataModel) {
        	Table<MeterValidationImpl> table = dataModel.addTable(name(), MeterValidationImpl.class);        
            table.map(MeterValidationImpl.class);
            Column meterId = table.column("METERID").number().notNull().conversion(NUMBER2LONG).add();
            table.column("ACTIVE").bool().map("isActive").add();
            table.primaryKey("VAL_PK_MA_METER_VALIDATION").on(meterId).add();
            table.foreignKey("VAL_FK_MA_METER_VALIDATION").references(MeteringService.COMPONENTNAME, "MTR_ENDDEVICE").onDelete(RESTRICT).map("meter").on(meterId).add();
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
            table.foreignKey("VAL_FK_RTYPEINVALRULE_RULE").references(VAL_VALIDATIONRULE.name()).onDelete(DeleteRule.CASCADE).map("rule").reverseMap("readingTypesInRule").composition().on(ruleIdColumn).add();
            table.foreignKey("VAL_FK_RTYPEINVALRULE_RTYPE").references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE").onDelete(RESTRICT).map("readingType").on(readingTypeMRIDColumn).add();
        }
    },
    VAL_DATAVALIDATIONTASK{
        @Override
        void addTo(DataModel dataModel){
            Table<DataValidationTask> table = dataModel.addTable(name(),DataValidationTask.class);
            table.map(DataValidationTaskImpl.class);
            table.setJournalTableName("VAL_DATAVALIDATIONTASKJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            Column endDeviceGroupId = table.column("ENDDEVICEGROUP").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column recurrentTaskId = table.column("RECURRENTTASK").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").add();
            table.addAuditColumns();
            table.foreignKey("VAL_FK_VALTASK2DEVICEGROUP")
                    .on(endDeviceGroupId)
                    .references(MeteringGroupsService.COMPONENTNAME, "MTG_ED_GROUP")
                    .map("endDeviceGroup")
                    .add();
            table.primaryKey("VAL_PK_DATAVALIDATIONTASK")
                    .on(idColumn)
                    .add();
            table.foreignKey("VAL_FK_RECURRENTTASK")
                    .on(recurrentTaskId)
                    .references(TaskService.COMPONENTNAME, "TSK_RECURRENT_TASK")
                    .map("recurrentTask")
                    .add();
        }
//    },
//    VAL_OCCURENCE{
//        @Override
//        void addTo(DataModel dataModel){
//            Table<DataValidationOccurence> table = dataModel.addTable(name(),DataValidationOccurence.class);
//            table.map(DataValidationOccurenceImpl.class);
//
//            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map("status").add();
//
//        }
    };


    abstract void addTo(DataModel component);
        
}