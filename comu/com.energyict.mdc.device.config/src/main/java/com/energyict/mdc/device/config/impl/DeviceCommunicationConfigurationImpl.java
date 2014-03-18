package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ShadowList;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialOutboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.exceptions.PartialConnectionTaskDoesNotExist;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link com.energyict.mdc.device.config.DeviceCommunicationConfiguration} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-15 (11:02)
 */
public class DeviceCommunicationConfigurationImpl extends PersistentIdObject<DeviceCommunicationConfiguration> implements ServerDeviceCommunicationConfiguration {

    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
//    private List<SecurityPropertySet> securityPropertySets;
//    private List<ComTaskEnablement> comTaskEnablements;
    private boolean supportsAllMessageCategories;
    private long userActions; // temp place holder for the enumset
//    private EnumSet<DeviceMessageUserAction> userActions = EnumSet.noneOf(DeviceMessageUserAction.class);
//    private List<DeviceMessageEnablement> deviceMessageEnablements;
    private List<PartialConnectionTask> partialConnectionTasks = new ArrayList<>();
    private List<ProtocolDialectConfigurationProperties> configurationPropertiesList = new ArrayList<>();

    @Inject
    DeviceCommunicationConfigurationImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(DeviceCommunicationConfiguration.class, dataModel, eventService, thesaurus);
    }

    static DeviceCommunicationConfigurationImpl from(DataModel dataModel, DeviceConfiguration deviceConfiguration) {
        return dataModel.getInstance(DeviceCommunicationConfigurationImpl.class).init(deviceConfiguration);
    }

//    public DeviceCommunicationConfigurationImpl(DeviceConfiguration deviceConfiguration, int id) {
//        super(id);
//        this.deviceConfiguration = deviceConfiguration;
//        this.deviceConfigurationId = (int) deviceConfiguration.getId();
//    }
//
//    public DeviceCommunicationConfigurationImpl(DeviceConfiguration deviceConfiguration, ResultSet resultSet) throws SQLException {
//        this.deviceConfiguration = deviceConfiguration;
//        this.doLoad(resultSet);
//    }

//    @Override
//    protected void doLoad(ResultSet resultSet) throws SQLException {
//        super.doLoad(resultSet);
//        this.doLoad(ResultSetIterator.newForPersistentIdObject(resultSet));
//    }
//
//    private void doLoad(ResultSetIterator resultSet) throws SQLException {
//        this.deviceConfigurationId = resultSet.nextInt();
//        this.supportsAllMessageCategories = resultSet.nextInt() != 0;
//        this.userActions = this.userActionsFromDatabaseValue(resultSet.nextLong());
//    }
//
//    private EnumSet<DeviceMessageUserAction> userActionsFromDatabaseValue(long userActions) {
//        return new DeviceMessageUserActionSetValueFactory().fromDatabaseValue(userActions);
//    }
//
//    @Override
//    protected int bindBody(PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
//        int parameterNumber = firstParameterNumber;
//        preparedStatement.setInt(parameterNumber++, this.deviceConfigurationId);
//        preparedStatement.setInt(parameterNumber++, this.toBoolean(this.supportsAllMessageCategories));
//        preparedStatement.setLong(parameterNumber++, this.userActionsToDatabaseValue());
//        return parameterNumber;
//    }

//    private long userActionsToDatabaseValue() {
//        return new DeviceMessageUserActionSetValueFactory().toDatabaseValue(this.userActions);
//    }

//    public void init(final DeviceCommunicationConfigurationShadow shadow) throws SQLException, BusinessException {
//        this.execute(new Transaction<Void>() {
//            public Void doExecute() throws BusinessException, SQLException {
//                doInit(shadow);
//                return null;
//            }
//        });
//    }

//    private void copy(DeviceCommunicationConfigurationShadow shadow, boolean b) {
//        this.supportsAllMessageCategories = shadow.isSupportAllMessageCategories();
//        if (shadow.isSupportAllMessageCategories()) {
//            this.userActions = EnumSet.copyOf(shadow.getAllCategoriesUserActions());
//        }
//        int i = Boolean.valueOf(b).compareTo(false);
//    }

//    private void copyNew(DeviceCommunicationConfigurationShadow shadow) {
//        this.copy(shadow);
//    }

//    private void copyUpdate(DeviceCommunicationConfigurationShadow shadow) {
//        this.copy(shadow);
//    }

//    private void validateNew(DeviceCommunicationConfigurationShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }

//    private void validateUpdate(DeviceCommunicationConfigurationShadow shadow) throws BusinessException {
//        this.validate(shadow);
//        this.validateObsoletePartialConnectionTasksNoLongerUsed(shadow.getPartialConnectionTaskShadows().getDeletedShadows(), shadow.getComTaskEnablementShadows());
//    }

//    private void validate(DeviceCommunicationConfigurationShadow shadow) throws BusinessException {
//        this.validateConstructionValidator(shadow);
//        this.validateDeviceConfiguration(shadow);
//        this.validateMessages(shadow);
//    }

//    private void validateConstructionValidator(DeviceCommunicationConfigurationShadow shadow) throws BusinessException {
//        if(!(shadow.getDeviceCommunicationConfigurationConstructionValidation() instanceof DeviceCommunicationConfigurationConstructionValidationImpl)){
//            throw new BusinessException("illegalObjectConstruction",
//                    "It is not allowed to create a '{0}' without the correct construction validator.",
//                    this.getClass().getSimpleName());
//        }
//    }

//    private void validateDeviceConfiguration(DeviceCommunicationConfigurationShadow shadow) throws InvalidValueException, InvalidReferenceException {
//        if (shadow.getDeviceConfigurationId() == 0) {
//            throw new InvalidValueException("XcannotBeEmpty", "\"{0}\" is a required property", "deviceCommunicationConfiguration.deviceConfiguration");
//        } else {
//            this.validateDeviceConfigurationExists(shadow.getDeviceConfigurationId());
//        }
//    }

//    private void validateMessages(DeviceCommunicationConfigurationShadow shadow) throws BusinessException {
//        if (shadow.isSupportAllMessageCategories() && this.hasMessageCategoriesOrSpecs(shadow)) {
//            throw new BusinessException("shouldNotSpecifyMessagesWhenAllCategoriesAreSupported", "It is not allowed to specify message categoriess and/or messages when all message categories are supported");
//        }
//        if (!shadow.isSupportAllMessageCategories() && this.hasUserActionsForAllCategories(shadow)) {
//            throw new BusinessException("shouldSpecifyUserCategoriesOnEveryDeviceMessageEnablement", "User actions should be specified when enabling device message categories or device message specs when not all categories are supported");
//        }
//        this.validateUniquenessOfMessageEnablements(shadow);
//        this.validateMessageUserActions(shadow);
//    }

