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

/**
 * Models the database tables that hold the data of the {@link PluggableClassRelationAttributeTypeUsage}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:27)
 */
public enum TableSpecs {

    MDCPCRATUSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PluggableClassRelationAttributeTypeUsage> table = dataModel.addTable(name(), PluggableClassRelationAttributeTypeUsage.class);
            table.map(PluggableClassRelationAttributeTypeUsage.class);
            Column pluggableClassColumn = table.column("PLUGGABLECLASS").number().notNull().map("pluggableClassId").add();
            Column relationAttributeTypeColumn = table.column("RELATIONATTRIBUTETYPE").
                                                    number().conversion(ColumnConversion.NUMBER2INT).
                                                    notNull().
                                                    map("relationAttributeTypeId").
                                                    add();
            table.primaryKey("PK_PCRATUSAGE").on(pluggableClassColumn, relationAttributeTypeColumn).add();
        }
    },

    MDCSSADAPTERMAPPING {
        @Override
        void addTo(DataModel dataModel) {
            Table<SecuritySupportAdapterMapping> table = dataModel.addTable(name(), SecuritySupportAdapterMapping.class);
            table.map(SecuritySupportAdapterMappingImpl.class);
            table.cache();
            Column deviceProtocolClassNameColumn = table.column("deviceprotocoljavaclassname").type("varchar2(255)").notNull().map("deviceProtocolJavaClassName").add();
            Column securitySupportClassNameColumn = table.column("securitysupportclassname").type("varchar2(255)").notNull().map("securitySupportJavaClassName").add();
            table.primaryKey("PK_MDCSSADAPTERMAPPING").on(deviceProtocolClassNameColumn, securitySupportClassNameColumn).add();
        }
    },

    MDCMESSAGEADAPTERMAPPING {
        @Override
        void addTo(DataModel dataModel) {
            Table<MessageAdapterMapping> table = dataModel.addTable(name(), MessageAdapterMapping.class);
            table.map(MessageAdapterMappingImpl.class);
            table.cache();
            Column deviceProtocolClassNameColumn = table.column("deviceprotocoljavaclassname").type("varchar2(255)").notNull().map("deviceProtocolJavaClassName").add();
            Column securitySupportClassNameColumn = table.column("messageadapterjavaclassname").type("varchar2(255)").notNull().map("messageAdapterJavaClassName").add();
            table.primaryKey("PK_MDCMSGADAPTERMAPPING").on(deviceProtocolClassNameColumn, securitySupportClassNameColumn).add();
        }
    },

    MDCCAPABILITIESADAPTERMAPPING {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceCapabilityMapping> table = dataModel.addTable(name(), DeviceCapabilityMapping.class);
            table.map(DeviceCapabilityAdapterMappingImpl.class);
            table.cache();
            Column deviceProtocolClassNameColumn = table.column("deviceprotocoljavaclassname").type("varchar2(255)").notNull().map("deviceProtocolJavaClassName").add();
            Column capabilitiesColumn = table.column("deviceprotocolcapabilities").number().notNull().map("deviceProtocolCapabilities").add();
            table.primaryKey("PK_MDCCAPADAPTERMAPPING").on(deviceProtocolClassNameColumn, capabilitiesColumn).add();
        }
    };

    abstract void addTo(DataModel component);

}