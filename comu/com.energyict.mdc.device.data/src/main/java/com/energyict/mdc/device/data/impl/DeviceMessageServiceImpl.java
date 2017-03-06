package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.not;

class DeviceMessageServiceImpl implements DeviceMessageService {

    private final DeviceDataModelService deviceDataModelService;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    DeviceMessageServiceImpl(DeviceDataModelService deviceDataModelService, ThreadPrincipalService threadPrincipalService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.threadPrincipalService = threadPrincipalService;
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
                this.deviceDataModelService.deviceService()
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