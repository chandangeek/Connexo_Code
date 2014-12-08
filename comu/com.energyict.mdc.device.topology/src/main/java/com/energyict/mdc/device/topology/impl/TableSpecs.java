package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.DeviceDataServices;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum TableSpecs {

    DTL_PHYSICALGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PhysicalGatewayReference> table = dataModel.addTable(name(), PhysicalGatewayReference.class);
            table.map(PhysicalGatewayReferenceImpl.class);
            Column originId = table.column("ORIGINID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column physicalGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            table.primaryKey("PK_DDC_PHYSICALGATEWAYREF").on(originId, intervalColumns.get(0)).add();
            table.foreignKey("FK_DDC_PHYSGATEWAYREF_ORIGIN").
                    on(originId).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).
                    composition().
                    add();
            table.foreignKey("FK_DDC_PHYSGATEWAYREF_GATEWAY").
                    on(physicalGatewayId).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).
                    add();
        }
    },
    ;

    abstract void addTo(DataModel component);

}