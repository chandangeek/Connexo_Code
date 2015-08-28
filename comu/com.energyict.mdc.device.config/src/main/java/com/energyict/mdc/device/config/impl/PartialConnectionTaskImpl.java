package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
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
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link com.energyict.mdc.device.config.PartialConnectionTask} interface.
 *
 * @author sva
 * @since 21/01/13 - 16:44
 */
public abstract class PartialConnectionTaskImpl extends PersistentNamedObject<PartialConnectionTask> implements ServerPartialConnectionTask {

    public static final Map<String, Class<? extends PartialConnectionTask>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends PartialConnectionTask>>of("0", PartialConnectionInitiationTaskImpl.class, "1", PartialInboundConnectionTaskImpl.class, "2", PartialScheduledConnectionTaskImpl.class);

    enum Fields {
        CONNECTION_TYPE_PLUGGABLE_CLASS("pluggableClass");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private final ProtocolPluggableService protocolPluggableService;

    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
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
    protected void validateDelete () {
        this.getEventService().postEvent(this.validateDeleteEventType().topic(), this);
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
    public DeviceConfiguration getConfiguration () {
        return this.configuration.get();
    }

    @Override
    public String toString () {
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
    public List<PartialConnectionTaskProperty> getProperties () {
        return Collections.<PartialConnectionTaskProperty>unmodifiableList(properties);
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
            }
            else {
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
                    }
                    else {
                        this.addedOrRemovedRequiredProperties.add(key);
                    }
                }
                iterator.remove();
                return;
            }
        }
    }

    @Override
    public TypedProperties getTypedProperties () {
        TypedProperties typedProperties = TypedProperties.inheritingFrom(this.getPluggableClass().getProperties(getPropertySpecs()));
        this.getProperties()
                .stream()
                .filter(p -> p.getValue() != null)
                .forEach(p -> typedProperties.setProperty(p.getName(), p.getValue()));
        return typedProperties.getUnmodifiableView();
    }

    private List<PropertySpec> getPropertySpecs() {
        return this.getPluggableClass().getConnectionType().getPropertySpecs();
    }

    @Override
    public ConnectionType getConnectionType () {
        return this.getPluggableClass().getConnectionType();
    }

    @Override
    public ConnectionTypePluggableClass getPluggableClass () {
        if (pluggableClass == null && pluggableClassId != 0) {
            pluggableClass = protocolPluggableService.findConnectionTypePluggableClass(pluggableClassId).get();
        }
        return pluggableClass;
    }

    @Override
    public ComPortPool getComPortPool () {
        return this.comPortPool.orNull();
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    // only to be used in the setDefault
    public void clearDefault() {
        this.isDefault = false;
        this.post();
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
            getPartialConnectionTasksImpls().forEach(PartialConnectionTaskImpl::clearDefault);
        }
        this.isDefault = asDefault;
    }

    private List<PartialConnectionTaskImpl> getPartialConnectionTasksImpls() {
        return getConfiguration()
                .getPartialConnectionTasks()
                .stream()
                .map(PartialConnectionTaskImpl.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public void save() {
        super.save();
        this.addedOrRemovedRequiredProperties.clear();
    }

    @Override
    protected Object toUpdateEventSource() {
        return new PartialConnectionTaskUpdateDetailsImpl(this, this.addedOrRemovedRequiredProperties);
    }

    public static class HasSpecValidator implements ConstraintValidator<PartialConnectionTaskPropertyMustHaveSpec, PartialConnectionTaskPropertyImpl> {

        @Override
        public void initialize(PartialConnectionTaskPropertyMustHaveSpec constraintAnnotation) {
        }

        @Override
        public boolean isValid(PartialConnectionTaskPropertyImpl value, ConstraintValidatorContext context) {
            ConnectionTypePluggableClass connectionTypePluggableClass = value.getPartialConnectionTask().getPluggableClass();
            return connectionTypePluggableClass.getPropertySpec(value.getName()) != null;
        }
    }

    public static class ValueValidator implements ConstraintValidator<PartialConnectionTaskPropertyValueHasCorrectType, PartialConnectionTaskPropertyImpl> {

        @Override
        public void initialize(PartialConnectionTaskPropertyValueHasCorrectType constraintAnnotation) {
        }

        @Override
        public boolean isValid(PartialConnectionTaskPropertyImpl value, ConstraintValidatorContext context) {
            ConnectionTypePluggableClass connectionTypePluggableClass = value.getPartialConnectionTask().getPluggableClass();
            PropertySpec propertySpec = connectionTypePluggableClass.getPropertySpec(value.getName());
            return propertySpec == null || value.getValue() == null || propertySpec.getValueFactory().getValueType().isInstance(value.getValue());
        }
    }

    protected boolean validateUniqueName() {
        String name = this.getName();
        DeviceConfiguration configuration = getConfiguration();
        for (PartialConnectionTask partialConnectionTask:configuration.getPartialConnectionTasks()) {
            if (partialConnectionTask.getName().equals(name) && partialConnectionTask.getId() != this.getId()) {
                return false;
            }
        }
        return true;
    }

}