//    private boolean hasMessageCategoriesOrSpecs(DeviceCommunicationConfigurationShadow shadow) {
//        return !shadow.getDeviceMessageEnablementShadows().isEmpty();
//    }
//
//    private boolean hasUserActionsForAllCategories(DeviceCommunicationConfigurationShadow shadow) {
//        return !shadow.getAllCategoriesUserActions().isEmpty();
//    }

//    private void validateUniquenessOfMessageEnablements(DeviceCommunicationConfigurationShadow shadow) throws DuplicateException {
//        Set<String> categoryKeys = new HashSet<>();
//        Set<String> specKeys = new HashSet<>();
//        for (DeviceMessageEnablementShadow enablementShadow : shadow.getDeviceMessageEnablementShadows()) {
//            if (enablementShadow instanceof DeviceMessageCategoryEnablementShadow) {
//                DeviceMessageCategoryEnablementShadow categoryEnablementShadow = (DeviceMessageCategoryEnablementShadow) enablementShadow;
//                String primaryKey = this.getPrimaryKey(categoryEnablementShadow);
//                if (!categoryKeys.add(primaryKey)) {
//                    throw new DuplicateException(
//                            "duplicateDeviceMessageCategoryXForConfigY",
//                            "Duplicate device message category {0} for configuration {1}",
//                            primaryKey, this.getDeviceConfiguration().getName());
//                }
//            } else {
//                DeviceMessageSpecEnablementShadow specEnablementShadow = (DeviceMessageSpecEnablementShadow) enablementShadow;
//                String primaryKey = this.getPrimaryKey(specEnablementShadow);
//                if (!specKeys.add(primaryKey)) {
//                    throw new DuplicateException(
//                            "duplicateDeviceMessageSpecXForConfigY",
//                            "Duplicate device message spec {0} for configuration {1}",
//                            primaryKey, this.getDeviceConfiguration().getName());
//                }
//            }
//        }
//    }

//    private void validateMessageUserActions(DeviceCommunicationConfigurationShadow shadow) throws BusinessException {
//        for (DeviceMessageEnablementShadow enablementShadow : shadow.getDeviceMessageEnablementShadows()) {
//            if (enablementShadow.getUserActions().isEmpty()) {
//                throw new BusinessException("messageUserActionsCannotBeEmpty", "Enabling device message categories or device message specs requires a non empty set of user actions");
//            }
//        }
//    }

//    private void validateDeviceConfigurationExists(int deviceConfigurationId) throws InvalidReferenceException {
//        DeviceConfiguration deviceConfiguration = this.findDeviceConfiguration(deviceConfigurationId);
//        if (deviceConfiguration == null) {
//            throw InvalidReferenceException.newForIdBusinessObject(deviceConfigurationId, "DeviceConfiguration");
//        }
//    }

//    protected void doInit(DeviceCommunicationConfigurationShadow shadow) throws SQLException, BusinessException {
//        this.copyNew(shadow);
//        this.validateNew(shadow);
//        this.postNew();
//        this.createMessageEnablements(shadow.getDeviceMessageEnablementShadows());
//        this.doSecurityPropertySetCrud(shadow.getSecurityPropertySetShadows());
//        this.doPartialConnectionTaskCrud(shadow.getPartialConnectionTaskShadows());
//        this.doConfigurationPropertiesCrud(shadow.getConfigurationPropertiesShadows());
//        this.prepareComTaskEnablementShadows(shadow.getComTaskEnablementShadows());
//        this.comTaskEnablements = this.getComTaskEnablementFactory().doCrud(this, shadow.getComTaskEnablementShadows());
//        this.created();
//    }

//    private void doDeviceMessageEnablementCrud(ShadowList<DeviceMessageEnablementShadow> enablementShadows) throws SQLException {
//        this.deleteAllMessageEnablements();
//        this.createMessageEnablements(enablementShadows);
//        this.deviceMessageEnablements = null;    // Invalidate the cache if any
//    }

//    private void deleteAllMessageEnablements() throws SQLException {
//        SqlBuilder sqlBuilder = new SqlBuilder("delete from mdcdevicemessagesforconfig where comconfig = ?");
//        sqlBuilder.bindInt(this.getId());
//        try (PreparedStatement preparedStatement = sqlBuilder.getStatement(Environment.DEFAULT.get().getConnection())) {
//            preparedStatement.executeUpdate();
//            // Don't care about the number of rows that were effectively deleted.
//        }
//    }

//    private void createMessageEnablements(ShadowList<DeviceMessageEnablementShadow> deviceMessageEnablementShadows) throws SQLException {
//        Collection<MessageEnablementSpec> enablementSpecs = new ArrayList<>();
//        this.collectEnablementSpecs(deviceMessageEnablementShadows.getNewShadows(), enablementSpecs);
//        this.collectEnablementSpecs(deviceMessageEnablementShadows.getRemainingShadows(), enablementSpecs);
//        this.createMessageEnablements(enablementSpecs);
//    }
//
//    private void collectEnablementSpecs(Collection<DeviceMessageEnablementShadow> enablementShadows, Collection<MessageEnablementSpec> specs) {
//        for (DeviceMessageEnablementShadow shadow : enablementShadows) {
//            specs.add(this.newMessageEnablementSpec(shadow));
//        }
//    }

//    private void createMessageEnablements(Collection<MessageEnablementSpec> enablementSpecs) throws SQLException {
//        try (BatchStatement statement = new BatchStatement("insert into mdcdevicemessagesforconfig (discriminator, msgpk, useractions, comconfig) values (?, ?, ?, ?)", enablementSpecs.size())) {
//            for (MessageEnablementSpec enablementSpec : enablementSpecs) {
//                enablementSpec.addToBatch(statement);
//                statement.setInt(CONFIG_INDEX, this.getId());
//                statement.addBatch();
//            }
//            statement.executeBatch();
//        }
//    }
//
//    private void doSecurityPropertySetCrud(ShadowList<SecurityPropertySetShadow> shadows) throws BusinessException, SQLException {
//        this.securityPropertySets = null;
//        List<SecurityPropertySet> currentPropertySets = new ArrayList<>();
//        this.updateExistingPropertySets(shadows.getUpdatedShadows(), currentPropertySets);
//        this.deleteObsoletePropertySets(shadows.getDeletedShadows());
//        this.createNewPropertySets(shadows.getNewShadows(), currentPropertySets);
//        this.copyUnmodifiedSecurityPropertySets(shadows.getRemainingShadows(), currentPropertySets);
//        this.securityPropertySets = currentPropertySets;
//    }

//    private void updateExistingPropertySets(List<SecurityPropertySetShadow> updatedShadows, List<SecurityPropertySet> updatedSecurityPropertySets)
//        throws BusinessException, SQLException {
//        for (SecurityPropertySetShadow shadow : updatedShadows) {
//            this.updateSecurityPropertySetFromShadow(shadow, updatedSecurityPropertySets);
//        }
//    }

