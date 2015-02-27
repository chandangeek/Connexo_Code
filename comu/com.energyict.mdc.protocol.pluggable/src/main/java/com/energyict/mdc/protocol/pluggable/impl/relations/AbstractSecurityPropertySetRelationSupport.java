package com.energyict.mdc.protocol.pluggable.impl.relations;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames;

import java.util.Optional;

/**
 * Provides code reuse opportunities for components
 * that provide {@link com.energyict.mdc.pluggable.RelationSupport}
 * or {@link com.energyict.mdc.pluggable.RelationTypeSupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-17 (16:54)
 */
public abstract class AbstractSecurityPropertySetRelationSupport implements SecurityPropertySetRelationAttributeTypeNames {

    private ProtocolPluggableService protocolPluggableService;
    private RelationService relationService;
    private PropertySpecService propertySpecService;
    private DeviceSecuritySupport securitySupport;
    private RelationType relationType;  // Cache

    protected AbstractSecurityPropertySetRelationSupport(ProtocolPluggableService protocolPluggableService, RelationService relationService, PropertySpecService propertySpecService, DeviceSecuritySupport securitySupport) {
        super();
        this.protocolPluggableService = protocolPluggableService;
        this.relationService = relationService;
        this.securitySupport = securitySupport;
        this.propertySpecService = propertySpecService;
    }

    protected DeviceSecuritySupport getSecuritySupport() {
        return securitySupport;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected Optional<RelationType> findRelationType(String relationTypeName) {
        return this.relationService.findRelationType(relationTypeName);
    }

    protected RelationType createRelationType(RelationTypeShadow shadow, PropertySpecService propertySpecService) {
        return this.relationService.createRelationType(shadow, propertySpecService);
    }

    protected String appropriateRelationTypeName() {
        return this.protocolPluggableService.createConformRelationTypeName(this.getSecuritySupport().getSecurityRelationTypeName());
    }

    protected boolean deviceProtocolHasSecurityProperties() {
        return !this.getSecuritySupport().getSecurityPropertySpecs().isEmpty();
    }

    public RelationType findRelationType() {
        if (this.relationType == null) {
            this.relationType = this.doFindRelationType();
        }
        return this.relationType;
    }

    public RelationType doFindRelationType() {
        if (this.deviceProtocolHasSecurityProperties()) {
            String relationTypeName = this.appropriateRelationTypeName();
            Optional<RelationType> relationType = this.findRelationType(relationTypeName);
            return relationType.orElseThrow(() -> new ApplicationException("Creation of relation type for security properties of "
                    + this.getSecuritySupport().getClass().getName() + " with relationTypeName " + relationTypeName + " was not created yet or failed before."));
        } else {
            return null;
        }
    }
}