package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.JupiterReferenceFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.LegacyReferenceFactory;
import com.energyict.mdc.dynamic.ValueFactory;
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
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.protocol.pluggable.ConnectionTypePropertyRelationAttributeTypeNames.CONNECTION_METHOD_ATTRIBUTE_NAME;

/**
 * Provides an implementation for the {@link ConnectionTypePluggableClass} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-31 (10:43)
 */
public final class ConnectionTypePluggableClassImpl extends PluggableClassWrapper<ConnectionType> implements ConnectionTypePluggableClass {

    private DataModel dataModel;
    private RelationService relationService;
    private ConnectionTypeService connectionTypeService;
    private RelationType relationType;  // Cache

    @Inject
    public ConnectionTypePluggableClassImpl(EventService eventService, Thesaurus thesaurus, DataModel dataModel, RelationService relationService, ConnectionTypeService connectionTypeService) {
        super(eventService, thesaurus);
        this.dataModel = dataModel;
        this.relationService = relationService;
        this.connectionTypeService = connectionTypeService;
    }

    static ConnectionTypePluggableClassImpl from (DataModel dataModel, PluggableClass pluggableClass) {
        return dataModel.getInstance(ConnectionTypePluggableClassImpl.class).initializeFrom(pluggableClass);
    }

    ConnectionTypePluggableClassImpl initializeFrom (PluggableClass pluggableClass) {
        this.setPluggableClass(pluggableClass);
        return this;
    }

    @Override
    public Discriminator discriminator() {
        return Discriminator.CONNECTIONTYE;
    }

    @Override
    protected void validateLicense() {
        // No license information to validate
    }

    @Override
    protected ConnectionType newInstance(PluggableClass pluggableClass) {
        return this.newInstance(pluggableClass.getJavaClassName());
    }

    private ConnectionType newInstance (String javaClassName) {
        return this.connectionTypeService.createConnectionType(javaClassName);
    }

    @Override
    public void save() {
        this.findOrCreateRelationType(true);
        super.save();
    }

