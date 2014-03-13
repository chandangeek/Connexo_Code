package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;

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

    MDCDEVICEPROPERTIES{
        @Override
        public void addTo(DataModel dataModel) {
            Table<Api> table = dataModel.add(name(), Api.class);
            table.map(Implementation.class);
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("CRONSTRING").varChar(80).notNull().map("cronstring").add();
            table.column("RECURRENTTASKSACTIVE").type("CHAR(1)").notNull().map("recurrenttasksactive").add();
            table.primaryKey("APS_PK_APPSERVER").on(name).add();
        }
    },

    MDCPHYSICALGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PhysicalGatewayReference> table = dataModel.addTable(name(), PhysicalGatewayReference.class);
            table.map(PhysicalGatewayReferenceImpl.class);
            Column physicalGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.primaryKey("DDC_U_PHY_GATEWAY").on(physicalGatewayId, intervalColumns.get(0)).add();
            table.foreignKey("DDC_FK_DEVPHYGATEWAY").on(physicalGatewayId).references(EISRTU.name()).onDelete(CASCADE).map("gateway").reverseMap("physicalGatewayReferenceDevice").composition().add();
        }
    },

    MDCCOMGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<CommunicationGatewayReference> table = dataModel.addTable(name(), CommunicationGatewayReference.class);
            table.map(CommunicationGatewayReferenceImpl.class);
            Column communicationGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.primaryKey("DDC_U_COM_GATEWAY").on(communicationGatewayId, intervalColumns.get(0)).add();
            table.foreignKey("DDC_FK_DEVCOMGATEWAY").on(communicationGatewayId).references(EISRTU.name()).onDelete(CASCADE).map("gateway").reverseMap("communicationGatewayReferenceDevice").composition().add();
        }
    },


    ;

    private TableSpecs() {
    }

    abstract void addTo(DataModel component);

}
