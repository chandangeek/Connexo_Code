package com.energyict.mdc.issue.datacollection.impl.templates;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.FindById;
import com.elster.jupiter.properties.IdWithNameValue;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@Component(name = "com.energyict.mdc.issue.datacollection.EventAggregationRuleTemplate",
        property = {"name=" + EventAggregationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class EventAggregationRuleTemplate extends AbstractDataCollectionTemplate {
    
    public static final String NAME = "EventAggregationRuleTemplate";
    public static final String THRESHOLD = NAME + ".threshold";
    public static final String EVENTTYPE = NAME + ".eventType";
    
    private volatile IssueService issueService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    
    private final PossibleEventTypes eventTypes = new PossibleEventTypes();

    //For OSGI
    public EventAggregationRuleTemplate() {
    }

    @Inject
    public EventAggregationRuleTemplate(NlsService nlsService, IssueService issueSerivce, IssueDataCollectionService issueDataCollectionService, PropertySpecService propertySpecService){
        setNlsService(nlsService);
        setIssueService(issueSerivce);
        setIssueDataCollectionService(issueDataCollectionService);
        setPropertySpecService(propertySpecService);
        activate();
    }

    @Activate
    public void activate(){
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        setThesaurus(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));
    }
    
    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    
    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    public final void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }
    
    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datacollection\n" +
                "import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Events from meters of concentrator @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\" )\n" +
                "\teval( event.computeCurrentThreshold() > @{" + THRESHOLD +"} )\n" +
                "then\n" +
                "\tSystem.out.println(\"Events from meters of concentrator @{ruleId}\");\n" +
                "\tDataCollectionEvent eventClone = event.cloneForAggregation();\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, eventClone);\n" +
                "end";
    }

    @Override
    public String getName() {
        return EventAggregationRuleTemplate.NAME;
    }

    @Override
    public String getDisplayName() {
        return MessageSeeds.TEMPLATE_EVT_AGGREGATION_NAME.getTranslated(getThesaurus());
    }

    @Override
    public String getDescription() {
        return MessageSeeds.TEMPLATE_EVT_AGGREGATION_DESCRIPTION.getTranslated(getThesaurus());
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE).get();
    }

    @Override
    public Optional<? extends Issue> createIssue(Issue baseIssue, IssueEvent event) {
        OpenIssueDataCollection issue = issueDataCollectionService.createIssue(baseIssue);
        event.apply(issue);
        issue.save();
        return Optional.of(issue);
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        return Optional.empty();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().longPropertySpecWithValues(THRESHOLD, true, LongStream.rangeClosed(0, 100).boxed().toArray(Long[]::new)));
        builder.add(getPropertySpecService().idWithNameValuePropertySpec(EVENTTYPE, true, eventTypes, eventTypes.eventTypes));
        return builder.build();
    }
    
    private class PossibleEventTypes implements FindById<EventType> {
        
        private final EventType[] eventTypes = {
                new EventType(DataCollectionEventDescription.CONNECTION_LOST.getUniqueKey(), MessageSeeds.EVENT_TITLE_CONNECTION_LOST),
                new EventType(DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE.getUniqueKey(), MessageSeeds.EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE),
                new EventType(DataCollectionEventDescription.UNABLE_TO_CONNECT.getUniqueKey(), MessageSeeds.EVENT_TITLE_UNABLE_TO_CONNECT),
        };
        
        @Override
        public Optional<EventType> findById(Object id) {
            for(EventType eventType : eventTypes) {
                if (eventType.getId().equals(id)) {
                    return Optional.of(eventType);
                }
            }
            return Optional.empty();
        }
    }
    
    private class EventType extends IdWithNameValue {
        
        Object id;
        MessageSeeds seed;
        
        public EventType(String id, MessageSeeds seed) {
            this.id = id;
            this.seed = seed;
        }
        
        @Override
        public Object getId() {
            return id;
        }

        @Override
        public String getName() {
            return seed.getTranslated(EventAggregationRuleTemplate.this.getThesaurus());
        }
    }
}