//    /**
//     * Updates the existing {@link SecurityPropertySet} that matches
//     * the id of the specified {@link SecurityPropertySetShadow}
//     * and throws a BusinessException if there is no matching SecurityPropertySet.
//     * The updated SecurityPropertySet is added to the list of updated SecurityPropertySets.
//     *
//     * @param shadow                      The SecurityPropertySetShadow
//     * @param updatedSecurityPropertySets The list of already updated SecurityPropertySets
//     * @throws BusinessException Thrown if the update of the SecurityPropertySet violates a business constraint or
//     *                           when there is no SecurityPropertySet with a matching id.
//     * @throws SQLException      Thrown if the update of the SecurityPropertySet violates a database constraint
//     */
//    private void updateSecurityPropertySetFromShadow(SecurityPropertySetShadow shadow, List<SecurityPropertySet> updatedSecurityPropertySets)
//        throws BusinessException, SQLException {
//        for (SecurityPropertySet securityPropertySet : this.getSecurityPropertySets()) {
//            if (securityPropertySet.getId() == shadow.getId()) {
//                ServerSecurityPropertySet updateableSecurityPropertySet = (ServerSecurityPropertySet) securityPropertySet;
//                updateableSecurityPropertySet.update(shadow);
//                updatedSecurityPropertySets.add(securityPropertySet);
//                return; // There is only one such SecurityPropertySet so we are done now
//            }
//        }
//        throw this.securityPropertySetDoesNotExist(shadow.getId());
//    }

//    private void deleteObsoletePropertySets(List<SecurityPropertySetShadow> obsoleteShadows) throws BusinessException, SQLException {
//        for (SecurityPropertySetShadow shadow : obsoleteShadows) {
//            this.deleteSecurityPropertySet(shadow.getId());
//        }
//    }

//    /**
//     * Deletes the existing SecurityPropertySet that matches
//     * the id of the specified SecurityPropertySetShadow
//     * and throws a BusinessException if there is no matching SecurityPropertySet.
//     * The updated SecurityPropertySet is added to the list of updated SecurityPropertySets.
//     *
//     * @param id The id of the obsolete SecurityPropertySet
//     * @throws BusinessException Thrown if the delete of the SecurityPropertySet violates a business constraint or
//     *                           when there is no SecurityPropertySet with a matching id.
//     * @throws SQLException      Thrown if the delete of the SecurityPropertySet violates a database constraint
//     */
//    private void deleteSecurityPropertySet(int id) throws BusinessException, SQLException {
//        this.getSecurityPropertySet(id).delete();
//    }
//
//    private void createNewPropertySets(List<SecurityPropertySetShadow> newShadows, List<SecurityPropertySet> currentPropertySets)
//        throws BusinessException, SQLException {
//        SecurityPropertySetFactory factory = this.getSecurityPropertySetFactory();
//        for (SecurityPropertySetShadow shadow : newShadows) {
//            currentPropertySets.add(factory.create(shadow, this));
//        }
//    }
//
//    private void copyUnmodifiedSecurityPropertySets(List<SecurityPropertySetShadow> remainingShadows, List<SecurityPropertySet> currentPropertySets)
//        throws BusinessException {
//        for (SecurityPropertySetShadow remainingSecurityPropertySetShadow : remainingShadows) {
//            if (!remainingSecurityPropertySetShadow.isDirty()) {
//                currentPropertySets.add(this.getSecurityPropertySet(remainingSecurityPropertySetShadow.getId()));
//            }
//        }
//    }
//
//    private SecurityPropertySet getSecurityPropertySet(int id) throws BusinessException {
//        for (SecurityPropertySet securityPropertySet : this.getSecurityPropertySets()) {
//            if (securityPropertySet.getId() == id) {
//                return securityPropertySet; // There is only one such SecurityPropertySet so we are done now
//            }
//        }
//        throw this.securityPropertySetDoesNotExist(id);
//    }
//
//    private BusinessException securityPropertySetDoesNotExist(int id) {
//        return new BusinessException(
//                "noSecurityPropertySetWithIdXInDeviceConfigurationY",
//                "There is no security property set with id {0} in device configuration {1}",
//                id,
//                this.getDeviceConfiguration().getName());
//    }

//    private BusinessException securityPropertySetDoesNotExist(String name) {
//        return new BusinessException(
//                "noSecurityPropertySetWithNameXInDeviceConfigurationY",
//                "There is no security property set with name {0} in device configuration {1}",
//                name,
//                this.getDeviceConfiguration().getName());
//    }
//
//    private BusinessException protocolDialectConfigurationPropertyDoesNotExist(String name) {
//        return new BusinessException(
//            "noProtocolDialectConfigurationPropertyWithNameXInDeviceConfigurationY",
//            "There is no protocol dialect configuration property with name '{0}' in device configuration '{1}'",
//            name, this.getDeviceConfiguration().getName());
//    }


//    private BusinessException configurationPropertiesDoesNotExist(int id) {
//        return new BusinessException(
//                "noDialectConfigurationPropertiesWithIdXInDeviceConfigurationY",
//                "There is no protocol dialect configuration properties with id {0} in device configuration {1}",
//                id,
//                this.getDeviceConfiguration().getName());
//    }

//    private void doConfigurationPropertiesCrud(ShadowList<ProtocolDialectConfigurationPropertiesShadow> shadows) throws BusinessException, SQLException {
//        this.configurationPropertiesList = null;
//        List<ProtocolDialectConfigurationProperties> currentConfigurationPropertiesList = new ArrayList<>();
//        this.updateExistingConfigurationProperties(shadows.getUpdatedShadows(), currentConfigurationPropertiesList);
//        this.deleteObsoleteConfigurationProperties(shadows.getDeletedShadows());
//        this.createNewConfigurationProperties(shadows.getNewShadows(), currentConfigurationPropertiesList);
//        this.copyUnmodifiedConfigurationProperties(shadows.getRemainingShadows(), currentConfigurationPropertiesList);
//        this.configurationPropertiesList = currentConfigurationPropertiesList;
//    }

//    private void updateExistingConfigurationProperties(List<ProtocolDialectConfigurationPropertiesShadow> updatedShadows, List<ProtocolDialectConfigurationProperties> updatedConfigurationProperties)
//        throws BusinessException, SQLException {
//        for (ProtocolDialectConfigurationPropertiesShadow shadow : updatedShadows) {
//            this.updateConfigurationPropertiesFromShadow(shadow, updatedConfigurationProperties);
//        }
//    }

