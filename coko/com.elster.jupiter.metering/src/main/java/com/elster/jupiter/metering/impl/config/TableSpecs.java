package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

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
    };

    abstract void addTo(DataModel component);

}