package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.MeterIssueEvent;
import com.energyict.mdc.issue.datacollection.impl.UnableToCreateEventException;
import org.osgi.service.event.EventConstants;

import java.util.Map;
import java.util.logging.Logger;

public class DataCollectionEventHandler implements MessageHandler {
    private static final Logger LOG = Logger.getLogger(DataCollectionEventHandler.class.getName());

    private final JsonService jsonService;
    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final MeteringService meteringService;
    private final CommunicationTaskService communicationTaskService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    public DataCollectionEventHandler(
            JsonService jsonService,
            IssueService issueService,
            IssueCreationService issueCreationService,
            MeteringService meteringService,
            CommunicationTaskService communicationTaskService,
            DeviceService deviceService,
            Thesaurus thesaurus) {
        this.jsonService = jsonService;
        this.issueService = issueService;
        this.issueCreationService = issueCreationService;
        this.meteringService = meteringService;
        this.communicationTaskService = communicationTaskService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);

        String topic = String.class.cast(map.get(EventConstants.EVENT_TOPIC));
        DataCollectionEventDescription eventDescription = DataCollectionEventDescription.getDescriptionByTopic(topic);
        if (eventDescription != null) {
            IssueEvent event = createEvent(map, eventDescription);
            if (event != null) {
                issueCreationService.dispatchCreationEvent(event);
            }
        }
    }

    private IssueEvent createEvent(Map<?, ?> map, DataCollectionEventDescription eventDescription) {
        IssueEvent event = null;
        try {
            if (DataCollectionEventDescription.DEVICE_EVENT.equals(eventDescription)) {
                event = new MeterIssueEvent(issueService, meteringService, communicationTaskService, deviceService, thesaurus, map);
            } else {
                event = new DataCollectionEvent(issueService, meteringService, communicationTaskService, deviceService, thesaurus, map);
            }
        } catch (UnableToCreateEventException e) {
            LOG.severe(e.getMessage());
        }
        return event;
    }
}
