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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.KeyAccessorPropertySpecWithPossibleValues;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.exceptions.CannotDeleteSecurityPropertySetWhileInUseException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AdvancedDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.protocol.api.security.DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Provides an implementation for the {@link SecurityPropertySet} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-14 (11:09)
 */
@LevelMustBeProvidedIfSupportedByDevice(groups = {Save.Create.class, Save.Update.class})
//Do not remove the public access modifier: CXO-2786
public class SecurityPropertySetImpl extends PersistentNamedObject<SecurityPropertySet> implements ServerSecurityPropertySet, PersistenceAware {

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private DeviceProtocol deviceProtocol;
    private int authenticationLevelId;
    private AuthenticationDeviceAccessLevel authenticationLevel;
    private int encryptionLevelId;
    private EncryptionDeviceAccessLevel encryptionLevel;
    private int securitySuiteId = -1;
    private SecuritySuite securitySuite;
    private int requestSecurityLevelId = -1;
    private RequestSecurityLevel requestSecurityLevel;
    private int responseSecurityLevelId = -1;
    private ResponseSecurityLevel responseSecurityLevel;
    private Set<DeviceSecurityUserAction> userActions = EnumSet.noneOf(DeviceSecurityUserAction.class);
    private List<UserActionRecord> userActionRecords = new ArrayList<>();
    private final ThreadPrincipalService threadPrincipalService;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public SecurityPropertySetImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus,
                                   ThreadPrincipalService threadPrincipalService) {
        super(SecurityPropertySet.class, dataModel, eventService, thesaurus);
        this.threadPrincipalService = threadPrincipalService;
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
        private DeviceSecurityUserAction userAction;
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

        UserActionRecord(SecurityPropertySet set, DeviceSecurityUserAction userAction) {
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
    public AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel() {
        if (this.authenticationLevel == null) {
            if (this.authenticationLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.authenticationLevel = new NoAuthentication(getThesaurus());
            } else {
                this.authenticationLevel = this.findAuthenticationLevel(this.authenticationLevelId);
            }
        }
        return this.authenticationLevel;
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
        List<AuthenticationDeviceAccessLevel> levels = this.getDeviceProtocol().getAuthenticationAccessLevels();
        for (AuthenticationDeviceAccessLevel level : levels) {
            if (id == level.getId()) {
                return level;
            }
        }
        return new NoAuthentication(getThesaurus());
    }

    @Override
    public EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel() {
        if (this.encryptionLevel == null) {
            if (this.encryptionLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.encryptionLevel = new NoEncryption(getThesaurus());
            } else {
                this.encryptionLevel = this.findEncryptionLevel(this.encryptionLevelId);
            }
        }
        return this.encryptionLevel;
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
        List<EncryptionDeviceAccessLevel> levels = this.getDeviceProtocol().getEncryptionAccessLevels();
        for (EncryptionDeviceAccessLevel level : levels) {
            if (id == level.getId()) {
                return level;
            }
        }
        return new NoEncryption(getThesaurus());
    }

    @Override
    public SecuritySuite getSecuritySuite() {
        if (this.securitySuite == null) {
            if (this.securitySuiteId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.securitySuite = new NoSecuritySuite(getThesaurus());
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
        if (this.getDeviceProtocol() instanceof AdvancedDeviceProtocolSecurityCapabilities) {
            List<SecuritySuite> securitySuites = ((AdvancedDeviceProtocolSecurityCapabilities) this.getDeviceProtocol()).getSecuritySuites();
            for (SecuritySuite suite : securitySuites) {
                if (id == suite.getId()) {
                    return suite;
                }
            }
        }
        return new NoSecuritySuite(getThesaurus());
    }

    @Override
    public RequestSecurityLevel getRequestSecurityLevel() {
        if (this.requestSecurityLevel == null) {
            if (this.requestSecurityLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.requestSecurityLevel = new NoRequestSecurity(getThesaurus());
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
        if (this.getDeviceProtocol() instanceof AdvancedDeviceProtocolSecurityCapabilities) {
            List<RequestSecurityLevel> requestSecurityLevels = ((AdvancedDeviceProtocolSecurityCapabilities) this.getDeviceProtocol()).getRequestSecurityLevels();
            for (RequestSecurityLevel level : requestSecurityLevels) {
                if (id == level.getId()) {
                    return level;
                }
            }
        }
        return new NoRequestSecurity(getThesaurus());
    }

    @Override
    public ResponseSecurityLevel getResponseSecurityLevel() {
        if (this.responseSecurityLevel == null) {
            if (this.responseSecurityLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.responseSecurityLevel = new NoResponseSecurity(getThesaurus());
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
        if (this.getDeviceProtocol() instanceof AdvancedDeviceProtocolSecurityCapabilities) {
            List<ResponseSecurityLevel> responseSecurityLevels = ((AdvancedDeviceProtocolSecurityCapabilities) this.getDeviceProtocol()).getResponseSecurityLevels();
            for (ResponseSecurityLevel level : responseSecurityLevels) {
                if (id == level.getId()) {
                    return level;
                }
            }
        }
        return new NoResponseSecurity(getThesaurus());
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
        DeviceType deviceType = getDeviceConfiguration().getDeviceType();
        return deviceType.getDeviceProtocolPluggableClass().orElseThrow(UnsupportedOperationException::new);
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public Set<PropertySpec> getPropertySpecs() {
        Map<String, PropertySpec> result = new HashMap<>();
        this.addMissingSecurityPropertiesAndAvoidDuplicates(result, this.findAuthenticationLevel(this.authenticationLevelId).getSecurityProperties());
        this.addMissingSecurityPropertiesAndAvoidDuplicates(result, this.findEncryptionLevel(this.encryptionLevelId).getSecurityProperties());

        return result.values()
                .stream()
                .map(ps -> KeyAccessorPropertySpecWithPossibleValues.addValuesIfApplicable(() -> this.getDeviceConfiguration().getDeviceType().getKeyAccessorTypes(), ps))
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
    public void setAuthenticationLevelId(int authenticationLevelId) {
        this.authenticationLevelId = authenticationLevelId;

    }

    @Override
    public void setEncryptionLevelId(int encryptionLevelId) {
        this.encryptionLevelId = encryptionLevelId;
    }

    @Override
    public void setSecuritySuiteId(int securitySuiteId) {
        this.securitySuiteId = securitySuiteId;
    }

    @Override
    public void setRequestSecurityLevelId(int requestSecurityLevelId) {
        this.requestSecurityLevelId = requestSecurityLevelId;
    }

    @Override
    public void setResponseSecurityLevelId(int responseSecurityLevelId) {
        this.responseSecurityLevelId = responseSecurityLevelId;
    }

    private static class NoAuthentication implements AuthenticationDeviceAccessLevel {

        private final Thesaurus thesaurus;

        private NoAuthentication(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.NO_AUTHENTICATION).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoAuthentication) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
    }

    private static class NoEncryption implements EncryptionDeviceAccessLevel {
        private final Thesaurus thesaurus;

        private NoEncryption(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.NO_ENCRYPTION).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoEncryption) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
    }

    private static class NoSecuritySuite implements SecuritySuite {

        private final Thesaurus thesaurus;

        public NoSecuritySuite(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.NO_SECURITY_SUITE).format();
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
    }

    private static class NoRequestSecurity implements RequestSecurityLevel {

        private final Thesaurus thesaurus;

        public NoRequestSecurity(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.NO_REQUEST_SECURITY).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoRequestSecurity) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
    }

    private static class NoResponseSecurity implements ResponseSecurityLevel {

        private final Thesaurus thesaurus;

        public NoResponseSecurity(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.NO_RESPONSE_SECURITY).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && ((NoResponseSecurity) o).getId() == NOT_USED_DEVICE_ACCESS_LEVEL_ID;
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

        private final Thesaurus thesaurus;

        @Inject
        LevelsAreSupportedValidator(Thesaurus thesaurus) {
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
            if (value.getDeviceProtocol() instanceof AdvancedDeviceProtocolSecurityCapabilities) {
                levels = value.getSecuritySuite().getEncryptionAccessLevels();
            } else {
                levels = value.getDeviceProtocol().getEncryptionAccessLevels();
            }
            return levels.isEmpty() ? Collections.singletonList(new NoEncryption(thesaurus)) : levels;
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
            if (value.getDeviceProtocol() instanceof AdvancedDeviceProtocolSecurityCapabilities) {
                levels = value.getSecuritySuite().getAuthenticationAccessLevels();
            } else {
                levels = value.getDeviceProtocol().getAuthenticationAccessLevels();
            }
            return levels.isEmpty() ? Collections.singletonList(new NoAuthentication(thesaurus)) : levels;
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
            if (value.getDeviceProtocol() instanceof AdvancedDeviceProtocolSecurityCapabilities) {
                securitySuites = ((AdvancedDeviceProtocolSecurityCapabilities) value.getDeviceProtocol()).getSecuritySuites();
            }
            return (securitySuites == null || securitySuites.isEmpty()) ? Collections.singletonList(new NoSecuritySuite(thesaurus)) : securitySuites;
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
            if (value.getDeviceProtocol() instanceof AdvancedDeviceProtocolSecurityCapabilities) {
                levels = value.getSecuritySuite().getRequestSecurityLevels();
            } else {
                levels = Collections.emptyList();
            }
            return levels.isEmpty() ? Collections.singletonList(new NoRequestSecurity(thesaurus)) : levels;
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
            if (value.getDeviceProtocol() instanceof AdvancedDeviceProtocolSecurityCapabilities) {
                levels = value.getSecuritySuite().getResponseSecurityLevels();
            } else {
                levels = Collections.emptyList();
            }
            return levels.isEmpty() ? Collections.singletonList(new NoResponseSecurity(thesaurus)) : levels;
        }
    }

    @Override
    public SecurityPropertySet cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        SecurityPropertySetBuilder builder = deviceConfiguration.createSecurityPropertySet(getName());
        builder.authenticationLevel(authenticationLevelId);
        builder.encryptionLevel(encryptionLevelId);
        builder.securitySuite(securitySuiteId);
        builder.requestSecurityLevel(requestSecurityLevelId);
        builder.responseSecurityLevel(responseSecurityLevelId);
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
