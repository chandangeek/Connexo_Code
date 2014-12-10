package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.topology.CommunicationPathSegment;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
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
            table.primaryKey("PK_DTL_PHYSICALGATEWAYREF").on(originId, intervalColumns.get(0)).add();
            table.foreignKey("FK_DTL_PHYSGATEWAYREF_ORIGIN").
                    on(originId).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).
                    add();
            table.foreignKey("FK_DTL_PHYSGATEWAYREF_GATEWAY").
                    on(physicalGatewayId).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).
                    add();
        }
    },

    DTL_COMPATHSEGMENT {
        @Override
        void addTo(DataModel dataModel) {
            Table<CommunicationPathSegment> table = dataModel.addTable(name(), CommunicationPathSegment.class);
            table.map(CommunicationPathSegmentImpl.IMPLEMENTERS);
            Column source = table.column("SRCDEVICE").notNull().number().conversion(NUMBER2LONG).add();
            Column target = table.column("TARGETDEVICE").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
            Column nextHop = table.column("NEXTHOPDEVICE").number().conversion(NUMBER2LONG).add();
            table.column("TIMETOLIVE_SECS").number().conversion(NUMBER2LONG).map(CommunicationPathSegmentImpl.Field.TIME_TO_LIVE.fieldName()).add();
            table.column("COST").number().conversion(NUMBER2INT).map(CommunicationPathSegmentImpl.Field.COST.fieldName()).add();
            table.primaryKey("PK_DTL_COMPATHSEGMENT").on(source, target, intervalColumns.get(0)).add();
            table.foreignKey("FK_DTL_COMPATHSEGMENT_SRC").
                    on(source).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(CommunicationPathSegmentImpl.Field.SOURCE.fieldName()).
                    add();
            table.foreignKey("FK_DTL_COMPATHSEGMENT_TARGET").
                    on(target).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(CommunicationPathSegmentImpl.Field.TARGET.fieldName()).
                    add();
            table.foreignKey("FK_DTL_COMPATHSEGMENT_NEXTHOP").
                    on(nextHop).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(CommunicationPathSegmentImpl.Field.NEXT_HOP.fieldName()).
                    add();
        }
    }
    ;

    abstract void addTo(DataModel component);

}