package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link PartialConnectionTask} interface.
 *
 * @author sva
 * @since 21/01/13 - 16:44
 */
public abstract class PartialConnectionTaskImpl<T extends ComPortPool> extends PersistentNamedObject<PartialConnectionTask> implements ServerPartialConnectionTask<T> {

    private final EngineModelService engineModelService;
    private final ProtocolPluggableService protocolPluggableService;

    private Reference<DeviceCommunicationConfiguration> configuration = ValueReference.absent();
    private Reference<ConnectionTypePluggableClass> pluggableClass = ValueReference.absent();
    private Reference<ComPortPool> comPortPool = ValueReference.absent();
    private boolean isDefault;
    private List<PartialConnectionTaskProperty> properties = new ArrayList<>();

    @Inject
    PartialConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(PartialConnectionTask.class, dataModel, eventService, thesaurus);
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
    }

//    private void bindReference (PreparedStatement preparedStatement, int parameterNumber, int referenceId) throws SQLException {
//        if (referenceId == 0) {
//            preparedStatement.setNull(parameterNumber, Types.INTEGER);
//        }
//        else {
//            preparedStatement.setInt(parameterNumber, referenceId);
//        }
//    }

//    protected DeviceCommunicationConfiguration validate (PartialConnectionTaskShadow shadow) throws BusinessException {
//        DeviceCommunicationConfiguration configuration = this.validateConfiguration(shadow);
//        this.validateConnectionTypePluggableClass(shadow.getConnectionTypePluggableClassId());
//        String name = shadow.getName();
//        this.validate(name);
//        if (!name.equals(this.getName())) {
//            this.validateConstraint(name, configuration);
//        }
//        this.validateComPortPool(shadow.getComPortPoolId());
//        this.validateProperties(shadow);
//        return configuration;
//    }
//
//    protected ConnectionTypePluggableClass validateConnectionTypePluggableClass (long connectionTypePluggableClassId) throws BusinessException {
//        ConnectionTypePluggableClass connectionTypePluggableClass = this.findConnectionTypePluggableClass(connectionTypePluggableClassId);
//        if (connectionTypePluggableClass == null) {
//            throw InvalidReferenceException.newForIdBusinessObject((int) connectionTypePluggableClassId, ConnectionTypePluggableClass.class.getName());
//        }
//        return connectionTypePluggableClass;
//    }
//
//    private void validateConstraint (String name, DeviceCommunicationConfiguration configuration) throws DuplicateException {
//        PartialConnectionTaskFactory factory = this.factoryInstance();
//        PartialConnectionTask connectionTaskWithTheSameName = factory.find(name, configuration);
//        if (connectionTaskWithTheSameName != null) {
//            if (this.getId() != connectionTaskWithTheSameName.getId()) {
//                throw new DuplicateException(
//                    "duplicatePartialConnectionTaskX",
//                    "A partial connection task with the name '{0}' already exists on configuration '{1}' (id={2,number})",
//                    name, configuration.getDeviceConfiguration().getName(), connectionTaskWithTheSameName.getId());
//            }
//        }
//    }
//
//    private void validateComPortPool (int comPortPoolId) throws InvalidReferenceException {
//        if (comPortPoolId != 0) {
//            ComPortPool comPortPool = this.findComPortPool(comPortPoolId);
//            if (comPortPool == null) {
//                throw InvalidReferenceException.newForIdBusinessObject(comPortPoolId, ComPortPool.class.getName());
//            }
//            if (!this.validateComPortPoolType(comPortPool)) {
//                throw InvalidReferenceException.newForIdBusinessObjectSubClass(comPortPoolId,  ComPortPool.class.getName(), this.expectedComPortPoolType());
//            }
//        }
//    }
//
//    protected abstract boolean validateComPortPoolType (ComPortPool comPortPool) throws InvalidReferenceException;

    protected abstract Class<T> expectedComPortPoolType ();

//    private void validateProperties (PartialConnectionTaskShadow shadow) throws BusinessException {
//        this.validatePropertiesAreLinkedToPropertySpecs(shadow);
//        this.validatePropertyValues(shadow);
//    }

//    private void validatePropertiesAreLinkedToPropertySpecs (PartialConnectionTaskShadow shadow) throws BusinessException {
//        ConnectionTypePluggableClass connectionTypePluggableClass = this.findConnectionTypePluggableClass(shadow.getConnectionTypePluggableClassId());
//        for (String propertyName : shadow.getTypedProperties().localPropertyNames()) {
//            if (connectionTypePluggableClass.getPropertySpec(propertyName) == null) {
//                throw new BusinessException(
//                        "connectionTaskPropertyXIsNotInConnectionTypeSpec",
//                        "ConnectionType '{0}' does not contain a specification for attribute '{1}'",
//                        connectionTypePluggableClass.getName(),
//                        propertyName);
//            }
//        }
//    }

