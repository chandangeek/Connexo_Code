package com.energyict.mdc.issue.datacollection.impl.templates;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.BooleanFactory;
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

@Component(name = "com.energyict.mdc.issue.datacollection.BasicDatacollectionRuleTemplate",
           property = {"name=" + BasicDataCollectionRuleTemplate.NAME},
           service = CreationRuleTemplate.class,
           immediate = true)
public class BasicDataCollectionRuleTemplate extends AbstractDataCollectionTemplate {
    static final String NAME = "BasicDataCollectionRuleTemplate";
    
    public static final String EVENTTYPE = NAME + ".eventType";
    public static final String AUTORESOLUTION = NAME + ".autoresolution";

    private final PossibleEventTypes eventTypes = new PossibleEventTypes();
    
    private volatile PropertySpecService propertySpecService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile IssueService issueService;

    //for OSGI
    public BasicDataCollectionRuleTemplate() {
    }

    @Inject
    public BasicDataCollectionRuleTemplate(IssueDataCollectionService issueDataCollectionService, NlsService nlsService, IssueService issueService, PropertySpecService propertySpecService) {
        setIssueDataCollectionService(issueDataCollectionService);
        setNlsService(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);

        activate();
    }

    @Activate
    public void activate() {
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        super.setThesaurus(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public final void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    
    @Reference
    public final void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
        super.setPropertySpecService(propertySpecService);
    }

    @Override
    public String getName() {
        return BasicDataCollectionRuleTemplate.NAME;
    }

    @Override
    public String getDescription() {
        return MessageSeeds.BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION.getTranslated(getThesaurus());
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datacollection\n" +
               "import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;\n" +
               "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
               "rule \"Basic datacollection rule @{ruleId}\"\n"+
               "when\n"+
               "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == false )\n"+
               "then\n"+
               "\tSystem.out.println(\"Trying to create issue by basic datacollection rule=@{ruleId}\");\n"+
               "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n"+
               "end\n" +
               "rule \"Auto-resolution section @{ruleId}\"\n"+
               "when\n"+
               "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == true, @{"+ AUTORESOLUTION + "} == 1 )\n"+
               "then\n"+
               "\tSystem.out.println(\"Trying to resolve issue by basic datacollection rule=@{ruleId}\");\n"+
               "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n"+
               "end";
    }

    @Override
    public Optional<? extends Issue> createIssue(Issue baseIssue, IssueEvent event) {
        if (!event.findExistingIssue().isPresent()) {
            OpenIssueDataCollection issue = issueDataCollectionService.createIssue(baseIssue);
            event.apply(issue);
            issue.save();
            return Optional.of(issue);
        } else {
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) event.findExistingIssue().get();
            if (IssueStatus.IN_PROGRESS.equals(dcIssue.getStatus().getKey())){
                dcIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).get());
                dcIssue.save();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(openIssue.close(issueService.findStatus(IssueStatus.RESOLVED).get()));
        }
        return issue;
    }
    
    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE).get();
    }
    
    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(propertySpecService.idWithNameValuePropertySpec(EVENTTYPE, true, eventTypes, eventTypes.eventTypes));
        builder.add(propertySpecService.newPropertySpecBuilder(new BooleanFactory())
                                       .name(AUTORESOLUTION)
                                       .setDefaultValue(true)
                                       .markRequired()
                                       .finish());
        return builder.build();
    }
    
    public Object[] getPossibleValuesForEventTypes() {
        return Arrays.asList(DataCollectionEventDescription.values()).stream().map(DataCollectionEventDescription::name).toArray();
    }
    
    @Override
    public String getDisplayName() {
        return MessageSeeds.BASIC_TEMPLATE_DATACOLLECTION_NAME.getTranslated(getThesaurus());
    }
    
    private class PossibleEventTypes implements FindById<EventType> {
        
        private final EventType[] eventTypes = {
                new EventType(DataCollectionEventDescription.CONNECTION_LOST.getUniqueKey(), MessageSeeds.EVENT_TITLE_CONNECTION_LOST),
                new EventType(DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE.getUniqueKey(), MessageSeeds.EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE),
                new EventType(DataCollectionEventDescription.UNABLE_TO_CONNECT.getUniqueKey(), MessageSeeds.EVENT_TITLE_UNABLE_TO_CONNECT),
                new EventType(DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE.getUniqueKey(), MessageSeeds.EVENT_TITLE_UNKNOWN_INBOUND_DEVICE),
                new EventType(DataCollectionEventDescription.UNKNOWN_OUTBOUND_DEVICE.getUniqueKey(), MessageSeeds.EVENT_TITLE_UNKNOWN_OUTBOUND_DEVICE)
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
            return seed.getTranslated(BasicDataCollectionRuleTemplate.this.getThesaurus());
        }
    }
}
