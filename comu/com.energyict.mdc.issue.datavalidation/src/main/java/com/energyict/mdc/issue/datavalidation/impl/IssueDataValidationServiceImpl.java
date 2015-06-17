package com.energyict.mdc.issue.datavalidation.impl;

import static com.elster.jupiter.util.conditions.Where.where;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.entity.IssueDataValidationImpl;
import com.energyict.mdc.issue.datavalidation.impl.entity.OpenIssueDataValidationImpl;
import com.google.inject.AbstractModule;

@Component(name = "com.energyict.mdc.issue.datavalidation",
           service = { InstallService.class, TranslationKeyProvider.class, IssueDataValidationService.class, IssueProvider.class },
           property = "name=" + IssueDataValidationService.COMPONENT_NAME,
           immediate = true)
public class IssueDataValidationServiceImpl implements IssueDataValidationService, TranslationKeyProvider, InstallService, IssueProvider {

    private volatile IssueService issueService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile MessageService messageService;

    private volatile DataModel dataModel;

    //for OSGI
    public IssueDataValidationServiceImpl() {
    }
    
    @Inject
    public IssueDataValidationServiceImpl(OrmService ormService, IssueService issueService, NlsService nlsService, EventService eventService, MessageService messageService) {
        setOrmService(ormService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setEventService(eventService);
        setMessageService(messageService);
        activate();
        if(!dataModel.isInstalled()) {
            install();
        }
    }
    
    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueDataValidationService.class).toInstance(IssueDataValidationServiceImpl.this);
                bind(EventService.class).toInstance(eventService);
                bind(MessageService.class).toInstance(messageService);
            }
        });
    }
    
    @Override
    public void install() {
        dataModel.getInstance(Installer.class).install();
    }
    
    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "NLS", "MSG", "EVT", "MTR", "ISU");
    }
    
    @Override
    public Optional<? extends IssueDataValidation> findIssue(long id) {
        Optional<OpenIssueDataValidation> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<OpenIssueDataValidation> findOpenIssue(long id) {
        return dataModel.query(OpenIssueDataValidation.class, OpenIssue.class)
                        .select(Where.where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".id").isEqualTo(id))
                        .stream()
                        .findFirst();
    }

    @Override
    public Optional<HistoricalIssueDataValidation> findHistoricalIssue(long id) {
        return dataModel.query(HistoricalIssueDataValidation.class, HistoricalIssue.class)
                        .select(Where.where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".id").isEqualTo(id))
                        .stream()
                        .findFirst();
    }

    @Override
    public OpenIssueDataValidation createIssue(Issue baseIssue) {
        OpenIssueDataValidationImpl issue = dataModel.getInstance(OpenIssueDataValidationImpl.class);
        issue.setIssue((OpenIssue)baseIssue);
        return issue;
    }
    
    @Override
    public String getComponentName() {
        return IssueDataValidationService.COMPONENT_NAME;
    }
    
    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }
    
    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(IssueDataValidationService.COMPONENT_NAME, "Issue Data Validation");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }
    
    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    
    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN);
    }
    
    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
    
    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        return issue instanceof OpenIssueDataValidation ? Optional.of(issue) : findOpenIssue(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof HistoricalIssueDataValidation ? Optional.of(issue) : findHistoricalIssue(issue.getId());
    }
    
    @Override
    public Finder<? extends IssueDataValidation> findAllDataValidationIssues(DataValidationIssueFilter filter) {
        Condition condition = buildConditionFromFilter(filter);

        Class<? extends IssueDataValidation> mainClass = null;
        Class<? extends Issue> issueEager = null;
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().allMatch(status -> !status.isHistorical())) {
            mainClass = OpenIssueDataValidation.class;
            issueEager = OpenIssue.class;
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(status -> status.isHistorical())) {
            mainClass = HistoricalIssueDataValidation.class;
            issueEager = HistoricalIssue.class;
        } else {
            mainClass = IssueDataValidation.class;
            issueEager = Issue.class;
        }
        return DefaultFinder.of(mainClass, condition, dataModel, issueEager, IssueStatus.class, EndDevice.class, User.class,  IssueReason.class, IssueType.class);
    }

    private Condition buildConditionFromFilter(DataValidationIssueFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by assignee
        Condition assigneeCondition = Condition.TRUE;
        if (filter.getAssignee().isPresent()) {
            assigneeCondition = where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".user").isEqualTo(filter.getAssignee().get());
        }
        if (filter.isUnassignedOnly()) {
            assigneeCondition = where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".user").isNull();
        }
        condition = condition.and(assigneeCondition);
        //filter by reason
        if (filter.getIssueReason().isPresent()) {
            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".reason").isEqualTo(filter.getIssueReason().get()));
        }
        //filter by device
        if (filter.getDevice().isPresent()) {
            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".device").isEqualTo(filter.getDevice().get()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".status").in(filter.getStatuses()));
        }
        return condition;
    }
}
