package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.JupiterReferenceFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.ConstraintShadow;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationAttributeTypeShadow;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.google.common.collect.Range;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME;

/**
 * Straightforward implementation of the {@link DeviceProtocolDialectUsagePluggableClass} interface.
 * <p/>
 * Copyrights EnergyICT
 * Date: 1/10/12
 * Time: 15:36
 */
public class DeviceProtocolDialectUsagePluggableClassImpl implements DeviceProtocolDialectUsagePluggableClass {

    private final DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private final DeviceProtocolDialect deviceProtocolDialect;
    private final DataModel dataModel;
    private final RelationService relationService;
    private final PropertySpecService propertySpecService;
    private RelationType relationType;  // Cache

    public DeviceProtocolDialectUsagePluggableClassImpl(DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceProtocolDialect deviceProtocolDialect, DataModel dataModel, RelationService relationService, PropertySpecService propertySpecService) {
        this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
        this.deviceProtocolDialect = deviceProtocolDialect;
        this.dataModel = dataModel;
        this.relationService = relationService;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public PluggableClassType getPluggableClassType () {
        return null;
    }

    @Override
    public RelationType findOrCreateRelationType(boolean activate) {
        if (this.deviceProtocolDialectHasProperties()) {
            String relationTypeName = getConformRelationTypeName();
            Optional<RelationType> relationType = this.findRelationType(relationTypeName);
            if (!relationType.isPresent()) {
                RelationType newRelationType = this.createRelationType(this.deviceProtocolDialect, this.propertySpecService);
                if (activate) {
                    this.activate(newRelationType);
                }
                relationType = Optional.of(newRelationType);
            }
            if (relationType.isPresent()) {
                this.registerRelationType(relationType.get(), this.deviceProtocolPluggableClass);
            }
            return relationType.orElse(null);
        } else {
            return null;
        }
    }

    private String getConformRelationTypeName() {
        return RelationUtils.createConformRelationTypeName(this.deviceProtocolDialect.getDeviceProtocolDialectName());
    }

    private Optional<RelationType> findRelationType(String relationTypeName) {
        return this.relationService.findRelationType(relationTypeName);
    }

    private RelationType createRelationType(DeviceProtocolDialect deviceProtocolDialect, PropertySpecService propertySpecService) {
        RelationTypeShadow relationTypeShadow = new RelationTypeShadow();
        relationTypeShadow.setSystem(true);
        relationTypeShadow.setName(getConformRelationTypeName());
        relationTypeShadow.setHasTimeResolution(true);
        RelationAttributeTypeShadow defaultAttribute = this.defaultAttributeTypeShadow();
        relationTypeShadow.setLockAttributeTypeShadow(defaultAttribute);
        relationTypeShadow.add(defaultAttribute);
        for (PropertySpec propertySpec : deviceProtocolDialect.getPropertySpecs()) {
            relationTypeShadow.add(this.relationAttributeTypeShadowFor(propertySpec, false));   // Not required because the user can decide to specify a value on the config level
        }
        relationTypeShadow.add(this.constraintShadowFor(defaultAttribute));
        return this.relationService.createRelationType(relationTypeShadow, propertySpecService);
    }

    private ConstraintShadow constraintShadowFor(RelationAttributeTypeShadow defaultAttributeTypeShadow) {
        ConstraintShadow shadow = new ConstraintShadow();
        shadow.add(defaultAttributeTypeShadow);
        shadow.setName("Unique " + this.deviceProtocolDialect.getDeviceProtocolDialectName());
        shadow.setRejectViolations(false);
        return shadow;
    }

    private void activate(RelationType relationType) {
        relationType.activate();
    }

    private RelationAttributeTypeShadow relationAttributeTypeShadowFor(PropertySpec propertySpec, boolean isRequired) {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(RelationUtils.createConformRelationAttributeName(propertySpec.getName()));
        shadow.setIsDefault(false);
        shadow.setRequired(isRequired);
        ValueFactory valueFactory = propertySpec.getValueFactory();
        shadow.setValueFactoryClass(valueFactory.getClass());
        return shadow;
    }

    private RelationAttributeTypeShadow defaultAttributeTypeShadow() {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        shadow.setRequired(true);
        shadow.setIsDefault(true);
        shadow.setObjectFactoryId(FactoryIds.DEVICE_PROTOCOL_DIALECT.id());
        shadow.setValueFactoryClass(JupiterReferenceFactory.class);
        return shadow;
    }

    @Override
    public RelationType findRelationType() {
        if (this.relationType == null) {
            this.relationType = this.doFindRelationType();
        }
        return this.relationType;
    }

    private RelationType doFindRelationType() {
        String relationTypeName = getConformRelationTypeName();
        Optional<RelationType> relationType = this.findRelationType(relationTypeName);
        if (this.deviceProtocolDialectHasProperties() && !relationType.isPresent()) {
            throw new ApplicationException("Creation of relation type " + relationTypeName + " for device protocol class " + this.deviceProtocolPluggableClass.getJavaClassName() + " and dialect " + this.deviceProtocolDialect.getDeviceProtocolDialectName() + " failed before.");
        }
        return relationType.orElse(null);
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
            this.unRegisterRelationType(this.deviceProtocolPluggableClass);
            if (!this.isUsedByAnotherPluggableClass(relationType)) {
                relationType.delete();
                this.relationType = null;
            }
        }
    }

