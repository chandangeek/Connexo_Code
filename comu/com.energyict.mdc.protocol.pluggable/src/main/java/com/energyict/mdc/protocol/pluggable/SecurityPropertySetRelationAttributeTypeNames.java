package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.protocol.api.device.Device;

/**
 * Defines constants that should be used for names of
 * {@link com.energyict.mdc.dynamic.relation.RelationAttributeType}
 * that relate to storing security properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (17:08)
 */
public interface SecurityPropertySetRelationAttributeTypeNames {

    /**
     * The name of the attribute that references the
     * {@link Device} that owns the security properties that are
     * saved in the {@link RelationType}
     * that is created by this AbstractSecurityPropertySetRelationSupport.
     */
    public String DEVICE_ATTRIBUTE_NAME = "device";

    /**
     * The name of the attribute that references the security property set
     * that defines the context of the security properties that are
     * saved in the {@link RelationType}
     * that is created by this SecurityPropertySetRelationTypeSupport.
     */
    public String SECURITY_PROPERTY_SET_ATTRIBUTE_NAME = "securityPropertySet";

}