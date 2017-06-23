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
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import oracle.jdbc.driver.DMSFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.stream.Collectors.toList;

class DeviceMessageServiceImpl implements DeviceMessageService {

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
        if (!deviceMessageQueryFilter.getDeviceGroups().isEmpty()) {
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

        Condition condition = allFilterConditions.stream().reduce(Condition.TRUE, Condition::and);
        return DefaultFinder.of(DeviceMessage.class, condition, this.deviceDataModelService.dataModel());
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
        for (DeviceMessageCategory deviceMessageCategory : deviceMessageQueryFilter.getMessageCategories()) {
            List<Long> deviceMessageDbIds = getMessageCategorySearchCondition(deviceMessageQueryFilter, deviceMessageCategory);
            deviceMessageConditions.add(Where.where(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).in(deviceMessageDbIds));
        }
        return deviceMessageConditions;
    }

    private List<Long> getMessageCategorySearchCondition(DeviceMessageQueryFilter deviceMessageQueryFilter, DeviceMessageCategory deviceMessageCategory) {
        List<DeviceMessageId> allDeviceMessageIdInCategory = deviceMessageCategory.getMessageSpecifications()
                .stream()
                .map(DeviceMessageSpec::getId)
                .collect(toList());
        List<DeviceMessageId> deviceMessageIds = deviceMessageQueryFilter.getDeviceMessages()
                .stream()
                .filter(allDeviceMessageIdInCategory::contains)
                .collect(toList());
        List<Long> deviceMessageDbIds;
        if (deviceMessageIds.isEmpty()) {
            deviceMessageDbIds = allDeviceMessageIdInCategory.stream()
                    .map(DeviceMessageId::dbValue)
                    .collect(toList());
        } else {
            deviceMessageDbIds = deviceMessageIds.stream()
                    .map(DeviceMessageId::dbValue)
                    .collect(toList());
        }
        return deviceMessageDbIds;
    }

    private Condition getDeviceGroupSearchCondition(Collection<EndDeviceGroup> endDeviceGroups) {
        return endDeviceGroups.stream()
                .map(endDeviceGroup -> ListOperator.IN.contains(endDeviceGroup.toSubQuery("id"), DeviceMessageImpl.Fields.DEVICE.fieldName()))
                .map(Condition.class::cast)
                .reduce(Condition.FALSE, Condition::or);
    }

    @Override
    public Optional<DeviceMessage> findDeviceMessageByIdentifier(MessageIdentifier identifier) {
        try {
            return this.exactlyOne(this.find(identifier.forIntrospection()), identifier);
        } catch (UnsupportedDeviceMessageIdentifierTypeName | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private List<DeviceMessage> find(Introspector introspector) throws UnsupportedDeviceMessageIdentifierTypeName {
        switch (introspector.getTypeName()) {
            case "DatabaseId": {
                return this
                        .findDeviceMessageById(Long.valueOf(introspector.getValue("databaseValue").toString()))
                        .map(Collections::singletonList)
                        .orElseGet(Collections::emptyList);
            }
            case "DeviceIdentifierAndProtocolInfoParts": {
                DeviceIdentifier deviceIdentifier = (DeviceIdentifier) introspector.getValue("device");
                String[] messageProtocolInfoParts = (String[]) introspector.getValue("protocolInfo");
                return this.deviceDataModelService.deviceService()
                        .findDeviceByIdentifier(deviceIdentifier)
                        .map(device -> this.findByDeviceAndProtocolInfoParts(device, messageProtocolInfoParts))
                        .orElseGet(Collections::emptyList);
            }
            case "Actual": {
                return Collections.singletonList((DeviceMessage) introspector.getValue("actual"));
            }
            default: {
                throw new UnsupportedDeviceMessageIdentifierTypeName();
            }
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

    public List<DeviceMessage> findByDeviceAndProtocolInfoParts(Device device, String... protocolInfoParts) {
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