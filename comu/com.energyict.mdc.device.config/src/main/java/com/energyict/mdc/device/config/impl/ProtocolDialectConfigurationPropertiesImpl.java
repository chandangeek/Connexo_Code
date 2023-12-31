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
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.device.config.DeleteEventType;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectSupport;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.config.KeyAccessorPropertySpecWithPossibleValues;
import com.energyict.mdc.device.config.exceptions.CannotDeleteProtocolDialectConfigurationPropertiesWhileInUseException;
import com.energyict.mdc.device.config.exceptions.NoSuchPropertyOnDialectException;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.UnmodifiableTypedProperties;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProtocolDialectConfigurationPropertiesImpl extends PersistentNamedObject<ProtocolDialectConfigurationProperties> implements ProtocolDialectConfigurationProperties {

    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    private DataModel dataModel;
    private Clock clock;

    private DeviceProtocolDialect protocolDialect;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOLDIALECT_REQUIRED + "}")
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String protocolDialectName;
    @Valid
    private List<ProtocolDialectConfigurationPropertyImpl> propertyList = new ArrayList<>();
    private Instant obsoleteDate;
    @SuppressWarnings("unused") //Used by the orm
    private String userName;
    @SuppressWarnings("unused") //Used by the orm
    private long version;
    @SuppressWarnings("unused") //Used by the orm
    private Instant createTime;
    @SuppressWarnings("unused") //Used by the orm
    private Instant modTime;

    private List<PropertySpec> propertySpecs;
    // transient
    private transient TypedProperties typedProperties;

    ProtocolDialectConfigurationPropertiesImpl() {
        super();
    }

    @Inject
    ProtocolDialectConfigurationPropertiesImpl(Clock clock, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ProtocolDialectConfigurationProperties.class, dataModel, eventService, thesaurus);
        this.dataModel = dataModel;
        this.clock = clock;
    }

    @Override
    protected void validateDelete() {
        this.getEventService().postEvent(EventType.PROTOCOLCONFIGURATIONPROPS_VALIDATEDELETE.topic(), this);
        List<PartialConnectionTask> partialConnectionTasks = this.dataModel.mapper(PartialConnectionTask.class).find(PartialConnectionTaskImpl.Fields.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES.fieldName(), this);
        if (!partialConnectionTasks.isEmpty()) {
            throw new CannotDeleteProtocolDialectConfigurationPropertiesWhileInUseException(this, this.getThesaurus(), MessageSeeds.PROTOCOLDIALECT_CONF_PROPS_IN_USE);
        }
    }

    @Override
    @XmlAttribute
    public String getName() {
        return name;
    }

    @Override
    protected void doSetName(String name) {
        this.name = name;
    }

    @Override
    @XmlTransient
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    @XmlTransient
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
        return deviceType.getDeviceProtocolPluggableClass()
                .map(DeviceProtocolPluggableClass::getDeviceProtocol)
                .map(DeviceProtocolDialectSupport::getDeviceProtocolDialects)
                .orElseGet(Collections::emptyList);

    }

    @Override
    @XmlElement(name = "deviceProtocolDialectName")
    public String getDeviceProtocolDialectName() {
        return this.protocolDialectName;
    }

    public void setDeviceProtocolDialectName(String name) {
        // For xml unmarshalling purposes only
    }

    @Override
    public boolean isObsolete() {
        return this.obsoleteDate != null;
    }

    @Override
    public Optional<Instant> getObsoleteDate() {
        return Optional.ofNullable(this.obsoleteDate);
    }

    @Override
    public void makeObsolete() {
        this.obsoleteDate = this.clock.instant();
        this.getDataModel().update(this, "obsoleteDate");
    }

    @Override
    @XmlAttribute
    public TypedProperties getTypedProperties() {
        if (typedProperties == null) {
            typedProperties = initializeTypedProperties();
        }
        return new UnmodifiableTypedProperties(typedProperties);
    }

    private TypedProperties initializeTypedProperties() {
        TypedProperties properties = TypedProperties.empty();
        for (ProtocolDialectConfigurationPropertyImpl property : propertyList) {
            ValueFactory<?> valueFactory = getPropertySpec(property.getName()).get().getValueFactory();
            properties.setProperty(property.getName(), valueFactory.fromStringValue(property.getValue()));
        }
        return properties;
    }

    @Override
    @XmlElements( {
            @XmlElement(type = BasicPropertySpec.class),
            @XmlElement(type = KeyAccessorPropertySpecWithPossibleValues.class),
            @XmlElement(type = UPLToConnexoPropertySpecAdapter.class),
    })
    public List<PropertySpec> getPropertySpecs() {
        if (propertySpecs == null && deviceConfiguration != null)
            if (this.getDeviceProtocolDialect() == null) {
                propertySpecs = Collections.emptyList();
            } else {
                propertySpecs = this.getDeviceProtocolDialect().getPropertySpecs();
            }
        return propertySpecs;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::adaptTo).collect(Collectors.toList());
    }

    @Override
    public final boolean isComplete() {
        return getPropertySpecs().stream().filter(PropertySpec :: isRequired).noneMatch(x->this.getProperty(x.getName()) == null);
    }

    @Override
    public long getVersion() {
        return this.version;
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

    void prepareDelete() {
        this.propertyList.clear();
    }

    @Override
    protected final void doDelete() {
        this.dataModel.mapper(ProtocolDialectConfigurationProperties.class).remove(this);
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

    private void updateProperty(String name, Object value) {
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
        return this.getPropertySpec(name)
                .map(PropertySpec::getValueFactory)
                .map(valueFactory -> valueFactory.toStringValue(value))
                .orElseThrow(() -> new NoSuchPropertyOnDialectException(getDeviceProtocolDialect(), name, this.getThesaurus(), MessageSeeds.PROTOCOL_DIALECT_HAS_NO_SUCH_PROPERTY));
    }

    @Override
    protected boolean validateUniqueName() {
        return true;
    }

    @Override
    public void save() {
        boolean update = getId() > 0;
        super.save();
        if (update) {
            dataModel.touch(deviceConfiguration.get());
        }
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}