//    /**
//     * Updates the existing {@link ProtocolDialectConfigurationProperties} that matches
//     * the id of the specified {@link ProtocolDialectConfigurationPropertiesShadow}
//     * and throws a BusinessException if there is no matching ProtocolDialectConfigurationProperties.
//     * The updated ProtocolDialectConfigurationProperties is added to the list of updated ProtocolDialectConfigurationProperties.
//     *
//     * @param shadow                         The ProtocolDialectConfigurationPropertiesShadow
//     * @param updatedConfigurationProperties The list of already updated ProtocolDialectConfigurationProperties
//     * @throws BusinessException Thrown if the update of the ProtocolDialectConfigurationProperties violates a business constraint or
//     *                           when there is no ProtocolDialectConfigurationProperties with a matching id.
//     * @throws SQLException      Thrown if the update of the ProtocolDialectConfigurationProperties violates a database constraint
//     */
//    private void updateConfigurationPropertiesFromShadow(ProtocolDialectConfigurationPropertiesShadow shadow,
//                                                         List<ProtocolDialectConfigurationProperties> updatedConfigurationProperties)
//        throws BusinessException, SQLException {
//        for (ProtocolDialectConfigurationProperties configurationProperties : this.getProtocolDialectConfigurationPropertiesList()) {
//            if (configurationProperties.getId() == shadow.getId()) {
//                configurationProperties.update(shadow);
//                updatedConfigurationProperties.add(configurationProperties);
//                return; // There is only one such element so we are done now
//            }
//        }
//        throw this.configurationPropertiesDoesNotExist(shadow.getId());
//    }

//    private void deleteObsoleteConfigurationProperties(List<ProtocolDialectConfigurationPropertiesShadow> obsoleteShadows) throws BusinessException, SQLException {
//        for (ProtocolDialectConfigurationPropertiesShadow shadow : obsoleteShadows) {
//            this.deleteConfigurationProperties(shadow.getId());
//        }
//    }

//    private void createNewConfigurationProperties(List<ProtocolDialectConfigurationPropertiesShadow> newShadows, List<ProtocolDialectConfigurationProperties> currentConfigurationProperties)
//        throws
//            BusinessException,
//            SQLException {
//        ProtocolDialectConfigurationPropertiesFactory factory = this.getProtocolDialectConfigurationPropertiesFactory();
//        for (ProtocolDialectConfigurationPropertiesShadow shadow : newShadows) {
//            currentConfigurationProperties.add(factory.create(shadow, this));
//        }
//    }

//    private void copyUnmodifiedConfigurationProperties(List<ProtocolDialectConfigurationPropertiesShadow> remainingShadows, List<ProtocolDialectConfigurationProperties> currentConfigurationProperties)
//        throws BusinessException {
//        for (ProtocolDialectConfigurationPropertiesShadow remainingConfigurationPropertiesShadow : remainingShadows) {
//            if (!remainingConfigurationPropertiesShadow.isDirty()) {
//                currentConfigurationProperties.add(this.getConfigurationProperties(remainingConfigurationPropertiesShadow.getId()));
//            }
//        }
//    }

//    /**
//     * Prepares the ComTaskEnablementShadows such that
//     * the ids of the newly created SecurityPropertySets
//     * and/or newly created {@link PartialConnectionTask}s
//     * and/or newly created {@link ProtocolDialectConfigurationProperties}s
//     * are copied into the ComTaskEnablementShadow
//     * that are using the shadow from which the newly created
//     * objects were constructed.
//     *
//     * @param shadows The List of ComTaskEnablementShadow
//     */
//    private void prepareComTaskEnablementShadows(ShadowList<ComTaskEnablementShadow> shadows) throws BusinessException {
//        for (ComTaskEnablementShadow shadow : shadows.getNewShadows()) {
//            this.prepareComTaskEnablementShadow(shadow);
//        }
//        for (ComTaskEnablementShadow shadow : shadows.getUpdatedShadows()) {
//            this.prepareComTaskEnablementShadow(shadow);
//        }
//    }
//
//    private void prepareComTaskEnablementShadow(ComTaskEnablementShadow shadow) throws BusinessException {
//        this.copySecurityPropertySetId(shadow);
//        this.copyProtocolDialectConfigurationPropertiesId(shadow);
//        if (shadow.getPartialConnectionTaskShadow() != null) {
//            this.copyPartialConnectionTaskId(shadow);
//        }
//    }
//
//    private void copySecurityPropertySetId(ComTaskEnablementShadow shadow) throws BusinessException {
//        if (shadow.getSecurityPropertySetShadow() != null) {
//            SecurityPropertySet securityPropertySet = this.findSecurityPropertySetByName(shadow.getSecurityPropertySetShadow().getName());
//            shadow.setSecurityPropertySetId(securityPropertySet.getId());
//        }
//    }
//
//    private SecurityPropertySet findSecurityPropertySetByName(String name) throws BusinessException {
//        for (SecurityPropertySet securityPropertySet : this.getSecurityPropertySets()) {
//            if (name.equals(securityPropertySet.getName())) {
//                return securityPropertySet;
//            }
//        }
//        throw this.securityPropertySetDoesNotExist(name);
//    }
//
//    private void copyProtocolDialectConfigurationPropertiesId(ComTaskEnablementShadow shadow) throws BusinessException {
//        if (shadow.getProtocolDialectConfigurationPropertiesShadow() != null) {
//            ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties =
//                this.findProtocolDialectConfigurationPropertyByName(shadow.getProtocolDialectConfigurationPropertiesShadow().getName());
//            shadow.setProtocolDialectConfigurationPropertiesId(protocolDialectConfigurationProperties.getId());
//        }
//    }

//    private void copyPartialConnectionTaskId(ComTaskEnablementShadow shadow) throws BusinessException {
//        if (shadow.getPartialConnectionTaskShadow() != null) {
//            PartialConnectionTask partialConnectionTask = this.findPartialConnectionTaskByName(shadow.getPartialConnectionTaskShadow().getName());
//            shadow.setPartialConnectionTaskId(partialConnectionTask.getId());
//        }
//    }

//    private BusinessException partialConnectionTaskDoesNotExist(String name) {
//        return new BusinessException(
//                "noPartialConnectionTaskWithNameXInDeviceConfigurationY",
//                "There is no partial connection task with name {0} in device configuration {1}",
//                name,
//                this.getDeviceConfiguration().getName());
//    }

