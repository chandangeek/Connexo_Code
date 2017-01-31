/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingImpl;

import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;

/**
 * Models the database tables that hold the data of the protocol pluggable bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:27)
 */
public enum TableSpecs {

    PPC_SECSUPPORTADAPTERMAPPING {
        @Override
        void addTo(DataModel dataModel) {
            Table<SecuritySupportAdapterMapping> table = dataModel.addTable(name(), SecuritySupportAdapterMapping.class);
            table.map(SecuritySupportAdapterMappingImpl.class);
            table.cache();
            Column deviceProtocolClassNameColumn = table.column("deviceprotocoljavaclassname").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("deviceProtocolJavaClassName").add();
            Column securitySupportClassNameColumn = table.column("securitysupportclassname").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("securitySupportJavaClassName").add();
            table.primaryKey("PK_PPC_SECSUPPORTADAPTRMAPPING").on(deviceProtocolClassNameColumn, securitySupportClassNameColumn).add();
        }
    },

    PPC_MESSAGEADAPTERMAPPING {
        @Override
        void addTo(DataModel dataModel) {
            Table<MessageAdapterMapping> table = dataModel.addTable(name(), MessageAdapterMapping.class);
            table.map(MessageAdapterMappingImpl.class);
            table.cache();
            Column deviceProtocolClassNameColumn = table.column("deviceprotocoljavaclassname").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("deviceProtocolJavaClassName").add();
            Column securitySupportClassNameColumn = table.column("messageadapterjavaclassname").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("messageAdapterJavaClassName").add();
            table.primaryKey("PK_PPC_MSGADAPTERMAPPING").on(deviceProtocolClassNameColumn, securitySupportClassNameColumn).add();
        }
    },

    PPC_CAPABILITIESADAPTERMAPPING {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceCapabilityMapping> table = dataModel.addTable(name(), DeviceCapabilityMapping.class);
            table.map(DeviceCapabilityAdapterMappingImpl.class);
            table.cache();
            Column deviceProtocolClassNameColumn = table.column("deviceprotocoljavaclassname").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("deviceProtocolJavaClassName").add();
            Column capabilitiesColumn = table.column("deviceprotocolcapabilities").number().notNull().map("deviceProtocolCapabilities").conversion(ColumnConversion.NUMBER2INT).add();
            table.primaryKey("PK_PPC_CAPADAPTERMAPPING").on(deviceProtocolClassNameColumn, capabilitiesColumn).add();
        }
    };

    abstract void addTo(DataModel component);

}