//    private void validatePropertyValues (PartialConnectionTaskShadow shadow) throws BusinessException {
//        ConnectionTypePluggableClass connectionTypePluggableClass = this.findConnectionTypePluggableClass(shadow.getConnectionTypePluggableClassId());
//        for (String propertyName : shadow.getTypedProperties().localPropertyNames()) {
//            this.validatePropertyValue(connectionTypePluggableClass, propertyName, shadow.getTypedProperties().getProperty(propertyName));
//        }
//    }

//    private void validatePropertyValue (ConnectionTypePluggableClass connectionTypePluggableClass, String propertyName, Object propertyValue)
//        throws InvalidValueException {
//        ConnectionType connectionType = connectionTypePluggableClass.getConnectionType();
//        PropertySpec propertySpec = connectionType.getPropertySpec(propertyName);
//        propertySpec.validateValue(propertyValue);
//    }
//
//    protected DeviceCommunicationConfiguration validateConfiguration (PartialConnectionTaskShadow shadow) throws InvalidReferenceException, InvalidValueException {
//        int configurationId = shadow.getConfigurationId();
//        DeviceCommunicationConfiguration configuration = this.findConfiguration(configurationId);
//        if (configuration == null) {
//            if (configurationId != 0) {
//                throw InvalidReferenceException.newForIdBusinessObject(configurationId, this.getDeviceCommunicationConfigurationFactory());
//            }
//            else {
//                throw new InvalidValueException("XcannotBeEmpty", "\"{0}\" is a required property", "partialConnectionTask.configuration");
//            }
//        }
//        return configuration;
//    }

//    protected void copy (PartialConnectionTaskShadow shadow) {
//        this.setName(shadow.getName());
//        this.configurationId = shadow.getConfigurationId();
//        this.configuration = null;
//        this.pluggableClassId = shadow.getConnectionTypePluggableClassId();
//        this.pluggableClass = null;
//        this.comportPoolId = shadow.getComPortPoolId();
//        this.comPortPool = null;
//        this.isDefault = shadow.isDefault();
//    }

    @Override
    protected void validateDelete () {
        // TODO ConnectionTask bundle should listen for delete events on this and veto if any clients exist
    }

    @Override
    public DeviceCommunicationConfiguration getConfiguration () {
        return this.configuration.get();
    }

    @Override
    public String toString () {
        return "PartialConnectionTask (" + this.getId() + ")";
    }

    @Override
    public PartialConnectionTaskProperty getProperty (String name) {
        for (PartialConnectionTaskProperty property : this.getProperties()) {
            if (name.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    @Override
    public List<PartialConnectionTaskProperty> getProperties () {
        return Collections.unmodifiableList(properties);
    }

    public void setProperty(String key, Object value) {
        for (PartialConnectionTaskProperty property : properties) {
            if (property.getName().equals(key)) {
                property.setValue(value);
                return;
            }
        }
        properties.add(PartialConnectionTaskPropertyImpl.from(this, key, value));
    }

    @Override
    public void removeProperty(String key) {
        for (Iterator<PartialConnectionTaskProperty> iterator = properties.iterator(); iterator.hasNext(); ) {
            if (iterator.next().getName().equals(key)) {
                iterator.remove();
                return;
            }
        }
    }

    @Override
    public TypedProperties getTypedProperties () {
        TypedProperties typedProperties = TypedProperties.inheritingFrom(this.getPluggableClass().getProperties(getPropertySpecs()));
        for (PartialConnectionTaskProperty property : this.getProperties()) {
            if (property.getValue() != null) {
                typedProperties.setProperty(property.getName(), property.getValue());
            }
        }
        return typedProperties.getUnmodifiableView();
    }

    private List<PropertySpec> getPropertySpecs() {
        return this.getPluggableClass().getConnectionType().getPropertySpecs();
    }

//    protected void postProperties (PartialConnectionTaskShadow shadow) throws BusinessException, SQLException {
//        this.getPropertyFactory().deleteAllFor(this);
//        this.getPropertyFactory().createProperties(this, shadow.getTypedProperties());
//    }

    protected void validateNotNull (Object propertyValue, String propertyName) throws InvalidValueException {
        if (propertyValue == null) {
            throw new InvalidValueException("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
        }
    }

    @Override
    public ConnectionType getConnectionType () {
        return this.getPluggableClass().getConnectionType();
    }

    @Override
    public ConnectionTypePluggableClass getPluggableClass () {
        return pluggableClass.get();
    }

    @Override
    public T getComPortPool () {
        return (T) this.comPortPool.get();
    }

    protected ComPortPool findComPortPool (int id) {
        return engineModelService.findComPortPool(id);
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    protected String getInvalidCharacters() {
        return "./";
    }

    @Override
    public void setComportPool(T comPortPool) {
        this.comPortPool.set(comPortPool);
    }

    @Override
    public void setConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass) {
        this.pluggableClass.set(connectionTypePluggableClass);
    }

    @Override
    public void setDefault(boolean asDefault) {
        this.isDefault = asDefault;
    }
}
