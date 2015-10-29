package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.ComScheduleOnDeviceQueueMessage;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceImpl;

/**
 * Copyrights EnergyICT
 * Date: 29.10.15
 * Time: 10:34
 */
public class DeviceConfigChangeMessagHandler implements MessageHandler {

    private JsonService jsonService;
    private DeviceService deviceService;
    private DeviceDataModelService deviceDataModelService;
    private Thesaurus thesaurus;

    @Override
    public void process(Message message) {
        SingleConfigChangeQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), SingleConfigChangeQueueMessage.class);
        Device modifiedDevice = deviceDataModelService.getTransactionService().execute(() -> {
            Device device = this.deviceService.findByUniqueMrid(queueMessage.deviceMrid).orElseThrow(DeviceConfigurationChangeException.noDeviceFoundForConfigChange(thesaurus, queueMessage.deviceMrid));
            ((ServerDeviceForConfigChange) device).lock();
            Device deviceWithNewConfig = DeviceConfigChangeExecutor.getInstance().execute((DeviceImpl) device, deviceDataModelService.deviceConfigurationService().findDeviceConfiguration(queueMessage.destinationDeviceConfigurationId).get());
            deviceDataModelService.messageService()
                    .getDestinationSpec(DeviceService.FINISHED_SINGLE_DEVICE_CONFIG_CHANGE_DESTINATION)
                    .orElseThrow(new NoDestinationSpecFound(thesaurus, DeviceService.FINISHED_SINGLE_DEVICE_CONFIG_CHANGE_DESTINATION))
                    .message(String.valueOf(queueMessage.deviceConfigChangeInActionId));
            return deviceWithNewConfig;
        });
    }
}
