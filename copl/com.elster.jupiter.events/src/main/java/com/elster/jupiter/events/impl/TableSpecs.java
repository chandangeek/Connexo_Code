/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

public enum TableSpecs {

    EVT_EVENTTYPE {
        @Override
        void addTo(DataModel dataModel) {
        	Table<EventType> table = dataModel.addTable(name(), EventType.class);
        	table.map(EventTypeImpl.class);
        	table.cache();
            Column topicColumn = table.column("TOPIC").varChar(NAME_LENGTH).notNull().map("topic").add();
            table.column("COMPONENT").varChar(3).notNull().map("component").add();
            table.column("SCOPE").varChar(NAME_LENGTH).notNull().map("scope").add();
            table.column("CATEGORY").varChar(NAME_LENGTH).notNull().map("category").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("PUBLISH").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("publish").add();
            table.column("FSMENABLED").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("fsmEnabled").add();
            table.addAuditColumns();

            table.primaryKey("EVT_PK_EVENTTYPE").on(topicColumn).add();
            //table.unique("EBT_UQ_TYPE_NAME").on(nameColumn).add();
        }
    },

    EVT_EVENTPROPERTYTYPE {
        @Override
        void addTo(DataModel dataModel) {
        	Table<EventPropertyType> table = dataModel.addTable(name(), EventPropertyType.class);
        	table.map(EventPropertyTypeImpl.class);
            Column topicColumn = table.column("TOPIC").varChar(NAME_LENGTH).notNull().map("eventTypeTopic").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("TYPE").number().notNull().conversion(NUMBER2ENUM).map("valueType").add();
            table.column("ACCESSPATH").varChar(NAME_LENGTH).notNull().map("accessPath").add();
            Column positionColumn = table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();

            table.primaryKey("EVT_PK_EVENTPROPERTYTYPE").on(topicColumn, nameColumn).add();
            table.unique("EVT_UK_EVENTPROPERTYTYPE").on(topicColumn, positionColumn).add();
            table.foreignKey("EVT_FK_EVENTTYPE_PROPERTY").references(EVT_EVENTTYPE.name()).onDelete(DeleteRule.CASCADE).map("eventType").reverseMap("eventPropertyTypes").on(topicColumn).composition().add();
        }
    };

    abstract void addTo(DataModel dataModel);

}
