package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {

    EVT_EVENTTYPE(EventType.class) {
        @Override
        void describeTable(Table table) {
        	table.map(EventTypeImpl.class);
        	table.cache();
            Column topicColumn = table.column("TOPIC").varChar(80).notNull().map("topic").add();
            table.column("COMPONENT").varChar(3).notNull().map("component").add();
            table.column("SCOPE").varChar(80).notNull().map("scope").add();
            table.column("CATEGORY").varChar(80).notNull().map("category").add();
            table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("PUBLISH").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("publish").add();

            table.primaryKey("EVT_PK_EVENTTYPE").on(topicColumn).add();
        }
    },
    EVT_EVENTPROPERTYTYPE(EventPropertyType.class) {
        @Override
        void describeTable(Table table) {
        	table.map(EventPropertyTypeImpl.class);
            Column topicColumn = table.column("TOPIC").varChar(80).notNull().map("eventTypeTopic").add();
            Column nameColumn = table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("TYPE").number().notNull().conversion(NUMBER2ENUM).map("valueType").add();
            table.column("ACCESSPATH").varChar(80).notNull().map("accessPath").add();
            Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();

            table.primaryKey("EVT_PK_EVENTPROPERTYTYPE").on(topicColumn, nameColumn).add();
            table.unique("EVT_UK_EVENTPROPERTYTYPE").on(topicColumn, positionColumn).add();
            table.foreignKey("EVT_FK_EVENTTYPE_PROPERTY").references(EVT_EVENTTYPE.name()).onDelete(DeleteRule.CASCADE).map("eventType").reverseMap("eventPropertyTypes").on(topicColumn).composition().add();
        }
    };

    private Class<?> api;
    
    TableSpecs(Class<?> api) {
    	this.api = api;
    }
    
    public void addTo(DataModel component) {
        Table table = component.addTable(name(),api);
        describeTable(table);
    }

    abstract void describeTable(Table table);

}
