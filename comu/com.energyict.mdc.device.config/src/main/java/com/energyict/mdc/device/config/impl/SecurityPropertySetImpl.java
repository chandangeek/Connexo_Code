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
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeleteEventType;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecuritySuite;
import com.energyict.mdc.device.config.KeyAccessorPropertySpecWithPossibleValues;
import com.energyict.mdc.device.config.exceptions.CannotDeleteSecurityPropertySetWhileInUseException;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.energyict.mdc.common.protocol.security.DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Provides an implementation for the {@link SecurityPropertySet} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-14 (11:09)
 */
@LevelMustBeProvidedIfSupportedByDevice(groups = {Save.Create.class, Save.Update.class})
@ValidClient(groups = {Save.Create.class, Save.Update.class})
@ValidConfigurationSecurityProperties(groups = {Save.Create.class, Save.Update.class})
//Do not remove the public access modifier: CXO-2786
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityPropertySetImpl extends PersistentNamedObject<SecurityPropertySet> implements ServerSecurityPropertySet, PersistenceAware {

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private DeviceProtocol deviceProtocol;
    private int authenticationLevelId;
    private AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel;
    private int encryptionLevelId;
    private EncryptionDeviceAccessLevel encryptionDeviceAccessLevel;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String clientDbValue;
    private Object client;
    private int securitySuiteId = -1;
    private SecuritySuite securitySuite;
    private int requestSecurityLevelId = -1;
    private RequestSecurityLevel requestSecurityLevel;
    private int responseSecurityLevelId = -1;
    private ResponseSecurityLevel responseSecurityLevel;
    private Set<SecurityAccessorUserAction> userActions = EnumSet.noneOf(SecurityAccessorUserAction.class);
    private List<UserActionRecord> userActionRecords = new ArrayList<>();
    @Valid
    private List<ConfigurationSecurityProperty> configurationSecurityProperties = new ArrayList<>();
    private ThreadPrincipalService threadPrincipalService;
    private ProtocolPluggableService protocolPluggableService;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    public SecurityPropertySetImpl() {
        super();
    }

    @Inject
    public SecurityPropertySetImpl(
            DataModel dataModel, EventService eventService, Thesaurus thesaurus,
            ThreadPrincipalService threadPrincipalService, ProtocolPluggableService protocolPluggableService) {
        super(SecurityPropertySet.class, dataModel, eventService, thesaurus);
        this.threadPrincipalService = threadPrincipalService;
        this.protocolPluggableService = protocolPluggableService;
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
    protected CreateEventType createEventType() {
        return CreateEventType.SECURITY_PROPERTY_SET;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.SECURITY_PROPERTY_SET;
    }

    @Override
    public DeleteEventType deleteEventType() {
        return DeleteEventType.SECURITY_PROPERTY_SET;
    }

    @Override
    protected void doDelete() {
        this.getDataModel().mapper(SecurityPropertySet.class).remove(this);
    }

    @Override
    public void prepareDelete() {
        this.validateDelete();
        this.configurationSecurityProperties.clear();
        this.userActionRecords.clear();
    }

    @Override
    protected void validateDelete() {
        List<ComTaskEnablement> comTaskEnablements = this.getDataModel().mapper(ComTaskEnablement.class).find(ComTaskEnablementImpl.Fields.SECURITY_PROPERTY_SET.fieldName(), this);
        if (!comTaskEnablements.isEmpty()) {
            throw new CannotDeleteSecurityPropertySetWhileInUseException(this, this.getThesaurus(), MessageSeeds.SECURITY_PROPERTY_SET_IN_USE);
        }
        this.getEventService().postEvent(EventType.SECURITY_PROPERTY_SET_VALIDATE_DELETE.topic(), this);
    }

    @Override
    protected boolean validateUniqueName() {
        return this.findOtherByName(this.getName()) == null;
    }

    private SecurityPropertySet findOtherByName(String name) {
        Optional<SecurityPropertySet> other = this.getDataMapper().getUnique("name", name, "deviceConfiguration", this.deviceConfiguration.get());
        if (other.isPresent()) {
            SecurityPropertySet otherSet = other.get();
            if (otherSet.getId() == this.getId()) {
                return null;
            } else {
                return other.get();
            }
        } else {
            return null;
        }
    }

    static class UserActionRecord {
        private SecurityAccessorUserAction userAction;
        private Reference<SecurityPropertySet> set = ValueReference.absent();
        @SuppressWarnings("unused")
        private String userName;
        @SuppressWarnings("unused")
        private long version;
        @SuppressWarnings("unused")
        private Instant createTime;
        @SuppressWarnings("unused")
        private Instant modTime;

        UserActionRecord() {
        }

        UserActionRecord(SecurityPropertySet set, SecurityAccessorUserAction userAction) {
            this();
            this.set.set(set);
            this.userAction = userAction;
        }
    }

    @Override
    public void postLoad() {
        userActions.addAll(userActionRecords.stream().map(userActionRecord -> userActionRecord.userAction).collect(toList()));
    }

    static SecurityPropertySetImpl from(DataModel dataModel, DeviceConfigurationImpl deviceConfiguration, String name) {
        return dataModel.getInstance(SecurityPropertySetImpl.class).init(deviceConfiguration, name);
    }

    private SecurityPropertySetImpl init(DeviceConfiguration deviceConfiguration, String name) {
        this.deviceConfiguration.set(deviceConfiguration);
        this.setName(name);
        return this;
    }

    @Override
    @XmlElement(type = UPLAuthenticationLevelAdapter.class)
    public AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel() {
        if (this.authenticationDeviceAccessLevel == null) {
            if (this.authenticationLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.authenticationDeviceAccessLevel = new NoAuthentication();
            } else {
                this.authenticationDeviceAccessLevel = this.findAuthenticationLevel(this.authenticationLevelId);
            }
        }
        return this.authenticationDeviceAccessLevel;
    }

    /**
     * Finds the {@link AuthenticationDeviceAccessLevel} with the specified id
     * and returns {@link NoAuthentication} when it does not exists.
     *
     * @param id The unique identifier of the AuthenticationDeviceAccessLevel
     * @return The AuthenticationDeviceAccessLevel or NoAuthenthication when the device protocol
     * does not have an AuthenticationDeviceAccessLevel with the specified id
     */
    private AuthenticationDeviceAccessLevel findAuthenticationLevel(int id) {
        if (this.getDeviceProtocol() != null) {
            return this.getDeviceProtocol().getAuthenticationAccessLevels().stream()
                    .filter(level -> level.getId() == id)
                    .findAny()
                    .map(this.protocolPluggableService::adapt)
                    .map(AuthenticationDeviceAccessLevel.class::cast)
                    .orElse(new NoAuthentication());
        }
        return null;
    }

    @Override
    @XmlElement(type = UPLEncryptionLevelAdapter.class)
    public EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel() {
        if (this.encryptionDeviceAccessLevel == null) {
            if (this.encryptionLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.encryptionDeviceAccessLevel = new NoEncryption();
            } else {
                this.encryptionDeviceAccessLevel = this.findEncryptionLevel(this.encryptionLevelId);
            }
        }
        return this.encryptionDeviceAccessLevel;
    }

    /**
     * Finds the {@link EncryptionDeviceAccessLevel} with the specified id
     * and returns {@link NoEncryption} when it does not exists.
     *
     * @param id The unique identifier of the EncryptionDeviceAccessLevel
     * @return The EncryptionDeviceAccessLevel or NoEncryption when the device protocol
     * does not have an EncryptionDeviceAccessLevel with the specified id
     */
    private EncryptionDeviceAccessLevel findEncryptionLevel(int id) {
        if (this.getDeviceProtocol() != null) {
            return this.getDeviceProtocol().getEncryptionAccessLevels().stream()
                    .filter(level -> level.getId() == id)
                    .findAny()
                    .map(this.protocolPluggableService::adapt)
                    .orElse(new NoEncryption());
        }
        return null;
    }

    @Override
    public Object getClient() {
        if (client == null && clientDbValue != null && !clientDbValue.isEmpty()) {
            Optional<com.energyict.mdc.upl.properties.PropertySpec> clientSecurityPropertySpec = getDeviceProtocol().getClientSecurityPropertySpec();
            client = clientSecurityPropertySpec.isPresent()
                    ? clientSecurityPropertySpec.get().getValueFactory().fromStringValue(clientDbValue)
                    : null;
        }
        return client;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        if (this.getDeviceProtocol() != null) {
            return getDeviceProtocol().getClientSecurityPropertySpec().isPresent()
                    ? Optional.of(UPLToConnexoPropertySpecAdapter.adaptTo(getDeviceProtocol().getClientSecurityPropertySpec().get()))
                    : Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    @JsonIgnore
    public SecuritySuite getSecuritySuite() {
        if (this.securitySuite == null) {
            if (this.securitySuiteId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.securitySuite = new NoSecuritySuite();
            } else {
                this.securitySuite = this.findSecuritySuite(this.securitySuiteId);
            }
        }
        return this.securitySuite;
    }

    /**
     * Finds the {@link SecuritySuite} with the specified id
     * and returns {@link NoSecuritySuite} when it does not exists.
     *
     * @param id The unique identifier of the SecuritySuite
     * @return The SecuritySuite or NoSecuritySuite when the device protocol
     * does not have a SecuritySuite with the specified id
     */
    private SecuritySuite findSecuritySuite(int id) {
        if (isAdvancedSecurity(this.getDeviceProtocol())) {
            return (castToAdvancedSecurity(this.getDeviceProtocol())).getSecuritySuites().stream()
                    .filter(level -> level.getId() == id)
                    .findAny()
                    .map(this.protocolPluggableService::adapt)
                    .orElse(new NoSecuritySuite());
        }
        return new NoSecuritySuite();
    }

    @Override
    @JsonIgnore
    public RequestSecurityLevel getRequestSecurityLevel() {
        if (this.requestSecurityLevel == null) {
            if (this.requestSecurityLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.requestSecurityLevel = new NoRequestSecurity();
            } else {
                this.requestSecurityLevel = this.findRequestSecurityLevel(this.requestSecurityLevelId);
            }
        }
        return this.requestSecurityLevel;
    }

    /**
     * Finds the {@link RequestSecurityLevel} with the specified id
     * and returns {@link NoRequestSecurity} when it does not exists.
     *
     * @param id The unique identifier of the EncryptionDeviceAccessLevel
     * @return The EncryptionDeviceAccessLevel or NoEncryption when the device protocol
     * does not have an EncryptionDeviceAccessLevel with the specified id
     */
    private RequestSecurityLevel findRequestSecurityLevel(int id) {
        if (isAdvancedSecurity(this.getDeviceProtocol())) {
            return (castToAdvancedSecurity(this.getDeviceProtocol())).getRequestSecurityLevels().stream()
                    .filter(level -> level.getId() == id)
                    .findAny()
                    .map(this.protocolPluggableService::adapt)
                    .orElse(new NoRequestSecurity());
        }
        return new NoRequestSecurity();
    }

    static boolean isAdvancedSecurity(DeviceProtocol deviceProtocol) {
        if (deviceProtocol instanceof UPLProtocolAdapter) {
            return ((UPLProtocolAdapter) deviceProtocol).getActual() instanceof AdvancedDeviceProtocolSecurityCapabilities;
        } else {
            return deviceProtocol instanceof AdvancedDeviceProtocolSecurityCapabilities;
        }
    }

    private static AdvancedDeviceProtocolSecurityCapabilities castToAdvancedSecurity(DeviceProtocol deviceProtocol) {
        if (deviceProtocol instanceof UPLProtocolAdapter) {
            return (AdvancedDeviceProtocolSecurityCapabilities) ((UPLProtocolAdapter) deviceProtocol).getActual();
        } else {
            return (AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol;
        }
    }

    @Override
    @JsonIgnore
    public ResponseSecurityLevel getResponseSecurityLevel() {
        if (this.responseSecurityLevel == null) {
            if (this.responseSecurityLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.responseSecurityLevel = new NoResponseSecurity();
            } else {
                this.responseSecurityLevel = this.findResponseSecurityLevel(this.responseSecurityLevelId);
            }
        }
        return this.responseSecurityLevel;
    }

    /**
     * Finds the {@link ResponseSecurityLevel} with the specified id
     * and returns {@link NoResponseSecurity} when it does not exists.
     *
     * @param id The unique identifier of the EncryptionDeviceAccessLevel
     * @return The EncryptionDeviceAccessLevel or NoEncryption when the device protocol
     * does not have an EncryptionDeviceAccessLevel with the specified id
     */
    private ResponseSecurityLevel findResponseSecurityLevel(int id) {
        if (isAdvancedSecurity(this.getDeviceProtocol())) {
            return (castToAdvancedSecurity(this.getDeviceProtocol())).getResponseSecurityLevels().stream()
                    .filter(level -> level.getId() == id)
                    .findAny()
                    .map(this.protocolPluggableService::adapt)
                    .orElse(new NoResponseSecurity());
        }
        return new NoResponseSecurity();
    }

    private DeviceProtocol getDeviceProtocol() {
        if (this.deviceProtocol == null) {
            DeviceProtocolPluggableClass protocolPluggableClass = this.getDeviceProtocolPluggableClass();
            if (protocolPluggableClass != null) {
                this.deviceProtocol = protocolPluggableClass.getDeviceProtocol();
            }
        }
        return this.deviceProtocol;
    }

    private DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        if (getDeviceConfiguration()  != null) {
            DeviceType deviceType = getDeviceConfiguration().getDeviceType();
            return deviceType.getDeviceProtocolPluggableClass().orElseThrow(UnsupportedOperationException::new);
        }
        return null;
    }

    @Override
    @XmlElement(type = ConfigurationSecurityPropertyImpl.class)
    public List<ConfigurationSecurityProperty> getConfigurationSecurityProperties() {
        return Collections.unmodifiableList(configurationSecurityProperties);
    }

    @Override
    public void addConfigurationSecurityProperty(String name, SecurityAccessorType keyAccessor) {
        this.configurationSecurityProperties.add(new ConfigurationSecurityPropertyImpl(this, name, keyAccessor));
    }

    @Override
    public void updateConfigurationSecurityProperty(String configurationSecurityPropertyName, SecurityAccessorType keyAccessor) {
        this.removeConfigurationSecurityProperty(configurationSecurityPropertyName);
        if (keyAccessor != null) {
            this.addConfigurationSecurityProperty(configurationSecurityPropertyName, keyAccessor);
        }
    }

    @Override
    public void removeConfigurationSecurityProperty(String configurationSecurityPropertyName) {
        Optional<ConfigurationSecurityProperty> configurationSecurityProperty = this.configurationSecurityProperties
                .stream()
                .filter(property -> property.getName().equals(configurationSecurityPropertyName))
                .findFirst();
        if (configurationSecurityProperty.isPresent()) {
            this.configurationSecurityProperties.remove(configurationSecurityProperty.get());
        }
    }

    @Override
    public Set<PropertySpec> getPropertySpecs() {
        Map<String, PropertySpec> result = new HashMap<>();
        if (this.findAuthenticationLevel(this.authenticationLevelId) != null) {
            this.addMissingSecurityPropertiesAndAvoidDuplicates(result, this.findAuthenticationLevel(this.authenticationLevelId).getSecurityProperties());
        }
        if (this.findEncryptionLevel(this.encryptionLevelId) != null) {
            this.addMissingSecurityPropertiesAndAvoidDuplicates(result, this.findEncryptionLevel(this.encryptionLevelId).getSecurityProperties());
        }
        if (this.findRequestSecurityLevel(this.requestSecurityLevelId) != null) {
            this.addMissingSecurityPropertiesAndAvoidDuplicates(result, this.findRequestSecurityLevel(this.requestSecurityLevelId).getSecurityProperties());
        }
        if (this.findResponseSecurityLevel(this.responseSecurityLevelId) != null) {
            this.addMissingSecurityPropertiesAndAvoidDuplicates(result, this.findResponseSecurityLevel(this.responseSecurityLevelId).getSecurityProperties());
        }

        return result.values()
                .stream()
                .map(ps -> KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> this.getDeviceConfiguration().getDeviceType().getSecurityAccessorTypes(), ps))
                .collect(toSet());
    }

    private void addMissingSecurityPropertiesAndAvoidDuplicates(Map<String, PropertySpec> result, List<PropertySpec> securityProperties) {
        for (PropertySpec propertySpec : securityProperties) {
            if (!result.containsKey(propertySpec.getName())) {
                result.put(propertySpec.getName(), propertySpec);
            }
        }
    }

    @Override
    @XmlTransient
    public DeviceConfiguration getDeviceConfiguration() {
        if (deviceConfiguration.isPresent()) {
            return this.deviceConfiguration.get();
        }
        return null;
    }

    @Override
    public void setAuthenticationLevelId(int authenticationLevelId) {
        this.authenticationLevelId = authenticationLevelId;
        this.authenticationDeviceAccessLevel = null;
    }

    @Override
    public void setEncryptionLevelId(int encryptionLevelId) {
        this.encryptionLevelId = encryptionLevelId;
        this.encryptionDeviceAccessLevel = null;
    }

    @Override
    public void setClient(Object client) {
        this.client = client;
        if (client == null) {
            this.clientDbValue = null;
        } else {
            Optional<com.energyict.mdc.upl.properties.PropertySpec> clientSecurityPropertySpec = this.getDeviceProtocol().getClientSecurityPropertySpec();
            this.clientDbValue = clientSecurityPropertySpec.isPresent() ? clientSecurityPropertySpec.get().getValueFactory().toStringValue(client) : null;
        }
    }

    @Override
    public void setSecuritySuiteId(int securitySuiteId) {
        this.securitySuiteId = securitySuiteId;
        this.securitySuite = null;
    }

    @Override
    public void setRequestSecurityLevelId(int requestSecurityLevelId) {
        this.requestSecurityLevelId = requestSecurityLevelId;
        this.requestSecurityLevel = null;
    }

    @Override
    public void setResponseSecurityLevelId(int responseSecurityLevelId) {
        this.responseSecurityLevelId = responseSecurityLevelId;
        this.responseSecurityLevel = null;
    }

    private static class NoAuthentication implements AuthenticationDeviceAccessLevel {

        private NoAuthentication() {
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return "-";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoAuthentication) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public int hashCode() {
            return getId();
        }

        @Override
        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        @Override
        public void setXmlType(String ignore) {
            //Ignore, only used for JSON
        }
    }

    private static class NoEncryption implements EncryptionDeviceAccessLevel {

        private NoEncryption() {
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return "-";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoEncryption) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public int hashCode() {
            return getId();
        }

        @Override
        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        @Override
        public void setXmlType(String ignore) {
            //Ignore, only used for JSON
        }
    }

    private static class NoSecuritySuite implements SecuritySuite {

        public NoSecuritySuite() {
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return "-";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<RequestSecurityLevel> getRequestSecurityLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<ResponseSecurityLevel> getResponseSecurityLevels() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoSecuritySuite) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public int hashCode() {
            return getId();
        }

        @Override
        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        @Override
        public void setXmlType(String ignore) {
            //Ignore, only used for JSON
        }
    }

    private static class NoRequestSecurity implements RequestSecurityLevel {

        public NoRequestSecurity() {
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return "-";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoRequestSecurity) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public int hashCode() {
            return getId();
        }

        @Override
        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        @Override
        public void setXmlType(String ignore) {
            //Ignore, only used for JSON
        }
    }

    private static class NoResponseSecurity implements ResponseSecurityLevel {

        public NoResponseSecurity() {
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return "-";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoResponseSecurity) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public int hashCode() {
            return getId();
        }

        @Override
        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        @Override
        public void setXmlType(String ignore) {
            //Ignore, only used for JSON
        }
    }

    @Override
    public void update() {
        save();
        getDataModel().touch(deviceConfiguration.get());
    }

    @Override
    public long getVersion() {
        return version;
    }

    static class LevelsAreSupportedValidator implements ConstraintValidator<LevelMustBeProvidedIfSupportedByDevice, SecurityPropertySetImpl> {

        private final ProtocolPluggableService protocolPluggableService;
        private final Thesaurus thesaurus;

        @Inject
        LevelsAreSupportedValidator(ProtocolPluggableService protocolPluggableService, Thesaurus thesaurus) {
            this.protocolPluggableService = protocolPluggableService;
            this.thesaurus = thesaurus;
        }

        @Override
        public void initialize(LevelMustBeProvidedIfSupportedByDevice constraintAnnotation) {
        }

        @Override
        public boolean isValid(SecurityPropertySetImpl value, ConstraintValidatorContext context) {
            return authLevelSupported(value) && encLevelSupported(value) && securitySuiteSupported(value) && requestSecurityLevelSupported(value) && responseSecurityLevelSupported(value);
        }

        private boolean encLevelSupported(SecurityPropertySetImpl value) {
            for (EncryptionDeviceAccessLevel supportedEncLevel : supportedEncryptionLevels(value)) {
                if (supportedEncLevel.getId() == value.encryptionLevelId) {
                    return true;
                }
            }
            return false;
        }

        private List<EncryptionDeviceAccessLevel> supportedEncryptionLevels(SecurityPropertySetImpl value) {
            List<EncryptionDeviceAccessLevel> levels;
            if (isAdvancedSecurity(value.getDeviceProtocol())) {
                levels = value.getSecuritySuite().getEncryptionAccessLevels();
            } else {
                levels = value.getDeviceProtocol().getEncryptionAccessLevels().stream().map(this.protocolPluggableService::adapt).collect(Collectors.toList());
            }
            return levels.isEmpty() ? Collections.singletonList(new NoEncryption()) : levels;
        }

        private boolean authLevelSupported(SecurityPropertySetImpl value) {
            for (AuthenticationDeviceAccessLevel supportedAuthLevel : supportedAuthenticationLevels(value)) {
                if (supportedAuthLevel.getId() == value.authenticationLevelId) {
                    return true;
                }
            }
            return false;
        }

        private List<AuthenticationDeviceAccessLevel> supportedAuthenticationLevels(SecurityPropertySetImpl value) {
            List<AuthenticationDeviceAccessLevel> levels;
            if (isAdvancedSecurity(value.getDeviceProtocol())) {
                levels = value.getSecuritySuite().getAuthenticationAccessLevels();
            } else {
                levels = value.getDeviceProtocol().getAuthenticationAccessLevels().stream().map(this.protocolPluggableService::adapt).collect(Collectors.toList());
            }
            return levels.isEmpty() ? Collections.singletonList(new NoAuthentication()) : levels;
        }

        private boolean securitySuiteSupported(SecurityPropertySetImpl value) {
            for (SecuritySuite securitySuite : supportedSecuritySuites(value)) {
                if (securitySuite.getId() == value.securitySuiteId) {
                    return true;
                }
            }
            return false;
        }

        private List<SecuritySuite> supportedSecuritySuites(SecurityPropertySetImpl value) {
            List<SecuritySuite> securitySuites = null;
            if (isAdvancedSecurity(value.getDeviceProtocol())) {
                securitySuites = castToAdvancedSecurity(value.getDeviceProtocol()).getSecuritySuites()
                        .stream()
                        .map(this.protocolPluggableService::adapt)
                        .collect(Collectors.toList());
            }
            return (securitySuites == null || securitySuites.isEmpty()) ? Collections.singletonList(new NoSecuritySuite()) : securitySuites;
        }

        private boolean requestSecurityLevelSupported(SecurityPropertySetImpl value) {
            for (RequestSecurityLevel requestSecurityLevel : supportedRequestSecurityLevels(value)) {
                if (requestSecurityLevel.getId() == value.requestSecurityLevelId) {
                    return true;
                }
            }
            return false;
        }

        private List<RequestSecurityLevel> supportedRequestSecurityLevels(SecurityPropertySetImpl value) {
            List<RequestSecurityLevel> levels;
            if (isAdvancedSecurity(value.getDeviceProtocol())) {
                levels = value.getSecuritySuite().getRequestSecurityLevels();
            } else {
                levels = Collections.emptyList();
            }
            return levels.isEmpty() ? Collections.singletonList(new NoRequestSecurity()) : levels;
        }

        private boolean responseSecurityLevelSupported(SecurityPropertySetImpl value) {
            for (ResponseSecurityLevel responseSecurityLevel : supportedResponseSecurityLevels(value)) {
                if (responseSecurityLevel.getId() == value.responseSecurityLevelId) {
                    return true;
                }
            }
            return false;
        }

        private List<ResponseSecurityLevel> supportedResponseSecurityLevels(SecurityPropertySetImpl value) {
            List<ResponseSecurityLevel> levels;
            if (isAdvancedSecurity(value.getDeviceProtocol())) {
                levels = value.getSecuritySuite().getResponseSecurityLevels();
            } else {
                levels = Collections.emptyList();
            }
            return levels.isEmpty() ? Collections.singletonList(new NoResponseSecurity()) : levels;
        }
    }

    static class ClientValidator implements ConstraintValidator<ValidClient, SecurityPropertySetImpl> {

        private String message;

        @Override
        public void initialize(ValidClient constraintAnnotation) {
            message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(SecurityPropertySetImpl value, ConstraintValidatorContext context) {
            if (value.getDeviceProtocol().getClientSecurityPropertySpec().isPresent()) {
                PropertySpec clientPropertySpec = UPLToConnexoPropertySpecAdapter.adaptTo(value.getDeviceProtocol().getClientSecurityPropertySpec().get());
                try {
                    if (!clientPropertySpec.validateValue(value.getClient())) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(message)
                                .addPropertyNode("client").addConstraintViolation()
                                .disableDefaultConstraintViolation();
                        return false;
                    }
                    return true;
                } catch (InvalidValueException e) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                            .addPropertyNode("client").addConstraintViolation()
                            .disableDefaultConstraintViolation();
                    return false;
                }
            }
            return true; // The device protocol doesn't support client, so no validation required
        }
    }

    @Override
    public SecurityPropertySet cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        SecurityPropertySetBuilder builder = deviceConfiguration.createSecurityPropertySet(getName());
        builder.client(getClient());
        builder.authenticationLevel(authenticationLevelId);
        builder.encryptionLevel(encryptionLevelId);
        builder.securitySuite(securitySuiteId);
        builder.requestSecurityLevel(requestSecurityLevelId);
        builder.responseSecurityLevel(responseSecurityLevelId);
        getConfigurationSecurityProperties().forEach(each -> builder.addConfigurationSecurityProperty(each.getName(), each.getSecurityAccessorType()));
        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SecurityPropertySetImpl) {
            SecurityPropertySetImpl impl = (SecurityPropertySetImpl) obj;
            return impl.getId() == this.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
