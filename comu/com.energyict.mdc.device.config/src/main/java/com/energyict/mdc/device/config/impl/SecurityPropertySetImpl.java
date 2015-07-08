package com.energyict.mdc.device.config.impl;

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
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.exceptions.CannotDeleteSecurityPropertySetWhileInUseException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import java.time.Instant;
import java.util.Optional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import com.google.common.collect.Range;
import org.hibernate.validator.constraints.NotEmpty;

import static com.energyict.mdc.protocol.api.security.DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;

/**
 * Provides an implementation for the {@link SecurityPropertySet} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-14 (11:09)
 */
@LevelMustBeProvidedIfSupportedByDevice(groups = {Save.Create.class, Save.Update.class})
public class SecurityPropertySetImpl extends PersistentNamedObject<SecurityPropertySet> implements ServerSecurityPropertySet, PersistenceAware {

    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    private String name;
    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private DeviceProtocol deviceProtocol;
    private int authenticationLevelId;
    private AuthenticationDeviceAccessLevel authenticationLevel;
    private int encryptionLevelId;
    private EncryptionDeviceAccessLevel encryptionLevel;
    private Set<DeviceSecurityUserAction> userActions = EnumSet.noneOf(DeviceSecurityUserAction.class);
    private List<UserActionRecord> userActionRecords = new ArrayList<>();
    private final ThreadPrincipalService threadPrincipalService;
    private final DeviceConfigurationService deviceConfigurationService;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

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
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.SECURITY_PROPERTY_SET;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(SecurityPropertySet.class).remove(this);
    }

    @Override
    protected void validateDelete() {
        List<ComTaskEnablement> comTaskEnablements = dataModel.mapper(ComTaskEnablement.class).find(ComTaskEnablementImpl.Fields.SECURITY_PROPERTY_SET.fieldName(), this);
        if (!comTaskEnablements.isEmpty()) {
            throw new CannotDeleteSecurityPropertySetWhileInUseException(this.getThesaurus(), this);
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
            }
            else {
                return other.get();
            }
        }
        else {
            return null;
        }
    }

    static class UserActionRecord {
        private DeviceSecurityUserAction userAction;
        private Reference<SecurityPropertySet> set = ValueReference.absent();
        private String userName;
        private long version;
        private Instant createTime;
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
        for (UserActionRecord userActionRecord : userActionRecords) {
            userActions.add(userActionRecord.userAction);
        }
    }

    @Inject
    public SecurityPropertySetImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, DeviceConfigurationService deviceConfigurationService) {
        super(SecurityPropertySet.class, dataModel, eventService, thesaurus);
        this.threadPrincipalService = threadPrincipalService;
        this.deviceConfigurationService = deviceConfigurationService;
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
    public List<Relation> getRelations(RelationAttributeType attrib, Instant date, boolean includeObsolete) {
        return attrib.getRelations(this, date, includeObsolete, 0, 0);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Instant date, boolean includeObsolete, int fromRow, int toRow) {
        return attrib.getRelations(this, date, includeObsolete, fromRow, toRow);
    }

    @Override
    public List<Relation> getAllRelations(RelationAttributeType attrib) {
        return attrib.getAllRelations(this);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Range<Instant> period, boolean includeObsolete) {
        return attrib.getRelations(this, period, includeObsolete);
    }

    @Override
    public AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel() {
        if (this.authenticationLevel == null) {
            if (this.authenticationLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.authenticationLevel = new NoAuthentication();
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
     *         does not have an AuthenticationDeviceAccessLevel with the specified id
     */
    private AuthenticationDeviceAccessLevel findAuthenticationLevel(int id) {
        List<AuthenticationDeviceAccessLevel> levels = this.getDeviceProtocol().getAuthenticationAccessLevels();
        for (AuthenticationDeviceAccessLevel level : levels) {
            if (id == level.getId()) {
                return level;
            }
        }
        return new NoAuthentication();
    }

    @Override
    public EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel() {
        if (this.encryptionLevel == null) {
            if (this.encryptionLevelId == NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
                this.encryptionLevel = new NoEncryption();
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
     *         does not have an EncryptionDeviceAccessLevel with the specified id
     */
    private EncryptionDeviceAccessLevel findEncryptionLevel(int id) {
        List<EncryptionDeviceAccessLevel> levels = this.getDeviceProtocol().getEncryptionAccessLevels();
        for (EncryptionDeviceAccessLevel level : levels) {
            if (id == level.getId()) {
                return level;
            }
        }
        return new NoEncryption();
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
        DeviceProtocolPluggableClass protocolClass = deviceType.getDeviceProtocolPluggableClass();
        if (protocolClass != null) {
            return protocolClass;
        } else { // DeviceType's Collection Method should be DeviceCollectionMethodType.HEADEND_SYSTEM, else we have a problem...
//            if (deviceType.getDeviceCollectionMethodType() == DeviceCollectionMethodType.COMSERVER) {
//                throw new ApplicationException(String.format("Protocol not defined for Device Type \"%s\"", deviceType.getName()));
//            }
            //throw CodingException.unsupportedMethod(this.getClass(), "getDeviceProtocolPluggableClass");
            throw new UnsupportedOperationException();
        }
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
        return new HashSet<>(result.values());
    }

    private void addMissingSecurityPropertiesAndAvoidDuplicates(Map<String, PropertySpec> result, List<PropertySpec> securityProperties) {
        for (PropertySpec propertySpec : securityProperties) {
            if (!result.containsKey(propertySpec.getName())) {
                result.put(propertySpec.getName(), propertySpec);
            }
        }
    }

    @Override
    public Set<DeviceSecurityUserAction> getUserActions() {
        return this.userActions;
    }

    @Override
    public void addUserAction(DeviceSecurityUserAction userAction) {
        boolean changed = userActions.add(userAction);
        if (changed) {
            userActionRecords.add(new UserActionRecord(this, userAction));
        }
    }

    @Override
    public void removeUserAction(DeviceSecurityUserAction userAction) {
        boolean changed = userActions.remove(userAction);
        if (changed) {
            for (Iterator<UserActionRecord> iterator = userActionRecords.iterator(); iterator.hasNext(); ) {
                if (iterator.next().userAction.equals(userAction)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public boolean currentUserIsAllowedToEditDeviceProperties() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return false;
        }
        User user = (User) principal;
        Set<DeviceSecurityUserAction> deviceSecurityUserActions = this.getUserActions();
        for (DeviceSecurityUserAction deviceSecurityUserAction : deviceSecurityUserActions) {
            if (editingIsAuthorizedFor(deviceSecurityUserAction, user)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean currentUserIsAllowedToViewDeviceProperties() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return false;
        }
        User user = (User) principal;
        Set<DeviceSecurityUserAction> deviceSecurityUserActions = this.getUserActions();
        for (DeviceSecurityUserAction deviceSecurityUserAction : deviceSecurityUserActions) {
            if (viewingIsAuthorizedFor(deviceSecurityUserAction, user)) {
                return true;
            }
        }
        return false;
    }

    public boolean viewingIsAuthorizedFor (DeviceSecurityUserAction action, User user) {
        return action.isViewing() && this.isAuthorized(action, user);
    }

    private boolean isAuthorized(DeviceSecurityUserAction action, User user) {
        Optional<Privilege> privilege = ((DeviceConfigurationServiceImpl) deviceConfigurationService).findPrivilege(action.getPrivilege());
        return privilege.isPresent() && user.hasPrivilege(privilege.get());
    }

    public boolean editingIsAuthorizedFor (DeviceSecurityUserAction action, User user) {
        return action.isEditing() && this.isAuthorized(action, user);
    }

    public boolean isExecutableIsAuthorizedFor(DeviceSecurityUserAction action, User user){
        return action.isExecutable() && this.isAuthorized(action, user);
    }

    @Override
    public void setAuthenticationLevel(int authenticationLevelId) {
        this.authenticationLevelId = authenticationLevelId;

    }

    @Override
    public void setEncryptionLevelId(int encryptionLevelId) {
        this.encryptionLevelId = encryptionLevelId;
    }

    private static class NoAuthentication implements AuthenticationDeviceAccessLevel {
        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslationKey() {
            return "NoAuthenthication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    private static class NoEncryption implements EncryptionDeviceAccessLevel {
        @Override
        public int getId() {
            return NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslationKey() {
            return "NoEncryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        protected String getInvalidCharacters() {
            return "./";
        }
    }

    @Override
    public void update() {
        dataModel.mapper(SecurityPropertySet.class).update(this);
    }

    public static class LevelsAreSupportedValidator implements ConstraintValidator<LevelMustBeProvidedIfSupportedByDevice, SecurityPropertySetImpl> {

        @Override
        public void initialize(LevelMustBeProvidedIfSupportedByDevice constraintAnnotation) {
        }

        @Override
        public boolean isValid(SecurityPropertySetImpl value, ConstraintValidatorContext context) {
            return authLevelSupported(value) && encLevelSupported(value);
        }

        private boolean encLevelSupported(SecurityPropertySetImpl value) {
            for (EncryptionDeviceAccessLevel supportedEncLevel : supportedEncryptionlevels(value)) {
                if (supportedEncLevel.getId() == value.encryptionLevelId) {
                    return true;
                }
            }
            return false;
        }

        private List<EncryptionDeviceAccessLevel> supportedEncryptionlevels(SecurityPropertySetImpl value) {
            List<EncryptionDeviceAccessLevel> levels = value.getDeviceProtocol().getEncryptionAccessLevels();
            return levels.isEmpty() ? Arrays.<EncryptionDeviceAccessLevel>asList(new NoEncryption()) : levels;
        }

        private boolean authLevelSupported(SecurityPropertySetImpl value) {
            for (AuthenticationDeviceAccessLevel supportedAuthLevel : supportedAutheticationLevels(value)) {
                if (supportedAuthLevel.getId() == value.authenticationLevelId) {
                    return true;
                }
            }
            return false;
        }

        private List<AuthenticationDeviceAccessLevel> supportedAutheticationLevels(SecurityPropertySetImpl value) {
            List<AuthenticationDeviceAccessLevel> levels = value.getDeviceProtocol().getAuthenticationAccessLevels();
            return levels.isEmpty() ? Arrays.<AuthenticationDeviceAccessLevel>asList(new NoAuthentication()) : levels;
        }
    }

    @Override
    public SecurityPropertySet cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        SecurityPropertySetBuilder builder = deviceConfiguration.createSecurityPropertySet(getName());
        builder.authenticationLevel(authenticationLevelId);
        builder.encryptionLevel(encryptionLevelId);
        getUserActions().stream().forEach(builder::addUserAction);
        return builder.build();
    }
}
