package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.security.Principal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides an implemention for the {@link SecurityPropertySet} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-14 (11:09)
 */
public class SecurityPropertySetImpl extends PersistentNamedObject<SecurityPropertySet> implements SecurityPropertySet, PersistenceAware {

    private Reference<DeviceCommunicationConfiguration> deviceCommunicationConfiguration = ValueReference.absent();
    private DeviceProtocol deviceProtocol;
    private int authenticationLevelId;
    private AuthenticationDeviceAccessLevel authenticationLevel;
    private int encryptionLevelId;
    private EncryptionDeviceAccessLevel encryptionLevel;
    private Set<DeviceSecurityUserAction> userActions = EnumSet.noneOf(DeviceSecurityUserAction.class);
    private List<UserActionRecord> userActionRecords = new ArrayList<>();
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    protected CreateEventType createEventType() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    protected UpdateEventType updateEventType() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    protected void doDelete() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    protected void validateDelete() {
        //TODO automatically generated method body, provide implementation.

    }

    static class UserActionRecord {
        private DeviceSecurityUserAction userAction;
        private Reference<SecurityPropertySet> set = ValueReference.absent();

        UserActionRecord() {
        }

        UserActionRecord(SecurityPropertySet set, DeviceSecurityUserAction userAction) {
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
    public SecurityPropertySetImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, UserService userService, DeviceConfigurationService deviceConfigurationService) {
        super(SecurityPropertySet.class, dataModel, eventService, thesaurus);
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    static SecurityPropertySetImpl from(DataModel dataModel, DeviceCommunicationConfigurationImpl deviceCommunicationConfiguration) {
        return dataModel.getInstance(SecurityPropertySetImpl.class).init(deviceCommunicationConfiguration);
    }

    private SecurityPropertySetImpl init(DeviceCommunicationConfiguration deviceCommunicationConfiguration) {
        this.deviceCommunicationConfiguration.set(deviceCommunicationConfiguration);
        return this;
    }

//    public SecurityPropertySetImpl() {
//        super();
//    }
//    protected SecurityPropertySetImpl(int id, DeviceCommunicationConfiguration deviceCommunicationConfiguration) {
//        super(id);
//        this.setDeviceCommunicationConfiguration(deviceCommunicationConfiguration);
//    }
//
//    protected SecurityPropertySetImpl(ResultSet resultSet) throws SQLException {
//        super();
//        this.doLoad(resultSet);
//    }
//
//    protected SecurityPropertySetImpl(ResultSet resultSet, DeviceCommunicationConfiguration deviceCommunicationConfiguration) throws SQLException {
//        super();
//        this.setDeviceCommunicationConfiguration(deviceCommunicationConfiguration);
//        this.doLoad(resultSet);
//    }

//    @Override
//    protected int bindBody(PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
//        int parameterNumber = firstParameterNumber;
//        preparedStatement.setInt(parameterNumber++, this.deviceCommunicationConfigurationId);
//        preparedStatement.setInt(parameterNumber++, this.authenticationLevelId);
//        preparedStatement.setInt(parameterNumber++, this.encryptionLevelId);
//        return parameterNumber;
//    }
//
//    @Override
//    protected void doLoad(ResultSet resultSet) throws SQLException {
//        super.doLoad(resultSet);
//        this.doLoad(ResultSetIterator.newForPersistentNamedObject(resultSet));
//    }
//
//    private void doLoad(ResultSetIterator resultSet) throws SQLException {
//        int deviceConfigurationId = resultSet.nextInt();
//        if (this.deviceCommunicationConfiguration == null) {
//            this.deviceCommunicationConfigurationId = deviceConfigurationId;
//        }
//        this.authenticationLevelId = resultSet.nextInt();
//        this.encryptionLevelId = resultSet.nextInt();
//    }

//    @Override
//    public String getType () {
//        return SecurityPropertySet.class.getName();
//    }
//
//    public void init(final SecurityPropertySetShadow shadow) throws BusinessException, SQLException {
//        this.execute(new Transaction<Void>() {
//            public Void doExecute() throws BusinessException, SQLException {
//                doInit(shadow);
//                return null;
//            }
//        });
//    }
//
//    public void doInit(SecurityPropertySetShadow shadow) throws BusinessException, SQLException {
//        this.validateNew(shadow);
//        this.copyNew(shadow);
//        this.postNew();
//        this.doUserActionCrud(shadow.getUserActions());
//        this.created();
//    }
//
//    private void doUserActionCrud(Set<DeviceSecurityUserAction> userActions) throws SQLException {
//        this.deleteAllUserActions();
//        this.createUserActions(userActions);
//    }

//    private void deleteAllUserActions() throws SQLException {
//        SqlBuilder sqlBuilder = new SqlBuilder("delete from mdcsecuritypropsetuseraction where securitypropertyset = ?");
//        sqlBuilder.bindInt(this.getId());
//        try (PreparedStatement preparedStatement = sqlBuilder.getStatement(Environment.DEFAULT.get().getConnection())) {
//            preparedStatement.executeUpdate();
//            // Don't care about the number of rows that were effectively deleted.
//        }
//    }
//
//    private void createUserActions(Set<DeviceSecurityUserAction> userActions) throws SQLException {
//        BatchStatement batchStatement = null;
//        try {
//            batchStatement = new BatchStatement("insert into mdcsecuritypropsetuseraction (securitypropertyset, useraction) values (?, ?)", userActions.size());
//            for (DeviceSecurityUserAction userAction : userActions) {
//                batchStatement.setInt(1, this.getId());
//                batchStatement.setInt(2, userAction.ordinal());
//                batchStatement.addBatch();
//            }
//            batchStatement.executeBatch();
//        } finally {
//            if (batchStatement != null) {
//                batchStatement.close();
//            }
//        }
//    }

//    private void validateNew(SecurityPropertySetShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }
//
//    private void validate(SecurityPropertySetShadow shadow) throws BusinessException {
//        String name = shadow.getName();
//        this.validate(name);
//        this.validateConstraint(name);
//        this.validateAuthenticationLevel(shadow);
//        this.validateEncryptionLevel(shadow);
//    }
//
//    private void validateAuthenticationLevel(SecurityPropertySetShadow shadow) throws BusinessException {
//        List<AuthenticationDeviceAccessLevel> levels = this.getDeviceProtocol().getAuthenticationAccessLevels();
//        if (levels.isEmpty()) {
//            if (shadow.getAuthenticationLevelId() != DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
//                this.unsupportedAuthenticationLevel(shadow);
//            }
//        } else {
//            AuthenticationDeviceAccessLevel authenticationLevel = this.findAuthenticationLevel(shadow.getAuthenticationLevelId());
//            if (authenticationLevel.getId() == DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
//                unsupportedAuthenticationLevel(shadow);
//            }
//        }
//    }
//
//    private void unsupportedAuthenticationLevel(SecurityPropertySetShadow shadow) throws BusinessException {
//        throw new BusinessException(
//                "deviceProtocolXDoesNotSupportAuthenticationLevelY",
//                "The device procotol '{0}' does not support authentication level {1,number}",
//                this.getDeviceProtocolPluggableClass().getName(),
//                shadow.getAuthenticationLevelId());
//    }
//
//    private void validateEncryptionLevel(SecurityPropertySetShadow shadow) throws BusinessException {
//        List<EncryptionDeviceAccessLevel> levels = this.getDeviceProtocol().getEncryptionAccessLevels();
//        if (levels.isEmpty()) {
//            if (shadow.getEncryptionLevelId() != DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
//                this.unsupportedEncryptionLevel(shadow);
//            }
//        } else {
//            EncryptionDeviceAccessLevel encryptionLevel = this.findEncryptionLevel(shadow.getEncryptionLevelId());
//            if (encryptionLevel.getId() == DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
//                unsupportedEncryptionLevel(shadow);
//            }
//        }
//    }
//
//    private void unsupportedEncryptionLevel(SecurityPropertySetShadow shadow) throws BusinessException {
//        throw new BusinessException(
//                "deviceProtocolXDoesNotSupportEncryptionLevelY",
//                "The device procotol '{0}' does not support encryption level {1,number}",
//                this.getDeviceProtocolPluggableClass().getName(),
//                shadow.getEncryptionLevelId());
//    }
//
//    @Override
//    protected void validateConstraint(String newName) throws DuplicateException {
//        SecurityPropertySet propertySetWithTheSameName = this.newFactoryInstance().find(newName, this.getDeviceCommunicationConfiguration());
//        if (propertySetWithTheSameName != null) {
//            if (this.getId() != propertySetWithTheSameName.getId()) {
//                throw new DuplicateException("duplicateSecurityPropertySetX",
//                        "A security set with the name '{0}' already exists (id={1,number}) for the device configuration '{2}'",
//                        newName, propertySetWithTheSameName.getId(), getDeviceCommunicationConfiguration().getDeviceConfiguration().getName());
//            }
//        }
//    }
//
//    private void copyNew(SecurityPropertySetShadow shadow) {
//        this.copy(shadow);
//    }
//
//    private void copy(SecurityPropertySetShadow shadow) {
//        this.setName(shadow.getName());
//        this.authenticationLevel = null;
//        this.authenticationLevelId = shadow.getAuthenticationLevelId();
//        this.encryptionLevel = null;
//        this.encryptionLevelId = shadow.getEncryptionLevelId();
//    }
//
//    private void doUpdate(SecurityPropertySetShadow shadow) throws BusinessException, SQLException {
//        this.validateUpdate(shadow);
//        this.copyUpdate(shadow);
//        this.post();
//        this.doUserActionCrud(shadow.getUserActions());
//        this.updated();
//    }
//
//    private void validateUpdate(SecurityPropertySetShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }
//
//    private void copyUpdate(SecurityPropertySetShadow shadow) {
//        this.copy(shadow);
//    }
//
//    @Override
//    protected void validateDelete() throws SQLException, BusinessException {
//        super.validateDelete();
//        validateUsageInComTaskEnablements();
//        validateUsageOnDevice();
//    }
//
//    private void validateUsageOnDevice() throws BusinessException {
//        if (this.isUsedOnDevices()) {
//            throw new BusinessException("securityPropertySetXIsStillUsedByDevices",
//                    "The security property set '{0}' is still used by at least one device",
//                    new Object[]{getName()});
//        }
//    }

//    @Override
//    public boolean isUsedOnDevices () {
//        DeviceProtocol protocol = getDeviceProtocol();
//        if (protocol != null) {
//            SecurityPropertySetRelationSupport relationSupport = new SecurityPropertySetRelationSupport(protocol, this);
//            return relationSupport.hasValues();
//        }
//        else {
//            return false;
//        }
//    }

//    private void validateUsageInComTaskEnablements() throws BusinessException {
//        final List<ComTaskEnablement> allComTaskEnablementsForSecPropSet = ManagerFactory.getCurrent().getComTaskEnablementFactory().findBySecurityPropertySet(this);
//        if (!allComTaskEnablementsForSecPropSet.isEmpty()) {
//            throw new BusinessException("securityPropertySetXIsStillUsedByComTaskEnablements",
//                    "The security property set '{0}' is still used by the comtaskenablements with ComTask : '{1}'",
//                    new Object[]{getName(), getComTaskEnablementNames(allComTaskEnablementsForSecPropSet)});
//        }
//    }

//    private String getComTaskEnablementNames(List<ComTaskEnablement> allComTaskEnablementsForSecPropSet) {
//        final String separator = " - ";
//        StringBuilder stringBuilder = new StringBuilder(allComTaskEnablementsForSecPropSet.size());
//        for (ComTaskEnablement comTaskEnablement : allComTaskEnablementsForSecPropSet) {
//            stringBuilder.append(comTaskEnablement.getComTask().getName());
//            stringBuilder.append(separator);
//        }
//        final String comTaskNames = stringBuilder.toString();
//        return comTaskNames.substring(0, comTaskNames.lastIndexOf(separator));
//    }

//    @Override
//    protected void deleteDependents() throws SQLException, BusinessException {
//        super.deleteDependents();
//        this.deleteAllUserActions();
//    }

    @Override
    public List<RelationType> getAvailableRelationTypes() {
//TODO        return this.getMdwInterface().getRelationTypeFactory().findByParticipant(this);
        return Collections.emptyList();
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete) {
        return attrib.getRelations(this, date, includeObsolete, 0, 0);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete, int fromRow, int toRow) {
        return attrib.getRelations(this, date, includeObsolete, fromRow, toRow);
    }

    @Override
    public List<Relation> getAllRelations(RelationAttributeType attrib) {
        return attrib.getAllRelations(this);
    }

    @Override
    public List<Relation> getRelations(RelationAttributeType attrib, Interval period, boolean includeObsolete) {
        return attrib.getRelations(this, period, includeObsolete);
    }

    @Override
    public AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel() {
        if (this.authenticationLevel == null) {
            if (this.authenticationLevelId == DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
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
            if (this.encryptionLevelId == DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID) {
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
        DeviceType deviceType = getDeviceCommunicationConfiguration().getDeviceConfiguration().getDeviceType();
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

//    protected SecurityPropertySetFactory newFactoryInstance() {
//        return ManagerFactory.getCurrent().getSecurityPropertySetFactory();
//    }

    @Override
    public DeviceConfiguration getDeviceCommunicationConfiguration() {
        return this.deviceCommunicationConfiguration.get().getDeviceConfiguration();
    }

//    private DeviceCommunicationConfiguration findDeviceConfiguration(int deviceConfigurationId) {
//        return this.getDeviceCommunicationConfigurationFactory().find(deviceConfigurationId);
//    }
//
//    private DeviceCommunicationConfigurationFactory getDeviceCommunicationConfigurationFactory() {
//        return ManagerFactory.getCurrent().getDeviceCommunicationConfigurationFactory();
//    }

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

    private Set<DeviceSecurityUserAction> fetch(ResultSet resultSet) throws SQLException {
        Set<DeviceSecurityUserAction> userActions = EnumSet.noneOf(DeviceSecurityUserAction.class);
        while (resultSet.next()) {
            userActions.add(DeviceSecurityUserAction.values()[resultSet.getInt(1)]);
        }
        return userActions;
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
        Optional<Privilege> privilege = ((DeviceConfigurationServiceImpl) deviceConfigurationService).findPrivilege(action);
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

//    private MdwInterface getMdwInterface() {
//        return ManagerFactory.getCurrent().getMdwInterface();
//    }
//
//    public SecurityPropertySetShadow getShadow() {
//        return new SecurityPropertySetShadow(this);
//    }

//    @Override
//    protected String[] getColumns() {
//        return SecurityPropertySetFactoryImpl.COLUMNS;
//    }

//    @Override
//    protected String getTableName() {
//        return SecurityPropertySetFactoryImpl.TABLENAME;
//    }
//
    private class NoAuthentication implements AuthenticationDeviceAccessLevel {
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

    private class NoEncryption implements EncryptionDeviceAccessLevel {
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

}
