package com.energyict.mdc.protocol.pluggable;

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
     * {@link com.energyict.mdc.protocol.api.device.BaseDevice} that owns the security properties.
     */
    public String DEVICE_ATTRIBUTE_NAME = "device";

    /**
     * The name of the attribute that references the security property set
     * that defines the context of the security properties.
     */
    public String SECURITY_PROPERTY_SET_ATTRIBUTE_NAME = "securityPropertySet";

}