package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.NoSuchPropertyOnDialectException;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.ValueFactory;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.google.inject.Inject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.elster.jupiter.util.conditions.Operator.EQUAL;

/**
 * @author sva
 * @since 5/03/13 - 16:12
 */
@ProtocolDialectConfigurationPropertiesCannotDuplicate(groups = {Save.Create.class})
//@ProtocolDialectConfigurationHasAllRequiredProperties(groups = {Save.Create.class, Save.Update.class})
class ProtocolDialectConfigurationPropertiesImpl extends PersistentNamedObject<ProtocolDialectConfigurationProperties> implements ProtocolDialectConfigurationProperties {

    private Reference<DeviceCommunicationConfiguration> deviceCommunicationConfiguration = ValueReference.absent();

    private final Clock clock;
    private final DataModel dataModel;

    private DeviceProtocolDialect protocolDialect;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.PROTOCOLDIALECT_REQUIRED_KEY + "}")
    private String protocolDialectName;
    private List<ProtocolDialectConfigurationProperty> propertyList = new ArrayList<>();

    private Date modDate;

    // transient
    private transient TypedProperties typedProperties;

    @Inject
    ProtocolDialectConfigurationPropertiesImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super(ProtocolDialectConfigurationProperties.class, dataModel, eventService, thesaurus);
        this.dataModel = dataModel;
        this.clock = clock;
    }

    @Override
    public void update() {
        save();
    }

    static class PrimaryKeyValidator implements ConstraintValidator<ProtocolDialectConfigurationPropertiesCannotDuplicate, ProtocolDialectConfigurationPropertiesImpl> {

        @Override
        public void initialize(ProtocolDialectConfigurationPropertiesCannotDuplicate constraintAnnotation) {
            // nothing for now
        }

        @Override
        public boolean isValid(ProtocolDialectConfigurationPropertiesImpl value, ConstraintValidatorContext context) {
            return value.dataModel.mapper(ProtocolDialectConfigurationProperties.class)
                    .select(
                            EQUAL.compare("deviceCommunicationConfiguration", value.getDeviceCommunicationConfiguration())
                                    .and(EQUAL.compare("protocolDialectName", value.protocolDialectName)))
                    .isEmpty();
        }


    }

    static class RequiredPropertiesValidator implements ConstraintValidator<ProtocolDialectConfigurationHasAllRequiredProperties, ProtocolDialectConfigurationPropertiesImpl> {

        @Override
        public void initialize(ProtocolDialectConfigurationHasAllRequiredProperties constraintAnnotation) {
            //nothing for now

        }

        @Override
        public boolean isValid(ProtocolDialectConfigurationPropertiesImpl value, ConstraintValidatorContext context) {
            TypedProperties typedProperties = value.getTypedProperties();
            for (PropertySpec propertySpec : value.getPropertySpecs()) {
                if (propertySpec.isRequired() && !typedProperties.hasValueFor(propertySpec.getName())) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    protected void validateDelete() {
        //TODO
//        ProtocolDialectPropertiesFactory factory = ManagerFactory.getCurrent().getProtocolDialectPropertiesFactory();
//        dataModel.mapper(PDProp)
//        if (factory.existsUsing(this)) {
//            throw new BusinessException(
//                    "protocolDialectConfigurationPropertiesXIsStillInUseByProtocolDialectProperties",
//                    "ProtocolDialectConfigurationProperties for {0} on configuration {1} is still being used by ProtocolDialectProperties on one or more devices",
//                    this.getDeviceProtocolDialect().getDeviceProtocolDialectName(),
//                    this.getDeviceCommunicationConfiguration().getDeviceConfiguration().getName());
//        }
    }

    @Override
    public DeviceCommunicationConfiguration getDeviceCommunicationConfiguration() {
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
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.protocolConfigurationPropertiesAlreadyExists(this.getThesaurus(), name);
    }

    @Override
    protected final void doDelete() {
        dataModel.mapper(ProtocolDialectConfigurationProperties.class).remove(this);
    }

    static ProtocolDialectConfigurationProperties from(DataModel dataModel, DeviceCommunicationConfiguration configuration, String name, DeviceProtocolDialect protocolDialect) {
        return dataModel.getInstance(ProtocolDialectConfigurationPropertiesImpl.class).init(configuration, name, protocolDialect);
    }

    ProtocolDialectConfigurationPropertiesImpl init(DeviceCommunicationConfiguration configuration, String name, DeviceProtocolDialect protocolDialect) {
        this.setName(name);
        this.deviceCommunicationConfiguration.set(configuration);
        this.protocolDialect = protocolDialect;
        this.protocolDialectName = protocolDialect.getDeviceProtocolDialectName();
        return this;
    }

    @Override
    public void save() {
        modDate = clock.now();
        super.save();
    }

    @Override
    public void setProperty(String name, Object value) {
        if (getTypedProperties().hasValueFor(name)) {
            updateExistingProperty(name, value);
            return;
        }
        setNewProperty(name, value);
    }

    @Override
    public void removeProperty(String name) {
        getTypedProperties().removeProperty(name);
        for (Iterator<ProtocolDialectConfigurationProperty> iterator = propertyList.iterator(); iterator.hasNext(); ) {
            if (iterator.next().getName().equals(name)) {
                iterator.remove();
            }
        }
    }

    private void setNewProperty(String name, Object value) {
        ProtocolDialectConfigurationProperty property = ProtocolDialectConfigurationProperty.forKey(this, name).setValue(asStringValue(name, value));
        propertyList.add(property);
        typedProperties.setProperty(name, value);
    }

    private void updateExistingProperty(String name, Object value) {
        for (ProtocolDialectConfigurationProperty property : propertyList) {
            if (property.getName().equals(name)) {
                property.setValue(asStringValue(name, value));
                typedProperties.setProperty(name, value);
                return;
            }
        }
        throw new IllegalStateException("This should not occur."); // if our typedProperties view has a value for name we should find its originating property
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
}
