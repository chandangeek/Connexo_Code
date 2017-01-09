package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.ItemizeAddCalendarMessage;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointFilter;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.Functions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.mdm.usagepoint.data.bulk.itimizer.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + UsagePointDataModelService.BULK_ITEMIZER_QUEUE_SUBSCRIBER,
                "destination=" + UsagePointDataModelService.BULK_ITEMIZER_QUEUE_DESTINATION},
        immediate = true)
public class ItemizerMessageHandlerFactory implements MessageHandlerFactory {

    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile SearchService searchService;
    private volatile MeteringService meteringService;

    @Override
    public MessageHandler newMessageHandler() {
        SearchDomain searchDomain = searchService.findDomain(UsagePoint.class.getName())
                .orElseThrow(() -> new IllegalStateException("Search domain for usage points not found"));
        return message -> {
            DestinationSpec destinationSpec = messageService.getDestinationSpec(UsagePointDataModelService.BULK_HANDLING_QUEUE_DESTINATION)
                    .orElseThrow(() -> new IllegalStateException("Queue " + UsagePointDataModelService.BULK_HANDLING_QUEUE_DESTINATION + " does not exist"));
            ItemizeAddCalendarMessage queueMessage = jsonService.deserialize(message.getPayload(), ItemizeAddCalendarMessage.class);
            Stream<UsagePoint> usagePointStream = toUsagePointStream(searchDomain, queueMessage.getUsagePointFilter(), queueMessage.getUsagePointMRIDs());
            usagePointStream.flatMap(usagePoint -> toMessagesStream(usagePoint, queueMessage.getCalendarIds(), queueMessage.getStartTime()))
                    .map(jsonService::serialize)
                    .map(destinationSpec::message)
                    .forEach(MessageBuilder::send);
        };
    }

    Stream<UsagePoint> toUsagePointStream(SearchDomain searchDomain, UsagePointFilter usagePointFilter, List<String> usagePointMRIDs) {
        if (usagePointFilter != null) {
            return usagePointStream(searchDomain, usagePointFilter);
        } else if (usagePointMRIDs != null) {
            return usagePointStream(usagePointMRIDs);
        }
        return Stream.empty();
    }

    private Stream<UsagePoint> usagePointStream(List<String> usagePointMRIDs) {
        return usagePointMRIDs
                .stream()
                .map(meteringService::findUsagePointByMRID)
                .flatMap(Functions.asStream());
    }

    private Stream<UsagePoint> usagePointStream(SearchDomain searchDomain, UsagePointFilter usagePointFilter) {
        SearchBuilder<Object> searchBuilder = searchService.search(searchDomain);
        for (SearchablePropertyValue propertyValue : searchDomain.getPropertiesValues(getPropertyMapper(usagePointFilter))) {
            addAsCondition(searchBuilder, propertyValue);
        }
        return searchBuilder.toFinder()
                .stream()
                .map(UsagePoint.class::cast);
    }

    private void addAsCondition(SearchBuilder<Object> searchBuilder, SearchablePropertyValue propertyValue) {
        try {
            propertyValue.addAsCondition(searchBuilder);
        } catch (InvalidValueException e) {
            throw new RuntimeException(e);
        }
    }

    Stream<AddCalendarMessage> toMessagesStream(UsagePoint usagePoint, List<Long> calendarIds, long startTime) {
        return calendarIds.stream()
                .map(calendarId -> new AddCalendarMessage(usagePoint.getId(), calendarId, startTime));
    }

    private Function<SearchableProperty, SearchablePropertyValue> getPropertyMapper(UsagePointFilter usagePointFilter) {
        return searchableProperty -> new SearchablePropertyValue(searchableProperty, usagePointFilter
                .getPropertyValue(searchableProperty));
    }


    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
}
