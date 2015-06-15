package com.energyict.mdc.issue.datacollection.impl.templates;

import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.CONNECTION_LOST;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNABLE_TO_CONNECT;

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
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
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
        EventTypes eventTypes = new EventTypes(getThesaurus(), CONNECTION_LOST, DEVICE_COMMUNICATION_FAILURE, UNABLE_TO_CONNECT);
        builder.add(getPropertySpecService().stringReferencePropertySpec(EVENTTYPE, true, eventTypes, eventTypes.getEventTypes()));
        return builder.build();
    }
}