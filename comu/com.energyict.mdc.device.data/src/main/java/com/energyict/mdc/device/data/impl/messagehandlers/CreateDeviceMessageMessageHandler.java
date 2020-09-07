/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.DeviceMessageQueueMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a single DeviceMessage for a single device with the provided attributes
 * Created by bvn on 3/25/15.
 */
public class CreateDeviceMessageMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(CreateDeviceMessageMessageHandler.class.getSimpleName());
    private JsonService jsonService;
    private DeviceService deviceService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private ThreadPrincipalService threadPrincipalService;
    private SecurityManagementService securityManagementService;

    @Override
    public void process(Message message) {
        DeviceMessageQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), DeviceMessageQueueMessage.class);
        threadPrincipalService.set(() -> queueMessage.createdByUser);
        Optional<Device> deviceOptional = deviceService.findDeviceById(queueMessage.deviceId);
        if (deviceOptional.isPresent()) {
            Optional<DeviceMessageSpec> deviceMessageSpec = deviceMessageSpecificationService.findMessageSpecById(queueMessage.deviceMessageId.dbValue());
            if (deviceMessageSpec.isPresent()) {
                Device.DeviceMessageBuilder deviceMessageBuilder = deviceOptional.get().newDeviceMessage(queueMessage.deviceMessageId)
                        .setReleaseDate(Instant.ofEpochSecond(queueMessage.releaseDate));

                for (PropertySpec propertySpec : deviceMessageSpec.get().getPropertySpecs()) {
                    if (queueMessage.properties.containsKey(propertySpec.getName())) {
                        Object value = queueMessage.properties.get(propertySpec.getName());
                        if (value instanceof LinkedHashMap && ((LinkedHashMap) value).containsKey("hsmKeyType") && ((LinkedHashMap) value).containsKey("name")) {
                            String name = ((LinkedHashMap) value).get("name").toString();
                            value = securityManagementService.findSecurityAccessorTypeByName(name).orElseThrow(() -> new RuntimeException("No such security accessor type: " + name));
                        }
                        try {
                            Object marshalPropertyValue = propertySpec.getValueFactory().fromStringValue(String.valueOf(value));
                            deviceMessageBuilder.addProperty(propertySpec.getName(), marshalPropertyValue);
                            LOGGER.info(String.format("Set property '%s' on device command '%s' to value '%s'", propertySpec.getName(), queueMessage.deviceMessageId, value));
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, String.format("Failed to set property '%s' on device command '%s': value '%s' was refused: %s", propertySpec.getName(), queueMessage.deviceMessageId, value, e.getMessage()));
                        }
                    }
                }
                DeviceMessage deviceMessage = deviceMessageBuilder.add();
                if (queueMessage.trigger) {
                    Device device = deviceOptional.get();
                    Optional<ComTaskEnablement> messageEnabledComTaskEnablement = device.getDeviceConfiguration().getComTaskEnablements().stream().filter(comTaskEnablement -> canPerformDeviceCommand(comTaskEnablement, deviceMessage.getDeviceMessageId())).findAny();
                    messageEnabledComTaskEnablement.ifPresent(comTaskEnablement -> {
                        Optional<ComTaskExecution> messageEnabledComTask = device.getComTaskExecutions().stream().filter(comTaskExecution -> comTaskExecution.getComTask().getId() == comTaskEnablement.getComTask().getId()).findAny();
                        if (messageEnabledComTask.isPresent()) {
                            messageEnabledComTask.get().runNow();
                        } else {
                            ComTaskExecution comTaskExecution = device.newAdHocComTaskExecution(comTaskEnablement).add();
                            comTaskExecution.runNow();
                        }
                        messageEnabledComTask.ifPresent(ComTaskExecution::runNow);
                    });

                }
                LOGGER.info(String.format("Added device command '%s' on device '%s'", queueMessage.deviceMessageId, deviceOptional.get().getName()));
            } else {
                LOGGER.log(Level.SEVERE, "Could not find device message spec with db value " + queueMessage.deviceMessageId.dbValue());
            }
        } else {
            LOGGER.log(Level.SEVERE, "Could not find device with id " + queueMessage.deviceId);
        }
    }

    private boolean canPerformDeviceCommand(ComTaskEnablement comTaskExecution, DeviceMessageId deviceMessageId) {
        Optional<ProtocolTask> messagesTask = comTaskExecution.getComTask().getProtocolTasks().stream().filter(protocolTask -> protocolTask instanceof MessagesTask).findAny();
        return messagesTask.filter(protocolTask -> ((MessagesTask) protocolTask).getDeviceMessageCategories().stream().anyMatch(deviceMessageCategory -> deviceMessageCategory.getMessageSpecifications().stream().anyMatch(deviceMessageSpec -> deviceMessageSpec.getId().equals(deviceMessageId)))).isPresent();
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(JsonService jsonService, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, ThreadPrincipalService threadPrincipalService, SecurityManagementService securityManagementService) {
        this.jsonService = jsonService;
        this.deviceService = deviceService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.threadPrincipalService = threadPrincipalService;
        this.securityManagementService = securityManagementService;
        return this;
    }
}
