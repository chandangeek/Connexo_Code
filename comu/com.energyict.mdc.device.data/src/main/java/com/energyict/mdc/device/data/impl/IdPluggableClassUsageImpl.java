package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.exceptions.NestedRelationTransactionException;
import com.energyict.mdc.device.data.exceptions.RelationIsAlreadyObsoleteException;
import com.energyict.mdc.dynamic.HasDynamicProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.CanLock;
import com.energyict.mdc.dynamic.relation.DefaultRelationParticipant;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClassUsageProperty;
import com.energyict.mdc.pluggable.PluggableClassWithRelationSupport;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 30/04/14
 * Time: 10:50
 */
public abstract class IdPluggableClassUsageImpl<D, T extends HasDynamicProperties, PT extends PluggableClassUsageProperty<T>>
        extends PersistentIdObject<D>
        implements
        CanLock,
        DefaultRelationParticipant,
        PropertyFactory<T, PT>  {

    private long pluggableClassId;
    private transient PropertyCache<T, PT> cache;

    private RelationService relationService;
    private Clock clock;

    protected IdPluggableClassUsageImpl(Class<D> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus, RelationService relationService, Clock clock) {
        super(domainClass, dataModel, eventService, thesaurus);
        this.cache = new PropertyCache<>(this);
        this.relationService = relationService;
        this.clock = clock;
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
     * all the {@link com.energyict.mdc.pluggable.PluggableClass properties}.
     */
    private void deleteAllProperties() {
        this.obsoleteAllProperties();
    }

    /**
     * Makes all the {@link Relation}s that hold the values of
     * all the {@link com.energyict.mdc.pluggable.PluggableClass properties} obsolete.
     */
    protected void obsoleteAllProperties()  {
        List<Relation> relations = this.getPluggableClass().getRelations(this, new Interval(null, null));
        for (Relation relation : relations) {
            try {
                relation.makeObsolete();
            }
            catch (BusinessException | SQLException e) {
                throw new RelationIsAlreadyObsoleteException(this.getThesaurus(), relation.getRelationType().getName());
            }
        }
    }

    protected long getPluggableClassId() {
        return pluggableClassId;
    }

    protected void setPluggableClassId(long pluggableClassId) {
        this.pluggableClassId = pluggableClassId;
    }

    public abstract PluggableClassWithRelationSupport getPluggableClass();

    @Override
    public Relation getDefaultRelation() {
        return this.getDefaultRelation(this.clock.now());
    }

    @Override
    public Relation getDefaultRelation(Date date) {
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
    public Object get(String propertyName, Date date) {
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

    protected TypedProperties getTypedProperties() {
        TypedProperties typedProperties = TypedProperties.inheritingFrom(this.getPluggableProperties());
        for (PT property : this.getAllLocalProperties(this.clock.now())) {
            if (property.getValue() != null) {
                typedProperties.setProperty(property.getName(), property.getValue());
            }
        }
        return typedProperties;
    }

    private TypedProperties getPluggableProperties() {
        return this.getPluggableClass().getProperties(this.getPluggablePropetySpecs());
    }

    protected abstract List<PropertySpec> getPluggablePropetySpecs();

    public List<PT> getProperties() {
        return this.getAllProperties();
    }

    public List<PT> getAllProperties() {
        return this.getAllProperties(this.clock.now());
    }

    public List<PT> getAllProperties(Date date) {
        return this.getAllLocalProperties(date);
    }

    private List<PT> getAllLocalProperties(Date date) {
        return this.cache.get(date);
    }

    @Override
    public List<PT> loadProperties(Date date) {
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
    public List<PT> loadProperties(Interval interval) {
        List<PT> properties = new ArrayList<>();
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

    public PT getProperty(String propertyName) {
        for (PT property : this.getAllProperties()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    protected List<PT> toProperties(Relation relation) {
        List<PT> properties = new ArrayList<>();
        for (RelationAttributeType attributeType : relation.getRelationType().getAttributeTypes()) {
            if (!isDefaultAttribute(attributeType) && this.attributeHasValue(relation, attributeType)) {
                properties.add(this.newPropertyFor(relation, attributeType));
            }
        }
        return properties;
    }

    protected void setProperty(String propertyName, Object value) {
        Date now = this.clock.now();
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.put(now, propertyName, value);
    }

    protected void removeProperty(String propertyName) {
        Date now = this.clock.now();
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.remove(now, propertyName);
    }

    private boolean attributeHasValue(Relation relation, RelationAttributeType attributeType) {
        return relation.get(attributeType) != null;
    }

    private boolean isDefaultAttribute(RelationAttributeType attributeType) {
        return this.getDefaultAttributeName().equals(attributeType.getName());
    }

    protected abstract String getDefaultAttributeName();

    protected abstract PT newPropertyFor(Relation relation, RelationAttributeType attributeType);

    protected abstract PT newInheritedPropertyFor(String propertyName, Object propertyValue);

    @Override
    public Object get(String attributeName) {
        return this.get(attributeName, this.clock.now());
    }

    @Override
    public Object get(RelationAttributeType attributeType, Date date) {
        return this.get(attributeType.getName(), date);
    }

    @Override
    public Object get(RelationAttributeType attributeType) {
        return this.get(attributeType, this.clock.now());
    }

    @Override
    public List<RelationType> getAvailableRelationTypes() {
        return this.relationService.findRelationTypesByParticipant(this);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete) {
        return attrib.getRelations(this, date, includeObsolete, 0, 0);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete, int fromRow, int toRow) {
        return attrib.getRelations(this, date, includeObsolete, fromRow, toRow);
    }

    @Override
    public List<Relation> getAllRelations(RelationAttributeType attrib) {
        return attrib.getAllRelations(this);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType defaultAttribute, Interval period, boolean includeObsolete) {
        return defaultAttribute.getRelations(this, period, includeObsolete);
    }

    protected void saveAllProperties () {
        if (this.cache.isDirty()) {
            if (this.getTypedProperties().localSize() == 0) {
                this.removeAllProperties();
            }
            else {
                this.saveAllProperties(
                        this.getAllProperties(),
                        new SimpleRelationTransactionExecutor<T>(
                                this,
                                this.clock.now(),
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
                throw new NestedRelationTransactionException(this.thesaurus, e, this.findRelationType().getName());
            }
            // Cannot collapse catch blocks because of the constructor
            catch (SQLException e) {
                throw new NestedRelationTransactionException(this.thesaurus, e, this.findRelationType().getName());
            }
        }
    }

    protected abstract RelationType findRelationType ();

    private void saveAllProperties(List<PT> propertyShadows, RelationTransactionExecutor<T> transactionExecutor) {
        for (PT shadow : propertyShadows) {
            transactionExecutor.add(shadow);
        }
        transactionExecutor.execute();
        this.clearPropertyCache();
    }

    protected Interval always() {
        return new Interval(null, null);
    }
}
