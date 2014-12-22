package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.exceptions.CannotDeleteProtocolDialectConfigurationPropertiesWhileInUseException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.NoSuchPropertyOnDialectException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 5/03/13 - 16:12
 */
//@ProtocolDialectConfigurationHasAllRequiredProperties(groups = {Save.Create.class, Save.Update.class})
class ProtocolDialectConfigurationPropertiesImpl extends PersistentNamedObject<ProtocolDialectConfigurationProperties> implements ProtocolDialectConfigurationProperties {

    private Reference<DeviceCommunicationConfiguration> deviceCommunicationConfiguration = ValueReference.absent();

    private final DataModel dataModel;

    private DeviceProtocolDialect protocolDialect;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    private String name;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOLDIALECT_REQUIRED + "}")
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String protocolDialectName;
    private List<ProtocolDialectConfigurationProperty> propertyList = new ArrayList<>();

    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    // transient
    private transient TypedProperties typedProperties;

    @Inject
    ProtocolDialectConfigurationPropertiesImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ProtocolDialectConfigurationProperties.class, dataModel, eventService, thesaurus);
        this.dataModel = dataModel;
    }

    static class PropertyValueValidator implements ConstraintValidator<ProtocolDialectConfigurationHasCorrectPropertyValues, ProtocolDialectConfigurationPropertiesImpl> {

        @Override
        public void initialize(ProtocolDialectConfigurationHasCorrectPropertyValues constraintAnnotation) {
            //nothing for now
        }

        @Override
        public boolean isValid(ProtocolDialectConfigurationPropertiesImpl value, ConstraintValidatorContext context) {
            //TODO values atm are validated upon setting...
            return true;
        }
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
    public DeviceConfiguration getDeviceCommunicationConfiguration() {
        return this.deviceCommunicationConfiguration.get().getDeviceConfiguration();
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.getDeviceCommunicationConfiguration().getDeviceConfiguration();
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

    private void setProtocolDialectName(String protocolDialectName) {
        this.protocolDialectName = protocolDialectName;
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
        for (ProtocolDialectConfigurationProperty property : propertyList) {
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

    static ProtocolDialectConfigurationProperties from(DataModel dataModel, DeviceCommunicationConfiguration configuration, DeviceProtocolDialect protocolDialect) {
        return dataModel.getInstance(ProtocolDialectConfigurationPropertiesImpl.class).init(configuration, protocolDialect);
    }

    ProtocolDialectConfigurationPropertiesImpl init(DeviceCommunicationConfiguration configuration, DeviceProtocolDialect protocolDialect) {
        this.setName(protocolDialect.getDeviceProtocolDialectName());
        this.deviceCommunicationConfiguration.set(configuration);
        this.protocolDialect = protocolDialect;
        this.protocolDialectName = protocolDialect.getDeviceProtocolDialectName();
        return this;
    }

    @Override
    public void setProperty(String name, Object value) {
        if (value != null) {
            if (getTypedProperties().hasValueFor(name)) {
                removeProperty(name);
            }
            setNewProperty(name, value);
        } else {
            removeProperty(name);
        }
    }

    @Override
    public void removeProperty(String name) {
        ProtocolDialectConfigurationProperty found = findProperty(name);
        propertyList.remove(found);
        getLocalAdjustableTypedProperties().removeProperty(name);
    }

    private TypedProperties getLocalAdjustableTypedProperties(){
        if(this.typedProperties == null){
            this.typedProperties = initializeTypedProperties();
        }
        return this.typedProperties;
    }

    private ProtocolDialectConfigurationProperty findProperty(String name) {
        for (ProtocolDialectConfigurationProperty candidate : propertyList) {
            if (candidate.getName().equals(name)) {
                return candidate;
            }
        }
        return null;
    }

    private void setNewProperty(String name, Object value) {
        ProtocolDialectConfigurationProperty property = ProtocolDialectConfigurationProperty.forKey(this, name).setValue(asStringValue(name, value));
        propertyList.add(property);
        getLocalAdjustableTypedProperties().setProperty(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return getTypedProperties().getProperty(name);
    }

    private String asStringValue(String name, Object value) {
        PropertySpec propertySpec = getPropertySpec(name);
        if (propertySpec == null) {
            throw new NoSuchPropertyOnDialectException(thesaurus, getDeviceProtocolDialect(), name);
        }
        return propertySpec.getValueFactory().toStringValue(value);
    }

    @Override
    protected boolean validateUniqueName() {
        return true;
    }
}
