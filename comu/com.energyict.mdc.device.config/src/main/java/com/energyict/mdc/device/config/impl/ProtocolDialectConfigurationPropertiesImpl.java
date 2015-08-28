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
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.exceptions.CannotDeleteProtocolDialectConfigurationPropertiesWhileInUseException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.NoSuchPropertyOnDialectException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ProtocolDialectConfigurationPropertiesImpl extends PersistentNamedObject<ProtocolDialectConfigurationProperties> implements ProtocolDialectConfigurationProperties {

    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    private final DataModel dataModel;

    private DeviceProtocolDialect protocolDialect;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    private String name;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOLDIALECT_REQUIRED + "}")
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String protocolDialectName;
    @Valid
    private List<ProtocolDialectConfigurationPropertyImpl> propertyList = new ArrayList<>();
    @SuppressWarnings("unused") //Used by the orm
    private String userName;
    @SuppressWarnings("unused") //Used by the orm
    private long version;
    @SuppressWarnings("unused") //Used by the orm
    private Instant createTime;
    @SuppressWarnings("unused") //Used by the orm
    private Instant modTime;

    // transient
    private transient TypedProperties typedProperties;

    @Inject
    ProtocolDialectConfigurationPropertiesImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ProtocolDialectConfigurationProperties.class, dataModel, eventService, thesaurus);
        this.dataModel = dataModel;
    }

    @Override
    protected void validateDelete() {
        this.getEventService().postEvent(EventType.PROTOCOLCONFIGURATIONPROPS_VALIDATEDELETE.topic(), this);
        List<ComTaskEnablement> comTaskEnablements = this.dataModel.mapper(ComTaskEnablement.class).find(ComTaskEnablementImpl.Fields.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES.fieldName(), this);
        if (!comTaskEnablements.isEmpty()) {
            throw new CannotDeleteProtocolDialectConfigurationPropertiesWhileInUseException(this.getThesaurus(), this);
        }
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
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public DeviceProtocolDialect getDeviceProtocolDialect() {
        if (this.protocolDialect == null) {
            this.protocolDialect = getDeviceProtocolDialect(this.protocolDialectName);
        }
        return this.protocolDialect;
    }

    protected DeviceProtocolDialect getDeviceProtocolDialect(String protocolDialectClassName) {
        for (DeviceProtocolDialect deviceProtocolDialect : getAllDeviceProtocolDialectsSupportedByTheDeviceType()) {
            if (deviceProtocolDialect.getDeviceProtocolDialectName().equals(protocolDialectClassName)) {
                return deviceProtocolDialect;
            }
        }
        return null;
    }

    private List<DeviceProtocolDialect> getAllDeviceProtocolDialectsSupportedByTheDeviceType() {
        DeviceType deviceType = this.getDeviceConfiguration().getDeviceType();
        DeviceProtocol deviceProtocol = deviceType.getDeviceProtocolPluggableClass().getDeviceProtocol();
        return deviceProtocol.getDeviceProtocolDialects();
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return this.protocolDialectName;
    }

    @Override
    public TypedProperties getTypedProperties() {
        if (typedProperties == null) {
            typedProperties = initializeTypedProperties();
        }
        return typedProperties.getUnmodifiableView();
    }

    private TypedProperties initializeTypedProperties() {
        TypedProperties properties = TypedProperties.empty();
        for (ProtocolDialectConfigurationPropertyImpl property : propertyList) {
            ValueFactory<?> valueFactory = getPropertySpec(property.getName()).getValueFactory();
            properties.setProperty(property.getName(), valueFactory.fromStringValue(property.getValue()));
        }
        return properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        if (this.getDeviceProtocolDialect() == null) {
            return new ArrayList<>(0);
        } else {
            return this.getDeviceProtocolDialect().getPropertySpecs();
        }
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (this.getDeviceProtocolDialect() == null) {
            return null;
        } else {
            return this.getDeviceProtocolDialect().getPropertySpec(name);
        }
    }

    @Override
    public final boolean isComplete() {
        return getPropertySpecs().stream().filter(PropertySpec :: isRequired).noneMatch(x->this.getProperty(x.getName()) == null);
    }

    @Override
    protected final CreateEventType createEventType() {
        return CreateEventType.PROTOCOLCONFIGPROPS;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PROTOCOLCONFIGPROPS;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PROTOCOLCONFIGPROPS;
    }

    @Override
    protected final void doDelete() {
        dataModel.mapper(ProtocolDialectConfigurationProperties.class).remove(this);
    }

    static ProtocolDialectConfigurationPropertiesImpl from(DataModel dataModel, DeviceConfiguration configuration, DeviceProtocolDialect protocolDialect) {
        return dataModel.getInstance(ProtocolDialectConfigurationPropertiesImpl.class).init(configuration, protocolDialect);
    }

    ProtocolDialectConfigurationPropertiesImpl init(DeviceConfiguration configuration, DeviceProtocolDialect protocolDialect) {
        this.setName(protocolDialect.getDeviceProtocolDialectName());
        this.deviceConfiguration.set(configuration);
        this.protocolDialect = protocolDialect;
        this.protocolDialectName = protocolDialect.getDeviceProtocolDialectName();
        return this;
    }

    @Override
    public void setProperty(String name, Object value) {
        if (value != null) {
            if (getTypedProperties().hasValueFor(name)) {
                this.updateProperty(name, value);
            }
            else {
                this.setNewProperty(name, value);
            }
        } else {
            removeProperty(name);
        }
    }

    public void updateProperty(String name, Object value) {
        this.findProperty(name).ifPresent(this::doRemoveProperty);
        this.setNewProperty(name, value);
    }

    @Override
    public void removeProperty(String name) {
        findProperty(name).ifPresent(this::validateAndRemoveProperty);
    }

    private void validateAndRemoveProperty(ProtocolDialectConfigurationPropertyImpl obsolete) {
        obsolete.validateDelete();
        doRemoveProperty(obsolete);
    }

    private void doRemoveProperty(ProtocolDialectConfigurationPropertyImpl obsolete) {
        this.propertyList.remove(obsolete);
        this.getLocalAdjustableTypedProperties().removeProperty(obsolete.getName());
    }

    private TypedProperties getLocalAdjustableTypedProperties(){
        if (this.typedProperties == null) {
            this.typedProperties = initializeTypedProperties();
        }
        return this.typedProperties;
    }

    private Optional<ProtocolDialectConfigurationPropertyImpl> findProperty(String name) {
        for (ProtocolDialectConfigurationPropertyImpl candidate : propertyList) {
            if (candidate.getName().equals(name)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private void setNewProperty(String name, Object value) {
        ProtocolDialectConfigurationPropertyImpl property = ProtocolDialectConfigurationPropertyImpl.forKey(this, name).setValue(asStringValue(name, value));
        Save.CREATE.validate(this.dataModel, property);
        this.propertyList.add(property);
        this.getLocalAdjustableTypedProperties().setProperty(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return getTypedProperties().getProperty(name);
    }

    @SuppressWarnings("unchecked")
    private String asStringValue(String name, Object value) {
        PropertySpec propertySpec = getPropertySpec(name);
        if (propertySpec == null) {
            throw new NoSuchPropertyOnDialectException(this.getThesaurus(), getDeviceProtocolDialect(), name);
        }
        return propertySpec.getValueFactory().toStringValue(value);
    }

    @Override
    protected boolean validateUniqueName() {
        return true;
    }

}