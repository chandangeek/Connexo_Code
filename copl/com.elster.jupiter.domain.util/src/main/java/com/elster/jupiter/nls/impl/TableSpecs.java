package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.DeleteRule.CASCADE;

enum TableSpecs {

    NLS_KEY {
        @Override
        void addTo(DataModel component) {
            Table<NlsKey> table = component.addTable(name(), NlsKey.class);

            table.map(NlsKeyImpl.class);
            Column componentColumn = table.column("COMPONENT").type("varchar2(3)").notNull().map("componentName").add();
            Column layerColumn = table.column("LAYER").type("varchar2(10)").notNull().conversion(ColumnConversion.CHAR2ENUM).map("layer").add();
            Column keyColumn = table.column("KEY").type("varchar2(64)").notNull().map("key").add();
            table.column("defaultMessage").type("varchar2(256)").map("defaultMessage").add();
            table.primaryKey("NLS_PK_NLSKEY").on(componentColumn, layerColumn, keyColumn).add();
        }
    },
    NLS_ENTRY {
        @Override
        void addTo(DataModel component) {
            Table<NlsEntry> table = component.addTable(name(), NlsEntry.class);
            table.map(NlsEntry.class);
            Column componentColumn = table.column("COMPONENT").type("varchar2(3)").notNull().add();
            Column layerColumn = table.column("LAYER").type("varchar2(10)").notNull().conversion(ColumnConversion.CHAR2ENUM).add();
            Column keyColumn = table.column("KEY").type("varchar2(64)").notNull().add();
            Column languageTag = table.column("LANGUAGETAG").type("varchar2(20)").notNull().map("languageTag").add();
            table.column("TRANSLATION").type("varchar2(256)").map("translation").add();
            table.primaryKey("NLS_PK_NLSENTRY").on(componentColumn, layerColumn, keyColumn, languageTag).add();
            table.foreignKey("NLS_FK_KEY_ENTRY").on(componentColumn, layerColumn, keyColumn).references(NLS_KEY.name()).onDelete(CASCADE).map("nlsKey").reverseMap("entries").composition().add();
        }
    };

    abstract void addTo(DataModel component);

}
