package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

import com.elster.insight.usagepoint.config.Formula;
import com.elster.insight.usagepoint.config.MetrologyConfigurationValidationRuleSetUsage;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.insight.usagepoint.config.impl.aggregation.AbstractNode;
import com.elster.insight.usagepoint.config.impl.aggregation.ExpressionNode;
import com.elster.insight.usagepoint.config.impl.aggregation.ServerFormulaImpl;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;

public enum TableSpecs {
    UPC_METROLOGYCONFIG {
        void addTo(DataModel dataModel) {
            Table<MetrologyConfiguration> table = dataModel.addTable(name(), MetrologyConfiguration.class);
            table.map(MetrologyConfigurationImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column(MetrologyConfigurationImpl.Fields.NAME.name()).varChar().notNull().map(MetrologyConfigurationImpl.Fields.NAME.fieldName()).add();
            table.column(MetrologyConfigurationImpl.Fields.ACTIVE.name()).bool().map(MetrologyConfigurationImpl.Fields.ACTIVE.fieldName()).notNull().add();
            table.addAuditColumns();
            table.unique("UPC_UK_METROLOGYCONFIGURATION").on(name).add();
            table.primaryKey("UPC_PK_METROLOGYCONFIGURATION").on(id).add();
        }
    },
    UPC_UPMC {
        void addTo(DataModel dataModel) {
            Table<UsagePointMetrologyConfiguration> table = dataModel
                    .addTable(name(), UsagePointMetrologyConfiguration.class);
            table.map(UsagePointMetrologyConfigurationImpl.class);
            Column id = table.addAutoIdColumn();
            Column usagePointIdColumn = table
                    .column("USAGEPOINTID")
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column metrologyConfigIdColumn = table
                    .column("METROLOGYCONFIGID")
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            table.addAuditColumns();
            table.primaryKey("UPC_PK_UPMC").on(id).add();
            table.unique("UPC_UK_UPMCUP").on(usagePointIdColumn).add();
            table.foreignKey("UPC_FK_UPMCUP")
                    .on(usagePointIdColumn)
                    .references(UsagePoint.class)
                    .onDelete(DeleteRule.RESTRICT)
                    .map("usagePoint")
                    .add();
            table.foreignKey("UPC_FK_UPMCMC")
                    .on(metrologyConfigIdColumn)
                    .references(UPC_METROLOGYCONFIG.name())
                    .onDelete(DeleteRule.RESTRICT)
                    .map("metrologyConfiguration")
                    .add();
        }
    },
    UPC_MCVALRULESETUSAGE {
        void addTo(DataModel dataModel) {
            Table<MetrologyConfigurationValidationRuleSetUsage> table = dataModel
                    .addTable(name(), MetrologyConfigurationValidationRuleSetUsage.class);
            table.map(MetrologyConfigurationValidationRuleSetUsageImpl.class);
            table.setJournalTableName("UPC_MCVALRULESETUSAGEJRNL");
            Column validationRuleSetIdColumn = table
                    .column("VALIDATIONRULESETID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column metrologyConfigurationIdColumn = table
                    .column("METROLOGYCONFIGID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();

            table.primaryKey("UPC_PK_SETCONFIGUSAGE")
                    .on(validationRuleSetIdColumn, metrologyConfigurationIdColumn).add();
            table.foreignKey("UPC_FK_RULESET")
                    .references(ValidationRuleSet.class)
                    .onDelete(RESTRICT)
                    .map("validationRuleSet")
                    .on(validationRuleSetIdColumn)
                    .add();
            table.foreignKey("UPC_FK_METROLOGYCONFIG")
                    .references("UPC_METROLOGYCONFIG")
                    .reverseMap("metrologyConfValidationRuleSetUsages")
                    .composition().map("metrologyConfiguration")
                    .on(metrologyConfigurationIdColumn)
                    .add();
        }
    },
    UPC_M_CONFIG_CPS_USAGES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<MetrologyConfigurationCustomPropertySetUsage> table = dataModel.addTable(name(), MetrologyConfigurationCustomPropertySetUsage.class);
            table.map(MetrologyConfigurationCustomPropertySetUsageImpl.class);
            Column metrologyConfig = table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.METROLOGY_CONFIG.name()).number().notNull().add();
            Column customPropertySet = table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.CUSTOM_PROPERTY_SET.name()).number().notNull().add();
            table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.name()).number().notNull().conversion(NUMBER2INT).map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.fieldName()).add();
            table.primaryKey("PK_M_CONFIG_CPS_USAGE").on(metrologyConfig, customPropertySet).add();
            table.foreignKey("FK_MCPS_USAGE_TO_CONFIG")
                    .references(UPC_METROLOGYCONFIG.name())
                    .on(metrologyConfig)
                    .onDelete(CASCADE)
                    .map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.METROLOGY_CONFIG.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.CUSTOM_PROPERTY_SETS.fieldName())
                    .reverseMapOrder(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_MCAS_USAGE_TO_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .onDelete(CASCADE)
                    .map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.CUSTOM_PROPERTY_SET.fieldName())
                    .add();
        }
    },
    UPC_FORMULA_NODE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ExpressionNode> table = dataModel.addTable(name(),ExpressionNode.class);
            table.map(AbstractNode.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("NODETYPE", "char(3)");

            // parent node
            Column parentColumn = table.column("PARENTID").number().conversion(NUMBER2LONG).add();

            Column argumentIndex = table.column("ARGUMENTINDEX").number().notNull().map("argumentIndex").conversion(NUMBER2INT).add();

            //OperationNode operator value
            table.column("OPERATOR").number().conversion(ColumnConversion.NUMBER2ENUM).map("operator").add();

            //FunctionCallNode function value
            table.column("FUNCTION").number().conversion(ColumnConversion.NUMBER2ENUM).map("function").add();

            //ConstantNode constantValue
            table.column("CONSTANTVALUE").number().conversion(NUMBER2LONG).map("constantValue").add();

            // ReadingTypeDeliverableNode readingTypeDeliverable value
            //todo add foreign key
            Column readingTypeDeliverableIdColumn = table.column("READINGTYPE_DELIVERABLE").number().conversion(NUMBER2LONG).map("readingTypeDeliverable").add();

            // ReadingTypeRequirementNode readingTypeRequirement value
            //todo add foreign key
            Column readingTypeRequirementIdColumn = table.column("READINGTYPE_REQUIREMENT").number().conversion(NUMBER2LONG).map("readingTypeRequirement").add();

            table.primaryKey("UPC_PK_FORMULA_NODE").on(idColumn).add();

            table.foreignKey("UPC_VALIDCHILD").references(UPC_FORMULA_NODE.name()).on(parentColumn).onDelete(DeleteRule.CASCADE)
                    .map("parent").reverseMap("children").reverseMapOrder("argumentIndex").add();
        }
    },
    UPC_FORMULA {
        @Override
        void addTo(DataModel dataModel) {
            Table<Formula> table = dataModel.addTable(name(), Formula.class);
            table.map(ServerFormulaImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("MODE").number().conversion(ColumnConversion.NUMBER2ENUM).map("mode").add();
            Column expressionNodeColumn = table.column("EXPRESSION_NODE_ID").number().conversion(NUMBER2LONG).add();
            table.primaryKey("UPC_PK_FORMULA").on(idColumn).add();
            table.foreignKey("UPC_VALIDNODE").references(UPC_FORMULA_NODE.name()).on(expressionNodeColumn).onDelete(DeleteRule.CASCADE)
                    .map("expressionNode").add();
        }
    };

    abstract void addTo(DataModel component);
}