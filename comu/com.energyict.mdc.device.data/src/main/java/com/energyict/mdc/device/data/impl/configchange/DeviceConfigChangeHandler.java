package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ItemizeConfigChangeQueueMessage;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.InvalidSearchDomain;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * This message handler that processes all messages related to config changes:
 * <ul>
 * <li>Delegating to single actions</li>
 * <li>Execution config changes on a single device</li>
 * <li>Checking to release the Business Lock</li>
 * </ul>
 */
public class DeviceConfigChangeHandler implements MessageHandler {

    //TODO create the init to set the services

    private MessageService messageService;
    private JsonService jsonService;
    private SearchService searchService;
    private Thesaurus thesaurus;
    private ServerDeviceService deviceService;
    private DeviceDataModelService deviceDataModelService;

    @Override
    public void process(Message message) {

    }

    private void processMessagePost(SingleConfigChangeQueueMessage singleConfigChangeQueueMessage, DestinationSpec destinationSpec) {
        destinationSpec.message(jsonService.serialize(singleConfigChangeQueueMessage)).send();
    }

    /**
     * Delegates a config change action for each device in the search
     */
    private class ConfigChangeItemizer implements Consumer<Message> {
        @Override
        public void accept(Message message) {
            DestinationSpec destinationSpec = messageService.getDestinationSpec(DeviceService.CONFIG_CHANGE_SINGLE_QUEUE_DESTINATION).orElseThrow(new NoDestinationSpecFound(thesaurus, DeviceService.CONFIG_CHANGE_SINGLE_QUEUE_DESTINATION));
            ItemizeConfigChangeQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), ItemizeConfigChangeQueueMessage.class);
            Stream<Device> deviceStream;
            if (queueMessage.search != null) {
                SearchDomain searchDomain = searchService.findDomain(Device.class.getName()).orElseThrow(() -> new InvalidSearchDomain(thesaurus, Device.class.getName()));
                SearchBuilder<Object> searchBuilder = searchService.search(searchDomain);

                //TODO complete the search
//                    searchDomain.getProperties().stream().
//                            filter(p -> queueMessage.search.searchItems.getPropertyValue(p) != null).
//                            forEach(searchableProperty -> {
//                                try {
//                                    if (searchableProperty.getSelectionMode() == SearchableProperty.SelectionMode.MULTI) {
//                                        searchBuilder.where(searchableProperty).in(getQueryParameterAsObjectList(queueMessage.filter, searchableProperty));
//                                    } else if (searchableProperty.getSpecification().getValueFactory().getValueType().equals(String.class)) {
//                                        searchBuilder.where(searchableProperty).likeIgnoreCase((String) getQueryParameterAsObject(queueMessage.filter, searchableProperty));
//                                    } else {
//                                        searchBuilder.where(searchableProperty).isEqualTo(getQueryParameterAsObject(queueMessage.filter, searchableProperty));
//                                    }
//                                } catch (InvalidValueException e) {
//                                    // LOG failure
//                                }
//                            });
                deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);
            } else {
                deviceStream = queueMessage.deviceMRIDs.stream().map(deviceService::findByUniqueMrid).filter(Optional::isPresent).map(Optional::get);
            }
            DeviceConfigChangeRequest deviceConfigChangeRequest = deviceService.findDeviceConfigChangeRequestById(queueMessage.deviceConfigChangeRequestId).orElseThrow(DeviceConfigurationChangeException.noDeviceConfigChangeRequestFound(thesaurus, queueMessage.deviceConfigChangeRequestId));
            deviceStream.forEach(
                    device -> {
                        DeviceConfigChangeInActionImpl deviceConfigChangeInAction = deviceConfigChangeRequest.addDeviceInAction(device);
                        processMessagePost(new SingleConfigChangeQueueMessage(device.getmRID(), queueMessage.destinationDeviceConfigurationId, deviceConfigChangeInAction.getId()), destinationSpec);
                    });
        }
    }

    /**
     * Checks to see if the Business lock should be removed
     */
    private class ReleaseBusinessLock implements Consumer<Message> {
        @Override
        public void accept(Message message) {
            long deviceConfigChangeRequestId = Long.valueOf(jsonService.deserialize(message.getPayload(), String.class));
            DeviceConfigChangeRequest deviceConfigChangeRequest = deviceService.findDeviceConfigChangeRequestById(deviceConfigChangeRequestId).orElseThrow(DeviceConfigurationChangeException.noDeviceConfigChangeRequestFound(thesaurus, deviceConfigChangeRequestId));
            deviceConfigChangeRequest.notifyDeviceInActionIsRemoved();
        }
    }

    private class SingleDeviceConfigChangeExecutor implements Consumer<Message> {

        @Override
        public void accept(Message message) {
            SingleConfigChangeQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), SingleConfigChangeQueueMessage.class);
            Device modifiedDevice = deviceDataModelService.getTransactionService().execute(() -> {
                Device device = deviceService.findByUniqueMrid(queueMessage.deviceMrid).orElseThrow(DeviceConfigurationChangeException.noDeviceFoundForConfigChange(thesaurus, queueMessage.deviceMrid));
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

}
