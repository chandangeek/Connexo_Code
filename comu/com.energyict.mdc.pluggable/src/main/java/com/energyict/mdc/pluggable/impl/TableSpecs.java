package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.pluggable.PluggableClass;

/**
 * Models the database tables that hold the data of the {@link PluggableClass}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:27)
 */
public enum TableSpecs {

    EISPLUGGABLECLASS {
        @Override
        void addTo(DataModel dataModel) {
            Table<PluggableClass> table = dataModel.addTable(name(), PluggableClass.class);
            table.map(PluggableClassImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("CPC_PK_PLUGGABLE").on(idColumn).add();
            table.column("NAME").type("varchar2(80)").notNull().map("name").add();
            table.column("JAVACLASSNAME").type("varchar2(512)").map("javaClassName").add();
            table.column("PLUGGABLETYPE").number().notNull().conversion(ColumnConversion.NUMBER2ENUM).map("pluggableType").add();
            table.column("MOD_DATE").number().notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
        }
    },

    EISPLUGGABLECLASSPROPERTIES {
        @Override
        void addTo(DataModel dataModel) {
            Table<PluggableClassProperty> table = dataModel.addTable(name(), PluggableClassProperty.class);
            table.map(PluggableClassProperty.class);
            Column pluggableClassColumn = table.column("PLUGGABLECLASSID").number().notNull().add();
            Column nameColumn = table.column("NAME").type("varchar2(256)").notNull().map("name").add();
            table.primaryKey("PK_PLUGGABLECLASS_PROPS").on(pluggableClassColumn, nameColumn).add();
            table.column("VALUE").type("varchar2(256)").notNull().map("value").add();
            table.foreignKey("FK_PLUGGABLEPROP_PLUGGABLE").on(pluggableClassColumn).references(EISPLUGGABLECLASS.name()).
                    map("pluggableClass").reverseMap("properties").composition().add();
        }
    };

    abstract void addTo(DataModel component);

}