//    private void doPartialConnectionTaskCrud(ShadowList<PartialConnectionTaskShadow> shadows) throws BusinessException, SQLException {
//        this.validateOnlyOneDefault(shadows);
//        this.validateUniqueName(shadows);
//        List<PartialConnectionTask> currentPartialConnectionTasks = new ArrayList<>();
//        this.updateExistingPartialConnectionTasks(shadows.getUpdatedShadows(), currentPartialConnectionTasks);
//        this.deleteObsoletePartialConnectionTasks(shadows.getDeletedShadows());
//        this.createNewPartialConnectionTasks(shadows.getNewShadows(), currentPartialConnectionTasks);
//        this.copyUnmodifiedPartialConnectionTasks(shadows.getRemainingShadows(), currentPartialConnectionTasks);
//        this.partialConnectionTasks = currentPartialConnectionTasks;
//    }
//
//    private void validateOnlyOneDefault(ShadowList<PartialConnectionTaskShadow> shadows) throws BusinessException {
//        int defaultCount = 0;
//        List<PartialConnectionTaskShadow> remaining = this.getRemainingShadows(shadows);
//        for (PartialConnectionTaskShadow shadow : remaining) {
//            if (shadow.isDefault()) {
//                defaultCount++;
//            }
//        }
//        if (defaultCount > 1) {
//            throw new BusinessException("only1DefaultPartialConnectionTaskAllowed", "Device configurations only support 1 default partial connection task");
//        }
//    }
//
//    private void validateUniqueName(ShadowList<PartialConnectionTaskShadow> shadows) throws BusinessException {
//        Set<String> inUse = new HashSet<>();
//        List<PartialConnectionTaskShadow> remaining = this.getRemainingShadows(shadows);
//        for (PartialConnectionTaskShadow shadow : remaining) {
//            if (!inUse.add(shadow.getName())) {
//                // Name was already in use
//                throw new BusinessException("partialConnectionTaskWithNameXAlreadyExists",
//                    "Device configuration '{0}' already contains a partial connection task with name '{1}'",
//                    getDeviceConfiguration().getName(), shadow.getName());
//            }
//        }
//    }

    /**
     * Gets the shadows that remain active in the specified ShadowList.
     *
     * @param shadows The ShadowList
     * @return The shadows that remain active
     * @see ShadowList#getNewShadows()
     * @see ShadowList#getRemainingShadows()
     */
//    private List<PartialConnectionTaskShadow> getRemainingShadows(ShadowList<PartialConnectionTaskShadow> shadows) {
//        List<PartialConnectionTaskShadow> remaining = new ArrayList<>(shadows.getNewShadows());
//        remaining.addAll(shadows.getRemainingShadows());
//        return remaining;
//    }
//
//    private void updateExistingPartialConnectionTasks(List<PartialConnectionTaskShadow> updatedShadows, List<PartialConnectionTask> updatedPartialConnectionTasks)
//        throws
//            BusinessException,
//            SQLException {
//        for (PartialConnectionTaskShadow shadow : updatedShadows) {
//            this.updatePartialConnectionTaskFromShadow(shadow, updatedPartialConnectionTasks);
//        }
//    }

    /**
     * Updates the existing {@link PartialConnectionTask} that matches
     * the id of the specified {@link PartialConnectionTaskShadow}
     * and throws a BusinessException if there is no matching PartialConnectionTask.
     * The updated PartialConnectionTask is added to the list of updated PartialConnectionTasks.
     *
     * @param shadow                        The PartialConnectionTaskShadow
     * @param updatedPartialConnectionTasks The list of already updated PartialConnectionTasks
     * @throws BusinessException Thrown if the update of the PartialConnectionTask violates a business constraint or
     *                           when there is no PartialConnectionTask with a matching id.
     * @throws SQLException      Thrown if the update of the PartialConnectionTask violates a database constraint
     */
//    private void updatePartialConnectionTaskFromShadow(PartialConnectionTaskShadow shadow, List<PartialConnectionTask> updatedPartialConnectionTasks)
//        throws
//            BusinessException,
//            SQLException {
//        PartialConnectionTask updateTarget = this.getPartialConnectionTask(shadow.getId());
//        // Remember that getPartialConnectionTask throws an exception when the task does not exist.
//        if (shadow instanceof PartialInboundConnectionTaskShadow) {
//            PartialInboundConnectionTask inboundConnectionTask = (PartialInboundConnectionTask) updateTarget;
//            PartialInboundConnectionTaskShadow updateShadow = (PartialInboundConnectionTaskShadow) shadow;
//            inboundConnectionTask.update(updateShadow);
//        } else if (shadow instanceof PartialOutboundConnectionTaskShadow) {
//            PartialOutboundConnectionTask outboundConnectionTask = (PartialOutboundConnectionTask) updateTarget;
//            PartialOutboundConnectionTaskShadow updateShadow = (PartialOutboundConnectionTaskShadow) shadow;
//            outboundConnectionTask.update(updateShadow);
//        } else if (shadow instanceof PartialConnectionInitiationTaskShadow) {
//            PartialConnectionInitiationTask connectionInitiationTask = (PartialConnectionInitiationTask) updateTarget;
//            PartialConnectionInitiationTaskShadow updateShadow = (PartialConnectionInitiationTaskShadow) shadow;
//            connectionInitiationTask.update(updateShadow);
//        } else {
//            throw CodingException.unknownPartialConnectionTaskShadowClass(shadow.getClass());
//        }
//        updatedPartialConnectionTasks.add(updateTarget);
//    }

//    private void validateObsoletePartialConnectionTasksNoLongerUsed (List<PartialConnectionTaskShadow> obsoleteShadows, ShadowList<ComTaskEnablementShadow> comTaskEnablementShadows)
//        throws BusinessException {
//        for (PartialConnectionTaskShadow shadow : obsoleteShadows) {
//            this.validateObsoletePartialConnectionTaskIsNoLongerUsed(shadow, comTaskEnablementShadows);
//        }
//    }
//
//    private void validateObsoletePartialConnectionTaskIsNoLongerUsed (PartialConnectionTaskShadow shadow, ShadowList<ComTaskEnablementShadow> comTaskEnablementShadows)
//        throws BusinessException {
//        for (ComTaskEnablementShadow comTaskEnablementShadow : comTaskEnablementShadows) {
//            if (shadow.getId() == comTaskEnablementShadow.getPartialConnectionTaskId()) {
//                ComTaskEnablement comTaskEnablement = this.getComTaskEnablementFactory().findById(comTaskEnablementShadow.getId());
//                throw new BusinessException(
//                        "partialConnectionTaskXIsStillInUseByComTaskEnablementY",
//                        "Partial connection task {0} cannot be deleted because it is still being used by com task enablement {1}",
//                        shadow.getName(),
//                        comTaskEnablement.getComTask().getName());
//            }
//        }
//    }

//    private void deleteObsoletePartialConnectionTasks(List<PartialConnectionTaskShadow> obsoleteShadows)
//        throws
//            BusinessException,
//            SQLException {
//        for (PartialConnectionTaskShadow shadow : obsoleteShadows) {
//            this.deletePartialConnectionTask(shadow.getId());
//        }
//    }

    /**
     * Deletes the existing {@link PartialConnectionTask} that matches
     * the id of the specified PartialConnectionTaskShadow
     * and throws a BusinessException if there is no matching PartialConnectionTask.
     * The updated PartialConnectionTask is added to the list of updated PartialConnectionTasks.
     *
     * @param id The id of the obsolete PartialConnectionTask
     * @throws BusinessException Thrown if the delete of the PartialConnectionTask violates a business constraint or
     *                           when there is no PartialConnectionTask with a matching id.
     * @throws SQLException      Thrown if the delete of the PartialConnectionTask violates a database constraint
     */
    private void deletePartialConnectionTask(int id)
        throws
            BusinessException,
            SQLException {
        this.getPartialConnectionTask(id).delete();
    }


