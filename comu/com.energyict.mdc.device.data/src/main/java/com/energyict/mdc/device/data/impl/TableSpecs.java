package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceProtocolProperty;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

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

//    MDCDEVICEPROPERTIES{
//        @Override
//        public void addTo(DataModel dataModel) {
//            Table<DeviceProtocolProperty> table = dataModel.addTable(name(), DeviceProtocolProperty.class);
//            table.map(DeviceProtocolPropertyImpl.class);
//            Column rtuid = table.column("RTUID").number().add();
//            table.column("NAME").varChar(255).notNull().map("name").add();
//            table.column("VALUE").varChar(255).map("stringValue").add();
//            table.foreignKey("FK_PROTOCOLINFOTYPEID").on(rtuid).references(EISRTU.name()).map("device").reverseMap("deviceProperties").composition().add();
//        }
//    },

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
    ;

    private TableSpecs() {
    }

    abstract void addTo(DataModel component);

}