    @Override
    public void delete() {
        this.deleteRelationType();
        super.delete();
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return super.getProperties(propertySpecs);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        ConnectionType connectionType = this.newInstance();
        return connectionType.getPropertySpec(name);
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public ConnectionType getConnectionType () {
        ConnectionType connectionType = this.newInstance();
        connectionType.copyProperties(this.getProperties(connectionType.getPropertySpecs()));
        return connectionType;
    }

    @Override
    public PluggableClassType getPluggableClassType () {
        return PluggableClassType.ConnectionType;
    }

    @Override
    public RelationAttributeType getDefaultAttributeType () {
        if (!this.connectionTypeHasProperties()) {
            return null;
        }
        else {
            return this.findRelationType().getAttributeType(CONNECTION_METHOD_ATTRIBUTE_NAME);
        }
    }

    @Override
    public Relation getRelation (RelationParticipant relationParticipant, Date date) {
        if (!this.connectionTypeHasProperties()) {
            return null;
        }
        else {
            List<Relation> relations = relationParticipant.getRelations(this.findRelationType().getAttributeType(CONNECTION_METHOD_ATTRIBUTE_NAME), date, false);
            if (relations.isEmpty()) {
                return null;
            }
            else if (relations.size() > 1) {
                throw new ApplicationException(MessageFormat.format("More than one default relation for the same date {0,date,yyy-MM-dd HH:mm:ss", date));
            }
            else {
                return relations.get(0);
            }
        }
    }

    @Override
    public List<Relation> getRelations (RelationParticipant relationParticipant, Interval period) {
        if (!this.connectionTypeHasProperties()) {
            return new ArrayList<>(0);
        }
        else {
            return relationParticipant.getRelations(this.findRelationType().getAttributeType(CONNECTION_METHOD_ATTRIBUTE_NAME), period, false);
        }
    }

    @Override
    public RelationType findRelationType () {
        if (this.relationType == null) {
            this.relationType = this.doFindRelationType();
        }
        return this.relationType;
    }

    private RelationType doFindRelationType () {
        if (this.connectionTypeHasProperties()) {
            String relationTypeName = this.relationTypeNameFor(this.newInstance());
            RelationType relationType = this.findRelationType(relationTypeName);
            if (relationType == null) {
                throw new ApplicationException("Creation of relation type for connection type " + this.getJavaClassName() + " failed before.");
            }
            return relationType;
        }
        else {
            return null;
        }
    }

    private boolean connectionTypeHasProperties () {
        return !this.getPropertySpecs().isEmpty();
    }

    @Override
    public RelationType findOrCreateRelationType (boolean activate) {
        if (this.connectionTypeHasProperties()) {
            ConnectionType connectionType = this.newInstance();
            String relationTypeName = this.relationTypeNameFor(connectionType);
            RelationType relationType = this.findRelationType(relationTypeName);
            if (relationType == null) {
                relationType = this.createRelationType(connectionType);
                if (activate) {
                    this.activate(relationType);
                }
            }
            if (relationType != null) {
                this.registerRelationType(relationType);
            }
            return relationType;
        }
        else {
            return null;
        }
    }

    private RelationType findRelationType (String relationTypeName) {
        return this.relationService.findRelationType(relationTypeName);
    }

    /**
     * Registers the fact that this ConnectionTypePluggableClass
     * uses the {@link RelationType} to hold attribute values.
     *
     * @param relationType The RelationType
     */
    private void registerRelationType(RelationType relationType) {
        PluggableClassRelationAttributeTypeRegistry typeRegistry = this.getPluggableClassRelationAttributeTypeRegistry();
        RelationAttributeType attributeType = relationType.getAttributeType(CONNECTION_METHOD_ATTRIBUTE_NAME);
        if (!typeRegistry.isRegistered(this, attributeType)) {
            typeRegistry.register(this, attributeType);
        }
    }

    @Override
    public void deleteRelationType () {
        RelationType relationType;
        try {
            relationType = this.findRelationType();
        }
        catch (ApplicationException e) {
            /* Creation of relation type failed before, no need to unRegister and delete the relation type
             * However, since we are compiling with AspectJ's Xlint option set to error level
             * to trap advice that does not apply,
             * it will not be happy until we actually code something here. */
            relationType = null;
        }
        if (relationType != null) {
            this.unregisterRelationType();
            if (!this.isUsedByAnotherPluggableClass(relationType)) {
                relationType.delete();
            }
        }
    }

    private boolean isUsedByAnotherPluggableClass (RelationType relationType) {
        PluggableClassRelationAttributeTypeRegistry registry = this.getPluggableClassRelationAttributeTypeRegistry();
        return registry.isDefaultAttribute(relationType.getAttributeType(CONNECTION_METHOD_ATTRIBUTE_NAME));
    }

    /**
     * Undo the registration of the fact that this ConnectionTypePluggableClass
     * uses the {@link RelationType} to hold attribute values.
     */
    private void unregisterRelationType () {
        if (this.connectionTypeHasProperties()) {
            RelationType relationType = this.findRelationType();
            this.getPluggableClassRelationAttributeTypeRegistry().unRegister(this, relationType.getAttributeType(CONNECTION_METHOD_ATTRIBUTE_NAME));
        }
    }

    private RelationType createRelationType (ConnectionType connectionType) {
        RelationTypeShadow relationTypeShadow = new RelationTypeShadow();
        relationTypeShadow.setSystem(true);
        relationTypeShadow.setName(this.relationTypeNameFor(connectionType));
        relationTypeShadow.setHasTimeResolution(true);
        RelationAttributeTypeShadow defaultAttribute = this.defaultAttributeTypeShadow();
        relationTypeShadow.setLockAttributeTypeShadow(defaultAttribute);
        relationTypeShadow.add(defaultAttribute);
        for (PropertySpec propertySpec : connectionType.getPropertySpecs()) {
            relationTypeShadow.add(this.relationAttributeTypeShadowFor(propertySpec));
        }
        relationTypeShadow.add(this.constraintShadowFor(connectionType, defaultAttribute));
        return this.relationService.createRelationType(relationTypeShadow);
    }

    private ConstraintShadow constraintShadowFor (ConnectionType connectionType, RelationAttributeTypeShadow defaultAttributeTypeShadow) {
        ConstraintShadow shadow = new ConstraintShadow();
        shadow.add(defaultAttributeTypeShadow);
        shadow.setName("Unique " + connectionType.getClass().getSimpleName());
        shadow.setRejectViolations(false);
        return shadow;
    }

    private RelationAttributeTypeShadow defaultAttributeTypeShadow () {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(CONNECTION_METHOD_ATTRIBUTE_NAME);
        shadow.setRequired(true);
        shadow.setIsDefault(true);
        shadow.setObjectFactoryId(FactoryIds.CONNECTION_METHOD.id());
        shadow.setValueFactoryClass(JupiterReferenceFactory.class);
        return shadow;
    }

    private RelationAttributeTypeShadow relationAttributeTypeShadowFor (PropertySpec propertySpec) {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setName(this.relationAttributeTypeNameFor(propertySpec.getName()));
        shadow.setIsDefault(false);
        shadow.setRequired(false);  // None of the attributes are required since they can be inherited from different levels
        ValueFactory valueFactory = propertySpec.getValueFactory();
        Class<? extends ValueFactory> valueFactoryClass = valueFactory.getClass();
        shadow.setValueFactoryClass(valueFactoryClass);
        if (valueFactory.isReference()) {
            BusinessObjectFactory businessObjectFactory = Environment.DEFAULT.get().findFactory(valueFactory.getValueType().getName());
            if (businessObjectFactory == null) {
                // Most likely a reference to a type of object that has already been moved to the new ORM framework
                CanFindByLongPrimaryKey finder = Environment.DEFAULT.get().finderFor(valueFactory.getValueType());
                shadow.setObjectFactoryId(finder.registrationKey().id());
            }
            else {
            shadow.setObjectFactoryId(businessObjectFactory.getId());
        }
        }
        return shadow;
    }

    private String relationTypeNameFor (ConnectionType connectionType) {
        return RelationUtils.createConformRelationTypeName(connectionType.getClass().getSimpleName());
    }

    private String relationAttributeTypeNameFor (String name) {
        return RelationUtils.createConformRelationAttributeName(name);
    }

    private void activate (RelationType relationType) {
        relationType.activate();
    }

    @Override
    protected void notifyDeleted() {
//        if (ManagerFactory.getCurrent().getConnectionTaskFactory().existsWithConnectionType(this)) {
//            throw new BusinessException(
//                    "connectionTypeXIsStillUsedByConnectionTasks",
//                    "The connection type pluggable class {0} is still in use by the at least one ConnectionTask",
//                    this.getName());
//        } else if (ManagerFactory.getCurrent().getPartialConnectionTaskFactory().existsWithConnectionType(this)) {
//            throw new BusinessException(
//                    "connectionTypeXIsStillUsedByPartialConnectionTasks",
//                    "The connection type pluggable class {0} is still in use by the at least one PartialConnectionTask",
//                    this.getName());
//        }
        throw new UnsupportedOperationException("ConnectionTypePluggableClassImpl#notifyDelete");
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.CONNECTIONTYPE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.CONNECTIONTYPE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.CONNECTIONTYPE;
    }

    @Override
    public boolean isInstance (ConnectionType connectionType) {
        return this.getJavaClassName().getClass().equals(connectionType.getClass().getName());
    }

    private PluggableClassRelationAttributeTypeRegistry getPluggableClassRelationAttributeTypeRegistry() {
        return new PluggableClassRelationAttributeTypeRegistry(this.dataModel.mapper(PluggableClassRelationAttributeTypeUsage.class));
    }

}