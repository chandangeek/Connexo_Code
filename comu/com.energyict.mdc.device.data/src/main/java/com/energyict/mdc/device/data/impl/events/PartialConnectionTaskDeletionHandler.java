package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles delete events that are being sent when a {@link PartialConnectionTask}
 * is about to be deleted and will veto the delete when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-09 (13:58)
 */
@Component(name="com.energyict.mdc.device.data.delete.partialconnectiontask.eventhandler", service = Subscriber.class, immediate = true)
public class PartialConnectionTaskDeletionHandler extends EventHandler<LocalEvent> {

    static final Pattern TOPIC = Pattern.compile("com/energyict/mdc/device/config/partial(.*)connectiontask/VALIDATE_DELETE");

    private volatile ServerDeviceDataService deviceDataService;
    private volatile Thesaurus thesaurus;

    public PartialConnectionTaskDeletionHandler() {
        super(LocalEvent.class);
    }

    @Inject
    PartialConnectionTaskDeletionHandler(ServerDeviceDataService deviceDataService) {
        this();
        this.deviceDataService = deviceDataService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.setDeviceDataService((ServerDeviceDataService) deviceDataService);
    }

    private void setDeviceDataService(ServerDeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
        this.thesaurus = deviceDataService.getThesaurus();
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        String topic = event.getType().getTopic();
        Matcher matcher = TOPIC.matcher(topic);
        if (matcher.matches()) {
            PartialConnectionTask partialConnectionTask = (PartialConnectionTask) event.getSource();
            this.validateNotUsedByDevice(partialConnectionTask);
        }
    }

    /**
     * Vetos the delection of the {@link PartialConnectionTask}
     * by throwing an exception when the PartialConnectionTask
     * is used by at least on Device, i.e. at least one
     * ConnectionTask that uses it on a Device.
     *
     * @param partialConnectionTask The PartialConnectionTask that is about to be deleted
     */
    private void validateNotUsedByDevice(PartialConnectionTask partialConnectionTask) {
        if (this.deviceDataService.hasConnectionTasks(partialConnectionTask)) {
            throw new VetoDeletePartialConnectionTaskException(this.thesaurus, partialConnectionTask);
        }
    }

}