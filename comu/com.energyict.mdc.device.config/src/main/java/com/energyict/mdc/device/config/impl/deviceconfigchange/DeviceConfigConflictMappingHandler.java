package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.impl.ServerDeviceType;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all changes related to updates on a DeviceConfiguration which can lead to a recalculation of
 * the DeviceConfigConflictMappings of the DeviceType
 */
@Component(name = "com.energyict.mdc.device.data.impl.events.DeviceConfigConflictMappingHandler", service = Subscriber.class, immediate = true)
public class DeviceConfigConflictMappingHandler extends EventHandler<LocalEvent> {

    static final Pattern TOPIC = Pattern.compile("com/energyict/mdc/device/config/partial(.*)connectiontask/DELETED");

    @Inject
    public DeviceConfigConflictMappingHandler() {
        super(LocalEvent.class);
    }

    @Override
    public void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        Matcher matcher = TOPIC.matcher(topic);
        if (matcher.matches()) {
            PartialConnectionTask partialConnectionTask = (PartialConnectionTask) event.getSource();
            ((ServerDeviceType) partialConnectionTask.getConfiguration().getDeviceType()).updateConflictingMappings();
        }
    }
}