//    private void createNewPartialConnectionTasks(List<PartialConnectionTaskShadow> newShadows, List<PartialConnectionTask> currentPartialConnectionTasks)
//        throws
//            BusinessException,
//            SQLException {
//        ServerPartialConnectionTaskFactory factory = this.getPartialConnectionTaskFactory();
//        for (PartialConnectionTaskShadow shadow : newShadows) {
//            shadow.setConfigurationId(this.getId());
//            currentPartialConnectionTasks.add(factory.createFrom(shadow));
//        }
//    }

//    private void copyUnmodifiedPartialConnectionTasks(List<PartialConnectionTaskShadow> remainingShadows, List<PartialConnectionTask> currentPartialConnectionTasks)
//        throws BusinessException {
//        for (PartialConnectionTaskShadow remainingPartialConnectionTaskShadow : remainingShadows) {
//            if (!remainingPartialConnectionTaskShadow.isDirty()) {
//                currentPartialConnectionTasks.add(this.getPartialConnectionTask(remainingPartialConnectionTaskShadow.getId()));
//            }
//        }
//    }

    private PartialConnectionTask getPartialConnectionTask(int id) throws BusinessException {
        for (PartialConnectionTask partialConnectionTask : this.getPartialConnectionTasks()) {
            if (partialConnectionTask.getId() == id) {
                return partialConnectionTask; // There is only one such PartialConnectionTask so we are done now
            }
        }
        throw new PartialConnectionTaskDoesNotExist(thesaurus, id);
    }

//    private BusinessException partialConnectionTaskDoesNotExist(int id) {
//        return new BusinessException(
//                "noPartialConnectionTaskWithIdXInDeviceConfigurationY",
//                "There is no partial connection task with id {0} in device configuration with name {1} (id: {2})",
//                id,
//                this.getDeviceConfiguration().getName(),
//                this.getDeviceConfiguration().getId());
//    }

//    @Override
//    public DeviceCommunicationConfigurationShadow getShadow() {
//        DeviceCommunicationConfigurationShadow deviceCommunicationConfigurationShadow = new DeviceCommunicationConfigurationShadow(this);
//        deviceCommunicationConfigurationShadow.setDeviceCommunicationConfigurationConstructionValidation(new DeviceCommunicationConfigurationConstructionValidationImpl());
//        return deviceCommunicationConfigurationShadow;
//    }

//    @Override
//    public void update(final DeviceCommunicationConfigurationShadow shadow) throws BusinessException, SQLException {
//        this.execute(new Transaction<Void>() {
//            public Void doExecute() throws BusinessException, SQLException {
//                doUpdate(shadow);
//                return null;
//            }
//        });
//    }

//    @Override
//    public List<DeviceMessageSpec> getAllowedDeviceMessageSpecsForCurrentUser() {
//        if (supportsAllMessageCategories()) {
//            if (this.isAllowedBasedOnUserActions(getAllCategoriesUserActions())) {
//                return getDeviceProtocolSupportedDeviceMessageSpecs();
//            } else {
//                return Collections.emptyList();
//            }
//        }
//        final List<DeviceMessageSpec> authorizedMessageSpecs = new ArrayList<>();
//        for (DeviceMessageEnablement deviceMessageEnablement : getDeviceMessageEnablements()) {
//            if (isDeviceMessageEnablementAllowedForCurrentUser(deviceMessageEnablement)) {
//                if (DeviceMessageSpecEnablement.class.isAssignableFrom(deviceMessageEnablement.getClass())) {
//                    authorizedMessageSpecs.add(((DeviceMessageSpecEnablement) deviceMessageEnablement).getDeviceMessageSpec());
//                } else if (DeviceMessageCategoryEnablement.class.isAssignableFrom(deviceMessageEnablement.getClass())) {
//                    authorizedMessageSpecs.addAll(getSupportedMessagesFromCategory((DeviceMessageCategoryEnablement) deviceMessageEnablement));
//                }
//            }
//        }
//        return authorizedMessageSpecs;
//    }

    /**
     * Return the messages from a specific category that are supported by the protocol.
     */
//    private List<DeviceMessageSpec> getSupportedMessagesFromCategory(DeviceMessageCategoryEnablement deviceMessageEnablement) {
//        List<DeviceMessageSpec> result = new ArrayList<>();
//        for (DeviceMessageSpec deviceMessageSpec : getDeviceProtocolSupportedDeviceMessageSpecs()) {
//            if (deviceMessageSpec.getCategory().equals(deviceMessageEnablement.getDeviceMessageCategory())) {
//                result.add(deviceMessageSpec);
//            }
//        }
//        return result;
//    }

    @Override
    public boolean isAuthorized(DeviceMessageSpec deviceMessageSpec) {
//        if (supportsAllMessageCategories()) {
//            return checkIsAuthorizedForAllCategories(deviceMessageSpec);
//        }
//        final DeviceMessageEnablement deviceMessageEnablementForDeviceMessageSpec = getDeviceMessageEnablementForDeviceMessageSpec(deviceMessageSpec);
//        return isDeviceMessageEnablementAllowedForCurrentUser(deviceMessageEnablementForDeviceMessageSpec);
        return true;
    }

//    @Override
//    public boolean isSupported(DeviceMessageSpec deviceMessageSpec) {
//        return supportsAllMessageCategories() || getDeviceMessageEnablementForDeviceMessageSpec(deviceMessageSpec) != null;
//    }

//    @Override
//    public boolean isExecutableForCurrentUser(ComTaskExecution comTaskExecution) {
//        boolean authorized = false;
//        final Role currentRole = getCurrentRole();
//        for (ComTaskEnablement comTaskEnablement : getEnabledComTasks()) {
//            for (DeviceSecurityUserAction deviceSecurityUserAction : comTaskEnablement.getSecurityPropertySet().getUserActions()) {
//                authorized |= deviceSecurityUserAction.isExecutableIsAuthorizedFor(currentRole);
//            }
//        }
//        return authorized;
//    }

//    private boolean checkIsAuthorizedForAllCategories(DeviceMessageSpec deviceMessageSpec) {
//        final List<DeviceMessageSpec> allSupportedMessages = getDeviceProtocolSupportedDeviceMessageSpecs();
//        return allSupportedMessages.contains(deviceMessageSpec) && isAllowedBasedOnUserActions(getAllCategoriesUserActions());
//
//    }

//    private List<DeviceMessageSpec> getDeviceProtocolSupportedDeviceMessageSpecs() {
//        return getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();
//    }

//    private boolean isDeviceMessageEnablementAllowedForCurrentUser(final DeviceMessageEnablement deviceMessageEnablement) {
//        return deviceMessageEnablement != null && isAllowedBasedOnUserActions(deviceMessageEnablement.getUserActions());
//    }

//    private boolean isAllowedBasedOnUserActions(final Set<DeviceMessageUserAction> userActions) {
//        Role currentRole = getCurrentRole();
//        boolean authorized = false;
//        for (DeviceMessageUserAction deviceMessageUserAction : userActions) {
//            authorized |= deviceMessageUserAction.isAuthorized(currentRole);
//        }
//        return authorized;
//    }
//
//    private Role getCurrentRole() {
//        return ManagerFactory.getCurrent().getMdwInterface().getUser().getRole();
//    }

