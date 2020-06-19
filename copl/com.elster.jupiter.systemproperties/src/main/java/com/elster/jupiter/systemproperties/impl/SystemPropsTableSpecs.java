package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.systemproperties.SystemProperty;


public enum SystemPropsTableSpecs {
    SYP_PROP {
        @Override
        public void addTo(DataModel dataModel) {
            Table<SystemProperty> table = dataModel.addTable(this.name(), SystemProperty.class);
            table.map(SystemPropertyImpl.class);
            Column name = table.column("KEY").varChar().map(SystemPropertyImpl.Fields.PROP_KEY.fieldName()).notNull().add();
            table.addAuditColumns();
            table.column("VALUE").varChar().map(SystemPropertyImpl.Fields.PROP_VALUE.fieldName()).notNull().add();
            table.primaryKey("PK_SYP_PROP").on(name).add();
        }
    };

    abstract void addTo(DataModel component);
}