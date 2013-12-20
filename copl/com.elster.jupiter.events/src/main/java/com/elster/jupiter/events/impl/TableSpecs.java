package com.elster.jupiter.events.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {

    EVT_EVENTTYPE {
        @Override
        void describeTable(Table table) {
            table.map(EventTypeImpl.class);
            Column topicColumn = table.column("TOPIC").type("varchar(80)").notNull().map("topic").add();
            table.column("COMPONENT").type("varchar(3)").notNull().map("component").add();
            table.column("SCOPE").type("varchar(80)").notNull().map("scope").add();
            table.column("CATEGORY").type("varchar(80)").notNull().map("category").add();
            table.column("NAME").type("varchar(80)").notNull().map("name").add();
            table.column("PUBLISH").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("publish").add();

            table.primaryKey("EVT_PK_EVENTTYPE").on(topicColumn).add();
        }
    },
    EVT_EVENTPROPERTYTYPE {
        @Override
        void describeTable(Table table) {
            table.map(EventPropertyTypeImpl.class);
            Column topicColumn = table.column("TOPIC").type("varchar(80)").notNull().map("eventTypeTopic").add();
            Column nameColumn = table.column("NAME").type("varchar(80)").notNull().map("name").add();
            table.column("TYPE").type("number").notNull().conversion(NUMBER2ENUM).map("valueType").add();
            table.column("ACCESSPATH").type("varchar(80)").notNull().map("accessPath").add();
            Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();

            table.primaryKey("EVT_PK_EVENTPROPERTYTYPE").on(topicColumn, nameColumn).add();
            table.unique("EVT_UK_EVENTPROPERTYTYPE").on(topicColumn, positionColumn).add();
            table.foreignKey("EVT_FK_EVENTTYPE_PROPERTY").references(EVT_EVENTTYPE.name()).onDelete(DeleteRule.CASCADE).map("eventType").reverseMap("eventPropertyTypes").on(topicColumn).composition().add();
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);

}