//    /**
//     * Finds the matching DeviceMessageEnablement for the given DeviceMessageSpec.
//     * If no match is found, then null is returned
//     *
//     * @param deviceMessageSpec to spec to find the matching enablement
//     * @return the matching DeviceMessageEnablement
//     */
//    private DeviceMessageEnablement getDeviceMessageEnablementForDeviceMessageSpec(final DeviceMessageSpec deviceMessageSpec) {
//        for (DeviceMessageEnablement deviceMessageEnablement : getDeviceMessageEnablements()) {
//            if (DeviceMessageSpecEnablement.class.isAssignableFrom(deviceMessageEnablement.getClass())) {
//                if (((DeviceMessageSpecEnablement) deviceMessageEnablement).getDeviceMessageSpec().equals(deviceMessageSpec)) {
//                    return deviceMessageEnablement;
//                }
//            } else if (DeviceMessageCategoryEnablement.class.isAssignableFrom(deviceMessageEnablement.getClass())) {
//                if (((DeviceMessageCategoryEnablement) deviceMessageEnablement).getDeviceMessageCategory().equals(deviceMessageSpec.getCategory())) {
//                    return deviceMessageEnablement;
//                }
//            }
//        }
//        return null;
//    }

//    private void doUpdate(DeviceCommunicationConfigurationShadow shadow) throws BusinessException, SQLException {
//        this.validateUpdate(shadow);
//        this.copyUpdate(shadow);
//        this.post();
//        this.doDeviceMessageEnablementCrud(shadow.getDeviceMessageEnablementShadows());
//        this.doSecurityPropertySetCrud(shadow.getSecurityPropertySetShadows());
//        this.getComTaskEnablementFactory().delete(this, shadow.getComTaskEnablementShadows());
//        this.doPartialConnectionTaskCrud(shadow.getPartialConnectionTaskShadows());
//        this.prepareComTaskEnablementShadows(shadow.getComTaskEnablementShadows());
//        this.comTaskEnablements = this.getComTaskEnablementFactory().doCrud(this, shadow.getComTaskEnablementShadows());
//        this.doConfigurationPropertiesCrud(shadow.getConfigurationPropertiesShadows());
//        this.updated();
//    }

//    @Override
//    public Command<DeviceCommunicationConfiguration> createConstructor() {
//        throw CodingException.unsupportedMethod(this.getClass(), "createConstructor");
//    }

//    @Override
//    public boolean isExportAllowed() {
//        return true;
//    }

//    @Override
//    protected void deleteDependents() throws SQLException, BusinessException {
//        super.deleteDependents();
//        this.deleteAllMessageEnablements();
//        this.deleteEnabledComTasks();
//        this.deleteSecurityPropertySets();
//        this.deletePartialConnectionTasks();
//        this.deleteConfigurationProperties();
//    }

//    private void deleteSecurityPropertySets() throws SQLException, BusinessException {
//        for (SecurityPropertySet securityPropertySet : this.getSecurityPropertySets()) {
//            securityPropertySet.delete();
//        }
//    }

