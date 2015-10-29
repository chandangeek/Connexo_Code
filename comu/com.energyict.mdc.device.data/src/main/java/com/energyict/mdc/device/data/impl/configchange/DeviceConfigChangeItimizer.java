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
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * This message handler will populate the queue with a 'change device config' action.
 * The provided devices are either listed exhaustively or defined by a search.
 */
public class DeviceConfigChangeItimizer implements MessageHandler {

    //TODO create the init to set the services

    private MessageService messageService;
    private JsonService jsonService;
    private SearchService searchService;
    private Thesaurus thesaurus;
    private ServerDeviceService deviceService;

    @Override
    public void process(Message message) {
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

    private void processMessagePost(SingleConfigChangeQueueMessage singleConfigChangeQueueMessage, DestinationSpec destinationSpec) {
        destinationSpec.message(jsonService.serialize(singleConfigChangeQueueMessage)).send();
    }

}
