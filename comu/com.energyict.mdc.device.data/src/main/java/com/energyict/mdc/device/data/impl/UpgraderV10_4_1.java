package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory;

import javax.inject.Inject;
import java.util.Optional;

public class UpgraderV10_4_1 implements Upgrader {

    private final DataModel dataModel;
    private final UserService userService;
    private final EventService eventService;
    private final PrivilegesProviderV10_4_1 privilegesProviderV10_4_1;
    private final MessageService messageService;

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    @Inject
    public UpgraderV10_4_1(DataModel dataModel, UserService userService, PrivilegesProviderV10_4_1 privilegesProviderV10_4_1, MessageService messageService, EventService eventService) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.privilegesProviderV10_4_1 = privilegesProviderV10_4_1;
        this.messageService = messageService;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10,4,1));
        userService.addModulePrivileges(privilegesProviderV10_4_1);
        installNewMessageHandlers();
        installNewEventTypeAndPublish();
        createSubscriberForMessageQueue();
    }

    private void installNewMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandlerIfNotYetPresent(defaultQueueTableSpec, CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME, SubscriberTranslationKeys.CRL_REQUEST_TASK_SUBSCRIBER);
    }

    private void createMessageHandlerIfNotYetPresent(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
            }
        }
    }

    private void installNewEventTypeAndPublish() {
        if (!this.eventService.getEventType(EventType.DEVICE_UPDATED_IPADDRESSV6.topic()).isPresent()) {
            EventTypeBuilder builder = eventService.buildEventTypeWithTopic(EventType.DEVICE_UPDATED_IPADDRESSV6.topic())
                    .name(EventType.DEVICE_UPDATED_IPADDRESSV6.name())
                    .component(DeviceDataServices.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System");
            this.addCustomProperties(builder).create();
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        eventTypeBuilder
                .withProperty("MRID", ValueType.STRING, "MRID")
                .withProperty("IPv6Address", ValueType.STRING, "IPv6Address")
                .shouldPublish();
        return eventTypeBuilder;
    }
    public static final TranslationKey IPV6ADDRESS_SUBSCRIBER_DISPLAYNAME =
            new SimpleTranslationKey("IPv6AddressSubscriber",
                    "Handle events to propagate ipv6 change address into MSG_RAWTOPICTABLE");  //lori

    private void createSubscriberForMessageQueue() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            if (!jupiterEvents.getSubscribers().stream()
                    .anyMatch(s -> s.getName().equals(IPV6ADDRESS_SUBSCRIBER_DISPLAYNAME.getKey()))) {
                jupiterEvents
                        .subscribe(IPV6ADDRESS_SUBSCRIBER_DISPLAYNAME,
                                DeviceDataServices.COMPONENT_NAME,
                                Layer.DOMAIN,
                                DestinationSpec.whereCorrelationId()
                                        .isEqualTo(EventType.DEVICE_UPDATED_IPADDRESSV6.topic()));
            }
        }
    }
}