//    private void deleteEnabledComTasks() throws SQLException, BusinessException {
//        for (ComTaskEnablement comTaskEnablement : this.getEnabledComTasks()) {
//            comTaskEnablement.delete();
//        }
//    }

    private void deletePartialConnectionTasks() throws SQLException, BusinessException {
        for (PartialConnectionTask partialConnectionTask : this.getPartialConnectionTasks()) {
            partialConnectionTask.delete();
        }
    }

    private void deleteConfigurationProperties() throws SQLException, BusinessException {
        for (ProtocolDialectConfigurationProperties configurationProperty : configurationPropertiesList) {
            configurationProperty.delete();
        }
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public List<SecurityPropertySet> getSecurityPropertySets() {
        return new ArrayList<>();
    }

    @Override
    public List<ComTaskEnablement> getEnabledComTasks() {
        return Collections.emptyList();
//        if (this.comTaskEnablements == null) {
//            this.comTaskEnablements = ManagerFactory.getCurrent().getComTaskEnablementFactory().findByDeviceCommunicationConfiguration(this);
//        }
//        return this.comTaskEnablements;
    }

    @Override
    public boolean supportsAllMessageCategories() {
        return supportsAllMessageCategories;
    }

    @Override
    public Set<DeviceMessageUserAction> getAllCategoriesUserActions() {
        return Collections.emptySet(); // TODO
    }

    @Override
    public List<DeviceMessageEnablement> getDeviceMessageEnablements() {
//        if (this.deviceMessageEnablements == null) {
//            this.deviceMessageEnablements = this.findDeviceMessageEnablements();
//        }
//        return deviceMessageEnablements;
        return Collections.emptyList();
    }

//    private List<DeviceMessageEnablement> findDeviceMessageEnablements() {
//        List<DeviceMessageEnablement> enablements = new ArrayList<>();
//        SqlBuilder sqlBuilder = new SqlBuilder("select discriminator, msgpk, useractions from mdcdevicemessagesforconfig where comconfig = ?");
//        sqlBuilder.bindInt(this.getId());
//        try (PreparedStatement preparedStatement = sqlBuilder.getStatement(Environment.DEFAULT.get().getConnection())) {
//            try (ResultSet resultSet = preparedStatement.executeQuery()) {
//                while (resultSet.next()) {
//                    enablements.add(MessageDiscriminator.fetch(resultSet));
//                }
//            }
//            return enablements;
//        } catch (SQLException e) {
//            throw new ApplicationException(e);
//        }
//    }

    @Override
    public List<PartialOutboundConnectionTask> getPartialOutboundConnectionTasks() {
        return this.filter(this.findAllPartialConnectionTasks(), new PartialOutboundConnectionTaskFilterPredicate());
    }

    public List<PartialInboundConnectionTask> getPartialInboundConnectionTasks() {
        return this.filter(this.findAllPartialConnectionTasks(), new PartialInboundConnectionTaskFilterPredicate());
    }

    @Override
    public List<PartialConnectionInitiationTask> getPartialConnectionInitiationTasks() {
        return this.filter(this.findAllPartialConnectionTasks(), new PartialConnectionInitiationTaskFilterPredicate());
    }

    @Override
    public List<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationPropertiesList() {
        return Collections.unmodifiableList(configurationPropertiesList);
    }

    @Override
    public List<PartialConnectionTask> getPartialConnectionTasks() {
        return this.findAllPartialConnectionTasks();
    }

    private List<PartialConnectionTask> findAllPartialConnectionTasks() {
        return this.partialConnectionTasks;
    }

    private <T extends PartialConnectionTask> List<T> filter(List<PartialConnectionTask> needsFiltering, PartialConnectionTaskFilterPredicate filterPredicate) {
        List<T> filtered = new ArrayList<>(needsFiltering.size());  // At most all tasks are retained by the filter
        for (PartialConnectionTask partialConnectionTask : needsFiltering) {
            if (filterPredicate.retain(partialConnectionTask)) {
                filtered.add((T) partialConnectionTask);
            }
        }
        return filtered;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICE_COMMUNICATION_CONFIGURATION;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICE_COMMUNICATION_CONFIGURATION;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICE_COMMUNICATION_CONFIGURATION;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(DeviceCommunicationConfiguration.class).remove(this);
    }

    @Override
    protected void validateDelete() {
        //TODO automatically generated method body, provide implementation.

    }

    private DeviceCommunicationConfigurationImpl init(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration.set(deviceConfiguration);
        return this;
    }

    private interface PartialConnectionTaskFilterPredicate<T extends PartialConnectionTask> {

        public boolean retain(PartialConnectionTask task);

    }

    private class PartialOutboundConnectionTaskFilterPredicate implements PartialConnectionTaskFilterPredicate<PartialOutboundConnectionTask> {

        @Override
        public boolean retain(PartialConnectionTask task) {
            return task instanceof PartialOutboundConnectionTask;
        }
    }

    private class PartialInboundConnectionTaskFilterPredicate implements PartialConnectionTaskFilterPredicate<PartialInboundConnectionTask> {

        @Override
        public boolean retain(PartialConnectionTask task) {
            return task instanceof PartialInboundConnectionTask;
        }
    }

    private class PartialConnectionInitiationTaskFilterPredicate implements PartialConnectionTaskFilterPredicate<PartialConnectionInitiationTask> {

        @Override
        public boolean retain(PartialConnectionTask task) {
            return task instanceof PartialConnectionInitiationTask;
        }
    }

//    private MessageEnablementSpec newMessageEnablementSpec(DeviceMessageEnablementShadow shadow) {
//        long userAction = new DeviceMessageUserActionSetValueFactory().toDatabaseValue(EnumSet.copyOf(shadow.getUserActions()));
//        if (shadow instanceof DeviceMessageCategoryEnablementShadow) {
//            DeviceMessageCategoryEnablementShadow categoryEnablementShadow = (DeviceMessageCategoryEnablementShadow) shadow;
//            return new MessageEnablementSpec(MessageDiscriminator.CATEGORY, this.getPrimaryKey(categoryEnablementShadow), userAction);
//        } else {
//            DeviceMessageSpecEnablementShadow specEnablementShadow = (DeviceMessageSpecEnablementShadow) shadow;
//            return new MessageEnablementSpec(MessageDiscriminator.SINGLE_MESSAGE, this.getPrimaryKey(specEnablementShadow), userAction);
//        }
//    }
//
//    private String getPrimaryKey(DeviceMessageCategoryEnablementShadow enablementShadow) {
//        return enablementShadow.getDeviceMessageCategory().getPrimaryKey().getValue();
//    }
//
//    private String getPrimaryKey(DeviceMessageSpecEnablementShadow enablementShadow) {
//        return enablementShadow.getDeviceMessageSpec().getPrimaryKey().getValue();
//    }

    private static final int MESSAGE_DISCRIMINATOR_INDEX = 1;
    private static final int MESSAGE_PK_INDEX = MESSAGE_DISCRIMINATOR_INDEX + 1;
    private static final int USERACTIONS_INDEX = MESSAGE_PK_INDEX + 1;
    private static final int CONFIG_INDEX = USERACTIONS_INDEX + 1;

//    private enum MessageDiscriminator {
//        CATEGORY {
//            @Override
//            protected DeviceMessageEnablement newFor(String valuePrimaryKey, Set<DeviceMessageUserAction> userActions) {
//                return new DeviceMessageCategoryEnablementImpl(valuePrimaryKey, userActions);
//            }
//        },
//        SINGLE_MESSAGE {
//            @Override
//            protected DeviceMessageEnablement newFor(String valuePrimaryKey, Set<DeviceMessageUserAction> userActions) {
//                return new DeviceMessageSpecEnablementImpl(valuePrimaryKey, userActions);
//            }
//        };
//
//        protected abstract DeviceMessageEnablement newFor(String valuePrimaryKey, Set<DeviceMessageUserAction> userActions);
//
//        private static DeviceMessageEnablement fetch(ResultSet resultSet) throws SQLException {
//            int discriminatorId = resultSet.getInt(MESSAGE_DISCRIMINATOR_INDEX);
//            for (MessageDiscriminator discriminator : values()) {
//                if (discriminator.ordinal() == discriminatorId) {
//                    String valuePrimaryKey = resultSet.getString(MESSAGE_PK_INDEX);
//                    Set<DeviceMessageUserAction> userActions = new DeviceMessageUserActionSetValueFactory().fromDatabaseValue(resultSet.getLong(USERACTIONS_INDEX));
//                    return discriminator.newFor(valuePrimaryKey, userActions);
//                }
//            }
//            throw PersistenceCodingException.unrecognizedDiscriminator(discriminatorId, DeviceMessageEnablement.class);
//        }
//    }

//    private final class MessageEnablementSpec {
//
//        private MessageDiscriminator discriminator;
//        private String valuePrimaryKey;
//        private long userActions;
//
//        private MessageEnablementSpec(MessageDiscriminator discriminator, String valuePrimaryKey, long userActions) {
//            super();
//            this.discriminator = discriminator;
//            this.valuePrimaryKey = valuePrimaryKey;
//            this.userActions = userActions;
//        }
//
//        private void addToBatch(BatchStatement statement) throws SQLException {
//            statement.setInt(MESSAGE_DISCRIMINATOR_INDEX, this.discriminator.ordinal());
//            statement.setString(MESSAGE_PK_INDEX, this.valuePrimaryKey);
//            statement.setLong(USERACTIONS_INDEX, this.userActions);
//        }
//    }


    @Override
    public void setSupportsAllMessageCategories(boolean supportAllMessageCategories) {
        this.supportsAllMessageCategories = supportAllMessageCategories;

    }

    @Override
    public void addSecurityPropertySet(SecurityPropertySet securityPropertySet) {
        this.getSecurityPropertySets().add(securityPropertySet);
    }

    @Override
    public PartialOutboundConnectionTaskBuilder createPartialOutboundConnectionTask() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public PartialInboundConnectionTaskBuilder createPartialInboundConnectionTask() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public PartialConnectionInitiationTaskBuilder createPartialConnectionInitiationTask() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public void addPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
        //TODO automatically generated method body, provide implementation.
    }

    @Override
    public ProtocolDialectConfigurationProperties createProtocolDialectConfigurationProperties(String name, DeviceProtocolDialect protocolDialect) {
        ProtocolDialectConfigurationProperties props = ProtocolDialectConfigurationPropertiesImpl.from(dataModel, this, name, protocolDialect);
        configurationPropertiesList.add(props);
        return props;
    }
}
