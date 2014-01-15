package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.ReferenceFactory;
import com.energyict.mdc.dynamic.ValueFactory;
import com.energyict.mdc.dynamic.relation.ConstraintShadow;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationAttributeTypeShadow;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeFactory;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdw.core.MeteringWarehouse;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Straightforward implementation of the {@link DeviceProtocolDialectUsagePluggableClass} interface.
 * <p/>
 * Copyrights EnergyICT
 * Date: 1/10/12
 * Time: 15:36
 */
public class DeviceProtocolDialectUsagePluggableClassImpl implements ServerDeviceProtocolDialectUsagePluggableClass {

    public static final String DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME = "deviceProtocolDialect";

    private final DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private final DeviceProtocolDialect deviceProtocolDialect;
    private final DataModel dataModel;
    private RelationType relationType;  // Cache

    public DeviceProtocolDialectUsagePluggableClassImpl(DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceProtocolDialect deviceProtocolDialect, DataModel dataModel) {
        this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
        this.deviceProtocolDialect = deviceProtocolDialect;
        this.dataModel = dataModel;
    }

    @Override
    public PluggableClassType getPluggableClassType () {
        return null;
    }

    @Override
    public RelationType findOrCreateRelationType(boolean activate) throws BusinessException, SQLException {
        if (this.deviceProtocolDialectHasProperties()) {
            String relationTypeName = getConformRelationTypeName();
            RelationType relationType = this.findRelationType(relationTypeName);
            if (relationType == null) {
                relationType = this.createRelationType(this.deviceProtocolDialect);
                if (activate) {
                    this.activate(relationType);
                }
            }
            if (relationType != null) {
                this.registerRelationType(relationType, deviceProtocolPluggableClass);
            }
            return relationType;
        } else {
            return null;
        }
    }

    private String getConformRelationTypeName() {
        return RelationUtils.createConformRelationTypeName(this.deviceProtocolDialect.getDeviceProtocolDialectName());
    }

    private RelationType findRelationType(String relationTypeName) {
        return getRelationTypeFactory().find(relationTypeName);
    }

    private RelationType createRelationType(DeviceProtocolDialect deviceProtocolDialect) throws BusinessException, SQLException {
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
        return getRelationTypeFactory().create(relationTypeShadow);
    }

    private RelationTypeFactory getRelationTypeFactory () {
        return ManagerFactory.getCurrent().getMdwInterface().getRelationTypeFactory();
    }


    private ConstraintShadow constraintShadowFor(RelationAttributeTypeShadow defaultAttributeTypeShadow) {
        ConstraintShadow shadow = new ConstraintShadow();
        shadow.add(defaultAttributeTypeShadow);
        shadow.setName("Unique " + this.deviceProtocolDialect.getDeviceProtocolDialectName());
        shadow.setRejectViolations(false);
        return shadow;
    }

    private void activate(RelationType relationType) throws BusinessException, SQLException {
        relationType.activate();
    }

    private RelationAttributeTypeShadow relationAttributeTypeShadowFor(PropertySpec propertySpec, boolean isRequired) {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(RelationUtils.createConformRelationAttributeName(propertySpec.getName()));
        shadow.setIsDefault(false);
        shadow.setRequired(isRequired);
        ValueFactory valueFactory = propertySpec.getValueFactory();
        shadow.setValueFactoryClass(valueFactory.getClass());
        if (valueFactory.isReference()) {
            shadow.setObjectFactoryId(MeteringWarehouse.getCurrent().findFactory(valueFactory.getValueType().getName()).getId());
        }
        return shadow;
    }

    private RelationAttributeTypeShadow defaultAttributeTypeShadow() {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        shadow.setRequired(true);
        shadow.setIsDefault(true);
        shadow.setObjectFactoryId(ManagerFactory.getCurrent().getProtocolDialectPropertiesFactory().getId());
        shadow.setValueFactoryClass(ReferenceFactory.class);
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
        RelationType relationType = this.findRelationType(relationTypeName);
        if (this.deviceProtocolDialectHasProperties() && relationType == null) {
            throw new ApplicationException("Creation of relation type " + relationTypeName + " for device protocol class " + this.deviceProtocolPluggableClass.getJavaClassName() + " and dialect " + this.deviceProtocolDialect.getDeviceProtocolDialectName() + " failed before.");
        }
        return relationType;
    }

    @Override
    public void deleteRelationType() throws BusinessException, SQLException {
        RelationType relationType;
        try {
            relationType = this.findRelationType();
        } catch (ApplicationException e) {
            /* Creation of relation type failed before, no need to unRegister and delete the relation type
             * However, since we are compiling with AspectJ's Xlint option set to error level
             * to trap advice that does not apply,
             * it will not be happy until we actually code something here. */
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
     *
     * @throws SQLException Indicates failures to execute the sql that registers the usage
     */
    public void registerRelationType(RelationType relationType, PluggableClass pluggableClass) throws SQLException {
        PluggableClassRelationAttributeTypeRegistry typeRegistry =
                new PluggableClassRelationAttributeTypeRegistry(this.dataModel.mapper(PluggableClassRelationAttributeTypeUsage.class));
        RelationAttributeType attributeType = relationType.getAttributeType(DeviceProtocolDialectUsagePluggableClassImpl.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        if (!typeRegistry.isRegistered(pluggableClass, attributeType)) {
            typeRegistry.register(pluggableClass, attributeType);
        }
    }

    /**
     * Undo the registration of the fact that this DeviceProtocolDialectPluggableClass
     * uses the {@link RelationType} to hold attribute values.
     *
     * @param pluggableClass The DeviceProtocolPluggableClass
     * @throws SQLException Indicates failures to execute the sql that registers the usage
     */
    private void unRegisterRelationType(DeviceProtocolPluggableClass pluggableClass) throws SQLException {
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
    public Relation getRelation(RelationParticipant relationParticipant, Date date) {
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
    public List<Relation> getRelations(RelationParticipant relationParticipant, Interval period) {
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
    public void setName(String name) throws BusinessException {
        this.deviceProtocolPluggableClass.setName(name);
    }

    @Override
    public Date getModificationDate() {
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
    public void save() throws BusinessException, SQLException {
        this.deviceProtocolPluggableClass.save();
    }

    @Override
    public String getJavaClassName() {
        return deviceProtocolPluggableClass.getJavaClassName();
    }

    @Override
    public void delete() throws BusinessException, SQLException {
        this.deviceProtocolPluggableClass.delete();
    }

    @Override
    public DeviceProtocolDialect getDeviceProtocolDialect() {
        return this.deviceProtocolDialect;
    }

}