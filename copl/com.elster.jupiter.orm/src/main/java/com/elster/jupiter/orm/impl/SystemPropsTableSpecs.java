package com.elster.jupiter.orm.impl;

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

public enum SystemPropsTableSpecs {
    SYS_PROP{
        @Override
        public void addTo(DataModel dataModel) {
            System.out.println("ADD TO DATAMODEL");
            Table<SystemProperty> table = dataModel.addTable(this.name(), SystemProperty.class);
            System.out.println("MAP");
            table.map(SystemPropertyImpl.class);
            System.out.println("ADD AUTO ID");
            //Column id = table.addAutoIdColumn();
            System.out.println("ADD AUDIT COLUMN");
            //table.addAuditColumns();
            System.out.println("ADD NAME COLUMN");
            Column name = table.column("PROPERTYNAME").varChar().map(SystemPropertyImpl.Fields.PROP_NAME.fieldName()).notNull().add();
            System.out.println("ADD VALUE COLUMN");
            table.column("PROPERTYVALUE").varChar().map(SystemPropertyImpl.Fields.PROP_VALUE.fieldName()).notNull().add();
            System.out.println("ADD PRIMARYKEY COLUMN");
            table.primaryKey("PK_SY_PROP").on(name).add();
            System.out.println("TABLE IS ADDED");
        }
    };

    abstract public void addTo(DataModel component);

}
