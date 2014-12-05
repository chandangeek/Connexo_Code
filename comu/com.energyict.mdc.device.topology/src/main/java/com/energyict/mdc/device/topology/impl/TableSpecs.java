package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.UserService;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum TableSpecs {

    DDC_PHYSICALGATEWAYREFERENCE {
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
                    references(DDC_DEVICE.name()).
                    onDelete(CASCADE).
                    map(GatewayReferenceImpl.Field.ORIGIN.fieldName()).
                    reverseMap("physicalGatewayReferenceDevice").
                    composition().
                    add();
            table.foreignKey("FK_DDC_PHYSGATEWAYREF_GATEWAY").
                    on(physicalGatewayId).
                    references(DDC_DEVICE.name()).
                    onDelete(CASCADE).
                    map(GatewayReferenceImpl.Field.GATEWAY.fieldName()).
                    add();
        }
    },
    ;

    abstract void addTo(DataModel component);

}