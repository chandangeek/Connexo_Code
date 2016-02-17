package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.insight.usagepoint.config.Formula;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.insight.usagepoint.config.impl.aggregation.AbstractNode;
import com.elster.insight.usagepoint.config.impl.aggregation.ExpressionNode;
import com.elster.insight.usagepoint.config.impl.aggregation.ServerFormulaImpl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
    UPC_MCVALRULESETUSAGE {
        void addTo(DataModel dataModel) {
            Table<MetrologyConfigurationValidationRuleSetUsage> table = dataModel
                    .addTable(name(), MetrologyConfigurationValidationRuleSetUsage.class);
            table.map(MetrologyConfigurationValidationRuleSetUsageImpl.class);
            table.setJournalTableName("UPC_MCVALRULESETUSAGEJRNL");
            Column validationRule = table
                    .column("VALIDATIONRULESETID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column metrologyConfiguration = table
                    .column("METROLOGYCONFIGID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");

            table.primaryKey("UPC_PK_SETCONFIGUSAGE")
                    .on(validationRule, metrologyConfiguration, intervalColumns.get(0))
                    .add();
            table.foreignKey("UPC_FK_RULESET")
                    .references(ValidationRuleSet.class)
                    .onDelete(RESTRICT)
                    .map(MetrologyConfigurationValidationRuleSetUsageImpl.Fields.VALIDATION_RULE_SET.fieldName())
                    .on(validationRule)
                    .add();
            table.foreignKey("UPC_FK_METROLOGYCONFIG")
                    .references(MetrologyConfiguration.class)
                    .map(MetrologyConfigurationValidationRuleSetUsageImpl.Fields.METROLOGY_CONFIGURATION.fieldName())
                    .on(metrologyConfiguration)
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
            table.column("CONSTANTVALUE").number().map("constantValue").add();

            // ReadingTypeDeliverableNode readingTypeDeliverable value
            //todo add foreign key
            Column readingTypeDeliverableIdColumn = table.column("READINGTYPE_DELIVERABLE").number().conversion(NUMBER2LONG).map("readingTypeDeliverable").add();

            // ReadingTypeRequirementNode readingTypeRequirement value
            //todo add foreign key
            Column readingTypeRequirementIdColumn = table.column("READINGTYPE_REQUIREMENT").number().conversion(NUMBER2LONG).map("readingTypeRequirement").add();

            table.primaryKey("UPC_PK_FORMULA_NODE").on(idColumn).add();

            table.foreignKey("UPC_VALIDCHILD").references(UPC_FORMULA_NODE.name()).on(parentColumn).onDelete(CASCADE)
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
            table.foreignKey("UPC_VALIDNODE").references(UPC_FORMULA_NODE.name()).on(expressionNodeColumn).onDelete(CASCADE)
                    .map("expressionNode").add();
        }
    };

    abstract void addTo(DataModel component);

}