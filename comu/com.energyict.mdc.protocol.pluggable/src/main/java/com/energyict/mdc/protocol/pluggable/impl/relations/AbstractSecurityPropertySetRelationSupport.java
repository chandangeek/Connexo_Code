package com.energyict.mdc.protocol.pluggable.impl.relations;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.sql.SQLException;

/**
 * Provides code reuse opportunities for components
 * that provide {@link com.energyict.mdc.pluggable.RelationSupport}
 * or {@link com.energyict.mdc.pluggable.RelationTypeSupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-17 (16:54)
 */
public abstract class AbstractSecurityPropertySetRelationSupport implements com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames {

    private ProtocolPluggableService protocolPluggableService;
    private RelationService relationService;
    private DeviceSecuritySupport securitySupport;
    private RelationType relationType;  // Cache

    protected AbstractSecurityPropertySetRelationSupport(ProtocolPluggableService protocolPluggableService, RelationService relationService, DeviceSecuritySupport securitySupport) {
        super();
        this.protocolPluggableService = protocolPluggableService;
        this.relationService = relationService;
        this.securitySupport = securitySupport;
    }

    protected DeviceSecuritySupport getSecuritySupport() {
        return securitySupport;
    }

    protected RelationType findRelationType(String relationTypeName) {
        return this.relationService.findRelationType(relationTypeName);
    }

    protected RelationType createRelationType (RelationTypeShadow shadow) {
        return this.relationService.createRelationType(shadow);
    }

    protected String appropriateRelationTypeName() {
        return this.protocolPluggableService.createConformRelationTypeName(this.getSecuritySupport().getSecurityRelationTypeName());
    }

    protected boolean deviceProtocolHasSecurityProperties() {
        return !this.getSecuritySupport().getSecurityProperties().isEmpty();
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
            RelationType relationType = this.findRelationType(relationTypeName);
            if (relationType == null) {
                throw new ApplicationException("Creation of relation type for security properties of "
                        + this.getSecuritySupport().getClass().getName() + " with relationTypeName " + relationTypeName + " was not created yet or failed before.");
            }
            return relationType;
        } else {
            return null;
        }
    }
}