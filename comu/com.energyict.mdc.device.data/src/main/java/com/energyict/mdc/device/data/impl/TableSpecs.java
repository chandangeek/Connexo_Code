package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.LoadProfile;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 */
public enum TableSpecs {

    EISRTU {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Device> table = dataModel.addTable(name(), Device.class);
            table.map(DeviceImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar(80).notNull().map("name").add();
            // TODO rename the column to serialNumber
            table.column("DEVICENAME").varChar(80).map("serialNumber").add();
            table.column("TIMEZONE").varChar(32).map("timeZoneId").add();
            Column externid = table.column("EXTERNID").varChar(255).map("externalName").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            Column deviceConfigId = table.column("DEVICECONFIGID").number().add();
            table.foreignKey("FK_EISRTU_DEVICECONFIG").on(deviceConfigId).references(DeviceConfigurationService.COMPONENTNAME, "EISDEVICECONFIG").map("deviceConfiguration").add();
            table.unique("UK_RTU_EXTID").on(externid).add();
            table.primaryKey("PK_RTU").on(id).add();
        }
    },

    EISINFOTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<InfoType> table = dataModel.addTable(name(), InfoType.class);
            table.map(InfoTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.primaryKey("PK_INFOTYPE").on(id).add();
            table.unique("UK_INFOTYPE").on(name).add();
        }
    },

    EISRTUINFO {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceProtocolProperty> table = dataModel.addTable(name(), DeviceProtocolProperty.class);
            table.map(DeviceProtocolPropertyImpl.class);
            Column deviceId = table.column("RTUID").number().notNull().conversion(NUMBER2LONG).add();
            Column infoTypeId = table.column("INFOTYPEID").map("infoTypeId").number().conversion(NUMBER2LONG).notNull().add();
            table.column("INFOVALUE").varChar(255).map("propertyValue").add();
            table.primaryKey("PK_RTUINFO").on(deviceId,infoTypeId).add();
            table.foreignKey("FK_RTUINFOTYPEID").on(infoTypeId).references(EISINFOTYPE.name()).map("infoTypeId").add();
            table.foreignKey("FK_RTUINFOTYPEDEVID").on(deviceId).references(EISRTU.name()).map("device").reverseMap("deviceProperties").composition().add();
        }
    },

    MDCPHYSICALGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PhysicalGatewayReference> table = dataModel.addTable(name(), PhysicalGatewayReference.class);
            table.map(PhysicalGatewayReferenceImpl.class);
            Column originId = table.column("ORIGINID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column physicalGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            table.primaryKey("DDC_U_PHY_GATEWAY").on(originId, intervalColumns.get(0)).add();
            table.foreignKey("DDC_FK_DEVPHYORIGIN").on(originId).references(EISRTU.name()).onDelete(CASCADE).map("origin").reverseMap("physicalGatewayReferenceDevice").composition().add();
            table.foreignKey("DDC_FK_DEVPHYGATEWAY").on(physicalGatewayId).references(EISRTU.name()).onDelete(CASCADE).map("gateway").add();
        }
    },

    MDCCOMGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<CommunicationGatewayReference> table = dataModel.addTable(name(), CommunicationGatewayReference.class);
            table.map(CommunicationGatewayReferenceImpl.class);
            Column originId = table.column("ORIGINID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column communicationGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            table.primaryKey("DDC_U_COM_GATEWAY").on(originId, intervalColumns.get(0)).add();
            table.foreignKey("DDC_FK_DEVCOMORIGIN").on(originId).references(EISRTU.name()).onDelete(CASCADE).map("origin").reverseMap("communicationGatewayReferenceDevice").composition().add();
            table.foreignKey("DDC_FK_DEVCOMGATEWAY").on(communicationGatewayId).references(EISRTU.name()).onDelete(CASCADE).map("gateway").add();
        }
    },

    EISLOADPROFILE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfile> table = dataModel.addTable(name(), LoadProfile.class);
            table.map(LoadProfileImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceId = table.column("RTUID").number().notNull().add();
            table.column("LASTREADING").number().map("lastReading").conversion(ColumnConversion.NUMBER2UTCINSTANT).add();
            Column loadprofilespecid = table.column("LOADPROFILESPECID").number().add();
            table.primaryKey("PK_LOADPROFILE").on(id).add();
            table.foreignKey("FK_LOADPROFILE_LOADPROFILESPEC").on(loadprofilespecid).references(DeviceConfigurationService.COMPONENTNAME, "EISLOADPROFILESPEC").map("loadProfileSpec").add();
            table.foreignKey("FK_LOADPROFILE_RTU").on(deviceId).references(EISRTU.name()).map("device").reverseMap("loadProfiles").composition().add();

        }
    },
//
//    EISDEVICECACHE {
//        @Override
//        public void addTo(DataModel dataModel) {
//            Table<DeviceCache> table = dataModel.addTable(name(), DeviceCache.class);
//            table.map(DeviceCacheImpl.class);
//            Column deviceId = table.column("RTUID").number().notNull().add();
//            table.column("CONTENT").type("BLOB(4000)").map("simpleCache").add();
//            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
//            table.primaryKey("PK_EISDEVICECACHE").on(deviceId).add();
//            table.foreignKey("FK_EISDEVICECACHE_RTU").on(deviceId).references(EISRTU.name()).map("device").add();
//        }
//    },
    ;

    private TableSpecs() {
    }

    abstract void addTo(DataModel component);

}
