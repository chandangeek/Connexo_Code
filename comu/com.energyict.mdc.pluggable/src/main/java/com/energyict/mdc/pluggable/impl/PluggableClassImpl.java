package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DuplicateException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link PluggableClass} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013, Dec 2 (09:07)
 */
public class PluggableClassImpl implements PluggableClass {

    private long id;
    @NotNull
    private String name;
    @NotNull
    private String javaClassName;
    @NotNull
    private PluggableClassType pluggableType;
    private List<PluggableClassProperty> properties = new ArrayList<>();
    private Date modificationDate;

    // Services that will be injected by the DataModel
    @Inject
    private DataModel dataModel;
    @Inject
    private EventService eventService;
    @Inject
    private Clock clock;

    PluggableClassImpl initialize (PluggableClassType type, String name, String javaClassName) throws BusinessException {
        this.validateName(name);
        this.validateUniqueName(name, type);
        this.validateJavaClassName(javaClassName);
        this.name = name;
        this.javaClassName = javaClassName;
        this.pluggableType = type;
        return this;
    }

    static PluggableClassImpl from (DataModel dataModel, PluggableClassType type, String name, String javaClassName) throws BusinessException {
        return dataModel.getInstance(PluggableClassImpl.class).initialize(type, name, javaClassName);
    }

    private void validateJavaClassName (String javaClassName) throws BusinessException {
        if (javaClassName == null || (javaClassName.trim().isEmpty())) {
            throw new BusinessException("javaClassNameCantBeEmpty", "The java class name cannot be empty");
        }
    }

    private void validateName(String newName) throws BusinessException {
        if (newName == null) {
            throw new BusinessException("nameCantBeEmpty", "Name cannot be empty");
        }
        if (newName.trim().isEmpty()) {
            throw new BusinessException("nameCantBeBlank", "Name cannot be blank");
        }
    }

    private void validateUniqueName(String name, PluggableClassType type) throws DuplicateException {
        if (!this.findOthersByName(name).isEmpty()) {
            throw new DuplicateException(
                    "duplicatePluggableClassX",
                    "A pluggable class with the name \"{0}\" already exists", name);
        }
    }

    private DataMapper<PluggableClass> getDataMapper() {
        // Todo: hold off until ORM injecting mechanism is finalized
        return null;
    }

    @Override
    public void save () {
        this.modificationDate = this.clock.now();
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
    }

    /**
     * Updates the changes made to this object.
     */
    protected void post() {
        this.getDataMapper().update(this);
    }

    public void delete() throws BusinessException, SQLException {
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
    public void setName(String name) throws BusinessException {
        this.validateName(name);
        if (!name.equals(this.getName())) {
            this.validateUniqueName(name, this.pluggableType);
        }
        this.name = name;
    }

    private List<PluggableClass> findOthersByName (String name) {
        return this.getDataMapper().find("name", name, "pluggableType", this.getPluggableClassType());
    }

    @Override
    public String getJavaClassName () {
        return this.javaClassName;
    }

    @Override
    public PluggableClassType getPluggableClassType() {
        return pluggableType;
    }

    @Override
    public Date getModificationDate() {
        return modificationDate;
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (PropertySpec propertySpec : propertySpecs) {
            PluggableClassProperty property = this.findProperty(propertySpec.getName());
            if (property != null) {
                typedProperties.setProperty(property.name, property.value);
            }
        }
        return typedProperties;
    }

    @Override
    public void setProperty(PropertySpec propertySpec, Object value) {
        PluggableClassProperty property = this.findProperty(propertySpec.getName());
        if (property == null) {
            this.properties.add(
                    new PluggableClassProperty(
                            this,
                            propertySpec.getName(),
                            propertySpec.getValueFactory().toStringValue(value)));
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