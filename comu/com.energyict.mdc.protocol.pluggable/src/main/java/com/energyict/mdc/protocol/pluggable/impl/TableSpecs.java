package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.*;

import static com.elster.jupiter.orm.Table.*;

/**
 * Models the database tables that hold the data of the {@link PluggableClassRelationAttributeTypeUsage}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:27)
 */
public enum TableSpecs {

    PPC_PCRATUSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PluggableClassRelationAttributeTypeUsage> table = dataModel.addTable(name(), PluggableClassRelationAttributeTypeUsage.class);
            table.map(PluggableClassRelationAttributeTypeUsage.class);
            Column pluggableClassColumn = table.column("PLUGGABLECLASS").number().notNull().number().conversion(ColumnConversion.NUMBER2LONG).map("pluggableClassId").add();
            Column relationAttributeTypeColumn = table.column("RELATIONATTRIBUTETYPE").
                                                    number().conversion(ColumnConversion.NUMBER2LONG).
                                                    notNull().
                                                    map("relationAttributeTypeId").
                                                    add();
            table.primaryKey("PK_PPC_PCRATUSAGE").on(pluggableClassColumn, relationAttributeTypeColumn).add();
        }
    },

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