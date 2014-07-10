package com.energyict.mdc.protocol.pluggable.impl.relations;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.pluggable.RelationSupport;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.util.time.Clock;

import java.util.List;

/**
 * Provides {@link RelationSupport} for SecurityPropertySets.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-10 (15:37)
 */
public class SecurityPropertySetRelationSupport extends AbstractSecurityPropertySetRelationSupport {

    private final Clock clock;
    private final RelationParticipant securityPropertySet;

    public SecurityPropertySetRelationSupport(RelationParticipant securityPropertySet, DeviceSecuritySupport securitySupport, ProtocolPluggableService protocolPluggableService, RelationService relationService, PropertySpecService propertySpecService, Clock clock) {
        super(protocolPluggableService, relationService, propertySpecService, securitySupport);
        this.clock = clock;
        this.securityPropertySet = securityPropertySet;
    }

    /**
     * Tests if the SecurityPropertySet has values,
     * i.e. there is at least one Device out there
     * that has values for this SecurityPropertySet.
     *
     * @return A flag that indicates if the SecurityPropertySet is used by a Device
     */
    public boolean hasValues () {
        if (this.deviceProtocolHasSecurityProperties()) {
            List<Relation> relations = this.getDefaultAttributeType().getRelations(this.securityPropertySet, this.clock.now(), false, 0, 1);
            return !relations.isEmpty();
        }
        else {
            return false;
        }
    }

    public RelationAttributeType getDefaultAttributeType () {
        if (!this.deviceProtocolHasSecurityProperties()) {
            return null;
        }
        else {
            return this.findRelationType().getAttributeType(SECURITY_PROPERTY_SET_ATTRIBUTE_NAME);
        }
    }

}