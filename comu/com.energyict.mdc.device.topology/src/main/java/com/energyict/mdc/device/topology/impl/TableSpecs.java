/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.CommunicationPathSegment;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.G3DeviceAddressInformation;
import com.energyict.mdc.device.topology.PLCNeighbor;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Version.version;

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
            table.map(AbstractPhysicalGatewayReferenceImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn().since(version(10, 2));
            Column originId = table.column("ORIGINID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column physicalGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            table.addAuditColumns();
            table.column("DISCRIMINATOR").type("char(1)").notNull().map(Column.TYPEFIELDNAME).since(version(10, 2)).installValue("0").add();
            table.primaryKey("PK_DTL_PHYSICALGATEWAYREF").on(idColumn).since(version(10, 2)).add();
            table.primaryKey("PK_DTL_PHYSICALGATEWAYREF").on(originId, intervalColumns.get(0)).upTo(version(10, 1)).add();
            table.unique("DTL_U_PHYSICALGATEWAYREF").on(originId, intervalColumns.get(0)).since(version(10, 2)).add();
            table.foreignKey("FK_DTL_PHYSGATEWAYREF_ORIGIN").
                    on(originId).
                    references(Device.class).
                    onDelete(CASCADE).
                    map(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).
                    add();
            table.foreignKey("FK_DTL_PHYSGATEWAYREF_GATEWAY").
                    on(physicalGatewayId).
                    references(Device.class).
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
            table.addAuditColumns();
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
            Column nextHop = table.column("NEXTHOPDEVICE").number().conversion(NUMBER2LONG).add();
            table.column("TIMETOLIVE_SECS").number().conversion(NUMBER2LONG).map(CommunicationPathSegmentImpl.Field.TIME_TO_LIVE.fieldName()).add();
            table.column("COST").number().conversion(NUMBER2INT).map(CommunicationPathSegmentImpl.Field.COST.fieldName()).add();
            table.primaryKey("PK_DTL_COMPATHSEGMENT").on(source, target, intervalColumns.get(0)).add();
            table.foreignKey("FK_DTL_COMPATHSEGMENT_SRC").
                    on(source).
                    references(Device.class).
                    onDelete(CASCADE).
                    map(CommunicationPathSegmentImpl.Field.SOURCE.fieldName()).
                    add();
            table.foreignKey("FK_DTL_COMPATHSEGMENT_TARGET").
                    on(target).
                    references(Device.class).
                    onDelete(CASCADE).
                    map(CommunicationPathSegmentImpl.Field.TARGET.fieldName()).
                    add();
            table.foreignKey("FK_DTL_COMPATHSEGMENT_NEXTHOP").
                    on(nextHop).
                    references(Device.class).
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
            table.addAuditColumns();
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
                    references(Device.class).
                    onDelete(CASCADE).
                    map(PLCNeighborImpl.Field.DEVICE.fieldName()).
                    add();
            table.foreignKey("FK_DTL_PLCNEIGHBOR_NEIGHBOR").
                    on(neighbor).
                    references(Device.class).
                    onDelete(CASCADE).
                    map(PLCNeighborImpl.Field.NEIGHBOR.fieldName()).
                    add();
        }
    },

    DTL_G3DEVICEADDRESS {
        @Override
        void addTo(DataModel dataModel) {
            Table<G3DeviceAddressInformation> table = dataModel.addTable(name(), G3DeviceAddressInformation.class);
            table.map(G3DeviceAddressInformationImpl.class);
            Column device = table.column("DEVICE").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.column("IPV6ADDRESS").varChar(Table.SHORT_DESCRIPTION_LENGTH).notNull().map(G3DeviceAddressInformationImpl.Field.IPV6_ADDRESS.fieldName()).add();
            table.column("SHORTIPV6ADDRESS").number().conversion(NUMBER2INT).notNull().map(G3DeviceAddressInformationImpl.Field.IPV6_SHORT_ADDRESS.fieldName()).add();
            table.column("LOGICALDEVICEID").number().conversion(NUMBER2INT).notNull().map(G3DeviceAddressInformationImpl.Field.LOGICAL_DEVICE_ID.fieldName()).add();
            table.primaryKey("PK_DTL_G3ADDRESSINFO").on(device, intervalColumns.get(0)).add();
            table.foreignKey("FK_DTL_G3ADDRESSINFO_DEVICE").
                    on(device).
                    references(Device.class).
                    onDelete(CASCADE).
                    map(G3DeviceAddressInformationImpl.Field.DEVICE.fieldName()).
                    add();
        }
    },
    DTL_DATALOGGERCHANNELUSAGE {
        void addTo(DataModel dataModel) {
            Table<DataLoggerChannelUsage> table = dataModel.addTable(name(), DataLoggerChannelUsage.class);
            table.map(DataLoggerChannelUsageImpl.class);
            table.since(version(10, 2));
            Column physicalGateWayReference = table.column(DataLoggerChannelUsageImpl.Field.PHYSICALGATEWAYREF.name())
                    .notNull()
                    .number()
                    .conversion(NUMBER2LONG)
                    .add();
            Column originChannel = table.column(DataLoggerChannelUsageImpl.Field.ORIGIN_CHANNEL.name())
                    .notNull()
                    .number()
                    .conversion(NUMBER2LONG)
                    .add();
            Column gatewayChannel = table.column(DataLoggerChannelUsageImpl.Field.GATEWAY_CHANNEL.name())
                    .notNull()
                    .number()
                    .conversion(NUMBER2LONG)
                    .add();
            table.primaryKey("PK_DTL_CHANNELUSAGE").on(physicalGateWayReference, originChannel).add();
            table.index("IX_DTL_CHANNELUSAGE_GATEWAY").on(gatewayChannel).add();
            table.foreignKey("FK_DTL_CU_GATEWAYREFERENCE")
                    .references(DTL_PHYSICALGATEWAYREFERENCE.name())
                    .on(physicalGateWayReference)
                    .onDelete(CASCADE)
                    .map(DataLoggerChannelUsageImpl.Field.PHYSICALGATEWAYREF.fieldName())
                    .reverseMap("dataLoggerChannelUsages")
                    .composition()
                    .add();
            table.foreignKey("FK_DTL_CHANNELUSAGE_ORIGIN").
                    on(originChannel).
                    references(Channel.class).
                    onDelete(CASCADE).
                    map(DataLoggerChannelUsageImpl.Field.ORIGIN_CHANNEL.fieldName()).
                    add();
            table.foreignKey("FK_DTL_CHANNELUSAGE_GATEWAY").
                    on(gatewayChannel).
                    references(Channel.class).
                    onDelete(CASCADE).
                    map(DataLoggerChannelUsageImpl.Field.GATEWAY_CHANNEL.fieldName()).
                    add();

        }
    };

    abstract void addTo(DataModel component);

}