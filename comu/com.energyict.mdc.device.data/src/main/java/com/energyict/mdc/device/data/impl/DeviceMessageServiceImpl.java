/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceMessageEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.stream.Collectors.toList;

class DeviceMessageServiceImpl implements ServerDeviceMessageService {

    /**
     * Enum listing up all different Introspector types that can be used in method DeviceMessageServiceImpl#findDeviceMessageByIdentifier(MessageIdentifier)
     */
    public enum IntrospectorTypes {
        DatabaseId("databaseValue", "device"),
        DeviceIdentifierAndProtocolInfoParts("device", "protocolInfo"),
        Actual("actual", "device", "databaseValue");

        private final String[] roles;

        IntrospectorTypes(String... roles) {
            this.roles = roles;
        }

        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList(roles));
        }

        public static Optional<IntrospectorTypes> forName(String name) {
            return Arrays.stream(values()).filter(type -> type.name().equals(name)).findFirst();
        }
    }

    private final DeviceDataModelService deviceDataModelService;
    private final ThreadPrincipalService threadPrincipalService;
    private final MeteringGroupsService meteringGroupsService;
    private final Clock clock;

    @Inject
    DeviceMessageServiceImpl(DeviceDataModelService deviceDataModelService, ThreadPrincipalService threadPrincipalService, MeteringGroupsService meteringGroupsService, Clock clock) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.threadPrincipalService = threadPrincipalService;
        this.meteringGroupsService = meteringGroupsService;
        this.clock = clock;
        Services.deviceMessageFinder(this);
    }

    @Override
    public Optional<com.energyict.mdc.upl.messages.DeviceMessage> find(MessageIdentifier identifier) {
        return this.findDeviceMessageByIdentifier(identifier).map(com.energyict.mdc.upl.messages.DeviceMessage.class::cast);
    }

    @Override
    public Optional<DeviceMessage> findDeviceMessageById(long id) {
        return this.deviceDataModelService.dataModel().mapper(DeviceMessage.class).getOptional(id);
    }

    @Override
    public Optional<DeviceMessage> findAndLockDeviceMessageByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(DeviceMessage.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<DeviceMessage> findAndLockDeviceMessageById(long id) {
        return Optional.ofNullable(this.deviceDataModelService.dataModel().mapper(DeviceMessage.class).lock(id));
    }

    @Override
    public boolean willDeviceMessageBePickedUpByPlannedComTask(Device device, DeviceMessage deviceMessage) {
        return device.getComTaskExecutions().stream().
                filter(not(ComTaskExecution::isOnHold)).
                map(ComTaskExecution::getComTask).
                map(ComTask::getProtocolTasks).
                flatMap(Collection::stream).
                filter(task -> task instanceof MessagesTask).
                flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId());
    }

    @Override
    public boolean willDeviceMessageBePickedUpByComTask(Device device, DeviceMessage deviceMessage) {
        return device.getDeviceConfiguration().
                getComTaskEnablements().stream().
                map(ComTaskEnablement::getComTask).
                flatMap(comTask -> comTask.getProtocolTasks().stream()).
                filter(task -> task instanceof MessagesTask).
                flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId());
    }

    @Override
    public ComTask getPreferredComTask(Device device, DeviceMessage deviceMessage) {
        return device.getComTaskExecutions().stream().
                filter(cte -> cte.isAdHoc() && cte.isOnHold()).
                map(ComTaskExecution::getComTask).
                filter(comTask -> comTask.getProtocolTasks().stream().
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst(). // An adHoc comTask that has already been executed (nextExecTimestamp==null)
                orElse(device.
                getDeviceConfiguration().
                getComTaskEnablements().stream().
                map(ComTaskEnablement::getComTask).
                filter(ct -> device.getComTaskExecutions().stream().
                        map(ComTaskExecution::getComTask).
                        noneMatch(comTask -> comTask.getId() == ct.getId())).
                filter(comTask -> comTask.getProtocolTasks().stream().
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst(). // A Dangling ComTask -> There is no ComTaskExecution yet but the enabled comTask supports the device message category
                orElse(device.
                getComTaskExecutions().stream().
                sorted(Comparator.comparing(ComTaskExecution::isAdHoc).thenComparing(ComTaskExecution::isScheduledManually).thenComparing(ComTaskExecution::isOnHold).reversed()).
                map(ComTaskExecution::getComTask).
                filter(comTask -> comTask.getProtocolTasks().stream().
                        filter(task -> task instanceof MessagesTask).
                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                        anyMatch(category -> category.getId() == deviceMessage.getSpecification().getCategory().getId())).
                findFirst().
                orElse(null)));
    }

    @Override
    public boolean canUserAdministrateDeviceMessage(DeviceConfiguration deviceConfiguration, DeviceMessageId deviceMessageId) {
        if (threadPrincipalService.getPrincipal() instanceof User) {
            User currentUser = (User) threadPrincipalService.getPrincipal();
            if (currentUser != null) {

                Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = deviceConfiguration.getDeviceMessageEnablements()
                        .stream()
                        .filter(deviceMessageEnablement -> DeviceMessageId.find(deviceMessageEnablement.getDeviceMessageDbValue()).isPresent())
                        .filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(deviceMessageId))
                        .findFirst();
                if (deviceMessageEnablementOptional.isPresent()) {
                    DeviceMessageEnablement deviceMessageEnablement = deviceMessageEnablementOptional.get();
                    return deviceMessageEnablement
                            .getUserActions()
                            .stream()
                            .anyMatch(deviceMessageUserAction -> currentUser.hasPrivilege("MDC", deviceMessageUserAction.getPrivilege()));
                }
            }
        }
        return true;
    }

    @Override
    public Finder<DeviceMessage> findDeviceMessagesByFilter(DeviceMessageQueryFilter deviceMessageQueryFilter) {
        List<Condition> allFilterConditions = new ArrayList<>();
        if (deviceMessageQueryFilter.getDevice().isPresent()) {
            allFilterConditions.add(getDeviceSearchCondition(deviceMessageQueryFilter.getDevice().get()));
        } else if (!deviceMessageQueryFilter.getDeviceGroups().isEmpty()) {
            allFilterConditions.add(getDeviceGroupSearchCondition(deviceMessageQueryFilter.getDeviceGroups()));
        }
        if (!deviceMessageQueryFilter.getMessageCategories().isEmpty()) {
            List<Condition> deviceMessageConditions = getAllMessageCategorySearchConditions(deviceMessageQueryFilter);
            allFilterConditions.add(deviceMessageConditions.stream().reduce(Condition.FALSE, Condition::or));
        }
        if (!deviceMessageQueryFilter.getStatuses().isEmpty()) {
            List<Condition> messageStatusConditions = getMessageStatusSearchCondition(deviceMessageQueryFilter.getStatuses());
            allFilterConditions.add(messageStatusConditions.stream().reduce(Condition.FALSE, Condition::or));
        }
        if (deviceMessageQueryFilter.getReleaseDateStart().isPresent() || deviceMessageQueryFilter.getReleaseDateEnd().isPresent()) {
            List<Condition> releaseDateConditions = getReleaseDateConditions(deviceMessageQueryFilter);
            allFilterConditions.add(releaseDateConditions.stream().reduce(Condition.TRUE, Condition::and));
        }
        if (deviceMessageQueryFilter.getSentDateStart().isPresent() || deviceMessageQueryFilter.getSentDateEnd().isPresent()) {
            List<Condition> sentDateConditions = getSentDateConditions(deviceMessageQueryFilter);
            allFilterConditions.add(sentDateConditions.stream().reduce(Condition.TRUE, Condition::and));
        }
        if (deviceMessageQueryFilter.getCreationDateStart().isPresent() || deviceMessageQueryFilter.getCreationDateEnd().isPresent()) {
            List<Condition> creationDateConditions = getCreationDateConditions(deviceMessageQueryFilter);
            allFilterConditions.add(creationDateConditions.stream().reduce(Condition.TRUE, Condition::and));
        }
        Condition condition = allFilterConditions.stream().reduce(Condition.TRUE, Condition::and);
        return DefaultFinder.of(DeviceMessage.class, condition, this.deviceDataModelService.dataModel())
                .defaultSortColumn(DeviceMessageImpl.Fields.RELEASEDATE.fieldName(), false);
    }

    private List<Condition> getReleaseDateConditions(DeviceMessageQueryFilter deviceMessageQueryFilter) {
        List<Condition> releaseDateConditions = new ArrayList<>();
        deviceMessageQueryFilter.getReleaseDateStart().ifPresent(date ->
                releaseDateConditions.add(Where.where(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).isGreaterThanOrEqual(date)));
        deviceMessageQueryFilter.getReleaseDateEnd().ifPresent(date ->
                releaseDateConditions.add(Where.where(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).isLessThanOrEqual(date)));
        return releaseDateConditions;
    }

    private List<Condition> getSentDateConditions(DeviceMessageQueryFilter deviceMessageQueryFilter) {
        List<Condition> sentDateConditions = new ArrayList<>();
        deviceMessageQueryFilter.getSentDateStart().ifPresent(date ->
                sentDateConditions.add(Where.where(DeviceMessageImpl.Fields.SENTDATE.fieldName()).isGreaterThanOrEqual(date)));
        deviceMessageQueryFilter.getSentDateEnd().ifPresent(date ->
                sentDateConditions.add(Where.where(DeviceMessageImpl.Fields.SENTDATE.fieldName()).isLessThanOrEqual(date)));
        return sentDateConditions;
    }

    private List<Condition> getCreationDateConditions(DeviceMessageQueryFilter deviceMessageQueryFilter) {
        List<Condition> creationDateConditions = new ArrayList<>();
        deviceMessageQueryFilter.getCreationDateStart().ifPresent(date ->
                creationDateConditions.add(Where.where(DeviceMessageImpl.Fields.CREATIONDATE.fieldName()).isGreaterThanOrEqual(date)));
        deviceMessageQueryFilter.getCreationDateEnd().ifPresent(date ->
                creationDateConditions.add(Where.where(DeviceMessageImpl.Fields.CREATIONDATE.fieldName()).isLessThanOrEqual(date)));
        return creationDateConditions;
    }

    private List<Condition> getMessageStatusSearchCondition(Collection<DeviceMessageStatus> deviceMessageStatuses) {
        List<Condition> messageStatusConditions = new ArrayList<>();
        if (deviceMessageStatuses.contains(DeviceMessageStatus.PENDING)) {
            messageStatusConditions.add(Where.where(DeviceMessageImpl.Fields.DEVICEMESSAGESTATUS.fieldName()).isEqualTo(DeviceMessageStatus.WAITING)
                    .and(Where.where(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).isNotNull())
                    .and(Where.where(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).isLessThan(this.clock.instant())));
        }
        if (deviceMessageStatuses.contains(DeviceMessageStatus.WAITING)) {
            messageStatusConditions.add(Where.where(DeviceMessageImpl.Fields.DEVICEMESSAGESTATUS.fieldName()).isEqualTo(DeviceMessageStatus.WAITING)
                    .and(Where.where(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).isNull()
                            .or(Where.where(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).isGreaterThanOrEqual(this.clock.instant()))));
        }
        ArrayList<DeviceMessageStatus> reducedDeviceMessageStatuses = new ArrayList<>(deviceMessageStatuses);
        reducedDeviceMessageStatuses.remove(DeviceMessageStatus.WAITING);
        reducedDeviceMessageStatuses.remove(DeviceMessageStatus.PENDING);
        if (!reducedDeviceMessageStatuses.isEmpty()) {
            messageStatusConditions.add(Where.where(DeviceMessageImpl.Fields.DEVICEMESSAGESTATUS.fieldName()).in(reducedDeviceMessageStatuses));
        }
        return messageStatusConditions;
    }

    private List<Condition> getAllMessageCategorySearchConditions(DeviceMessageQueryFilter deviceMessageQueryFilter) {
        List<Condition> deviceMessageConditions = new ArrayList<>();
        deviceMessageConditions.add(Where.where(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).in(
                deviceMessageQueryFilter.getMessageCategories().stream()
                        .flatMap(dmc -> getMessageCategorySearchCondition(deviceMessageQueryFilter, dmc).stream()).collect(Collectors.toList())));
        return deviceMessageConditions;
    }

    private List<Long> getMessageCategorySearchCondition(DeviceMessageQueryFilter deviceMessageQueryFilter, DeviceMessageCategory deviceMessageCategory) {
        List<DeviceMessageId> allDeviceMessageIdInCategory = deviceMessageCategory.getMessageSpecifications()
                .stream()
                .map(DeviceMessageSpec::getId)
                .collect(toList());
        List<DeviceMessageId> specificDeviceMessageIds = deviceMessageQueryFilter.getDeviceMessages()
                .stream()
                .filter(allDeviceMessageIdInCategory::contains)
                .collect(toList());
        List<Long> deviceMessageDbIds;
        if (specificDeviceMessageIds.isEmpty()) {
            deviceMessageDbIds = allDeviceMessageIdInCategory.stream()
                    .map(DeviceMessageId::dbValue)
                    .collect(toList());
        } else {
            deviceMessageDbIds = specificDeviceMessageIds.stream()
                    .map(DeviceMessageId::dbValue)
                    .collect(toList());
        }
        return deviceMessageDbIds;
    }

    private Condition getDeviceGroupSearchCondition(Collection<EndDeviceGroup> endDeviceGroups) {
        return endDeviceGroups.stream()
                .map(endDeviceGroup -> ListOperator.IN.contains(endDeviceGroup.toSubQuery("AMRID"), DeviceMessageImpl.Fields.DEVICE.fieldName()))
                .map(Condition.class::cast)
                .reduce(Condition.FALSE, Condition::or);
    }

    private Condition getDeviceSearchCondition(Device device) {
        return where(DeviceMessageImpl.Fields.DEVICE.fieldName()).isEqualTo(device);
    }

    @Override
    public Optional<DeviceMessage> findDeviceMessageByIdentifier(MessageIdentifier identifier) {
        try {
            return this.exactlyOne(this.find(identifier.forIntrospection()), identifier);
        } catch (UnsupportedDeviceMessageIdentifierTypeName | IllegalArgumentException | NotUniqueException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<DeviceMessage> findDeviceFirmwareMessages(Device device) {
        Condition condition = where(DeviceMessageImpl.Fields.DEVICE.fieldName()).isEqualTo(device)
                .and(where(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).isGreaterThanOrEqual(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE.dbValue()))
                .and(where(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).isLessThanOrEqual(DeviceMessageId.TRANSFER_HES_CA_CONFIG_IMAGE.dbValue()));
        return this.deviceDataModelService
                .dataModel()
                .query(DeviceMessage.class)
                .select(condition);
    }

    @Override
    public List<DeviceMessageId> findKeyRenewalMessages() {
        return EndDeviceControlTypeMapping.KEY_RENEWAL.getPossibleDeviceMessageIds();
    }

    private List<DeviceMessage> find(Introspector introspector) throws UnsupportedDeviceMessageIdentifierTypeName {
        if (introspector.getTypeName().equals(IntrospectorTypes.DatabaseId.name())) {
            return this.findDeviceMessageById(Long.valueOf(introspector.getValue(IntrospectorTypes.DatabaseId.roles[0]).toString()))
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        } else if (introspector.getTypeName().equals(IntrospectorTypes.DeviceIdentifierAndProtocolInfoParts.name())) {
            DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue(IntrospectorTypes.DeviceIdentifierAndProtocolInfoParts.roles[0]);
            String[] messageProtocolInfoParts = (String[]) introspector.getValue(IntrospectorTypes.DeviceIdentifierAndProtocolInfoParts.roles[1]);
            return this.deviceDataModelService.deviceService()
                    .findDeviceByIdentifier(deviceIdentifier)
                    .map(device -> this.findByDeviceAndProtocolInfoParts(device, messageProtocolInfoParts))
                    .orElseGet(Collections::emptyList);
        } else if (introspector.getTypeName().equals(IntrospectorTypes.Actual.name())) {
            return Collections.singletonList((DeviceMessage) introspector.getValue(IntrospectorTypes.Actual.roles[0]));
        } else {
            throw new UnsupportedDeviceMessageIdentifierTypeName();
        }
    }

    private Optional<DeviceMessage> exactlyOne(List<DeviceMessage> allMessages, MessageIdentifier identifier) {
        if (allMessages.isEmpty()) {
            return Optional.empty();
        } else {
            if (allMessages.size() > 1) {
                throw new NotUniqueException(identifier.toString());
            } else {
                return Optional.of(allMessages.get(0));
            }
        }
    }

    protected List<DeviceMessage> findByDeviceAndProtocolInfoParts(Device device, String... protocolInfoParts) {
        Condition protocolInfoPartsCondition =
                Stream
                        .of(protocolInfoParts)
                        .reduce(
                                Condition.TRUE,
                                (condition, protocolInfoPart) -> condition.and(where(DeviceMessageImpl.Fields.PROTOCOLINFO.fieldName()).like(protocolInfoPart)),
                                Condition::and);
        Condition deviceCondition = where(DeviceMessageImpl.Fields.DEVICE.fieldName()).isEqualTo(device);
        return this.deviceDataModelService
                .dataModel()
                .query(DeviceMessage.class)
                .select(deviceCondition.and(protocolInfoPartsCondition));
    }

    private static class UnsupportedDeviceMessageIdentifierTypeName extends RuntimeException {
    }

}
