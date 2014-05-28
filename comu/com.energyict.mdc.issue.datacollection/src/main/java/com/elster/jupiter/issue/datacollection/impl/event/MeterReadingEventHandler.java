package com.elster.jupiter.issue.datacollection.impl.event;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.datacollection.MeterReadingIssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import org.osgi.service.event.EventConstants;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

public class MeterReadingEventHandler implements MessageHandler {
    private static final Logger LOG = Logger.getLogger(MeterReadingEventHandler.class.getName());

    // TODO make com.elster.jupiter.metering.impl.EventType.METERREADING_CREATED public?
    private static final String METER_READING_EVENT_TOPIC = "com/elster/jupiter/metering/meterreading/CREATED";
    private static final String METER_READING_EVENT_METER_ID = "meterId";
    private static final String METER_READING_EVENT_READING_START = "start";
    private static final String METER_READING_EVENT_READING_END = "end";


    private final JsonService jsonService;
    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final MeteringService meteringService;

    public MeterReadingEventHandler(JsonService jsonService, IssueService issueService, IssueCreationService issueCreationService, MeteringService meteringService) {
        this.jsonService = jsonService;
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
        this.meteringService = meteringService;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        String topic = String.class.cast(map.get(EventConstants.EVENT_TOPIC));
        if (!METER_READING_EVENT_TOPIC.equalsIgnoreCase(topic)){
            return;
        }

        Long meterId = (Long) map.get(METER_READING_EVENT_METER_ID);
        Optional<Meter> meterRef = meteringService.findMeter(meterId);
        if(meterRef.isPresent()) {
            Long readingStart = (Long) map.get(METER_READING_EVENT_READING_START);
            Long readingEnd = (Long) map.get(METER_READING_EVENT_READING_END);
            IssueStatus status = getEventStatus(issueService);
            Set<ReadingType> readingTypes = meterRef.get().getReadingTypes(new Interval(new Date(readingStart), new Date(readingEnd)));
            for (ReadingType readingType : readingTypes) {
                MeterReadingIssueEvent event = new MeterReadingIssueEvent(meterRef.get(), readingType, status, issueService);
                issueCreationService.dispatchCreationEvent(event);
            }
        }
    }

    private IssueStatus getEventStatus(IssueService issueService) {
        Query<IssueStatus> statusQuery = issueService.query(IssueStatus.class);
        List<IssueStatus> statusList = statusQuery.select(where("isFinal").isEqualTo(Boolean.FALSE));
        if (statusList.isEmpty()) {
            LOG.severe("Issue creation failed, because no not-final statuses was found");
            return null;
        }
        return statusList.get(0);
    }
}
