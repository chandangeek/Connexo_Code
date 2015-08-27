package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.google.common.collect.Range;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.exceptions.DuplicateNameException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.NestedRelationTransactionException;
import com.energyict.mdc.device.data.exceptions.RelationIsAlreadyObsoleteException;
import com.energyict.mdc.dynamic.relation.CanLock;
import com.energyict.mdc.dynamic.relation.DefaultRelationParticipant;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassWithRelationSupport;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectProperty;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME;

/**
 * Provides an implementation for the {@link ProtocolDialectProperties} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-31 (08:54)
 */
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
public class ProtocolDialectPropertiesImpl
        extends PersistentNamedObject<ProtocolDialectProperties>
        implements
            CanLock,
            DefaultRelationParticipant,
            PropertyFactory<DeviceProtocolDialect, DeviceProtocolDialectProperty>,
            ProtocolDialectProperties,
            PersistenceAware {

    private long pluggableClassId;
    private transient PropertyCache<DeviceProtocolDialect, DeviceProtocolDialectProperty> cache;

    private Clock clock;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> configurationProperties = ValueReference.absent();
    private DeviceProtocolDialectUsagePluggableClass deviceProtocolDialectUsagePluggableClass;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED + "}")
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private AttributeNameMapper attributeNameMapper = new AttributeNameMapper();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private ProtocolPluggableService protocolPluggableService;

    @Inject
    public ProtocolDialectPropertiesImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ProtocolPluggableService protocolPluggableService) {
        super(ProtocolDialectProperties.class, dataModel, eventService, thesaurus);
        this.cache = new PropertyCache<>(this);
        this.clock = clock;
        this.protocolPluggableService = protocolPluggableService;
    }

    public ProtocolDialectPropertiesImpl initialize (Device device, ProtocolDialectConfigurationProperties configurationProperties) {
        this.device.set(device);
        this.configurationProperties.set(configurationProperties);
        this.setName(configurationProperties.getName());
        this.setDeviceProtocolPluggableClassFromConfigurationProperties();
        return this;
    }

    private void setDeviceProtocolPluggableClassFromConfigurationProperties() {
        this.setDeviceProtocolPluggableClass(this.getDeviceProtocolPluggableClass(configurationProperties.get()));
    }

    private DeviceProtocolPluggableClass getDeviceProtocolPluggableClass(ProtocolDialectConfigurationProperties configurationProperties) {
        return configurationProperties.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
    }

    DeviceProtocol getDeviceProtocol () {
        return this.getDeviceProtocolPluggableClass(this.getProtocolDialectConfigurationProperties()).getDeviceProtocol();
    }

    private void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocol) {
        this.deviceProtocolPluggableClass = deviceProtocol;
        this.pluggableClassId = deviceProtocol.getId();
    }

    @Override
    public void postLoad() {
        this.setDeviceProtocolPluggableClassFromConfigurationProperties();
    }

    @Override
    public void save() {
        super.save();
        this.saveAllProperties();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    public void lock() {
        this.getDataMapper().lock(this.getId());
    }

    @Override
    protected void doDelete() {
        this.deleteAllProperties();
    }

    /**
     * Deletes the {@link Relation}s that hold the values of
     * all the {@link PluggableClass properties}.
     */
    private void deleteAllProperties() {
        this.obsoleteAllProperties();
    }

    /**
     * Makes all the {@link Relation}s that hold the values of
     * all the {@link PluggableClass properties} obsolete.
     */
    protected void obsoleteAllProperties()  {
        List<Relation> relations = this.getPluggableClass().getRelations(this, Range.all());
        for (Relation relation : relations) {
            try {
                relation.makeObsolete();
            }
            catch (BusinessException | SQLException e) {
                throw new RelationIsAlreadyObsoleteException(this.getThesaurus(), relation.getRelationType().getName());
            }
        }
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate for now
    }

    @Override
    public Device getDevice () {
        return this.device.get();
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return this.configurationProperties.get();
    }

    @Override
    public String getDeviceProtocolDialectName () {
        if (this.getProtocolDialectConfigurationProperties() == null) {
            return "";
        }
        else {
            return getProtocolDialectConfigurationProperties().getDeviceProtocolDialectName();
        }
    }

    @Override
    public String getName() {
        if (this.configurationProperties.isPresent()) {
            return getProtocolDialectConfigurationProperties().getName();
        }
        else {
            return "";
        }
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return null;
    }

    @Override
    public DeviceProtocolDialectProperty newProperty(String propertyName, Object propertyValue, Instant activeDate) {
        return new DeviceProtocolDialectPropertyImpl(propertyName, propertyValue, Range.all(), this.getPluggableClass(), false);
    }

    @Override
    public Object get(String attributeName) {
        return this.get(attributeName, this.clock.instant());
    }

    @Override
    public Object get(RelationAttributeType attributeType, Instant date) {
        return this.get(attributeType.getName(), date);
    }

    @Override
    public Object get(RelationAttributeType attributeType) {
        return this.get(attributeType, this.clock.instant());
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Instant date, boolean includeObsolete) {
        return attrib.getRelations(this, date, includeObsolete, 0, 0);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Instant date, boolean includeObsolete, int fromRow, int toRow) {
        return attrib.getRelations(this, date, includeObsolete, fromRow, toRow);
    }

    @Override
    public List<Relation> getAllRelations(RelationAttributeType attrib) {
        return attrib.getAllRelations(this);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType defaultAttribute, Range<Instant> period, boolean includeObsolete) {
        return defaultAttribute.getRelations(this, period, includeObsolete);
    }

    protected void saveAllProperties () {
        if (this.cache.isDirty()) {
            if (this.getTypedProperties().localSize() == 0) {
                this.removeAllProperties();
            }
            else {
                this.saveAllProperties(
                        this.getAllLocalProperties(),
                        new SimpleRelationTransactionExecutor<>(
                                this,
                                this.clock.instant(),
                                this.findRelationType(),
                                this.getThesaurus()));
            }
        }
    }

    protected void removeAllProperties() {
        Relation relation = getDefaultRelation();
        if (relation != null) {
            try {
                relation.makeObsolete();
            }
            catch (BusinessException e) {
                throw new NestedRelationTransactionException(this.getThesaurus(), e, this.findRelationType().getName());
            }
            // Cannot collapse catch blocks because of the constructor
            catch (SQLException e) {
                throw new NestedRelationTransactionException(this.getThesaurus(), e, this.findRelationType().getName());
            }
        }
    }

    private RelationType findRelationType() {
        return this.getDeviceProtocolDialectUsagePluggableClass().findRelationType();
    }

    @Override
    public TypedProperties getTypedProperties() {
        TypedProperties typedProperties = TypedProperties.empty();
        if (this.getProtocolDialectConfigurationProperties() != null) {
            typedProperties = TypedProperties.inheritingFrom(this.getProtocolDialectConfigurationProperties().getTypedProperties());
        }
        typedProperties.setAllProperties(this.getLocalTypedProperties());
        return checkForCorrectRelationAttributeNamings(typedProperties);
    }

    private TypedProperties getLocalTypedProperties() {
        TypedProperties typedProperties = TypedProperties.inheritingFrom(this.getPluggableProperties());
        this.getAllLocalProperties(this.clock.instant())
                .stream()
                .filter(property -> property.getValue() != null)
                .forEach(property ->
                        typedProperties.setProperty(
                                property.getName(),
                                property.getValue()));
        return typedProperties;
    }

    private void saveAllProperties(List<DeviceProtocolDialectProperty> propertyShadows, RelationTransactionExecutor<DeviceProtocolDialect> transactionExecutor) {
        propertyShadows.forEach(transactionExecutor::add);
        transactionExecutor.execute();
        this.clearPropertyCache();
    }

    private TypedProperties checkForCorrectRelationAttributeNamings(TypedProperties typedProperties) {
        TypedProperties correctedProperties = TypedProperties.inheritingFrom(typedProperties.getInheritedProperties());
        typedProperties
                .localPropertyNames()
                .stream()
                .forEach(propertyName -> {
                    String propertySpecName = this.attributeNameMapper.toPropertySpecName(propertyName);
                    correctedProperties.setProperty(propertySpecName, typedProperties.getLocalValue(propertyName));
                });
        return correctedProperties;
    }

    @Override
    public DeviceProtocolDialectUsagePluggableClass getPluggableClass() {
        return getDeviceProtocolDialectUsagePluggableClass();
    }

    private Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long pluggableClassId) {
        return this.protocolPluggableService.findDeviceProtocolPluggableClass(pluggableClassId);
    }

    private DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass() {
        if (this.deviceProtocolDialectUsagePluggableClass == null) {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.getDeviceProtocolPluggableClass();
            this.deviceProtocolDialectUsagePluggableClass = this.protocolPluggableService.getDeviceProtocolDialectUsagePluggableClass(deviceProtocolPluggableClass, this.getDeviceProtocolDialectName());
        }
        return this.deviceProtocolDialectUsagePluggableClass;
    }

    private DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        if (this.deviceProtocolPluggableClass == null) {
            this.deviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(pluggableClassId).get();
        }
        return this.deviceProtocolPluggableClass;
    }

    @Override
    public Relation getDefaultRelation() {
        return this.getDefaultRelation(this.clock.instant());
    }

    @Override
    public Relation getDefaultRelation(Instant date) {
        return this.getPluggableClass().getRelation(this, date);
    }

    @Override
    public RelationAttributeType getDefaultAttributeType() {
        return this.getPluggableClass().getDefaultAttributeType();
    }

    @Override
    public RelationType getDefaultRelationType() {
        return this.getPluggableClass().findRelationType();
    }

    protected void clearPropertyCache() {
        this.cache.clear();
    }

    @Override
    public Object get(String propertyName, Instant date) {
        PluggableClassWithRelationSupport pluggableClass = this.getPluggableClass();
        if (pluggableClass.findRelationType().hasAttribute(propertyName)) {
            // Should in fact be at most one since this is the default relation
            Relation relation = this.getDefaultRelation(date);
            if (relation == null) {
                // No relation active on the specified Date, therefore no value
                return null;
            } else {
                return relation.get(propertyName);
            }
        }
        // Either no properties configured on the PluggableClass or not one of my properties
        return null;
    }

    private TypedProperties getPluggableProperties() {
        return this.getPluggableClass().getProperties(this.getPluggablePropertySpecs());
    }

    private List<PropertySpec> getPluggablePropertySpecs() {
        return this.getDeviceProtocolDialectUsagePluggableClass().getDeviceProtocolDialect().getPropertySpecs();
    }

    public List<DeviceProtocolDialectProperty> getProperties() {
        return this.getAllProperties();
    }

    public List<DeviceProtocolDialectProperty> getAllProperties() {
        return this.getAllProperties(this.clock.instant());
    }

    private List<DeviceProtocolDialectProperty> getAllLocalProperties() {
        return this.getAllLocalProperties(this.clock.instant());
    }

    private List<DeviceProtocolDialectProperty> getAllLocalProperties(Instant date) {
        return this.cache.get(date);
    }

    @Override
    public List<DeviceProtocolDialectProperty> loadProperties(Instant date) {
        Relation defaultRelation = this.getDefaultRelation(date);
        /* defaultRelation is null when the pluggable class has no properties.
         * In that case, no relation type was created. */
        if (defaultRelation != null) {
            return this.toProperties(defaultRelation);
        } else {
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<DeviceProtocolDialectProperty> loadProperties(Range<Instant> interval) {
        List<DeviceProtocolDialectProperty> properties = new ArrayList<>();
        RelationAttributeType defaultAttributeType = this.getDefaultAttributeType();
        /* defaultAttributeType is null when the pluggable class has no properties.
         * In that case, no relation type was created. */
        if (defaultAttributeType != null) {
            List<Relation> relations = this.getRelations(defaultAttributeType, interval, false);
            for (Relation relation : relations) {
                properties.addAll(this.toProperties(relation));
            }
        }
        return properties;
    }

    public DeviceProtocolDialectProperty getProperty(String propertyName) {
        for (DeviceProtocolDialectProperty property : this.getAllProperties()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    protected List<DeviceProtocolDialectProperty> toProperties(Relation relation) {
        List<DeviceProtocolDialectProperty> properties = new ArrayList<>();
        for (RelationAttributeType attributeType : relation.getRelationType().getAttributeTypes()) {
            if (!isDefaultAttribute(attributeType) && this.attributeHasValue(relation, attributeType)) {
                properties.add(this.newPropertyFor(relation, attributeType));
            }
        }
        return properties;
    }

    private DeviceProtocolDialectProperty newPropertyFor(Relation relation, RelationAttributeType attributeType) {
        return new DeviceProtocolDialectPropertyImpl(relation, attributeType.getName(), this.getPluggableClass());
    }
    @Override
    public void setProperty(String propertyName, Object value) {
        Instant now = this.clock.instant();
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.put(now, this.attributeNameMapper.toAttributeTypeName(propertyName), value);
    }

    @Override
    public void removeProperty(String propertyName) {
        Instant now = this.clock.instant();
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.remove(now, this.attributeNameMapper.toAttributeTypeName(propertyName));
    }

    private boolean attributeHasValue(Relation relation, RelationAttributeType attributeType) {
        return relation.get(attributeType) != null;
    }

    private boolean isDefaultAttribute(RelationAttributeType attributeType) {
        return this.getDefaultAttributeName().equals(attributeType.getName());
    }

    private String getDefaultAttributeName() {
        return DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME;
    }

    @Override
    public List<DeviceProtocolDialectProperty> getAllProperties(Instant date) {
        List<DeviceProtocolDialectProperty> allProperties = new ArrayList<>();
        List<DeviceProtocolDialectProperty> localProperties = this.getAllLocalProperties(date);
        this.addConfigurationProperties(allProperties, localProperties);
        return this.checkForRelationAttributeNameConversions(allProperties);
    }

    private void addConfigurationProperties(List<DeviceProtocolDialectProperty> allProperties, List<DeviceProtocolDialectProperty> localProperties) {
        final ProtocolDialectConfigurationProperties configurationProperties = this.getProtocolDialectConfigurationProperties();
        if (configurationProperties != null) {
            TypedProperties inheritedProperties = configurationProperties.getTypedProperties();
            for (String inheritedPropertyName : inheritedProperties.propertyNames()) {
                if (!propertySpecifiedIn(localProperties, inheritedPropertyName)) { // If the inherited property is overruled by a local property, then do not add it
                    allProperties.add(
                            newInheritedPropertyFor(
                                    inheritedPropertyName,
                                    inheritedProperties.getProperty(inheritedPropertyName)
                            )
                    );
                }
            }
        }
        allProperties.addAll(localProperties);
    }

    private DeviceProtocolDialectProperty newInheritedPropertyFor (String propertyName, Object propertyValue) {
        return new DeviceProtocolDialectPropertyImpl(propertyName, propertyValue, Range.all(), this.getPluggableClass(), true);
    }

    /**
     * Method to check if the given list of {@link DeviceProtocolDialectProperty DeviceProtocolDialectProperties} contains a property with the specified name.
     *
     * @param properties    The list of DeviceProtocolProperties
     * @param propertyName  The property name to be searched for
     * @return true if the list contains a property with the specified name
     *         false if not the case
     */
    private boolean propertySpecifiedIn(List<DeviceProtocolDialectProperty> properties, String propertyName) {
        for (DeviceProtocolDialectProperty dialectProperty : properties) {
            if (dialectProperty.getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reverts the names of the DeviceProtocolDialectProperty back to the original name
     * as provided by the pluggable class in case the name was modified during
     * relation type creation to comply with naming conventions
     * for relation type attributes (such as length, removal of special characters,...).
     *
     * @param allProperties the list of DeviceProtocolDialectProperties we received from the relation
     * @return a proper list of properties which can be forwarded to a DeviceProtocol
     */
    private List<DeviceProtocolDialectProperty> checkForRelationAttributeNameConversions(List<DeviceProtocolDialectProperty> allProperties) {
        return allProperties
                    .stream()
                    .map(dialectProperty -> new DeviceProtocolDialectPropertyImpl(
                            attributeNameMapper.toPropertySpecName(dialectProperty.getName()),
                            dialectProperty.getValue(),
                            dialectProperty.getActivePeriod(),
                            dialectProperty.getPluggableClass(),
                            dialectProperty.isInherited()))
                    .collect(Collectors.toList());
    }

    private class AttributeNameMapper {
        private boolean initialized = false;
        private Map<String, String> propertySpecName2AttributeTypeName;
        private Map<String, String> attributeTypeName2PropertySpecName;

        String toAttributeTypeName(String propertySpecName) {
            this.ensureMappingInitialized();
            return this.propertySpecName2AttributeTypeName.get(propertySpecName);
        }

        String toPropertySpecName(String attributeTypeName) {
            this.ensureMappingInitialized();
            return this.attributeTypeName2PropertySpecName.get(attributeTypeName);
        }

        private void ensureMappingInitialized() {
            if (!this.initialized) {
                this.propertySpecName2AttributeTypeName = new HashMap<>();
                this.attributeTypeName2PropertySpecName = new HashMap<>();
                getDeviceProtocolDialectUsagePluggableClass()
                        .getDeviceProtocolDialect()
                        .getPropertySpecs()
                        .stream()
                        .forEach(this::initializeMappingFor);
                this.initialized = true;
            }
        }

        private void initializeMappingFor(PropertySpec propertySpec) {
            String attributeTypeName = protocolPluggableService.createConformRelationAttributeName(propertySpec.getName());
            this.propertySpecName2AttributeTypeName.put(propertySpec.getName(), attributeTypeName);
            this.attributeTypeName2PropertySpecName.put(attributeTypeName, propertySpec.getName());
        }
    }
}