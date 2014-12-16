package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.topology.CommunicationPathSegment;
import com.energyict.mdc.device.topology.PLCNeighbor;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
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
    },

    DTL_PLCNEIGHBOR {
        @Override
        void addTo(DataModel dataModel) {
            Table<PLCNeighbor> table = dataModel.addTable(name(), PLCNeighbor.class);
            table.map(PLCNeighborImpl.IMPLEMENTERS);
            Column device = table.column("DEVICE").notNull().number().conversion(NUMBER2LONG).add();
            Column neighbor = table.column("NEIGHBOR").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
            table.column("MODULATION_SCHEME").number().conversion(NUMBER2ENUM).notNull().map(PLCNeighborImpl.Field.MODULATION_SCHEME.fieldName()).add();
            table.column("MODULATION").number().conversion(NUMBER2ENUM).notNull().map(PLCNeighborImpl.Field.MODULATION.fieldName()).add();
            table.column("TXGAIN").number().conversion(NUMBER2INT).map(PLCNeighborImpl.Field.TX_GAIN.fieldName()).add();
            table.column("TXRES").number().conversion(NUMBER2INT).map(PLCNeighborImpl.Field.TX_RESOLUTION.fieldName()).add();
            table.column("TXCOEFF").number().conversion(NUMBER2INT).map(PLCNeighborImpl.Field.TX_COEFFICIENT.fieldName()).add();
            table.column("TXLQI").number().conversion(NUMBER2INT).map(PLCNeighborImpl.Field.LINK_QUALITY_INDICATOR.fieldName()).add();
            table.column("TIMETOLIVE_SECS").number().conversion(NUMBER2LONG).map(PLCNeighborImpl.Field.TIME_TO_LIVE.fieldName()).add();
            table.column("TONEMAP").number().conversion(NUMBER2LONG).map(PLCNeighborImpl.Field.TONE_MAP.fieldName()).add();
            table.column("TM_TIMETOLIVE_SECS").number().conversion(NUMBER2LONG).map(PLCNeighborImpl.Field.TONE_MAP_TIME_TO_LIVE.fieldName()).add();
            table.column("PHASEINFO").number().conversion(NUMBER2ENUM).map(PLCNeighborImpl.Field.PHASE_INFO.fieldName()).add();
            table.primaryKey("PK_DTL_PLCNEIGHBOR").on(device, neighbor, intervalColumns.get(0)).add();
            table.foreignKey("FK_DTL_PLCNEIGHBOR_DEV").
                    on(device).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(PLCNeighborImpl.Field.DEVICE.fieldName()).
                    add();
            table.foreignKey("FK_DTL_PLCNEIGHBOR_NEIGHBOR").
                    on(neighbor).
                    references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").
                    onDelete(CASCADE).
                    map(PLCNeighborImpl.Field.NEIGHBOR.fieldName()).
                    add();
        }
    }
    ;

    abstract void addTo(DataModel component);

}