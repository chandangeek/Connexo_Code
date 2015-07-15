package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.EventType;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.util.Checks;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

    static final Pattern TOPIC = Pattern.compile("com/energyict/mdc/device/config/partial(.*)connectiontask/UPDATE");

    private volatile EventService eventService;

    // For OSGi purposes
    public PartialConnectionTaskUpdateHandler() {
        super(LocalEvent.class);
    }

    // For testing purposes
    PartialConnectionTaskUpdateHandler(EventService eventService) {
        this();
        this.setEventService(eventService);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        Matcher matcher = TOPIC.matcher(topic);
        if (matcher.matches()) {
            String removedProperties = this.getRemovedProperties(event);
            if (!Checks.is(removedProperties).empty()) {
                // At least one required property was removed, so post event
                PartialConnectionTask partialConnectionTask = (PartialConnectionTask) event.getSource();
                this.eventService.postEvent(EventType.CONNECTIONTASK_CHECK_ALL_ACTIVE.topic(), new RevalidateAllConnectionTasksAfterPropertyRemoval(partialConnectionTask.getId()));
            }
        }
    }

    private String getRemovedProperties(LocalEvent event) {
        return (String) event.toOsgiEvent().getProperty("removedRequiredProperties");
    }

}