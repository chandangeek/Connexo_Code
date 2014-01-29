package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (11:17)
 */
public enum TableSpecs {

    EISSYSRTUTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceType> table = dataModel.addTable(this.name(), DeviceType.class);
            table.map(DeviceTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("CHANNELCOUNT").number().notNull().map("channelcount").add();
            table.column("DESCRIPTION").varChar(4000).map("description").add();
            table.column("PROTOTYPEID").number().map("prototypeid").add();
            table.column("USECHANNELJOURNAL").number().notNull().map("usechanneljournal").add();
            table.column("NEEDSPROXY").number().map("needsproxy").add();
            table.column("DEVICEPROTOCOLPLUGGABLEID").number().map("deviceprotocolpluggableid").add();
            table.column("DEVICEUSAGETYPE").number().map("deviceusagetype").add();
            table.column("DEVICECOLLECTIONMETHOD").number().map("devicecollectionmethod").add();
            table.column("COMMUNICATIONFUNCTIONMASK").number().map("communicationfunctionmask").add();
            table.unique("UK_SYSRTUTYPE").on(name).add();
            table.primaryKey("PK_SYSRTUTYPE").on(id).add();
        }
    },

    EISLOGBOOKTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBookType> table = dataModel.addTable(this.name(), LogBookType.class);
            table.map(LogBookTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("DESCRIPTION").varChar(255).map("description").add();
            table.column("OBISCODE").varChar(80).notNull().map("obiscode").add();
            table.unique("UK_EISLOGBOOKTYPE").on(name).add();
            table.primaryKey("PK_EISLOGBOOKTYPE").on(id).add();
        }
    },

    EISLOADPROFILETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileType> table = dataModel.addTable(this.name(), LoadProfileType.class);
            table.map(LoadProfileTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("DESCRIPTION").varChar(255).map("description").add();
            table.column("OBISCODE").varChar(80).notNull().map("obiscode").add();
            table.column("INTERVALCOUNT").number().notNull().map("intervalcount").add();
            table.column("INTERVALUNIT").number().notNull().map("intervalunit").add();
            table.column("MOD_DATE").type("DATE").notNull().map("modDate").insert("sysdate").update("sysdate").add();
            table.unique("UK_LOADPROFILETYPE").on(name).add();
            table.primaryKey("PK_LOADPROFILETYPE").on(id).add();
        }
    },

    EISRTUREGISTERMAPPING {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterMapping> table = dataModel.addTable(this.name(), RegisterMapping.class);
            table.map(RegisterMappingImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            Column obiscode = table.column("OBISCODE").varChar(80).notNull().map("obiscode").add();
            Column productspecid = table.column("PRODUCTSPECID").number().notNull().add();
            table.column("MOD_DATE").type("DATE").notNull().map("modDate").insert("sysdate").update("sysdate").add();
            table.column("CUMULATIVE").number().notNull().map("cumulative").add();
            Column registergroupid = table.column("REGISTERGROUPID").number().add();
            table.column("DESCRIPTION").varChar(255).map("description").add();
            table.foreignKey("FK_EISREGMAPREGGROUP").on(registergroupid).references(EISRTUREGISTERGROUP.name()).map("eisrturegistergroup").add();
            table.foreignKey("FK_EISREGMAPPRODSPEC").on(productspecid).references(EISPRODUCTSPEC.name()).map("eisproductspec").add();
            table.unique("UK_RTUREGMAPPINGNAME").on(name).add();
            table.unique("UK_RTUREGMAPPINGOBISPROD").on(obiscode,productspecid).add();
            table.primaryKey("PK_RTUREGISTERMAPPING").on(id).add();
        }
    },

    EISRTUREGISTERGROUP {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterGroup> table = dataModel.addTable(this.name(), RegisterGroup.class);
            table.map(RegisterGroupImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(256).notNull().map("name").add();
            table.column("MOD_DATE").type("DATE").notNull().map("modificationDate").add();
            table.column("USEABLEINMMR").number().map("useableinmmr").add();
            table.unique("UK_RTUREGISTERGROUP").on(name).add();
            table.primaryKey("PK_RTUREGISTERGROUP").on(id).add();
        }
    },

    EISPRODUCTSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProductSpec> table = dataModel.addTable(this.name(), ProductSpec.class);
            table.map(ProductSpecImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("READINGTYPE").varChar(100).map("readingType").add();
            table.column("MOD_DATE").type("DATE").notNull().map("modDate").insert("sysdate").update("sysdate").add();
            table.primaryKey("PK_PRODUCTSPEC").on(id).add();
        }
    };

    abstract void addTo(DataModel component);

}