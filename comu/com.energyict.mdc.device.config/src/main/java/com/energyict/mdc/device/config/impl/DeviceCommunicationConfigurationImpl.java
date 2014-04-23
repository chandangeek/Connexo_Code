package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
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
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.ServerDeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.config.exceptions.CannotDisableComTaskThatWasNotEnabledException;
import com.energyict.mdc.device.config.exceptions.PartialConnectionTaskDoesNotExist;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import javax.inject.Inject;
import javax.validation.Valid;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceCommunicationConfiguration} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-15 (11:02)
 */
public class DeviceCommunicationConfigurationImpl extends PersistentIdObject<DeviceCommunicationConfiguration> implements ServerDeviceCommunicationConfiguration {

    enum Fields {
        COM_TASK_ENABLEMENTS("comTaskEnablements"),
        SECURITY_PROPERTY_SETS("securityPropertySets");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }
    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private List<SecurityPropertySet> securityPropertySets = new ArrayList<>();
    private List<ComTaskEnablement> comTaskEnablements = new ArrayList<>();
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
        for (Iterator<PartialConnectionTask> iterator = partialConnectionTasks.iterator(); iterator.hasNext(); ) {
            PartialConnectionTask next = iterator.next();
            if (next.getId() == id) {
                iterator.remove();
                return;
            }
        }
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
    public List<PartialScheduledConnectionTaskImpl> getPartialOutboundConnectionTasks() {
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
        if (partialConnectionTasks.remove(partialConnectionTask) && getId() > 0) {
            eventService.postEvent(((PersistentIdObject) partialConnectionTask).deleteEventType().topic(), partialConnectionTask);
        }
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
        for (DeviceProtocolDialect deviceProtocolDialect : deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getDeviceProtocolDialects()) {
            findOrCreateProtocolDialectConfigurationProperties(deviceProtocolDialect);
        }
        return this;
    }

    private interface PartialConnectionTaskFilterPredicate<T extends PartialConnectionTask> {

        public boolean retain(PartialConnectionTask task);

    }

    private class PartialOutboundConnectionTaskFilterPredicate implements PartialConnectionTaskFilterPredicate<PartialScheduledConnectionTask> {

        @Override
        public boolean retain(PartialConnectionTask task) {
            return task instanceof PartialScheduledConnectionTask;
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
        Save.CREATE.validate(dataModel, securityPropertySet);
        securityPropertySets.add(securityPropertySet);
    }

    @Override
    public PartialScheduledConnectionTaskBuilder newPartialScheduledConnectionTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ConnectionStrategy connectionStrategy) {
        return new PartialScheduledConnectionTaskBuilderImpl(dataModel, this).name(name)
                .pluggableClass(connectionType)
                .rescheduleDelay(rescheduleRetryDelay)
                .connectionStrategy(connectionStrategy);
    }

    @Override
    public PartialInboundConnectionTaskBuilder newPartialInboundConnectionTask(String name, ConnectionTypePluggableClass connectionType) {
        return new PartialInboundConnectionTaskBuilderImpl(dataModel, this)
                .name(name)
                .pluggableClass(connectionType);
    }