    private boolean isUsedByAnotherPluggableClass(RelationType relationType) {
        PluggableClassRelationAttributeTypeRegistry registry =
                new PluggableClassRelationAttributeTypeRegistry(this.dataModel.mapper(PluggableClassRelationAttributeTypeUsage.class));
        return registry.isDefaultAttribute(relationType.getAttributeType(DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME));
    }


    /**
     * Registers the fact that this ConnectionTypePluggableClass
     * uses the {@link RelationType} to hold attribute values.
     */
    public void registerRelationType(RelationType relationType, PluggableClass pluggableClass) {
        PluggableClassRelationAttributeTypeRegistry typeRegistry =
                new PluggableClassRelationAttributeTypeRegistry(this.dataModel.mapper(PluggableClassRelationAttributeTypeUsage.class));
        RelationAttributeType attributeType = relationType.getAttributeType(DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        if (!typeRegistry.isRegistered(pluggableClass, attributeType)) {
            typeRegistry.register(pluggableClass, attributeType);
        }
    }

    /**
     * Undo the registration of the fact that this DeviceProtocolDialectPluggableClass
     * uses the {@link RelationType} to hold attribute values.
     *
     * @param pluggableClass The DeviceProtocolPluggableClass
     */
    private void unRegisterRelationType(DeviceProtocolPluggableClass pluggableClass) {
        if (this.deviceProtocolDialectHasProperties()) {
            RelationType relationType = this.findRelationType();
            PluggableClassRelationAttributeTypeRegistry registry =
                    new PluggableClassRelationAttributeTypeRegistry(this.dataModel.mapper(PluggableClassRelationAttributeTypeUsage.class));
            registry.unRegister(pluggableClass, relationType.getAttributeType(DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME));
        }
    }

    @Override
    public RelationAttributeType getDefaultAttributeType() {
        if (!this.deviceProtocolDialectHasProperties()) {
            return null;
        } else {
            return this.findRelationType().getAttributeType(DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        }
    }

    @Override
    public Relation getRelation(RelationParticipant relationParticipant, Instant date) {
        if (!this.deviceProtocolDialectHasProperties()) {
            return null;
        } else {
            List<Relation> relations = relationParticipant.getRelations(this.findRelationType().getAttributeType(DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME), date, false);
            if (relations.isEmpty()) {
                return null;
            } else if (relations.size() > 1) {
                throw new ApplicationException(MessageFormat.format("More than one default relation for the same date {0,date,yyy-MM-dd HH:mm:ss", date));
            } else {
                return relations.get(0);
            }
        }
    }

    private boolean deviceProtocolDialectHasProperties() {
        return !this.deviceProtocolDialect.getPropertySpecs().isEmpty();
    }

    @Override
    public List<Relation> getRelations(RelationParticipant relationParticipant, Range<Instant> period) {
        if (this.deviceProtocolDialectHasProperties()) {
            return relationParticipant.getRelations(this.findRelationType().getAttributeType(DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME), period, false);
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public long getId() {
        return this.deviceProtocolPluggableClass.getId();
    }

    @Override
    public String getName() {
        return deviceProtocolPluggableClass.getName();
    }

    @Override
    public void setName(String name)  {
        this.deviceProtocolPluggableClass.setName(name);
    }

    @Override
    public Instant getModificationDate() {
        return this.deviceProtocolPluggableClass.getModificationDate();
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return this.deviceProtocolPluggableClass.getProperties(propertySpecs);
    }

    @Override
    public void setProperty(PropertySpec propertySpec, Object value) {
        this.deviceProtocolPluggableClass.setProperty(propertySpec, value);
    }

    @Override
    public void removeProperty(PropertySpec propertySpec) {
        this.deviceProtocolPluggableClass.removeProperty(propertySpec);
    }

    @Override
    public void save() {
        this.deviceProtocolPluggableClass.save();
    }

    @Override
    public String getJavaClassName() {
        return deviceProtocolPluggableClass.getJavaClassName();
    }

    @Override
    public void delete() {
        this.deviceProtocolPluggableClass.delete();
    }

    @Override
    public DeviceProtocolDialect getDeviceProtocolDialect() {
        return this.deviceProtocolDialect;
    }

}