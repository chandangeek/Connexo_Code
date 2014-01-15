package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

/**
 * Models the database tables that hold the data of the {@link PluggableClassRelationAttributeTypeUsage}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:27)
 */
public enum TableSpecs {

    MDCPCRATUSAGE {
        @Override
        Class apiClass() {
            return PluggableClassRelationAttributeTypeUsage.class;
        }

        @Override
        void describeTable(Table table) {
            table.map(PluggableClassRelationAttributeTypeUsage.class);
            Column pluggableClassColumn = table.column("PLUGGABLECLASS").number().notNull().map("pluggableClassId").add();
            Column relationAttributeTypeColumn = table.column("RELATIONATTRIBUTETYPE").
                                                    number().conversion(ColumnConversion.NUMBER2INT).
                                                    notNull().
                                                    map("relationAttributeTypeId").
                                                    add();
            table.primaryKey("PK_PCRATUSAGE").on(pluggableClassColumn, relationAttributeTypeColumn).add();
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name(), this.apiClass());
        describeTable(table);
    }

    abstract void describeTable(Table table);

    abstract Class apiClass ();

}