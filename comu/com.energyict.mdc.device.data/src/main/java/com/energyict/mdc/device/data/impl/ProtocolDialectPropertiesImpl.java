package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.google.common.collect.Range;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.exceptions.DuplicateNameException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectProperty;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME;

/**
 * Provides an implementation for the {@link ProtocolDialectProperties} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-31 (08:54)
 */
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
public class ProtocolDialectPropertiesImpl
        extends NamedPluggableClassUsageImpl<ProtocolDialectProperties, DeviceProtocolDialect, DeviceProtocolDialectProperty>
        implements
            ProtocolDialectProperties,
            PersistenceAware {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> configurationProperties = ValueReference.absent();
    private DeviceProtocolDialectUsagePluggableClass deviceProtocolDialectUsagePluggableClass;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED + "}")
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private Map<String,String> relationAttributeNamePropertyMap;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    private ProtocolPluggableService protocolPluggableService;

    @Inject
    public ProtocolDialectPropertiesImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ProtocolPluggableService protocolPluggableService) {
        super(ProtocolDialectProperties.class, dataModel, eventService, thesaurus, clock);
        this.protocolPluggableService = protocolPluggableService;
    }

    public ProtocolDialectPropertiesImpl initialize (Device device, ProtocolDialectConfigurationProperties configurationProperties) {
        this.device.set(device);
        this.configurationProperties.set(configurationProperties);
        this.setName(configurationProperties.getName());
        this.setDeviceProtocolPluggableClassFromConfigurationProperties();
        return this;
    }

    private void setDeviceProtocolPluggableClassFromConfigurationProperties() {
        this.setDeviceProtocolPluggableClass(this.getDeviceProtocolPluggableClass(configurationProperties.get()));
    }

    private DeviceProtocolPluggableClass getDeviceProtocolPluggableClass(ProtocolDialectConfigurationProperties configurationProperties) {
        return configurationProperties.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
    }

    DeviceProtocol getDeviceProtocol () {
        return this.getDeviceProtocolPluggableClass(this.getProtocolDialectConfigurationProperties()).getDeviceProtocol();
    }

    private void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocol) {
        this.deviceProtocolPluggableClass = deviceProtocol;
        this.setPluggableClassId(deviceProtocol.getId());
    }

    @Override
    public void postLoad() {
        this.setDeviceProtocolPluggableClassFromConfigurationProperties();
    }

    @Override
    public void save() {
        super.save();
        this.saveAllProperties();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate for now
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        // Make the superclass' method public
        super.setProperty(propertyName, value);
    }

    @Override
    public void removeProperty(String propertyName) {
        // Make the superclass' method public
        super.removeProperty(propertyName);
    }

    @Override
    public Device getDevice () {
        return this.device.get();
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return this.configurationProperties.get();
    }

    @Override
    public String getDeviceProtocolDialectName () {
        if (this.getProtocolDialectConfigurationProperties() == null) {
            return "";
        }
        else {
            return getProtocolDialectConfigurationProperties().getDeviceProtocolDialectName();
        }
    }

    @Override
    public String getName() {
        if (this.configurationProperties.isPresent()) {
            return getProtocolDialectConfigurationProperties().getName();
        }
        else {
            return "";
        }
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return null;
    }

    @Override
    protected String getDefaultAttributeName() {
        return DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME;
    }

    @Override
    protected DeviceProtocolDialectProperty newPropertyFor(Relation relation, RelationAttributeType attributeType) {
        return new DeviceProtocolDialectPropertyImpl(relation, attributeType.getName(), this.getPluggableClass());
    }

    @Override
    protected DeviceProtocolDialectProperty newInheritedPropertyFor (String propertyName, Object propertyValue) {
        return new DeviceProtocolDialectPropertyImpl(propertyName, propertyValue, Range.all(), this.getPluggableClass(), true);
    }

    @Override
    public DeviceProtocolDialectProperty newProperty(String propertyName, Object propertyValue, Instant activeDate) {
        return new DeviceProtocolDialectPropertyImpl(propertyName, propertyValue, Range.all(), this.getPluggableClass(), false);
    }

    @Override
    protected RelationType findRelationType() {
        return this.getDeviceProtocolDialectUsagePluggableClass().findRelationType();
    }

    @Override
    public TypedProperties getTypedProperties() {
        TypedProperties typedProperties = TypedProperties.empty();
        if (this.getProtocolDialectConfigurationProperties() != null) {
            typedProperties = TypedProperties.inheritingFrom(this.getProtocolDialectConfigurationProperties().getTypedProperties());
        }
        typedProperties.setAllProperties(super.getTypedProperties());
        return checkForCorrectRelationAttributeNamings(typedProperties);
    }

    private TypedProperties checkForCorrectRelationAttributeNamings(TypedProperties typedProperties) {
        final TypedProperties inheritedProperties = typedProperties.getInheritedProperties();
        TypedProperties newAllProperties = TypedProperties.inheritingFrom(inheritedProperties);
        for (String propertyName : typedProperties.propertyNames()) {
            if (!typedProperties.isInheritedValueFor(typedProperties.getProperty(propertyName), propertyName)) {
                final String correctedPropertyName = this.getCorrectedRelationAttributeName(propertyName);
                if (correctedPropertyName != null) {
                    newAllProperties.setProperty(correctedPropertyName, typedProperties.getProperty(propertyName));
                }
            }
        }
        return newAllProperties;
    }

    private String getCorrectedRelationAttributeName(String propertyName) {
        return this.getRelationAttributeNamePropertyMap().get(propertyName);
    }

    @Override
    public DeviceProtocolDialectUsagePluggableClass getPluggableClass() {
        return getDeviceProtocolDialectUsagePluggableClass();
    }

    private Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long pluggableClassId) {
        return this.protocolPluggableService.findDeviceProtocolPluggableClass(pluggableClassId);
    }

    private DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass() {
        if (this.deviceProtocolDialectUsagePluggableClass == null) {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.getDeviceProtocolPluggableClass();
            this.deviceProtocolDialectUsagePluggableClass = this.protocolPluggableService.getDeviceProtocolDialectUsagePluggableClass(deviceProtocolPluggableClass, this.getDeviceProtocolDialectName());
        }
        return this.deviceProtocolDialectUsagePluggableClass;
    }

    private DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        if (this.deviceProtocolPluggableClass == null) {
            this.deviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(this.getPluggableClassId()).get();
        }
        return this.deviceProtocolPluggableClass;
    }

    @Override
    protected List<PropertySpec> getPluggablePropetySpecs() {
        return this.getDeviceProtocolDialectUsagePluggableClass().getDeviceProtocolDialect().getPropertySpecs();
    }

    @Override
    public List<DeviceProtocolDialectProperty> getAllProperties(Instant date) {
        List<DeviceProtocolDialectProperty> allProperties = new ArrayList<>();
        List<DeviceProtocolDialectProperty> localProperties = super.getAllProperties(date);
        this.addConfigurationProperties(allProperties, localProperties);
        return this.checkForRelationAttributeNameConversions(allProperties);
    }

    private void addConfigurationProperties(List<DeviceProtocolDialectProperty> allProperties, List<DeviceProtocolDialectProperty> localProperties) {
        final ProtocolDialectConfigurationProperties configurationProperties = this.getProtocolDialectConfigurationProperties();
        if (configurationProperties != null) {
            TypedProperties inheritedProperties = configurationProperties.getTypedProperties();
            for (String inheritedPropertyName : inheritedProperties.propertyNames()) {
                if (!propertySpecifiedIn(localProperties, inheritedPropertyName)) { // If the inherited property is overruled by a local property, then do not add it
                    allProperties.add(
                            newInheritedPropertyFor(
                                    inheritedPropertyName,
                                    inheritedProperties.getProperty(inheritedPropertyName)
                            )
                    );
                }
            }
        }
        allProperties.addAll(localProperties);
    }

    /**
     * Method to check if the given list of {@link DeviceProtocolDialectProperty DeviceProtocolDialectProperties} contains a property with the specified name.
     *
     * @param properties    The list of DeviceProtocolProperties
     * @param propertyName  The property name to be searched for
     * @return true if the list contains a property with the specified name
     *         false if not the case
     */
    private boolean propertySpecifiedIn(List<DeviceProtocolDialectProperty> properties, String propertyName) {
        for (DeviceProtocolDialectProperty dialectProperty : properties) {
            if (dialectProperty.getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reverts the names of the DeviceProtocolDialectProperty back to the original name
     * as provided by the pluggable class in case the name was modified during
     * relation type creation to comply with naming conventions
     * for relation type attributes (such as length, removal of special characters,...).
     *
     * @param allProperties the list of DeviceProtocolDialectProperties we received from the relation
     * @return a proper list of properties which can be forwarded to a DeviceProtocol
     */
    private List<DeviceProtocolDialectProperty> checkForRelationAttributeNameConversions(List<DeviceProtocolDialectProperty> allProperties) {
        List<DeviceProtocolDialectProperty> nameConvertedProperties = new ArrayList<>(allProperties.size());
        Map<String, String> relationAttributeNamePropertyMap = getRelationAttributeNamePropertyMap();
        for (DeviceProtocolDialectProperty dialectProperty : allProperties) {
            DeviceProtocolDialectPropertyImpl deviceProtocolDialectProperty =
                    new DeviceProtocolDialectPropertyImpl(
                            relationAttributeNamePropertyMap.get(dialectProperty.getName()),
                            dialectProperty.getValue(),
                            dialectProperty.getActivePeriod(),
                            dialectProperty.getPluggableClass(), dialectProperty.isInherited());
            nameConvertedProperties.add(deviceProtocolDialectProperty);
        }
        return nameConvertedProperties;
    }

    protected Map<String, String> getRelationAttributeNamePropertyMap() {
        if (this.relationAttributeNamePropertyMap == null) {
            this.relationAttributeNamePropertyMap = new HashMap<>();
            List<PropertySpec> allPropertySpecs = new ArrayList<>();
            allPropertySpecs.addAll(getDeviceProtocolDialectUsagePluggableClass().getDeviceProtocolDialect().getPropertySpecs());
            for (PropertySpec propertySpec : allPropertySpecs) {
                String attributeName = this.protocolPluggableService.createConformRelationAttributeName(propertySpec.getName());
                this.relationAttributeNamePropertyMap.put(attributeName, propertySpec.getName());
            }
        }
        return this.relationAttributeNamePropertyMap;
    }

}