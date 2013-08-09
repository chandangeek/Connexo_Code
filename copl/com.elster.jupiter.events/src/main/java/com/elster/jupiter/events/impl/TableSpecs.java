package com.elster.jupiter.events.impl;

import com.elster.jupiter.orm.AssociationMapping;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {

    EVT_EVENTTYPE {
        @Override
        void describeTable(Table table) {
            Column topicColumn = table.addColumn("TOPIC", "varchar(80)", true, NOCONVERSION, "topic");
            table.addColumn("COMPONENT", "varchar(3)", true, NOCONVERSION, "component");
            table.addColumn("SCOPE", "varchar(80)", true, NOCONVERSION, "scope");
            table.addColumn("CATEGORY", "varchar(80)", true, NOCONVERSION, "category");
            table.addColumn("NAPE", "varchar(80)", true, NOCONVERSION, "name");
            table.addColumn("PUBLISH", "char(1)", true, CHAR2BOOLEAN, "publish");

            table.addPrimaryKeyConstraint("EVT_PK_EVENTTYPE", topicColumn);
        }
    },
    EVT_EVENTPROPERTYTYPE {
        @Override
        void describeTable(Table table) {
            Column topicColumn = table.addColumn("TOPIC", "varchar(80)", true, NOCONVERSION, "eventTypeTopic");
            Column nameColumn = table.addColumn("NAME", "varchar(80)", true, NOCONVERSION, "name");
            table.addColumn("TYPE", "number", true, NUMBER2ENUM, "valueType");
            table.addColumn("ACCESSPATH", "varchar(80)", true, NOCONVERSION, "accessPath");
            Column positionColumn = table.addColumn("POSITION", "number", true, NUMBER2INT, "position");

            table.addPrimaryKeyConstraint("EVT_PK_EVENTPROPERTYTYPE", topicColumn, nameColumn);
            table.addUniqueConstraint("EVT_UK_EVENTPROPERTYTYPE", topicColumn, positionColumn);
            table.addForeignKeyConstraint("EVT_FK_EVENTTYPE_PROPERY", EVT_EVENTTYPE.name(), DeleteRule.CASCADE, new AssociationMapping("eventType"), topicColumn);
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);

}
