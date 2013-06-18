package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NOCONVERSION;

enum TableSpecs {

    RECURRENT_TASK {
        @Override
        void describeTable(Table table) {
            Column nameColumn = table.addColumn("NAME", "varchar2(80)" , true , NOCONVERSION , "name");
            table.addColumn("COMPONENT","varchar2(3)",true,NOCONVERSION,"componentName");
            table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION, "description");
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.addPrimaryKeyConstraint("USR_PK_PRIVILEGES",nameColumn);
        }
    };

    void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);


}
