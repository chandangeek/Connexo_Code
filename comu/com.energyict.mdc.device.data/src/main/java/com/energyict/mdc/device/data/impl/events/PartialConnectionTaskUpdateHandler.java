/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles update events that are being sent when a {@link PartialConnectionTask}
 * was updated and will check if any properties were removed.
 * If that was the case, it will post another event to be handled
 * a-synchronously and mark all dependent connection tasks as incomplete.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-14 (16:50)
 */
@Component(name = "com.energyict.mdc.device.data.update.partialconnectiontask.eventhandler", service = Subscriber.class, immediate = true)
@SuppressWarnings("unused")
public class PartialConnectionTaskUpdateHandler extends EventHandler<LocalEvent> {

    static final Pattern TOPIC = Pattern.compile("com/energyict/mdc/device/config/partial(.*)connectiontask/UPDATED");

    private volatile MessageService messageService;

    // For OSGi purposes
    public PartialConnectionTaskUpdateHandler() {
        super(LocalEvent.class);
    }

    // For testing purposes
    PartialConnectionTaskUpdateHandler(MessageService messageService) {
        this();
        this.setMessageService(messageService);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        Matcher matcher = TOPIC.matcher(topic);
        if (matcher.matches()) {
            PartialConnectionTaskUpdateDetails updateDetails = (PartialConnectionTaskUpdateDetails) event.getSource();
            if (!updateDetails.getAddedOrRemovedRequiredProperties().isEmpty()) {
                // At least one required property was removed, so post event
                StartConnectionTasksRevalidationAfterPropertyRemoval
                    .forPublishing(this.messageService)
                    .with(updateDetails.getId())
                    .publish();
            }

            if (connectionFunctionWasUpdated(updateDetails.getPartialConnectionTask(), updateDetails.getPreviousConnectionFunction())) {
                StartConnectionTasksRevalidationAfterConnectionFunctionModification
                    .forPublishing(this.messageService)
                    .with(updateDetails.getId(), updateDetails.getPreviousConnectionFunction().isPresent() ? updateDetails.getPreviousConnectionFunction().get().getId() : 0)
                    .publish();
            }
        }
    }

    private boolean connectionFunctionWasUpdated(PartialConnectionTask partialConnectionTask, Optional<ConnectionFunction> previousConnectionFunction) {
        Optional<ConnectionFunction> currentConnectionFunction = partialConnectionTask.getConnectionFunction();
        if (previousConnectionFunction.isPresent() && currentConnectionFunction.isPresent()) {
            return previousConnectionFunction.get().getId() != currentConnectionFunction.get().getId(); // Or in other words: both are present, but the function has changed
        } else {
            return previousConnectionFunction.isPresent() || currentConnectionFunction.isPresent(); // Or in other words: removed or added the function
        }                                                                                           // Returns false (no update) when both are missing
    }

}