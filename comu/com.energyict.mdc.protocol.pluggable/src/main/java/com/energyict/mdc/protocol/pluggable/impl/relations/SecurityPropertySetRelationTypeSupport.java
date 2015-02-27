package com.energyict.mdc.protocol.pluggable.impl.relations;

import com.elster.jupiter.properties.BooleanFactory;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.JupiterReferenceFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.ConstraintShadow;
import com.energyict.mdc.dynamic.relation.RelationAttributeTypeShadow;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;
import com.energyict.mdc.pluggable.RelationTypeSupport;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.PluggableClassRelationAttributeTypeRegistry;
import com.energyict.mdc.protocol.pluggable.impl.PluggableClassRelationAttributeTypeUsage;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;

import java.util.Optional;

/**
 * Provides {@link RelationTypeSupport} for the security properties of a {@link DeviceSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-17 (15:49)
 */
public class SecurityPropertySetRelationTypeSupport extends AbstractSecurityPropertySetRelationSupport implements RelationTypeSupport {

    private DataMapper<PluggableClassRelationAttributeTypeUsage> mapper;
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    public SecurityPropertySetRelationTypeSupport(
            DataModel dataModel,
            ProtocolPluggableService protocolPluggableService,
            RelationService relationService,
            PropertySpecService propertySpecService,
            DeviceSecuritySupport securitySupport,
            DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        super(protocolPluggableService, relationService, propertySpecService, securitySupport);
        this.mapper = dataModel.mapper(PluggableClassRelationAttributeTypeUsage.class);
        this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
    }

    @Override
    public RelationType findOrCreateRelationType(boolean activate) {
        if (this.deviceProtocolHasSecurityProperties()) {
            String relationTypeName = this.appropriateRelationTypeName();
            Optional<RelationType> relationType = this.findRelationType(relationTypeName);
            if (!relationType.isPresent()) {
                RelationType newRelationType = this.createRelationType(this.getSecuritySupport(), this.getPropertySpecService());
                if (activate) {
                    this.activate(newRelationType);
                }
                relationType = Optional.of(newRelationType);
            }
            if (relationType.isPresent()) {
                this.registerRelationType(relationType.get());
            }
            return relationType.orElse(null);
        } else {
            return null;
        }
    }

    private RelationType createRelationType(DeviceSecuritySupport securitySupport, PropertySpecService propertySpecService) {
        RelationTypeShadow relationTypeShadow = new RelationTypeShadow();
        relationTypeShadow.setSystem(true);
        relationTypeShadow.setName(this.appropriateRelationTypeName());
        relationTypeShadow.setHasTimeResolution(true);
        RelationAttributeTypeShadow deviceAttribute = this.deviceAttributeTypeShadow();
        RelationAttributeTypeShadow securityPropertySetAttribute = this.securityPropertySetAttributeTypeShadow();
        RelationAttributeTypeShadow statusAttributeTypeShadow = this.statusAttributeTypeShadow();
        relationTypeShadow.setLockAttributeTypeShadow(deviceAttribute);
        relationTypeShadow.add(deviceAttribute);
        relationTypeShadow.add(securityPropertySetAttribute);
        relationTypeShadow.add(statusAttributeTypeShadow);
        for (PropertySpec propertySpec : securitySupport.getSecurityPropertySpecs()) {
            relationTypeShadow.add(this.relationAttributeTypeShadowFor(propertySpec));
        }
        relationTypeShadow.add(this.constraintShadowFor(deviceAttribute, securityPropertySetAttribute));
        return this.createRelationType(relationTypeShadow, propertySpecService);
    }

    private RelationAttributeTypeShadow deviceAttributeTypeShadow() {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(DEVICE_ATTRIBUTE_NAME);
        shadow.setRequired(true);
        shadow.setIsDefault(true);
        shadow.setNavigatable(false);
        shadow.setObjectFactoryId(FactoryIds.DEVICE.id());
        shadow.setValueFactoryClass(JupiterReferenceFactory.class);
        return shadow;
    }