    @Override
    public PartialConnectionInitiationTaskBuilder newPartialConnectionInitiationTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay) {
        return new PartialConnectionInitiationTaskBuilderImpl(dataModel, this)
                .name(name)
                .pluggableClass(connectionType)
                .rescheduleDelay(rescheduleRetryDelay);
    }

    public void addPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
        Save.CREATE.validate(dataModel, partialConnectionTask);
        partialConnectionTasks.add(partialConnectionTask);
    }

    @Override
    public ProtocolDialectConfigurationProperties findOrCreateProtocolDialectConfigurationProperties(DeviceProtocolDialect protocolDialect) {
        for (ProtocolDialectConfigurationProperties candidate : configurationPropertiesList) {
            if (candidate.getDeviceProtocolDialect().equals(protocolDialect)) {
                return candidate;
            }
        }
        ProtocolDialectConfigurationProperties props = ProtocolDialectConfigurationPropertiesImpl.from(dataModel, this, protocolDialect);
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

    @Override
    public void save() {
        boolean created = getId() == 0;
        super.save();
        for (PartialConnectionTask partialConnectionTask : partialConnectionTasks) {
            eventService.postEvent(((PersistentIdObject) partialConnectionTask).createEventType().topic(), partialConnectionTask);
        }
    }

    @Override
    public List<ComTaskEnablement> getComTaskEnablements() {
        return Collections.unmodifiableList(this.comTaskEnablements);
    }

    @Override
    public ComTaskEnablementBuilder enableComTask(ComTask comTask, SecurityPropertySet securityPropertySet) {
        ComTaskEnablementImpl underConstruction = dataModel.getInstance(ComTaskEnablementImpl.class).initialize(DeviceCommunicationConfigurationImpl.this, comTask, securityPropertySet);
        return new ComTaskEnablementBuilderImpl(underConstruction);
    }

    @Override
    public void disableComTask(ComTask comTask) {
        Iterator<ComTaskEnablement> comTaskEnablementIterator = this.comTaskEnablements.iterator();
        while (comTaskEnablementIterator.hasNext()) {
            ComTaskEnablement comTaskEnablement = comTaskEnablementIterator.next();
            if (comTaskEnablement.getComTask().getId() == comTask.getId()) {
                ComTaskEnablementImpl each = (ComTaskEnablementImpl) comTaskEnablement;
                each.validateDelete();
                comTaskEnablementIterator.remove();
                return;
            }
        }
        throw new CannotDisableComTaskThatWasNotEnabledException(this.getThesaurus(), this.getDeviceConfiguration(), comTask);
    }

    private void addComTaskEnblement (ComTaskEnablementImpl comTaskEnablement) {
        comTaskEnablement.adding();
        Save.CREATE.validate(this.dataModel, comTaskEnablement);
        this.comTaskEnablements.add(comTaskEnablement);
        comTaskEnablement.added();
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

    private enum ComTaskEnablementBuildingMode {
        UNDERCONSTRUCTION {
            @Override
            protected void verify() {
                // All calls are fine as long as we are under construction
            }
        },
        COMPLETE {
            @Override
            protected void verify() {
                throw new IllegalStateException("The communication task enablement building process is already complete");
            }
        };

        protected abstract void verify();
    }

    private class ComTaskEnablementBuilderImpl implements ComTaskEnablementBuilder {
        private ComTaskEnablementBuildingMode mode;
        private ComTaskEnablementImpl underConstruction;

        private ComTaskEnablementBuilderImpl(ComTaskEnablementImpl underConstruction) {
            super();
            this.mode = ComTaskEnablementBuildingMode.UNDERCONSTRUCTION;
            this.underConstruction = underConstruction;
        }

        @Override
        public ComTaskEnablementBuilder setNextExecutionSpecsFrom(TemporalExpression temporalExpression) {
            this.mode.verify();
            this.underConstruction.setNextExecutionSpecsFrom(temporalExpression);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setIgnoreNextExecutionSpecsForInbound(boolean flag) {
            this.mode.verify();
            this.underConstruction.setIgnoreNextExecutionSpecsForInbound(flag);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
            this.mode.verify();
            this.underConstruction.setPartialConnectionTask(partialConnectionTask);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties) {
            this.mode.verify();
            this.underConstruction.setProtocolDialectConfigurationProperties(properties);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder useDefaultConnectionTask(boolean flagValue) {
            this.mode.verify();
            this.underConstruction.useDefaultConnectionTask(flagValue);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setPriority(int priority) {
            this.mode.verify();
            this.underConstruction.setPriority(priority);
            return this;
        }

        @Override
        public ComTaskEnablement add() {
            this.mode.verify();
            addComTaskEnblement(this.underConstruction);
            this.mode = ComTaskEnablementBuildingMode.COMPLETE;
            return this.underConstruction;
        }

    }

}
