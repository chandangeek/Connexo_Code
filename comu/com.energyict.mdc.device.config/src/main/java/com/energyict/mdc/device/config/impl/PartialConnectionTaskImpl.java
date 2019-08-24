/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.PartialConnectionTaskBuilder;
import com.energyict.mdc.common.device.config.PartialConnectionTaskProperty;
import com.energyict.mdc.common.device.config.ServerPartialConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.config.AbstractConnectionTypeDelegate;
import com.energyict.mdc.device.config.AbstractConnectionTypePluggableClassDelegate;
import com.energyict.mdc.device.config.KeyAccessorPropertySpecWithPossibleValues;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.UnmodifiableTypedProperties;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link PartialConnectionTask} interface.
 *
 * @author sva
 * @since 21/01/13 - 16:44
 */
@ProtocolDialectConfigurationPropertiesMustBeFromSameConfiguration(groups = {Save.Create.class, Save.Update.class})
@ProvidedConnectionFunctionMustBeSupportedByTheDeviceProtocol(groups = {Save.Create.class, Save.Update.class})
@ProvidedConnectionFunctionMustBeUniqueForDeviceConfiguration(groups = {Save.Create.class, Save.Update.class})
abstract class PartialConnectionTaskImpl extends PersistentNamedObject<PartialConnectionTask> implements ServerPartialConnectionTask {

    public static final Map<String, Class<? extends PartialConnectionTask>> IMPLEMENTERS = ImmutableMap.of("0", PartialConnectionInitiationTaskImpl.class, "1", PartialInboundConnectionTaskImpl.class, "2", PartialScheduledConnectionTaskImpl.class);

    enum Fields {
        CONNECTION_TYPE_PLUGGABLE_CLASS("pluggableClass"),
        PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES("protocolDialectConfigurationProperties"),
        CONNECTION_FUNCTION("connectionFunction");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private final ProtocolPluggableService protocolPluggableService;

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    private Reference<DeviceConfiguration> configuration = ValueReference.absent();
    @NotNull()
    private long pluggableClassId;
    private ConnectionTypePluggableClass pluggableClass;
    private Reference<ComPortPool> comPortPool = ValueReference.absent();
    private boolean isDefault;
    @Valid
    private List<PartialConnectionTaskPropertyImpl> properties = new ArrayList<>();
    /**
     * Holds the name of all required properties that were added or removed during
     * an edit session to be published as part of the update event.
     */
    private List<String> addedOrRemovedRequiredProperties = new ArrayList<>();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> protocolDialectConfigurationProperties = ValueReference.absent();
    private long connectionFunctionDbValue;
    private Optional<ConnectionFunction> connectionFunction = Optional.empty();
    /**
     * Holds the previous connection function in case the connection function was edited
     * during an edit session, so it can be published as part of the update event.
     */
    private Optional<ConnectionFunction> previousConnectionFunction = Optional.empty();

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    PartialConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ProtocolPluggableService protocolPluggableService) {
        super(PartialConnectionTask.class, dataModel, eventService, thesaurus);
        this.protocolPluggableService = protocolPluggableService;
    }

    void setConfiguration(DeviceConfiguration configuration) {
        this.configuration.set(configuration);
    }

    protected abstract ValidateDeleteEventType validateDeleteEventType();

    @Override
    public void validateDelete() {
        if (getConfiguration().getComTaskEnablements()
                .stream()
                .filter(comTaskEnablement -> comTaskEnablement.hasPartialConnectionTask())
                .map(comTaskEnablement -> comTaskEnablement.getPartialConnectionTask().get())
                .filter(partialConnectionTask -> partialConnectionTask.getId() == getId())
                .findAny()
                .isPresent()) {
            throw CannotDeleteBecauseStillInUseException.connectionTaskIsInUse(this.getThesaurus(), this, MessageSeeds.CONNECTION_TASK_USED_BY_COMTASK_ENABLEMENT);
        }
        this.getEventService().postEvent(this.validateDeleteEventType().topic(), this);
    }

