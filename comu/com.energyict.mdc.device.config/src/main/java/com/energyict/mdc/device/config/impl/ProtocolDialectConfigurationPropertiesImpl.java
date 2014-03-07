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

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 5/03/13 - 16:12
 */
class ProtocolDialectConfigurationPropertiesImpl extends PersistentNamedObject<ProtocolDialectConfigurationProperties> implements ProtocolDialectConfigurationProperties {

    private Reference<DeviceCommunicationConfiguration> deviceCommunicationConfiguration = ValueReference.absent();

    private final Clock clock;

    private DeviceProtocolDialect protocolDialect;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.PROTOCOLDIALECT_REQUIRED_KEY + "}")
    private String protocolDialectName;
    private List<ProtocolDialectConfigurationProperty> propertyList = new ArrayList<>();

    private Date modDate;

    // transient
    private transient TypedProperties typedProperties;

    @Inject
    ProtocolDialectConfigurationPropertiesImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super(ProtocolDialectConfigurationProperties.class, dataModel, eventService, thesaurus);
        this.clock = clock;
    }

    /**
     * Loads the ProtocolDialectConfigurationProperties properties from the {@link java.sql.ResultSet}.
     *
     * @throws java.sql.SQLException Thrown by the ResultSet API
     */
//    protected void doLoad(ResultSet resultSet) throws SQLException {
//        super.doLoad(resultSet);
//        this.deviceCommunicationConfigurationId = resultSet.getInt(3);
//        this.protocolDialectName = resultSet.getString(4);
//    }

//    protected void init(final ProtocolDialectConfigurationPropertiesShadow shadow) throws SQLException, BusinessException {
//        this.execute(new Transaction<Void>() {
//            public Void doExecute () throws BusinessException, SQLException {
//                doInit(shadow);
//                return null;
//            }
//        });
//    }

//    private void doInit(ProtocolDialectConfigurationPropertiesShadow shadow) throws SQLException, BusinessException {
//        this.validateNew(shadow);
//        this.copyNew(shadow);
//        this.postNew();
//        this.created();
//    }

    @Override
    public void update() {
        save();
    }

//    private void validateNew(ProtocolDialectConfigurationPropertiesShadow shadow) throws BusinessException {
//        this.validatePrimaryKey(shadow);
//        this.validateProperties(shadow);
//    }

//    private void validateUpdate(ProtocolDialectConfigurationPropertiesShadow shadow) throws BusinessException {
//        this.validatePrimaryKeyNotChanged(shadow);
//        this.validateProperties(shadow);
//        this.validateNoRequiredPropertiesAreRemoved(shadow);
//    }

//    private void validatePrimaryKey(ProtocolDialectConfigurationPropertiesShadow shadow) throws BusinessException {
//        DeviceCommunicationConfiguration configuration = getDeviceCommunicationConfiguration();  // DeviceConfiguration available, cause it is the owner of this ConfigurationProperties
//        DeviceProtocolDialect dialect = this.validateDeviceProtocolDialect(shadow);
//
//        ProtocolDialectConfigurationPropertiesFactory configurationPropertiesFactory = (ProtocolDialectConfigurationPropertiesFactory) getFactory();
//        ProtocolDialectConfigurationProperties configurationProperties = configurationPropertiesFactory.findByConfigurationAndDialect(configuration, dialect);
//        if (configurationProperties != null) {
//            throw new DuplicateException(
//                    "duplicateDialectConfigurationProperties",
//                    "A dialect configuration properties having device configuration  \"{0}\" and device protocol dialect \"{1}\" already exists.",
//                    configuration.getId(),
//                    dialect.getDeviceProtocolDialectName());
//        }
//    }

//    private DeviceProtocolDialect validateDeviceProtocolDialect(ProtocolDialectConfigurationPropertiesShadow shadow) throws InvalidValueException, CodingException {
//        String dialectClassName = shadow.getProtocolDialectName();
//        if (dialectClassName == null || dialectClassName.isEmpty()) {
//            throw new InvalidValueException("XcannotBeEmpty", "\"{0}\" is a required property", "configurationProperties.dialectClassName");
//        }
//        return this.getDeviceProtocolDialect(dialectClassName);
//    }

//    private void validatePrimaryKeyNotChanged(ProtocolDialectConfigurationPropertiesShadow shadow) throws BusinessException {
//        if (!this.getDeviceProtocolDialectName().equals(shadow.getProtocolDialectName())) {
//            throw new BusinessException("configurationProperties.updateOfDialectClassNotAllowed", "The device protocol dialect class cannot be changed.");
//        }
//    }

//    private void validateProperties(ProtocolDialectConfigurationPropertiesShadow shadow) throws BusinessException {
//        this.validatePropertiesAreLinkedToPropertySpecs(shadow);
//        this.validatePropertyValues(shadow);
//    }

