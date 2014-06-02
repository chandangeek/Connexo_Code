package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationTypeSupport;

/**
 * Creates the appropriate {@link RelationType}
 * to hold {@link PropertySpec seccurity properties}
 * of newly created {@link DeviceProtocolPluggableClass}es
 * if that does not already exists.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-16 (15:02)
 */
public class DeviceProtocolSecurityRelationTypeCreator {

    public static void createRelationType(DataModel dataModel, ProtocolPluggableService protocolPluggableService, RelationService relationService, DeviceProtocolPluggableClass deviceProtocolPluggableClass, PropertySpecService propertySpecService) {
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        SecurityPropertySetRelationTypeSupport relationTypeSupport =
                new SecurityPropertySetRelationTypeSupport(
                        dataModel,
                        protocolPluggableService,
                        relationService,
                        propertySpecService,
                        deviceProtocol,
                        deviceProtocolPluggableClass);
        relationTypeSupport.findOrCreateRelationType(true);
    }

}