package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.exceptions.PartialConnectionTaskDoesNotExist;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.inject.Inject;
import javax.validation.Valid;
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
    private List<SecurityPropertySet> securityPropertySets = new ArrayList<>();
//    private List<ComTaskEnablement> comTaskEnablements;
    private boolean supportsAllMessageCategories;
    private long userActions; // temp place holder for the enumset
//    private EnumSet<DeviceMessageUserAction> userActions = EnumSet.noneOf(DeviceMessageUserAction.class);
//    private List<DeviceMessageEnablement> deviceMessageEnablements;
    @Valid
    private List<PartialConnectionTask> partialConnectionTasks = new ArrayList<>();
    @Valid
    private List<ProtocolDialectConfigurationProperties> configurationPropertiesList = new ArrayList<>();

    @Inject
    DeviceCommunicationConfigurationImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(DeviceCommunicationConfiguration.class, dataModel, eventService, thesaurus);
    }

    static DeviceCommunicationConfigurationImpl from(DataModel dataModel, DeviceConfiguration deviceConfiguration) {
        return dataModel.getInstance(DeviceCommunicationConfigurationImpl.class).init(deviceConfiguration);
    }

//    private void validateConstructionValidator(DeviceCommunicationConfigurationShadow shadow) throws BusinessException {
//        if(!(shadow.getDeviceCommunicationConfigurationConstructionValidation() instanceof DeviceCommunicationConfigurationConstructionValidationImpl)){
//            throw new BusinessException("illegalObjectConstruction",
//                    "It is not allowed to create a '{0}' without the correct construction validator.",
//                    this.getClass().getSimpleName());
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

    /**
     * Deletes the existing {@link com.energyict.mdc.device.config.PartialConnectionTask} that matches
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
        return Collections.unmodifiableList(securityPropertySets);
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
    public List<PartialOutboundConnectionTaskImpl> getPartialOutboundConnectionTasks() {
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

    @Override
    public void remove(PartialConnectionTask partialConnectionTask) {
        partialConnectionTasks.remove(partialConnectionTask);
    }

    private List<PartialConnectionTask> findAllPartialConnectionTasks() {
        return Collections.unmodifiableList(this.partialConnectionTasks);
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
            return task instanceof PartialInboundConnectionTaskImpl;
        }
    }

    private class PartialConnectionInitiationTaskFilterPredicate implements PartialConnectionTaskFilterPredicate<PartialConnectionInitiationTask> {

        @Override
        public boolean retain(PartialConnectionTask task) {
            return task instanceof PartialConnectionInitiationTaskImpl;
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
        securityPropertySets.add(securityPropertySet);
    }

    @Override
    public PartialOutboundConnectionTaskBuilder createPartialOutboundConnectionTask() {
        return new PartialOutboundConnectionTaskBuilderImpl(dataModel, this);
    }

    @Override
    public PartialInboundConnectionTaskBuilder createPartialInboundConnectionTask() {
        return new PartialInboundConnectionTaskBuilderImpl(dataModel, this);
    }

    @Override
    public PartialConnectionInitiationTaskBuilder createPartialConnectionInitiationTask() {
        return new PartialConnectionInitiationTaskBuilderImpl(dataModel, this);
    }

    @Override
    public void addPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
        partialConnectionTasks.add(partialConnectionTask);
    }

    @Override
    public ProtocolDialectConfigurationProperties createProtocolDialectConfigurationProperties(String name, DeviceProtocolDialect protocolDialect) {
        ProtocolDialectConfigurationProperties props = ProtocolDialectConfigurationPropertiesImpl.from(dataModel, this, name, protocolDialect);
        configurationPropertiesList.add(props);
        return props;
    }

    @Override
    public SecurityPropertySetBuilder createSecurityPropertySet(String name) {
        return new InternalSecurityPropertySetBuilder(name);
    }

    @Override
    public void removeSecurityPropertySet(SecurityPropertySet propertySet) {
        securityPropertySets.remove(propertySet);
    }

    private class InternalSecurityPropertySetBuilder implements SecurityPropertySetBuilder {
        private final SecurityPropertySetImpl underConstruction;

        private InternalSecurityPropertySetBuilder(String name) {
            this.underConstruction = SecurityPropertySetImpl.from(dataModel, DeviceCommunicationConfigurationImpl.this, name);
        }

        @Override
        public SecurityPropertySetBuilder authenticationLevel(int level) {
            underConstruction.setAuthenticationLevel(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder encryptionLevel(int level) {
            underConstruction.setEncryptionLevelId(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder addUserAction(DeviceSecurityUserAction userAction) {
            underConstruction.addUserAction(userAction);
            return this;
        }

        @Override
        public SecurityPropertySet build() {
            DeviceCommunicationConfigurationImpl.this.addSecurityPropertySet(underConstruction);
            return underConstruction;
        }
    }
}