    @Override
    public void prepareDelete() {
        this.properties.clear();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void doSetName(String name) {
        this.name = name;
    }

    @Override
    public DeviceConfiguration getConfiguration() {
        return this.configuration.get();
    }

    @Override
    public String toString() {
        return "PartialConnectionTask (" + this.getId() + ")";
    }

    @Override
    public PartialConnectionTaskProperty getProperty(String name) {
        for (PartialConnectionTaskProperty property : this.getProperties()) {
            if (name.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    @Override
    public List<PartialConnectionTaskProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PartialConnectionTaskImpl partialConnectionTask = (PartialConnectionTaskImpl) o;
        return id == partialConnectionTask.id;
    }

    public void setProperty(String key, Object value) {
        for (PartialConnectionTaskPropertyImpl property : properties) {
            if (property.getName().equals(key)) {
                property.setValue(value);
                if (this.getId() != 0) {
                    Save.UPDATE.validate(this.getDataModel(), property);
                    property.save();
                }
                return;
            }
        }
        PartialConnectionTaskPropertyImpl property = PartialConnectionTaskPropertyImpl.from(this.getDataModel(), this, key, value);
        Save.CREATE.validate(this.getDataModel(), property);
        properties.add(property);
        if (property.isRequired()) {
            if (this.addedOrRemovedRequiredProperties.contains(key)) {
                // The property was removed in the same edit session
                this.addedOrRemovedRequiredProperties.remove(key);
            } else {
                this.addedOrRemovedRequiredProperties.add(key);
            }
        }
    }

    @Override
    public void removeProperty(String key) {
        for (Iterator<PartialConnectionTaskPropertyImpl> iterator = properties.iterator(); iterator.hasNext(); ) {
            PartialConnectionTaskPropertyImpl property = iterator.next();
            if (property.getName().equals(key)) {
                if (property.isRequired()) {
                    if (this.addedOrRemovedRequiredProperties.contains(key)) {
                        // The property was added in the same edit session
                        this.addedOrRemovedRequiredProperties.remove(key);
                    } else {
                        this.addedOrRemovedRequiredProperties.add(key);
                    }
                }
                iterator.remove();
                return;
            }
        }
    }

    @Override
    public TypedProperties getTypedProperties() {
        TypedProperties typedProperties = TypedProperties.inheritingFrom(this.getPluggableClass().getProperties(getPropertySpecs()));
        this.getProperties()
                .stream()
                .filter(p -> p.getValue() != null)
                .forEach(p -> typedProperties.setProperty(p.getName(), p.getValue()));
        return new UnmodifiableTypedProperties(typedProperties);
    }

    private List<PropertySpec> getPropertySpecs() {
        return this.getPluggableClass().getConnectionType().getPropertySpecs();
    }

    @Override
    public ConnectionType getConnectionType() {
        return this.getPluggableClass().getConnectionType();
    }

    @Override
    public ConnectionTypePluggableClass getPluggableClass() {
        if (pluggableClass == null && pluggableClassId != 0) {
            pluggableClass = protocolPluggableService.findConnectionTypePluggableClass(pluggableClassId).get();
        }
        return new PluggableClassWithPossibleValues(pluggableClass);
    }

    @Override
    public ComPortPool getComPortPool() {
        return this.comPortPool.orNull();
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    public void clearDefault() {
        this.isDefault = false;
        this.post();
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        /* Since this is a required property,
         * we could actually use get() but we
         * want the javax.validation components
         * to validate that this property is not null
         * so we are using orElse(null) instead. */
        return this.protocolDialectConfigurationProperties.orElse(null);
    }

    @Override
    public void setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties) {
        this.protocolDialectConfigurationProperties.set(properties);
    }

    @Override
    public Optional<ConnectionFunction> getConnectionFunction() {
        if (!this.connectionFunction.isPresent() && this.connectionFunctionDbValue != 0) {
            Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = getConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
            List<ConnectionFunction> supportedConnectionFunctions = deviceProtocolPluggableClass.isPresent()
                    ? deviceProtocolPluggableClass.get().getProvidedConnectionFunctions()
                    : Collections.emptyList();
            this.connectionFunction = supportedConnectionFunctions.stream().filter(cf -> cf.getId() == this.connectionFunctionDbValue).findFirst();
        }
        return this.connectionFunction;
    }

    @Override
    public void setConnectionFunction(ConnectionFunction connectionFunction) {
        this.previousConnectionFunction = getConnectionFunction();
        this.connectionFunction = Optional.ofNullable(connectionFunction);
        this.connectionFunctionDbValue = connectionFunction != null ? connectionFunction.getId() : 0;
    }

    final void doSetComportPool(ComPortPool comPortPool) {
        this.comPortPool.set(comPortPool);
    }

    @Override
    public void setConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass) {
        this.pluggableClass = connectionTypePluggableClass;
        this.pluggableClassId = connectionTypePluggableClass == null ? 0 : connectionTypePluggableClass.getId();
    }

    public void setDefault(boolean asDefault) {
        if (asDefault) {
            ((DeviceConfigurationImpl) this.configuration.get()).clearDefaultExcept(this);
        }
        this.isDefault = asDefault;
    }

    @Override
    public void save() {
        super.save();
        this.addedOrRemovedRequiredProperties.clear();
        this.previousConnectionFunction = Optional.empty();
        getDataModel().touch(configuration.get());
    }

    @Override
    protected Object toUpdateEventSource() {
        return new PartialConnectionTaskUpdateDetailsImpl(this, this.addedOrRemovedRequiredProperties, this.previousConnectionFunction);
    }

    public PartialConnectionTaskBuilder cloneForDeviceConfig(PartialConnectionTaskBuilder builder) {
        builder.connectionFunction(getConnectionFunction().orElse(null));
        return builder;
    }

    static class HasSpecValidator implements ConstraintValidator<PartialConnectionTaskPropertyMustHaveSpec, PartialConnectionTaskPropertyImpl> {

        @Override
        public void initialize(PartialConnectionTaskPropertyMustHaveSpec constraintAnnotation) {
        }

        @Override
        public boolean isValid(PartialConnectionTaskPropertyImpl value, ConstraintValidatorContext context) {
            ConnectionTypePluggableClass connectionTypePluggableClass = value.getPartialConnectionTask().getPluggableClass();
            return connectionTypePluggableClass.getPropertySpec(value.getName()).isPresent();
        }
    }

    static class ValueValidator implements ConstraintValidator<PartialConnectionTaskPropertyValueHasCorrectType, PartialConnectionTaskPropertyImpl> {

        @Override
        public void initialize(PartialConnectionTaskPropertyValueHasCorrectType constraintAnnotation) {
        }

        @Override
        public boolean isValid(PartialConnectionTaskPropertyImpl value, ConstraintValidatorContext context) {
            ConnectionTypePluggableClass connectionTypePluggableClass = value.getPartialConnectionTask().getPluggableClass();
            Optional<PropertySpec> propertySpec = connectionTypePluggableClass.getPropertySpec(value.getName());
            return !propertySpec.isPresent() || value.getValue() == null || propertySpec.get().getValueFactory().getValueType().isInstance(value.getValue());
        }
    }

    protected boolean validateUniqueName() {
        String name = this.getName();
        DeviceConfiguration configuration = getConfiguration();
        for (PartialConnectionTask partialConnectionTask : configuration.getPartialConnectionTasks()) {
            if (partialConnectionTask.getName().equals(name) && partialConnectionTask.getId() != this.getId()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long getVersion() {
        return version;
    }

    /**
     * Modifies behaviour so a wrapper for the ConnectionType is returned, containing possible values for the
     * KeyAccessorType PropertySpecs
     */
    private class PluggableClassWithPossibleValues extends AbstractConnectionTypePluggableClassDelegate {

        public PluggableClassWithPossibleValues(ConnectionTypePluggableClass connectionTypePluggableClass) {
            super(connectionTypePluggableClass);
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return super.getPropertySpecs().stream().
                    map(ps -> KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> getConfiguration().getDeviceType().getSecurityAccessorTypes(), ps)).
                    collect(Collectors.toList());
        }

        @Override
        public Optional<PropertySpec> getPropertySpec(String name) {
            return super.getPropertySpec(name).
                    map(ps -> KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> getConfiguration().getDeviceType().getSecurityAccessorTypes(), ps));
        }

        @Override
        public ConnectionType getConnectionType() {
            return new ConnectionTypeWithPossibleValues(super.getConnectionType());
        }
    }

    /**
     * The wrapper is used to add additional PossibleValue to KeyAccessorType possible values
     */
    private class ConnectionTypeWithPossibleValues extends AbstractConnectionTypeDelegate {

        private ConnectionTypeWithPossibleValues(ConnectionType connectionType) {
            super(connectionType);
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return this.connectionType.getPropertySpecs().stream().
                    map(ps -> KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> getConfiguration().getDeviceType().getSecurityAccessorTypes(), ps)).
                    collect(Collectors.toList());
        }

        @Override
        public Optional<PropertySpec> getPropertySpec(String name) {
            return this.connectionType.getPropertySpec(name).
                    map(ps -> KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> getConfiguration().getDeviceType().getSecurityAccessorTypes(), ps));
        }
    }
}