//    private void validatePropertiesAreLinkedToPropertySpecs(ProtocolDialectConfigurationPropertiesShadow shadow) throws BusinessException {
//        DeviceProtocolDialect protocolDialect = this.getDeviceProtocolDialect(shadow.getProtocolDialectName());
//        for (String propertyName : shadow.getTypedProperties().localPropertyNames()) {
//            if (protocolDialect.getPropertySpec(propertyName) == null) {
//                throw new BusinessException(
//                        "deviceProtocolPropertyXIsNotInDeviceProtocolDialect",
//                        "DeviceProtocolDialect {0} does not contain a specification for attribute {1}",
//                        protocolDialect.getDeviceProtocolDialectName(),
//                        propertyName);
//            }
//        }
//    }

//    private void validatePropertyValues(ProtocolDialectConfigurationPropertiesShadow shadow) throws BusinessException {
//        DeviceProtocolDialect protocolDialect = this.getDeviceProtocolDialect(shadow.getProtocolDialectName());
//        for (String propertyName : shadow.getTypedProperties().localPropertyNames()) {
//            this.validatePropertyValue(protocolDialect, propertyName, shadow.getTypedProperties().getProperty(propertyName));
//        }
//    }

//    private void validatePropertyValue(DeviceProtocolDialect protocolDialect, String propertyName, Object propertyValue) throws InvalidValueException {
//        PropertySpec propertySpec = protocolDialect.getPropertySpec(propertyName);
//        propertySpec.validateValue(propertyValue);
//    }

//    private void validateNoRequiredPropertiesAreRemoved(ProtocolDialectConfigurationPropertiesShadow shadow) throws BusinessException {
//        DeviceProtocolDialect protocolDialect = this.getDeviceProtocolDialect(shadow.getProtocolDialectName());
//        TypedProperties shadowProperties = shadow.getTypedProperties();
//        TypedProperties oldProperties = getTypedProperties();
//
//        for (String propertyName : oldProperties.localPropertyNames()) {
//            if ((shadowProperties.getProperty(propertyName) == null) &&
//                    protocolDialect.getPropertySpec(propertyName).isRequired()) {
//                throw new BusinessException("configurationProperties.requiredAttributeCannotBeDeleted", "Cannot delete attribute {1} because it is a required attribute of device protocol dialect {0}.",
//                        protocolDialect.getDeviceProtocolDialectName(),
//                        propertyName);
//            }
//        }
//    }

//    protected void copy(ProtocolDialectConfigurationPropertiesShadow shadow) {
//        super.copyProperties(shadow);
//        this.setName(shadow.getName());
//        this.setProtocolDialectName(shadow.getProtocolDialectName());
//    }

//    @Override
//    protected void validateDelete() throws SQLException, BusinessException {
//        super.validateDelete();
//        ProtocolDialectPropertiesFactory factory = ManagerFactory.getCurrent().getProtocolDialectPropertiesFactory();
//        if (factory.existsUsing(this)) {
//            throw new BusinessException(
//                    "protocolDialectConfigurationPropertiesXIsStillInUseByProtocolDialectProperties",
//                    "ProtocolDialectConfigurationProperties for {0} on configuration {1} is still being used by ProtocolDialectProperties on one or more devices",
//                    this.getDeviceProtocolDialect().getDeviceProtocolDialectName(),
//                    this.getDeviceCommunicationConfiguration().getDeviceConfiguration().getName());
//        }
//    }

    @Override
    public DeviceCommunicationConfiguration getDeviceCommunicationConfiguration() {
        return this.deviceCommunicationConfiguration.orNull(); // TODO should become get() instead of orNull
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
        DeviceType deviceType = this.getDeviceCommunicationConfiguration().getDeviceConfiguration().getDeviceType();
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
        return typedProperties;
    }

    private TypedProperties initializeTypedProperties() {
        TypedProperties properties = TypedProperties.empty();
        for (ProtocolDialectConfigurationProperty property : propertyList) {
            ValueFactory<?> valueFactory = getPropertySpec(property.getName()).getValueFactory();
            typedProperties.setProperty(property.getName(), valueFactory.fromStringValue(property.getValue()));
        }
        return properties;
    }

//    @Override
//    protected int bindBody(PreparedStatement preparedStatement, int offset) throws SQLException {
//        int parameterNumber = offset;
//        preparedStatement.setInt(parameterNumber++, this.getDeviceCommunicationConfigurationId());
//        preparedStatement.setString(parameterNumber++, this.getDeviceProtocolDialectName());
//        return parameterNumber;
//    }

//    @Override
//    protected PropertyPersister doGetPropertyPersister() {
//        return ProtocolDialectConfigurationPropertiesFactoryImpl.newPropertyPersister();
//    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        if (this.getDeviceProtocolDialect() == null) {
            return new ArrayList<>(0);
        }
        else {
            return this.getDeviceProtocolDialect().getPropertySpecs();
        }
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        if (this.getDeviceProtocolDialect() == null) {
            return null;
        }
        else {
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
    protected void doDelete() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    protected void validateDelete() {
        //TODO automatically generated method body, provide implementation.

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
