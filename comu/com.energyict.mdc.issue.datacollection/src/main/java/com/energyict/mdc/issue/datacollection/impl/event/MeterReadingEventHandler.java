package com.energyict.mdc.issue.datacollection.impl.event;

import com.energyict.mdc.issue.datacollection.event.MeterReadingEvent;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.osgi.service.event.EventConstants;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class MeterReadingEventHandler implements MessageHandler {
    private static final Logger LOG = Logger.getLogger(MeterReadingEventHandler.class.getName());

    private static final String METER_READING_EVENT_TOPIC = "com/elster/jupiter/metering/meterreading/CREATED";
    private static final String METER_READING_EVENT_METER_ID = "meterId";
    private static final String METER_READING_EVENT_READING_START = "start";
    private static final String METER_READING_EVENT_READING_END = "end";

    private final JsonService jsonService;
    private final IssueCreationService issueCreationService;
    private final IssueService issueService;
    private final MeteringService meteringService;
    private final Clock clock;
    private final Thesaurus thesaurus;

    public MeterReadingEventHandler(
            JsonService jsonService,
            IssueService issueService,
            IssueCreationService issueCreationService,
            MeteringService meteringService,
            Clock clock,
            Thesaurus thesaurus) {
        this.jsonService = jsonService;
        this.issueCreationService = issueCreationService;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        String topic = String.class.cast(map.get(EventConstants.EVENT_TOPIC));
        if (!METER_READING_EVENT_TOPIC.equalsIgnoreCase(topic)) {
            return;
        }

        long meterId = getLong(map, METER_READING_EVENT_METER_ID);
        Optional<Meter> meterRef = meteringService.findMeter(meterId);
        if (meterRef.isPresent()) {
            Long readingStart = getLong(map, METER_READING_EVENT_READING_START);
            Long readingEnd = getLong(map, METER_READING_EVENT_READING_END);
            IssueStatus status = getEventStatus(issueService);
            Set<ReadingType> readingTypes =
                    meterRef.get().getReadingTypes(
                            Range.range(
                                    Instant.ofEpochMilli(readingStart), BoundType.OPEN,
                                    Instant.ofEpochMilli(readingEnd), BoundType.CLOSED));
            List<IssueEvent> events = new ArrayList<>(readingTypes.size());
            events.addAll(
                    readingTypes.stream()
                        .map(readingType -> new MeterReadingEvent(meterRef.get(), readingType, status, issueService, clock))
                        .collect(Collectors.toList()));
            issueCreationService.dispatchCreationEvent(events);
        }
    }

    private IssueStatus getEventStatus(IssueService issueService) {
        Query<IssueStatus> statusQuery = issueService.query(IssueStatus.class);
        List<IssueStatus> statusList = statusQuery.select(where("isHistorical").isEqualTo(Boolean.FALSE));
        if (statusList.isEmpty()) {
            LOG.severe("Issue creation failed, because no non-final statuses was found");
            return null;
        }
        return statusList.get(0);
    }

    private Long getLong(Map<?, ?> map, String key) {
        return ((Number) map.get(key)).longValue();
    }
}
