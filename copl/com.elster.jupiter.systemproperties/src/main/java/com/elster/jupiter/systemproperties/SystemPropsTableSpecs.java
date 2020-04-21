package com.elster.jupiter.systemproperties;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;


public enum SystemPropsTableSpecs {
    SYS_PROP{
        @Override
        public void addTo(DataModel dataModel) {

            Table<SystemProperty> table = dataModel.addTable(this.name(), SystemProperty.class);
            table.map(SystemPropertyImpl.class);
            Column name = table.column("PROPERTYNAME").varChar().map(SystemPropertyImpl.Fields.PROP_NAME.fieldName()).notNull().add();
            table.column("PROPERTYVALUE").varChar().map(SystemPropertyImpl.Fields.PROP_VALUE.fieldName()).notNull().add();
            table.primaryKey("PK_SY_PROP").on(name).add();
        }
    };

    abstract void addTo(DataModel component);

}


