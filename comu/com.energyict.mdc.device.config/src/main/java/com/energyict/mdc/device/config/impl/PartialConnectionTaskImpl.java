package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link com.energyict.mdc.device.config.PartialConnectionTask} interface.
 *
 * @author sva
 * @since 21/01/13 - 16:44
 */
public abstract class PartialConnectionTaskImpl extends PersistentNamedObject<PartialConnectionTask> implements PartialConnectionTask {

    public static final Map<String, Class<? extends PartialConnectionTask>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends PartialConnectionTask>>of("0", PartialConnectionInitiationTaskImpl.class, "1", PartialInboundConnectionTaskImpl.class, "2", PartialScheduledConnectionTaskImpl.class);

    private final EngineModelService engineModelService;
    private final ProtocolPluggableService protocolPluggableService;


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

    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    private String name;
    private Reference<DeviceCommunicationConfiguration> configuration = ValueReference.absent();
    @NotNull()
    private long pluggableClassId;
    private ConnectionTypePluggableClass pluggableClass;
    private Reference<ComPortPool> comPortPool = ValueReference.absent();
    private boolean isDefault;
    @Valid
    private List<PartialConnectionTaskPropertyImpl> properties = new ArrayList<>();
    private Date modDate;

    @Inject
    PartialConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(PartialConnectionTask.class, dataModel, eventService, thesaurus);
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
    }

    void setConfiguration(DeviceCommunicationConfiguration configuration) {
        this.configuration.set(configuration);
    }

    protected abstract Class<? extends ComPortPool> expectedComPortPoolType ();

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
        return this.configuration.get().getDeviceConfiguration();
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
                    Save.UPDATE.validate(dataModel, property);
                    property.save();
                }
                return;
            }
        }
        PartialConnectionTaskPropertyImpl property = PartialConnectionTaskPropertyImpl.from(dataModel, this, key, value);
        Save.CREATE.validate(dataModel, property);
        properties.add(property);
    }

    @Override
    public void removeProperty(String key) {
        for (Iterator<PartialConnectionTaskPropertyImpl> iterator = properties.iterator(); iterator.hasNext(); ) {
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
        if (pluggableClass == null && pluggableClassId != 0) {
            pluggableClass = protocolPluggableService.findConnectionTypePluggableClass(pluggableClassId);
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

    String getInvalidCharacters() {
        return "./";
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
        if(asDefault){
            for (PartialConnectionTaskImpl partialConnectionTask : getPartialConnectionTasksImpls()) {
                partialConnectionTask.clearDefault();
            }
        }
        this.isDefault = asDefault;
    }

    private List<PartialConnectionTaskImpl> getPartialConnectionTasksImpls() {
        List<PartialConnectionTaskImpl> connectionTaskImpls = new ArrayList<>();
        for (PartialConnectionTask partialConnectionTask : getConfiguration().getPartialConnectionTasks()) {
            connectionTaskImpls.add((PartialConnectionTaskImpl) partialConnectionTask);
        }
        return connectionTaskImpls;
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
            if (propertySpec != null) {
                return value.getValue() == null || propertySpec.getValueFactory().getValueType().isInstance(value.getValue());
            }
            return true; // missing spec is covered by different validation
        }
    }

    protected boolean validateUniqueName() {
        String name = this.getName();
        DeviceConfiguration configuration = getConfiguration();
        for(PartialConnectionTask partialConnectionTask:configuration.getPartialConnectionTasks()){
            if(partialConnectionTask.getName().equals(name) && partialConnectionTask.getId()!=this.getId()){
                return false;
            }
        }
        return true;
    }
}
