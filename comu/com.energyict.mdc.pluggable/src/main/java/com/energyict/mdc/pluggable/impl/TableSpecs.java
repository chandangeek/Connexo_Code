package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.pluggable.PluggableClass;

import static com.elster.jupiter.orm.Table.*;

/**
 * Models the database tables that hold the data of the {@link PluggableClass}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:27)
 */
public enum TableSpecs {

    CPC_PLUGGABLECLASS {
        @Override
        void addTo(DataModel dataModel) {
            Table<PluggableClass> table = dataModel.addTable(name(), PluggableClass.class);
            table.map(PluggableClassImpl.class).cache();
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("PK_CPC_PLUGGABLE").on(idColumn).add();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("JAVACLASSNAME").type("varchar2(512)").map("javaClassName").add();
            table.column("PLUGGABLETYPE").number().notNull().conversion(ColumnConversion.NUMBER2ENUMPLUSONE).map("pluggableType").add();
            table.column("MODTIME").number().notNull().conversion(ColumnConversion.NUMBER2INSTANT).map("modTime").add();
        }
    },

    CPC_PLUGGABLECLASSPROPERTIES {
        @Override
        void addTo(DataModel dataModel) {
            Table<PluggableClassProperty> table = dataModel.addTable(name(), PluggableClassProperty.class);
            table.map(PluggableClassProperty.class);
            Column pluggableClassColumn = table.column("PLUGGABLECLASSID").number().notNull().add();
            Column nameColumn = table.column("NAME").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("name").add();
            table.addAuditColumns();
            table.primaryKey("PK_CPC_PLUGGABLECLASS_PROPS").on(pluggableClassColumn, nameColumn).add();
            table.column("VALUE").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("value").add();
            table.
                foreignKey("FK_CPC_PLUGGABLEPROP_PLUGGABLE").
                on(pluggableClassColumn).
                references(CPC_PLUGGABLECLASS.name()).
                map("pluggableClass").
                reverseMap("properties").
                composition().
                add();
        }
    };

    abstract void addTo(DataModel component);

}