package com.energyict.mdc.pluggable.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.FieldValidationException;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.exceptions.DuplicateNameException;
import com.energyict.mdc.pluggable.exceptions.JavaClassNameIsRequiredException;
import com.energyict.mdc.pluggable.exceptions.NameIsRequiredException;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link PluggableClass} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013, Dec 2 (09:07)
 */
public class PluggableClassImpl implements PluggableClass {
    private final Provider<PluggableClassProperty> pluggableClassPropertyProvider;

    private long id;
    @NotNull
    private String name;
    @NotNull
    private String javaClassName;
    @NotNull
    private PersistentPluggableClassType pluggableType;
    private List<PluggableClassProperty> properties = new ArrayList<>();
    private Instant modTime;

    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;
    private Clock clock;

    @Inject
    public PluggableClassImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, Provider<PluggableClassProperty> pluggableClassPropertyProvider) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.pluggableClassPropertyProvider = pluggableClassPropertyProvider;
    }

    PluggableClassImpl initialize (PluggableClassType type, String name, String javaClassName) {
        this.pluggableType = PersistentPluggableClassType.forActualType(type);
        this.validateName(name);
        this.validateUniqueName(name, type);
        this.validateJavaClassName(javaClassName);
        this.name = name;
        this.javaClassName = javaClassName;
        return this;
    }

    static PluggableClassImpl from (DataModel dataModel, PluggableClassType type, String name, String javaClassName) {
        return dataModel.getInstance(PluggableClassImpl.class).initialize(type, name, javaClassName);
    }

    private void validateJavaClassName (String javaClassName) {
        if (javaClassName == null || (javaClassName.trim().isEmpty())) {
            throw new JavaClassNameIsRequiredException(MessageSeeds.NAME_IS_REQUIRED, this.thesaurus, this.getName());
        }
    }

    private void validateName(String newName) {
        if (newName == null) {
            throw new NameIsRequiredException(MessageSeeds.NAME_IS_REQUIRED, this.thesaurus);
        }
        if (newName.trim().isEmpty()) {
            throw new NameIsRequiredException(MessageSeeds.NAME_IS_REQUIRED, this.thesaurus);
        }
    }

    private void validateUniqueName(String name, PluggableClassType type) {
        if (!this.findOthersByName(name).isEmpty()) {
            throw new DuplicateNameException(MessageSeeds.ALREADY_EXISTS, this.thesaurus, name, type);
        }
    }

    private DataMapper<PluggableClass> getDataMapper() {
        return this.dataModel.mapper(PluggableClass.class);
    }

    @Override
    public void save () {
        this.modTime = this.clock.instant();
        this.removeNullPropertyValues();
        if (this.id > 0) {
            this.post();
        }
        else {
            this.postNew();
        }
    }

    private void removeNullPropertyValues() {
        Iterator<PluggableClassProperty> propertyIterator = this.properties.iterator();
        while (propertyIterator.hasNext()) {
            PluggableClassProperty property = propertyIterator.next();
            if (Checks.is(property.value).empty()) {
                propertyIterator.remove();
            }
        }
    }

    /**
     * Saves this object for the first time.
     */
    protected void postNew() {
        this.getDataMapper().persist(this);
        this.eventService.postEvent(EventType.CREATED.topic(), this);
    }

    /**
     * Updates the changes made to this object.
     */
    protected void post() {
        this.getDataMapper().update(this);
        this.eventService.postEvent(EventType.UPDATED.topic(), this);
    }

    public void delete() {
        this.properties.clear();
        this.notifyDependents();
        this.getDataMapper().remove(this);
    }

    private void notifyDependents() {
        this.eventService.postEvent(EventType.DELETED.topic(), this);
    }

    @Override
    public String toString () {
        return this.getName() + " (" + getJavaClassName() + ")";
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name)  {
        this.validateName(name);
        if (!name.equals(this.getName())) {
            this.validateUniqueName(name, this.pluggableType.toActualType());
        }
        this.name = name;
    }

    private List<PluggableClass> findOthersByName (String name) {
        return this.getDataMapper().find("name", name);
    }

    @Override
    public String getJavaClassName () {
        return this.javaClassName;
    }

    @Override
    public PluggableClassType getPluggableClassType() {
        return pluggableType.toActualType();
    }

    @Override
    public Instant getModificationDate() {
        return this.modTime;
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (PropertySpec propertySpec : propertySpecs) {
            PluggableClassProperty property = this.findProperty(propertySpec.getName());
            if (property != null) {
                typedProperties.setProperty(property.name, propertySpec.getValueFactory().fromStringValue(property.value));
            }
        }
        return typedProperties;
    }

    @Override
    public void setProperty(PropertySpec propertySpec, Object value) {
        PluggableClassProperty property = this.findProperty(propertySpec.getName());
        if (property == null) {
            PluggableClassProperty pluggableClassProperty = pluggableClassPropertyProvider.get();
            pluggableClassProperty.init(
                    this,
                    propertySpec.getName(),
                    propertySpec.getValueFactory().toStringValue(value));
            this.properties.add(pluggableClassProperty);
        } else {
            if (!propertySpec.getValueFactory().getValueType().isAssignableFrom(value.getClass())) {
                throw new FieldValidationException("Incorrect type", propertySpec.getName());
            }
            property.value = propertySpec.getValueFactory().toStringValue(value);
            property.save();
        }
    }

    @Override
    public void removeProperty(PropertySpec propertySpec) {
        Iterator<PluggableClassProperty> propertyIterator = this.properties.iterator();
        while (propertyIterator.hasNext()) {
            PluggableClassProperty property = propertyIterator.next();
            if (property.name.equals(propertySpec.getName())) {
                propertyIterator.remove();
            }
        }
    }

    private PluggableClassProperty findProperty(String name) {
        for (PluggableClassProperty property : this.properties) {
            if (name.equals(property.name)) {
                return property;
            }
        }
        return null;
    }

}