    private RelationAttributeTypeShadow securityPropertySetAttributeTypeShadow() {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(SECURITY_PROPERTY_SET_ATTRIBUTE_NAME);
        shadow.setRequired(true);
        shadow.setIsDefault(false);
        shadow.setNavigatable(false);
        shadow.setObjectFactoryId(FactoryIds.SECURITY_SET.id());
        shadow.setValueFactoryClass(JupiterReferenceFactory.class);
        return shadow;
    }

    private RelationAttributeTypeShadow statusAttributeTypeShadow() {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(STATUS_ATTRIBUTE_NAME);
        shadow.setRequired(true);
        shadow.setIsDefault(false);
        shadow.setNavigatable(false);
        shadow.setValueFactoryClass(BooleanFactory.class);
        return shadow;
    }

    private RelationAttributeTypeShadow relationAttributeTypeShadowFor(PropertySpec propertySpec) {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(propertySpec.getName());
        shadow.setIsDefault(false);
        shadow.setRequired(false);
        ValueFactory valueFactory = propertySpec.getValueFactory();
        Class<? extends ValueFactory> valueFactoryClass = valueFactory.getClass();
        shadow.setValueFactoryClass(valueFactoryClass);
        return shadow;
    }

    private ConstraintShadow constraintShadowFor(RelationAttributeTypeShadow deviceAttributeTypeShadow, RelationAttributeTypeShadow securityPropertySetAttributeTypeShadow) {
        ConstraintShadow shadow = new ConstraintShadow();
        shadow.add(deviceAttributeTypeShadow);
        shadow.add(securityPropertySetAttributeTypeShadow);
        shadow.setName("Unique " + this.appropriateRelationTypeName());
        shadow.setRejectViolations(false);
        return shadow;
    }

    private void activate(RelationType relationType) {
        relationType.activate();
    }

    private void registerRelationType(RelationType relationType) {
        PluggableClassRelationAttributeTypeRegistry registry = new PluggableClassRelationAttributeTypeRegistry(this.mapper);
        if (!registry.isDefaultAttribute(relationType.getAttributeType(DEVICE_ATTRIBUTE_NAME))) {
            registry.register(this.deviceProtocolPluggableClass, relationType.getAttributeType(DEVICE_ATTRIBUTE_NAME));
            registry.register(this.deviceProtocolPluggableClass, relationType.getAttributeType(SECURITY_PROPERTY_SET_ATTRIBUTE_NAME));
        }
    }

    private void unregisterRelationType() {
        if (this.deviceProtocolHasSecurityProperties()) {
            RelationType relationType = this.findRelationType();
            PluggableClassRelationAttributeTypeRegistry registry = new PluggableClassRelationAttributeTypeRegistry(this.mapper);
            registry.unRegister(this.deviceProtocolPluggableClass, relationType.getAttributeType(DEVICE_ATTRIBUTE_NAME));
            registry.unRegister(this.deviceProtocolPluggableClass, relationType.getAttributeType(SECURITY_PROPERTY_SET_ATTRIBUTE_NAME));
        }
    }

    @Override
    public void deleteRelationType() {
        RelationType relationType;
        try {
            relationType = this.findRelationType();
        } catch (ApplicationException e) {
            /* Creation of relation type failed before, no need to unRegister and delete the relation type. */
            relationType = null;
        }
        if (relationType != null) {
            this.unregisterRelationType();
            if (!this.isUsedByAnotherPluggableClass(relationType)) {
                relationType.delete();
            }
        }
    }

    private boolean isUsedByAnotherPluggableClass(RelationType relationType) {
        PluggableClassRelationAttributeTypeRegistry registry = new PluggableClassRelationAttributeTypeRegistry(this.mapper);
        return registry.isDefaultAttribute(relationType.getAttributeType(DEVICE_ATTRIBUTE_NAME))
                || registry.isDefaultAttribute(relationType.getAttributeType(SECURITY_PROPERTY_SET_ATTRIBUTE_NAME));
    }

}